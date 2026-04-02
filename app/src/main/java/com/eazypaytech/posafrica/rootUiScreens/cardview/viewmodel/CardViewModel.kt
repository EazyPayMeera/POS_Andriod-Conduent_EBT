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
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.toAmountFormat
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
    fun updateTransResult(sharedViewModel: SharedViewModel, txnStatus : TxnStatus?,originalDateTime:String,AuthCode:String,posCondition:String)
    {
        sharedViewModel.objRootAppPaymentDetail.txnStatus = txnStatus
        viewModelScope.launch {
            dbRepository.fetchTxnById(sharedViewModel.objRootAppPaymentDetail.id)?.let {
                it.txnStatus = txnStatus?.toString()?:""
                it.originalDateTime = originalDateTime
                it.hostAuthCode = AuthCode
                it.posConditionCode = posCondition
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
                                sharedViewModel.objRootAppPaymentDetail.hostResMessage = BuilderConstants.getIsoResponseMessage(response.paymentServiceTxnDetails?.hostRespCode.toString())
                                sharedViewModel.objRootAppPaymentDetail.hostAuthCode = response.paymentServiceTxnDetails?.hostAuthCode
                                sharedViewModel.objRootAppPaymentDetail.settlementDate = response.paymentServiceTxnDetails?.settlementDate
                                sharedViewModel.objRootAppPaymentDetail.expiryDate = response.paymentServiceTxnDetails?.expiryDate
                                sharedViewModel.objRootAppPaymentDetail.rrn = response.paymentServiceTxnDetails?.rrn
                                sharedViewModel.objRootAppPaymentDetail.currencyCode = response.paymentServiceTxnDetails?.currencyCode
                                sharedViewModel.objRootAppPaymentDetail.originalDateTime = response.paymentServiceTxnDetails?.dateTime
                                sharedViewModel.objRootAppPaymentDetail.hostAuthCode = response.paymentServiceTxnDetails?.hostAuthCode
                                sharedViewModel.objRootAppPaymentDetail.posCondition = response.paymentServiceTxnDetails?.posCondition
                                updateTransResult(sharedViewModel, emvStatusToTransStatus(response.paymentServiceTxnDetails?.hostRespCode),
                                    response.paymentServiceTxnDetails?.dateTime.toString(),sharedViewModel.objRootAppPaymentDetail.hostAuthCode.toString(),sharedViewModel.objRootAppPaymentDetail.posCondition.toString()
                                ).let {
                                    val rawAdditionalAmt = response.paymentServiceTxnDetails?.additionalAmt

                                    if (!rawAdditionalAmt.isNullOrBlank() && rawAdditionalAmt != "null") {
                                        try {
                                            val balance = parseEBTBalances(rawAdditionalAmt)
                                            sharedViewModel.objRootAppPaymentDetail.snapEndBalance = balance.snap
                                            sharedViewModel.objRootAppPaymentDetail.cashEndBalance = balance.cash
                                            sharedViewModel.objRootAppPaymentDetail.cashEndBalance = balance.cash
                                        } catch (e: Exception) {
                                            sharedViewModel.objRootAppPaymentDetail.additionalAmt = "0.0"
                                        }
                                    } else {
                                        sharedViewModel.objRootAppPaymentDetail.additionalAmt = "0.0"
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
                                else if(isCardNotPresent(response.status))
                                {
                                    navigateToManualScreen(navHostController)
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
            val digits = bcdBytes.joinToString("") { byte ->
                val high = (byte shr 4) and 0x0F
                val low  = byte and 0x0F
                "$high$low"
            }

            return digits.toLong() / 100.0
        }

        val cashBalance = extractBalance(0)
        val snapBalance = extractBalance(10)

        return EBTBalance(snap = snapBalance, cash = cashBalance)
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
                            showProgressVar.value = false
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


    fun isCardNotPresent(status: Any?) : Boolean
    {
        return when(status)
        {
            EmvServiceResult.CardCheckStatus.NO_CARD_DETECTED,
            EmvServiceResult.CardCheckStatus.TIMEOUT,
            -> true
            else -> false
        }
    }

    fun isCardCheckStatusError(status: Any?) : Boolean
    {
        return when(status)
        {
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