package com.eazypaytech.pos.features.cards.ui

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.res.stringResource
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
import com.analogics.paymentservicecore.data.model.emv.CardEntryMode
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
import kotlinx.coroutines.Dispatchers
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
    private var isCardDetected = false
    var isChipCardSwiped = mutableStateOf(false)
    /**
     * Navigates to Approval screen.
     *
     * Flow:
     * - Keeps CardScreen in back stack
     * - Avoids duplicate instances using launchSingleTop
     */

    fun navigateToApprovalScreen(navHostController: NavHostController) {
        viewModelScope.launch {
            navHostController.navigate(AppNavigationItems.ApprovedScreen.route){
                popUpTo(AppNavigationItems.CardScreen.route) { inclusive = false }
                launchSingleTop = true
            }
        }
    }

    /**
     * Navigates to Manual Card entry screen.
     *
     * Flow:
     * - Stops ongoing EMV transaction
     * - Redirects user to manual input flow
     */
    fun navigateToCardScreen(navHostController: NavHostController) {
        viewModelScope.launch {
            navHostController.navigateAndClean(AppNavigationItems.CardScreen.route)
        }
    }

    fun navigateToManualScreen(navHostController: NavHostController) {
        viewModelScope.launch {
            navHostController.navigateAndClean(AppNavigationItems.ManualCardScreen.route)
        }
    }

    /**
     * Aborts current payment and navigates to Dashboard.
     */
    fun abortPayment(navHostController: NavHostController) {
        viewModelScope.launch {
            navHostController.navigateAndClean(AppNavigationItems.DashBoardScreen.route)
            emvServiceRepository.abortPayment()
        }
    }

    /**
     * Shows confirmation dialog for cancel action.
     *
     * Flow:
     * - If user confirms → abort payment
     * - Else → dismiss dialog
     */
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

    fun toManualEntry(navHostController: NavHostController) {
        viewModelScope.launch {
            navHostController.navigate(AppNavigationItems.ManualCardScreen.route){
                popUpTo(AppNavigationItems.CardScreen.route) { inclusive = false }
                launchSingleTop = true
            }
        }
    }

