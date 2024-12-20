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
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.emvCardCheckStatusToMsgId
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.emvStatusToTransStatus
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.getCurrentDateTime
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.navigateAndClean
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

    fun onCancelClick(navHostController: NavHostController) {
        navHostController.navigateAndClean(AppNavigationItems.DashBoardScreen.route)
        viewModelScope.launch {
            emvServiceRepository.abortPayment()
        }
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
                                    if(isStatusSuggestAnotherCard(response.status)!=true)
                                        navigateToApprovalScreen(navHostController)
                                    else
                                        displayEmvError()
                                }
                            }

                            is EmvServiceResult.CardCheckResult -> {
                                when (response.status) {
                                    EmvServiceResult.CardCheckStatus.CARD_INSERTED,
                                    EmvServiceResult.CardCheckStatus.CARD_SWIPED,
                                    EmvServiceResult.CardCheckStatus.CARD_TAPPED -> {
                                        emvInProgress.value = true
                                        showProgressVar.value = true
                                        displayInfoMsgId.value =
                                            emvCardCheckStatusToMsgId(response.status as EmvServiceResult.CardCheckStatus)
                                    }

                                    else -> {
                                        emvInProgress.value = false
                                        showProgressVar.value = false
                                        displayInfoMsgId.value = EmvServiceResult.DisplayMsgId.NONE
                                    }
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

    fun displayEmvError()
    {
        CustomDialogBuilder.composeAlertDialog(onButtonClick = {
            viewModelScope.launch{
                delay(AppConstants.CARD_CHECK_RESTART_DELAY_MS)
                startPayment(context,sharedViewModel,navHostController)
            }
        })
        emvInProgress.value = false
    }

    fun isStatusSuggestAnotherCard(status: Any?) : Boolean
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