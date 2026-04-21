package com.eazypaytech.pos.features.cards.ui

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.analogics.builder_core.data.constants.BuilderConstants
import com.eazypaytech.paymentservicecore.constants.AppConstants
import com.analogics.paymentservicecore.data.listeners.responseListener.IEmvServiceResponseListener
import com.analogics.paymentservicecore.data.model.PaymentServiceTxnDetails
import com.analogics.paymentservicecore.data.model.emv.EmvServiceResult
import com.analogics.paymentservicecore.data.model.EBTBalance
import com.analogics.paymentservicecore.data.model.TxnStatus
import com.analogics.paymentservicecore.domain.repository.emvService.EmvServiceRepository
import com.analogics.paymentservicecore.utils.PaymentServiceUtils
import com.eazypaytech.pos.R
import com.eazypaytech.pos.features.activity.ui.SharedViewModel
import com.eazypaytech.pos.features.dialogs.ui.CustomDialogBuilder
import com.eazypaytech.pos.navigation.AppNavigationItems
import com.eazypaytech.pos.core.utils.emvMsgIdToStringId
import com.eazypaytech.pos.core.utils.emvStatusToTransStatus
import com.eazypaytech.pos.core.utils.getCurrentDateTime
import com.eazypaytech.pos.core.utils.navigateAndClean
import com.eazypaytech.pos.core.utils.miscellaneous.NetworkUtils
import com.analogics.securityframework.data.repository.TxnDBRepository
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
            navHostController.navigate(AppNavigationItems.ApprovedScreen.route){
                popUpTo(AppNavigationItems.CardScreen.route) { inclusive = false }
                launchSingleTop = true
            }
        }
    }

    fun navigateToManualScreen(navHostController: NavHostController) {
        viewModelScope.launch {
            navHostController.navigate(AppNavigationItems.ManualCardScreen.route){
                popUpTo(AppNavigationItems.CardScreen.route) { inclusive = false }
                launchSingleTop = true
            }
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
        CustomDialogBuilder.Companion.composeAlertDialog(
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
    fun updateTransResult(sharedViewModel: SharedViewModel, txnStatus: TxnStatus?, originalDateTime: String, AuthCode: String, posCondition: String) {

        val txnId = sharedViewModel.objRootAppPaymentDetail.id
        sharedViewModel.objRootAppPaymentDetail.txnStatus = txnStatus
        viewModelScope.launch {
            try {
                val txn = dbRepository.fetchTxnById(txnId)
                if (txn == null) {
                    return@launch
                }
                txn.txnStatus = txnStatus?.toString() ?: ""
                txn.originalDateTime = originalDateTime
                txn.hostAuthCode = AuthCode
                txn.posConditionCode = posCondition
                dbRepository.updateTxn(txn)
            } catch (e: Exception) {
                Log.e("UPDATE_TXN", "Error updating transaction", e)
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
                                sharedViewModel.objRootAppPaymentDetail.hostRespCode = response.paymentServiceTxnDetails?.hostRespCode
                                sharedViewModel.objRootAppPaymentDetail.hostAuthCode = response.paymentServiceTxnDetails?.hostAuthCode
                                sharedViewModel.objRootAppPaymentDetail.settlementDate = response.paymentServiceTxnDetails?.settlementDate
                                sharedViewModel.objRootAppPaymentDetail.expiryDate = response.paymentServiceTxnDetails?.expiryDate
                                sharedViewModel.objRootAppPaymentDetail.rrn = response.paymentServiceTxnDetails?.rrn
                                sharedViewModel.objRootAppPaymentDetail.currencyCode = response.paymentServiceTxnDetails?.currencyCode
                                sharedViewModel.objRootAppPaymentDetail.originalDateTime = response.paymentServiceTxnDetails?.originalDateTime
                                updateTransResult(sharedViewModel, emvStatusToTransStatus(response.paymentServiceTxnDetails?.hostRespCode),
                                    sharedViewModel.objRootAppPaymentDetail.originalDateTime.toString(),sharedViewModel.objRootAppPaymentDetail.hostAuthCode.toString(),sharedViewModel.objRootAppPaymentDetail.posCondition.toString()
                                ).let {
                                    val rawAdditionalAmt = response.paymentServiceTxnDetails?.additionalAmt

                                    if (!rawAdditionalAmt.isNullOrBlank() && rawAdditionalAmt != "null") {
                                        try {
                                            val balance = parseEBTBalances(rawAdditionalAmt)
                                            sharedViewModel.objRootAppPaymentDetail.snapEndBalance = balance.snap
                                            sharedViewModel.objRootAppPaymentDetail.cashEndBalance = balance.cash
                                            updateBalance(sharedViewModel)
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

    @RequiresApi(Build.VERSION_CODES.O)
    fun updateBalance(sharedViewModel: SharedViewModel) {
        viewModelScope.launch {
            dbRepository.fetchTxnById(sharedViewModel.objRootAppPaymentDetail.id)?.let { txn ->
                txn.cashEndBalance = sharedViewModel.objRootAppPaymentDetail.cashEndBalance.toString()
                txn.snapEndBalance = sharedViewModel.objRootAppPaymentDetail.snapEndBalance.toString()
                dbRepository.updateTxn(txn)
            }
        }
    }

    fun parseEBTBalances(hexString: String): EBTBalance {
        val blocks = hexString.chunked(20)
        var snapBalance = 0.0
        var cashBalance = 0.0
        blocks.forEach { block ->
            val bytes = block.chunked(2).map { it.toInt(16) }
            val accountType = bytes[0] // first byte
            val bcdBytes = bytes.subList(4, 10)
            val digits = bcdBytes.joinToString("") { byte ->
                val high = (byte shr 4) and 0x0F
                val low  = byte and 0x0F
                "$high$low"
            }
            val amount = digits.toLong() / 100.0
            when (accountType) {
                0x96 -> {
                    cashBalance = amount
                }
                0x98 -> {
                    snapBalance = amount
                }
            }
        }
        return EBTBalance(
            snap = snapBalance,
            cash = cashBalance
        )
    }

    fun checkNetwork(context: Context)
    {
        if(NetworkUtils().checkForInternet(context)!=true)
        {
            CustomDialogBuilder.Companion.composeAlertDialog(
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
        CustomDialogBuilder.Companion.composeAlertDialog(
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