//    fun cardRetry(navHostController: NavHostController) {
//        CustomDialogBuilder.composeAlertDialog(
//            title = "Chip Error",
//            message = "How would you like to proceed?",
//            okBtnText = "Swipe",        // → Fallback
//            onOkClick = {
//                // ✅ Set fallback flag and restart payment
//                sharedViewModel.objRootAppPaymentDetail.isFallback = true
//                sharedViewModel.objRootAppPaymentDetail.cardEntryMode = CardEntryMode.FALLBACK_MAGSTRIPE
//                viewModelScope.launch {
//                    emvServiceRepository.abortPayment()
//                    delay(AppConstants.CARD_CHECK_RESTART_DELAY_MS)
//                    startPayment(context, sharedViewModel, navHostController)
//                }
//            },
//            cancelBtnText = "Manual",  // → Manual
//            onCancelClick = {
//                // ✅ Clear fallback flag and go to manual
//                sharedViewModel.objRootAppPaymentDetail.isFallback = false
//                sharedViewModel.objRootAppPaymentDetail.cardEntryMode = CardEntryMode.MANUAL
//                showProgressVar.value = false
//
//                navigateToManualScreen(navHostController)
//            }
//        )
//    }

    /**
     * Updates transaction result in database.
     *
     * Flow:
     * - Updates status, auth code, datetime, and POS condition
     * - Persists updated transaction in DB
     */

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun updateTransResult(sharedViewModel: SharedViewModel, txnStatus: TxnStatus?, originalDateTime: String, AuthCode: String, posCondition: String) {

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
                txn.hostResMessage = sharedViewModel.objRootAppPaymentDetail.hostResMessage
                txn.cashEndBalance = sharedViewModel.objRootAppPaymentDetail.cashEndBalance.toString()
                txn.snapEndBalance = sharedViewModel.objRootAppPaymentDetail.snapEndBalance.toString()
                Log.d("DATABASE","Txn Update from CardViewModel")
                dbRepository.updateTxn(txn)
            } catch (e: Exception) {
                Log.e("UPDATE_TXN", "Error updating transaction", e)
            }
        }
    }

    /**
     * Initiates EMV payment process.
     *
     * Flow:
     * - Checks network availability
     * - Sets transaction datetime
     * - Starts EMV payment
     * - Handles EMV callbacks:
     *      → Transaction result
     *      → Card detection states
     *      → Display messages
     */
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
                        Log.d("EMV", "Response = $response")
                        when (response) {
                            is EmvServiceResult.TransResult -> {
                                viewModelScope.launch(Dispatchers.Main) {
                                    sharedViewModel.objRootAppPaymentDetail.isChipSwiped = false
                                sharedViewModel.objRootAppPaymentDetail.hostResMessage = BuilderConstants.getIsoResponseMessage(response.paymentServiceTxnDetails?.hostRespCode.toString())
                                sharedViewModel.objRootAppPaymentDetail.hostRespCode = response.paymentServiceTxnDetails?.hostRespCode
                                sharedViewModel.objRootAppPaymentDetail.hostAuthCode = response.paymentServiceTxnDetails?.hostAuthCode
                                sharedViewModel.objRootAppPaymentDetail.settlementDate = response.paymentServiceTxnDetails?.settlementDate
                                sharedViewModel.objRootAppPaymentDetail.expiryDate = response.paymentServiceTxnDetails?.expiryDate
                                sharedViewModel.objRootAppPaymentDetail.rrn = response.paymentServiceTxnDetails?.rrn
                                sharedViewModel.objRootAppPaymentDetail.currencyCode = response.paymentServiceTxnDetails?.currencyCode
                                sharedViewModel.objRootAppPaymentDetail.originalDateTime = response.paymentServiceTxnDetails?.originalDateTime
                                    updateTransResult(
                                        sharedViewModel,
                                        emvStatusToTransStatus(response.paymentServiceTxnDetails?.hostRespCode),
                                        sharedViewModel.objRootAppPaymentDetail.originalDateTime.toString(),
                                        sharedViewModel.objRootAppPaymentDetail.hostAuthCode.toString(),
                                        sharedViewModel.objRootAppPaymentDetail.posCondition.toString()
                                    ).let {
                                        val rawAdditionalAmt =
                                            response.paymentServiceTxnDetails?.additionalAmt

                                        if (!rawAdditionalAmt.isNullOrBlank() && rawAdditionalAmt != "null") {
                                            try {
                                                val balance = parseEBTBalances(rawAdditionalAmt)
                                                sharedViewModel.objRootAppPaymentDetail.snapEndBalance =
                                                    balance.snap
                                                sharedViewModel.objRootAppPaymentDetail.cashEndBalance =
                                                    balance.cash
                                                updateBalance(sharedViewModel)
                                            } catch (e: Exception) {
                                                sharedViewModel.objRootAppPaymentDetail.additionalAmt =
                                                    "0.0"
                                            }
                                        } else {
                                            sharedViewModel.objRootAppPaymentDetail.additionalAmt =
                                                "0.0"
                                        }
                                        if (isStatusTryAnotherCard(response.status) == true) {
                                            displayEmvError(response.displayMsgId)
                                        } else {
                                            showProgressVar.value = false
                                            navigateToApprovalScreen(navHostController)
                                        }
                                    }
                                }
                            }

                            is EmvServiceResult.CardCheckResult -> {
                                viewModelScope.launch(Dispatchers.Main) {  // ✅ add this
                                    emvInProgress.value = false
                                    showProgressVar.value = false
                                    Log.d("EMV", "CardCheckResult → status: ${response.status} | displayMsgId: ${response.displayMsgId} | isFallback: ${sharedViewModel.objRootAppPaymentDetail.isFallback}")

                                    when {
                                        response.status == EmvServiceResult.CardCheckStatus.CHIP_CARD_SWIPED
                                                && sharedViewModel.objRootAppPaymentDetail.isFallback != true -> {
                                            sharedViewModel.objRootAppPaymentDetail.isChipSwiped = true
                                            emvServiceRepository.abortPayment()
                                            viewModelScope.launch(Dispatchers.Main) {
                                                delay(100) // ✅ let recomposition settle before showing dialog
                                                CustomDialogBuilder.composeAlertDialog(
                                                    title = context.getString(R.string.default_alert_title_error),
                                                    message = context.getString(R.string.emv_msg_id_chip_detected),
                                                    onOkClick = {
                                                        viewModelScope.launch {
                                                            navigateToCardScreen(navHostController)
                                                        }
                                                    }
                                                )
                                            }
                                        }

                                        isCardCheckStatusInProgress(response.status) -> {
                                            isCardDetected = true
                                            emvInProgress.value = true
                                            showProgressVar.value = true
                                            response.displayMsgId?.let {
                                                displayInfoMsgId.value = it
                                            }
                                        }

                                        isCardNotPresent(response.status) -> {
                                            if (!isCardDetected) {
                                                abortPayment(navHostController)
                                            }
                                        }

                                        isCardCheckStatusError(response.status) -> {
                                            displayEmvError(response.displayMsgId)
                                        }
                                    }
                                }  // ✅ close launch
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

    /**
     * Updates EBT balance (cash & SNAP) in database.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun updateBalance(sharedViewModel: SharedViewModel) {
        try {
            val id = sharedViewModel.objRootAppPaymentDetail.id ?: run {
                Log.e("UPDATE_BALANCE", "❌ id is null")
                return
            }
            val newCash = sharedViewModel.objRootAppPaymentDetail.cashEndBalance ?: 0.0
            val newSnap = sharedViewModel.objRootAppPaymentDetail.snapEndBalance ?: 0.0

            Log.d("UPDATE_BALANCE", "▶ START — id=$id, cash=$newCash, snap=$newSnap")

            if (newCash == 0.0 && newSnap == 0.0) {
                Log.e("UPDATE_BALANCE", "❌ ABORTED — both balances are 0.0, skipping update")
                return
            }

            dbRepository.updateBalancesOnly(id, newCash, newSnap)

            Log.d("UPDATE_BALANCE", "✅ SUCCESS — cash=$newCash, snap=$newSnap")

        } catch (e: Exception) {
            Log.e("UPDATE_BALANCE", "❌ Error updating balance: ${e.message}")
        }
    }

    /**
     * Parses EBT balance from hex string.
     *
     * Extracts:
     * - Cash balance (accountType 0x96)
     * - SNAP balance (accountType 0x98)
     *
     * Only processes Available Balance (amountType 0x02)
     */
    fun parseEBTBalances(hexString: String): EBTBalance {
        val blocks = hexString.chunked(20)
        var snapBalance = 0.0
        var cashBalance = 0.0

        blocks.forEach { block ->
            val bytes = block.chunked(2).map { it.toInt(16) }
            val accountType = bytes[0]  // positions 1-2: account type
            val amountType = bytes[1]   // positions 3-4: amount type

            // Only process Available Balance (0x02)
            if (amountType != 0x02) return@forEach

            val bcdBytes = bytes.subList(4, 10)
            val digits = bcdBytes.joinToString("") { byte ->
                val high = (byte shr 4) and 0x0F
                val low  = byte and 0x0F
                "$high$low"
            }
            val amount = digits.toLong() / 100.0

            when (accountType) {
                0x96 -> cashBalance = amount   // Cash Benefit
                0x98 -> snapBalance = amount   // Food Stamp Benefit
            }
        }

        return EBTBalance(snap = snapBalance, cash = cashBalance)
    }

    /**
     * Checks internet connectivity.
     * Shows error dialog if no network available.
     */
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

    /**
     * Displays EMV error dialog and handles retry logic.
     *
     * Flow:
     * - Shows error message
     * - If retry allowed → restarts payment (limited attempts)
     * - Else → navigates to manual or aborts
     */
    fun displayEmvError(displayMsgId: EmvServiceResult.DisplayMsgId?, abort : Boolean?=false, restart : Boolean?=true)
    {

        var message : String? = null
        val resolvedMsgId = if (cardRetryCount == 2) {
            EmvServiceResult.DisplayMsgId.MAX_CHIP_RETRY
        } else {
            displayMsgId
        }
        emvMsgIdToStringId(resolvedMsgId)?.let {
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
                            sharedViewModel.objRootAppPaymentDetail.isFallback = true
                            navigateToCardScreen(navHostController)
//                            viewModelScope.launch {
//                                emvServiceRepository.abortPayment()
//                                delay(AppConstants.CARD_CHECK_RESTART_DELAY_MS)
//                                startPayment(context, sharedViewModel, navHostController)
//                            }
                        }
                    }
                }?:let {
                    abortPayment(navHostController)
                }
        })
    }

    /**
     * Checks if card interaction is in progress.
     */
    fun isCardCheckStatusInProgress(status: Any?): Boolean {

        return when (status) {
            EmvServiceResult.CardCheckStatus.CARD_INSERTED,
            EmvServiceResult.CardCheckStatus.CARD_SWIPED,
            EmvServiceResult.CardCheckStatus.CARD_TAPPED -> true
            else -> false
        }
    }



    /**
     * Checks if no card is present (timeout).
     */
    fun isCardNotPresent(status: Any?): Boolean {
        Log.d("EMV_STATUS", "Status = $status")

        return when (status) {
            EmvServiceResult.CardCheckStatus.TIMEOUT -> !isCardDetected
            else -> false
        }
    }

    /**
     * Checks if card detection resulted in an error.
     */
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

    /**
     * Determines if display message requires popup dialog.
     */
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

    /**
     * Checks if transaction requires another card attempt.
     */
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