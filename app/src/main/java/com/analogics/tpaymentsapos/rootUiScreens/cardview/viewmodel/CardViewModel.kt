package com.analogics.tpaymentsapos.rootUiScreens.cardview.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.analogics.paymentservicecore.constants.AppConstants
import com.analogics.paymentservicecore.listeners.responseListener.IEmvServiceResponseListener
import com.analogics.paymentservicecore.model.PaymentServiceTxnDetails
import com.analogics.paymentservicecore.model.emv.EmvServiceResult
import com.analogics.paymentservicecore.models.TxnStatus
import com.analogics.paymentservicecore.repository.emvService.EmvServiceRepository
import com.analogics.paymentservicecore.utils.PaymentServiceUtils
import com.analogics.securityframework.database.dbRepository.TxnDBRepository
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import com.analogics.tpaymentsapos.rootUiScreens.activity.SharedViewModel
import com.analogics.tpaymentsapos.rootUiScreens.dialogs.CustomDialogBuilder
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.emvMsgIdToStringId
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.emvStatusToTransStatus
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.getCurrentDateTime
import com.analogics.tpaymentsapos.R
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.navigateAndClean
import com.analogics.tpaymentsapos.rootUtils.miscellaneous.NetworkUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CardViewModel @Inject constructor(private  var emvServiceRepository: EmvServiceRepository, var dbRepository: TxnDBRepository) : ViewModel() {

    var emvInProgress = mutableStateOf(false)
    var showProgressVar = mutableStateOf(true)
    var displayInfoMsgId = mutableStateOf(EmvServiceResult.DisplayMsgId.NONE)
    lateinit var context: Context
    lateinit var sharedViewModel: SharedViewModel
    lateinit var navHostController : NavHostController

    fun navigateToApprovalScreen(navHostController: NavHostController) {
        viewModelScope.launch {
            Log.d("Approved Screen","Going to Approved Screen")
            navHostController.navigate(AppNavigationItems.ApprovedScreen.route) // Navigate to the desired screen
        }
    }

    fun onUpiClick(navHostController: NavHostController) {
        navHostController.navigate(AppNavigationItems. BarcodeScreen.route)
        viewModelScope.launch {
            emvServiceRepository.abortPayment()
        }
    }

    fun abortPayment(navHostController: NavHostController) {
        viewModelScope.launch {
            navHostController.navigateAndClean(AppNavigationItems.DashBoardScreen.route)
            emvServiceRepository.abortPayment()
        }
    }

    fun onCancelClick(navHostController: NavHostController) {
        CustomDialogBuilder.composeAlertDialog(
            title = context.getString(R.string.cancel_dialogue),
            message = context.getString(R.string.dialogue_cancel_request),
            okBtnText = context.getString(R.string.yes),
            onOkClick = {
                abortPayment(navHostController)
            },
            cancelBtnText = context.getString(R.string.cancel_no),
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun updateTransResult(sharedViewModel: SharedViewModel, txnStatus : TxnStatus?)
    {
        sharedViewModel.objRootAppPaymentDetail.txnStatus = txnStatus
        viewModelScope.launch {
            dbRepository.fetchTxnById(sharedViewModel.objRootAppPaymentDetail.id)?.let {
                it.txnStatus = txnStatus?.toString()?:""
                dbRepository.updateTxn(it)
            }
        }
    }

    fun startPayment(
        context: Context,
        sharedViewModel: SharedViewModel,
        navHostController: NavHostController
    ) {
        this.context = context
        this.sharedViewModel = sharedViewModel
        this.navHostController = navHostController

        viewModelScope.launch {
            checkNetwork(context)
            sharedViewModel.objRootAppPaymentDetail.dateTime = getCurrentDateTime()
            emvServiceRepository.startPayment(
                context = context,
                    paymentServiceTxnDetails = PaymentServiceUtils.transformObject<PaymentServiceTxnDetails>(sharedViewModel.objRootAppPaymentDetail),
                iEmvServiceResponseListener = object :
                    IEmvServiceResponseListener {
                    @RequiresApi(Build.VERSION_CODES.O)
                    @SuppressLint("DefaultLocale")
                    override fun onEmvServiceResponse(response: Any) {
                        when (response) {
                            is EmvServiceResult.TransResult -> {
                                Log.d("EMVServiceResponse", "Transaction Status: ${response.status}")
                                /* Update Transaction Result & Store in DB */
                                updateTransResult(sharedViewModel, emvStatusToTransStatus(response.status)).let {
                                    if(isStatusTryAnotherCard(response.status)==true)
                                        displayEmvError(response.displayMsgId)
                                    else
                                        navigateToApprovalScreen(navHostController)
                                }
                            }

                            is EmvServiceResult.CardCheckResult -> {
                                emvInProgress.value = false
                                showProgressVar.value = false
                                if(isCardCheckStatusInProgress(response.status)) {
                                    emvInProgress.value = true
                                    showProgressVar.value = true
                                    response.displayMsgId?.let {
                                        displayInfoMsgId.value = it
                                    }
                                }
                                else if(isCardCheckStatusAbort(response.status)) {
                                    displayEmvError(response.displayMsgId, true)
                                }
                                else if(isCardCheckStatusError(response.status)) {
                                    displayEmvError(response.displayMsgId)
                                }
                            }
                        }
                    }

                    override fun onEmvServiceDisplayMessage(
                        displayMsgId: EmvServiceResult.DisplayMsgId
                    ) {
                        displayInfoMsgId.value = displayMsgId
                    }
                })
        }
    }

    fun checkNetwork(context: Context)
    {
        if(NetworkUtils().checkForInternet(context)!=true)
        {
            CustomDialogBuilder.composeAlertDialog(
                title = context.getString(R.string.default_alert_title_error),
                message = context.getString(R.string.err_no_internet_connection),
            )
        }
    }

    fun displayEmvError(displayMsgId: EmvServiceResult.DisplayMsgId?, abort : Boolean?=false)
    {
        /* Display error & restart payment */
        emvInProgress.value = false
        var message : String? = null
        emvMsgIdToStringId(displayMsgId)?.let {
            message = context.getString(it)
        }
        CustomDialogBuilder.composeAlertDialog(
            title = context.getString(R.string.default_alert_title_error),
            message = message,
            onOkClick = {
                abort?.takeIf { it == false }?.let {
                    viewModelScope.launch {
                        delay(AppConstants.CARD_CHECK_RESTART_DELAY_MS)
                        startPayment(context, sharedViewModel, navHostController)
                    }
                }?:let {
                    abortPayment(navHostController)
                }
        })
    }

    fun isCardCheckStatusInProgress(status: Any?) : Boolean
    {
        return when(status)
        {
            EmvServiceResult.CardCheckStatus.CARD_INSERTED,
            EmvServiceResult.CardCheckStatus.CARD_SWIPED,
            EmvServiceResult.CardCheckStatus.CARD_TAPPED,
            -> true
            else -> false
        }
    }

    fun isCardCheckStatusAbort(status: Any?) : Boolean
    {
        return when(status)
        {
            EmvServiceResult.CardCheckStatus.TIMEOUT
            -> true
            else -> false
        }
    }

    fun isCardCheckStatusError(status: Any?) : Boolean
    {
        return when(status)
        {
            EmvServiceResult.CardCheckStatus.NO_CARD_DETECTED,
            EmvServiceResult.CardCheckStatus.NOT_ICC_CARD,
            EmvServiceResult.CardCheckStatus.USE_ICC_CARD,
            EmvServiceResult.CardCheckStatus.BAD_SWIPE,
            EmvServiceResult.CardCheckStatus.NEED_FALLBACK,
            EmvServiceResult.CardCheckStatus.MULTIPLE_CARDS,
            EmvServiceResult.CardCheckStatus.DEVICE_BUSY,
            EmvServiceResult.CardCheckStatus.ERROR,
            -> true
            else -> false
        }
    }

    fun isStatusTryAnotherCard(status: Any?) : Boolean
    {
        return when(status)
        {
            EmvServiceResult.TransStatus.TRY_ANOTHER_INTERFACE,
            EmvServiceResult.TransStatus.RETRY,
            EmvServiceResult.TransStatus.CARD_BLOCKED,
            EmvServiceResult.TransStatus.APP_BLOCKED,
            EmvServiceResult.TransStatus.APP_SELECTION_FAILED,
            EmvServiceResult.TransStatus.NO_EMV_APPS,
            EmvServiceResult.TransStatus.INVALID_ICC_CARD,
            -> true
            else -> false
        }
    }
}