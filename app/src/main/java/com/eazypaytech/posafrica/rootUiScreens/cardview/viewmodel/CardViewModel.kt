package com.eazypaytech.posafrica.rootUiScreens.cardview.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composition
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.eazypaytech.builder_core.constants.BuilderConstants
import com.eazypaytech.paymentservicecore.constants.AppConstants
import com.eazypaytech.paymentservicecore.listeners.responseListener.IEmvServiceResponseListener
import com.eazypaytech.paymentservicecore.model.PaymentServiceTxnDetails
import com.eazypaytech.paymentservicecore.model.emv.EmvServiceResult
import com.eazypaytech.paymentservicecore.models.EBTBalance
import com.eazypaytech.paymentservicecore.models.TxnStatus
import com.eazypaytech.paymentservicecore.models.TxnType
import com.eazypaytech.paymentservicecore.repository.emvService.EmvServiceRepository
import com.eazypaytech.paymentservicecore.utils.PaymentServiceUtils
import com.eazypaytech.securityframework.database.dbRepository.TxnDBRepository
import com.eazypaytech.posafrica.navigation.AppNavigationItems
import com.eazypaytech.posafrica.rootUiScreens.activity.SharedViewModel
import com.eazypaytech.posafrica.rootUiScreens.dialogs.CustomDialogBuilder
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.emvMsgIdToStringId
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.emvStatusToTransStatus
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.getCurrentDateTime
import com.eazypaytech.posafrica.R
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.navigateAndClean
import com.eazypaytech.posafrica.rootUtils.miscellaneous.NetworkUtils
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CardViewModel @Inject constructor(private var emvServiceRepository: EmvServiceRepository, var dbRepository: TxnDBRepository) : ViewModel() {

    var emvInProgress = mutableStateOf(false)
    var showProgressVar = mutableStateOf(false)
    var displayInfoMsgId = mutableStateOf(EmvServiceResult.DisplayMsgId.NONE)
    lateinit var context: Context
    lateinit var sharedViewModel: SharedViewModel
    lateinit var navHostController : NavHostController
    var cardRetryCount = 0

    fun navigateToApprovalScreen(navHostController: NavHostController) {
        viewModelScope.launch {
            navHostController.navigate(AppNavigationItems.ApprovedScreen.route) // Navigate to the desired screen
        }
    }

    fun navigateToManualScreen(navHostController: NavHostController) {
        viewModelScope.launch {
            navHostController.navigate(AppNavigationItems.ManualCardScreen.route) // Navigate to the desired screen
            emvServiceRepository.abortPayment()
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
                        Log.d("EMV_RESPONSE", Gson().toJson(response))
                        when (response) {
                            is EmvServiceResult.TransResult -> {
                                Log.d("TRANS_RESULT", Gson().toJson(response))
                                updateTransResult(sharedViewModel, emvStatusToTransStatus(response.status)).let {
                                    sharedViewModel.objRootAppPaymentDetail.hostResMessage = BuilderConstants.getIsoResponseMessage(response.hostRespCode.toString())

                                    val balance = parseEBTBalances(response.additionalAmt.toString())
                                    Log.d("TAG", "txnType: ${sharedViewModel.objRootAppPaymentDetail.txnType}")
                                    if(sharedViewModel.objRootAppPaymentDetail.txnType == TxnType.BALANCE_ENQUIRY_SNAP)
                                    {
                                        sharedViewModel.objRootAppPaymentDetail.additionalAmt =
                                            balance.snap.toString()
                                    }
                                    else
                                    {
                                        sharedViewModel.objRootAppPaymentDetail.additionalAmt =
                                            balance.cash.toString()
                                    }
                                    if(isStatusTryAnotherCard(response.status)==true) {
                                        displayEmvError(response.displayMsgId)
                                    }
                                    else
                                    {
                                        showProgressVar.value = false
                                        navigateToApprovalScreen(navHostController)
                                    }
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
                                    displayEmvError(response.displayMsgId, abort = true)
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
                        if(isDispIdNeedPopupMsg(displayMsgId)) {
                            displayEmvError(displayMsgId, restart = false)
                        }
                        else {
                            displayInfoMsgId.value = displayMsgId
                            if(displayMsgId != EmvServiceResult.DisplayMsgId.NONE) {
                                emvInProgress.value = false
                                showProgressVar.value = true
                            }
                        }
                    }
                })
        }
    }

    fun parseEBTBalances(hexString: String): EBTBalance {
        val bytes = hexString.chunked(2).map { it.toInt(16) }

        fun extractBalance(startIndex: Int): Double {
            val bcdBytes = bytes.subList(startIndex + 4, startIndex + 10)

            var value = 0L
            for (byte in bcdBytes) {
                val high = (byte shr 4) and 0x0F
                val low  = byte and 0x0F

                value = value * 10 + high
                value = value * 10 + low
            }

            return value / 100.0
        }

        var snap = 0.0
        var cash = 0.0

        var index = 0
        while (index + 10 <= bytes.size) {
            val tag = bytes[index]   // 0x96 or 0x98

            val amount = extractBalance(index)

            when (tag) {
                0x96 -> cash = amount   // SNAP
                0x98 -> snap = amount   // CASH
            }

            index += 10
        }

        return EBTBalance(snap = snap, cash = cash)
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



    fun displayEmvError(displayMsgId: EmvServiceResult.DisplayMsgId?, abort : Boolean?=false, restart : Boolean?=true)
    {
        emvInProgress.value = false
        showProgressVar.value = false
        var message : String? = null
        emvMsgIdToStringId(displayMsgId)?.let {
            message = context.getString(it)
        }
        CustomDialogBuilder.composeAlertDialog(
            title = context.getString(R.string.default_alert_title_error),
            message = message,
            onOkClick = {
                abort?.takeIf { it == false }?.let {
                    if(restart==true) {
                        if (cardRetryCount < AppConstants.CARD_RETRY_COUNT) {
                            cardRetryCount++
                            viewModelScope.launch {
                                emvServiceRepository.abortPayment()
                                delay(AppConstants.CARD_CHECK_RESTART_DELAY_MS)
                                startPayment(context, sharedViewModel, navHostController)
                            }
                        }else {
                            cardRetryCount = 0
                            navigateToManualScreen(navHostController)
                        }
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

    fun isDispIdNeedPopupMsg(displayMsgId: EmvServiceResult.DisplayMsgId?) : Boolean
    {
        return when(displayMsgId)
        {
            EmvServiceResult.DisplayMsgId.RETRY,
            EmvServiceResult.DisplayMsgId.SEE_PHONE_AND_PRESENT_CARD_AGAIN,
            EmvServiceResult.DisplayMsgId.TAP_CARD_AGAIN
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