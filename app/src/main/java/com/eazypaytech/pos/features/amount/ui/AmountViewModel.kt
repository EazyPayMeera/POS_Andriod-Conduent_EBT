package com.eazypaytech.pos.features.amount.ui

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.analogics.builder_core.data.constants.BuilderConstants
import com.analogics.paymentservicecore.data.listeners.responseListener.IApiServiceResponseListener
import com.analogics.paymentservicecore.data.model.PaymentServiceTxnDetails
import com.analogics.paymentservicecore.data.model.error.ApiServiceError
import com.analogics.paymentservicecore.data.model.error.ApiServiceTimeout
import com.analogics.paymentservicecore.data.model.TxnStatus
import com.analogics.paymentservicecore.data.model.TxnType
import com.analogics.paymentservicecore.data.model.emv.CardEntryMode
import com.analogics.paymentservicecore.domain.repository.apiService.ApiServiceRepository
import com.analogics.paymentservicecore.utils.PaymentServiceUtils
import com.analogics.paymentservicecore.utils.toDecimalFormat
import com.eazypaytech.pos.R
import com.eazypaytech.pos.features.activity.ui.SharedViewModel
import com.eazypaytech.pos.features.dialogs.ui.CustomDialogBuilder
import com.eazypaytech.pos.navigation.AppNavigationItems
import com.eazypaytech.pos.domain.model.ObjRootAppPaymentDetails
import com.eazypaytech.pos.core.utils.formatAmount
import com.eazypaytech.pos.core.utils.getCurrentDateTime
import com.eazypaytech.pos.core.utils.navigateAndClean
import com.eazypaytech.pos.core.utils.transformToAmountDouble
import com.analogics.securityframework.data.repository.TxnDBRepository
import com.eazypaytech.pos.core.utils.emvStatusToTransStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AmountViewModel @Inject constructor(private var apiServiceRepository: ApiServiceRepository, private val dbRepository: TxnDBRepository) : ViewModel() {

    var transAmount by mutableStateOf("")
        private set

    var isReadOnly by mutableStateOf(true)
        private set
    private val _origTotalAmount = MutableStateFlow<String?>(null)
    val origTotalAmount: StateFlow<String?> = _origTotalAmount
    private val _origDateTime = MutableStateFlow<String?>(null)
    val origDateTime: StateFlow<String?> = _origDateTime
    /**
     * Initializes amount screen data based on transaction type.
     *
     * Flow:
     * - If amount is empty:
     *      - For VOID_LAST → load original transaction values and set read-only mode
     *      - For others → load current txn amount and allow editing
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun onLoad(sharedViewModel: SharedViewModel) {
        transAmount.ifEmpty {
            when (sharedViewModel.objRootAppPaymentDetail.txnType) {
                TxnType.VOID_LAST -> {
                    transAmount = formatAmount(
                        sharedViewModel.objRootAppPaymentDetail.originalTxnAmount
                            ?.toDoubleOrNull() ?: 0.00
                    )
                    _origTotalAmount.value = formatAmount(
                        sharedViewModel.objRootAppPaymentDetail.originalTtlAmount
                            ?.toDoubleOrNull() ?: 0.00
                    )
                    _origDateTime.value = sharedViewModel.objRootAppPaymentDetail.dateTime
                    isReadOnly = true
                }
                else -> {
                    transAmount = formatAmount(
                        sharedViewModel.objRootAppPaymentDetail.txnAmount ?: 0.00
                    )
                    isReadOnly = false
                }
            }
        }
    }
    /**
     * Handles amount input change.
     *
     * Flow:
     * - Formats and stores input amount
     * - Returns numeric value as string for further processing
     */
    fun onAmountChange(newValue: String): String {
        transAmount = formatAmount(newValue)
        return transformToAmountDouble(newValue).toString()
    }

    /**
     * Handles confirm action for amount entry.
     *
     * Flow:
     * - Validates amount (must be > 0)
     * - Calculates total transaction amount
     * - Navigates or triggers API based on transaction type:
     *      - FOODSTAMP_RETURN → go to Card screen
     *      - VOID_LAST / E_VOUCHER → perform online authentication
     *      - PURCHASE_CASHBACK → navigate to Cashback screen
     *      - Others → go to Card screen
     */

    @RequiresApi(Build.VERSION_CODES.O)
    fun onConfirm(navHostController: NavHostController, sharedViewModel: SharedViewModel) {
        if (sharedViewModel.objRootAppPaymentDetail.txnType == TxnType.PURCHASE_CASHBACK) {
            sharedViewModel.objPosConfig?.apply { isCashback = false }
        }
        if (transformToAmountDouble(transAmount) < 0.01) {
            CustomDialogBuilder.composeAlertDialog(
                title = navHostController.context.getString(R.string.default_alert_title_error),
                message = navHostController.context.getString(R.string.err_zero_amt_not_allowed)
            )
        } else {
            calculateTotal(sharedViewModel)
            when (sharedViewModel.objRootAppPaymentDetail.txnType) {
                TxnType.FOODSTAMP_RETURN -> {
                    navHostController.navigate(AppNavigationItems.CardScreen.route) {
                        popUpTo(AppNavigationItems.AmountScreen.route) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
                TxnType.VOID_LAST, TxnType.E_VOUCHER -> {
                    authenticateTransaction(sharedViewModel, navHostController)
                }
                TxnType.PURCHASE_CASHBACK -> {
                    navHostController.navigate(AppNavigationItems.CashBackScreen.route) {
                        popUpTo(AppNavigationItems.AmountScreen.route) { inclusive = true }
                        launchSingleTop = true
                    }
                }
                else -> {
                    navHostController.navigate(AppNavigationItems.CardScreen.route) {
                        popUpTo(AppNavigationItems.AmountScreen.route) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            }
        }
    }
    /**
     * Handles cancel action.
     *
     * Flow:
     * - Navigates user back to Dashboard screen
     * - Clears previous navigation stack
     */

    fun onCancel(navHostController: NavHostController) {
        navHostController.navigateAndClean(AppNavigationItems.DashBoardScreen.route)
    }
    /**
     * Calculates tax amount based on given percentage.
     *
     * Formula:
     * - tax = (txnAmount * percent) / 100
     */

    private fun calculateTax(txnAmount: Double, percent: Double): Double {
        return txnAmount * percent / 100.00
    }
    /**
     * Calculates transaction amount and total amount based on transaction type.
     *
     * Flow:
     * - For FOODSTAMP_RETURN & VOID_LAST:
     *      - Total amount is taken from input
     *      - Transaction amount is derived by subtracting charges (tip, service, VAT)
     * - For other transactions:
     *      - Transaction amount is taken from input
     *      - VAT is calculated if tax is enabled
     *      - Total amount = txnAmount + VAT + serviceCharge
     * - Also updates dateTime for VOID transactions
     */
    @SuppressLint("SuspiciousIndentation")
    private fun calculateTotal(sharedViewModel: SharedViewModel) {
        when (sharedViewModel.objRootAppPaymentDetail.txnType) {
            TxnType.FOODSTAMP_RETURN -> {
                sharedViewModel.objRootAppPaymentDetail.ttlAmount =
                    transformToAmountDouble(transAmount)
                sharedViewModel.objRootAppPaymentDetail.txnAmount =
                    sharedViewModel.objRootAppPaymentDetail.ttlAmount
                        ?.minus(sharedViewModel.objRootAppPaymentDetail.tip ?: 0.00)
                        ?.minus(sharedViewModel.objRootAppPaymentDetail.serviceCharge ?: 0.00)
                        ?.minus(sharedViewModel.objRootAppPaymentDetail.VAT ?: 0.00)
            }
            TxnType.VOID_LAST -> {
                sharedViewModel.objRootAppPaymentDetail.dateTime = getCurrentDateTime()
                sharedViewModel.objRootAppPaymentDetail.ttlAmount =
                    transformToAmountDouble(transAmount)
                sharedViewModel.objRootAppPaymentDetail.txnAmount =
                    sharedViewModel.objRootAppPaymentDetail.ttlAmount
                        ?.minus(sharedViewModel.objRootAppPaymentDetail.tip ?: 0.00)
                        ?.minus(sharedViewModel.objRootAppPaymentDetail.serviceCharge ?: 0.00)
                        ?.minus(sharedViewModel.objRootAppPaymentDetail.VAT ?: 0.00)
            }
            else -> {
                sharedViewModel.objRootAppPaymentDetail.txnAmount =
                    transformToAmountDouble(transAmount)

                if (sharedViewModel.objPosConfig?.isTaxEnabled == true) {
                    sharedViewModel.objRootAppPaymentDetail.VAT = calculateTax(
                        (sharedViewModel.objRootAppPaymentDetail.txnAmount ?: 0.00) +
                                (sharedViewModel.objRootAppPaymentDetail.serviceCharge ?: 0.00),
                        sharedViewModel.objPosConfig?.vatPercent ?: 0.00
                    )
                }

                sharedViewModel.objRootAppPaymentDetail.ttlAmount =
                    (sharedViewModel.objRootAppPaymentDetail.txnAmount ?: 0.00) +
                            (sharedViewModel.objRootAppPaymentDetail.VAT ?: 0.00) +
                            (sharedViewModel.objRootAppPaymentDetail.serviceCharge ?: 0.00)
            }
        }
    }
    /**
     * Fetches the last transaction from DB and prepares it for further processing (e.g., void/reversal).
     *
     * Flow:
     * - Retrieve last transaction from database
     * - If already voided → show error and navigate back to dashboard
     * - Else → transform DB entity into sharedViewModel object
     * - Copy required fields and enrich with POS config data
     * - If no transaction found → show error dialog
     */
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun fetchLastTransaction(navHostController: NavHostController, context: Context, sharedViewModel: SharedViewModel) {
        val lastTxn = dbRepository.fetchLastTransactionByTxnType()
        Log.d("DB_DEBUG", "lastTxn: $lastTxn")
        lastTxn?.let {
            if (it.isVoided == true || it.txnType == TxnType.VOID_LAST.toString()) {
                CustomDialogBuilder.composeAlertDialog(
                    title = context.getString(R.string.default_alert_title_error),
                    message = context.getString(R.string.err_txn_already_voided),
                    onOkClick = {
                        CoroutineScope(Dispatchers.Main).launch {
                            delay(500)
                            navHostController.navigateAndClean(AppNavigationItems.DashBoardScreen.route)
                        }
                    }
                )

            } else {
                val transformedTxn =
                    PaymentServiceUtils.transformObject<ObjRootAppPaymentDetails>(it)

                transformedTxn?.let {
                    sharedViewModel.objRootAppPaymentDetail = it.copy(
                        id = sharedViewModel.objRootAppPaymentDetail.id,
                        txnType = sharedViewModel.objRootAppPaymentDetail.txnType,
                        fnsNumber = sharedViewModel.objPosConfig?.fnsNumber,
                        merchantNameLocation = sharedViewModel.objPosConfig?.merchantNameLocation,
                        merchantBankName = sharedViewModel.objPosConfig?.merchantBankName,
                        merchantType = sharedViewModel.objPosConfig?.merchantType,
                        procId = sharedViewModel.objPosConfig?.procId,
                        stateCode = sharedViewModel.objPosConfig?.stateCode,
                        countyCode = sharedViewModel.objPosConfig?.countyCode,
                        postalServiceCode = sharedViewModel.objPosConfig?.postalServiceCode
                    )
                    sharedViewModel.objRootAppPaymentDetail.isFallback = it.isFallback
                    sharedViewModel.objRootAppPaymentDetail.processingCode = it.processingCode
                    sharedViewModel.objRootAppPaymentDetail.rrn = it.rrn
                    sharedViewModel.objRootAppPaymentDetail.localTime = it.localTime
                    sharedViewModel.objRootAppPaymentDetail.localDate = it.localDate
                    sharedViewModel.objRootAppPaymentDetail.dateTime = it.dateTime
                    sharedViewModel.objRootAppPaymentDetail.settlementDate = it.settlementDate
                    sharedViewModel.objRootAppPaymentDetail.posConditionCode = it.posConditionCode
                    sharedViewModel.objRootAppPaymentDetail.stan = it.stan
                    sharedViewModel.objRootAppPaymentDetail.posEntryMode = it.posEntryMode
                    sharedViewModel.objRootAppPaymentDetail.cardEntryMode =
                        if (sharedViewModel.objRootAppPaymentDetail.isFallback == true)
                            CardEntryMode.FALLBACK_MAGSTRIPE
                        else
                            it.cardEntryMode
                    sharedViewModel.objRootAppPaymentDetail.originalTxnType = it.txnType
                    sharedViewModel.objRootAppPaymentDetail.currencyCode = it.currencyCode
                    sharedViewModel.objRootAppPaymentDetail.originalDateTime = it.originalDateTime
                    sharedViewModel.objRootAppPaymentDetail.hostAuthCode = it.hostAuthCode
                    sharedViewModel.objRootAppPaymentDetail.receiptEmvData = it.receiptEmvData
                    sharedViewModel.objRootAppPaymentDetail.originalTxnType = it.txnType

                    sharedViewModel.objRootAppPaymentDetail.originalCashback =
                        it.cashback.toDecimalFormat()
                    sharedViewModel.objRootAppPaymentDetail.originalTtlAmount =
                        it.ttlAmount.toDecimalFormat()
                    sharedViewModel.objRootAppPaymentDetail.originalTxnAmount =
                        it.txnAmount.toDecimalFormat()
                    sharedViewModel.objRootAppPaymentDetail.originalHostTxnRef = it.hostTxnRef
                }
            }
        } ?: run {
            CustomDialogBuilder.composeAlertDialog(
                title = context.getString(R.string.default_alert_title_error),
                message = context.getString(R.string.err_txn_not_found)
            )
        }
    }

    /**
     * This function handles the complete online transaction authentication flow.
     *
     * Flow:
     * - Initiates API call to perform online authorization using transaction details
     * - Shows a progress dialog while the API request is in progress
     * - Transforms local transaction data into API request format
     *
     * On Success:
     * - Hides progress dialog
     * - Updates sharedViewModel with response data received from host:
     *      - settlementDate (for batch settlement)
     *      - rrn (Retrieval Reference Number)
     *      - hostAuthCode (authorization code)
     *      - originalDateTime (host transaction timestamp)
     *      - stan (System Trace Audit Number)
     *      - hostRespCode (response code from host)
     * - Maps host response code to a readable message
     * - Determines final transaction status (APPROVED / DECLINED)
     * - Calls updateTransResult() to persist updated transaction in local database
     * - Navigates to Approved screen (used for both success/failure result display)
     *
     * On Error:
     * - Navigates to Approved screen (fallback handling)
     *
     * On Timeout:
     * - Displays an alert dialog with timeout message
     *
     * Notes:
     * - Uses viewModelScope coroutine to perform async API operation
     * - Ensures UI responsiveness by avoiding blocking main thread
     * - Handles exceptions safely using try-catch
     */

    @RequiresApi(Build.VERSION_CODES.O)
    fun authenticateTransaction(sharedViewModel: SharedViewModel, navHostController: NavHostController) {
        viewModelScope.launch {
            try {
                CustomDialogBuilder.composeProgressDialog(true)
                apiServiceRepository.apiServiceRequestOnlineAuth(
                    paymentServiceTxnDetails = PaymentServiceUtils.transformObject<PaymentServiceTxnDetails>(
                        sharedViewModel.objRootAppPaymentDetail
                    ),
                    object : IApiServiceResponseListener {
                        override fun onApiServiceSuccess(response: PaymentServiceTxnDetails) {
                            CustomDialogBuilder.composeProgressDialog(false)
                            val settlementDate = response.settlementDate
                            sharedViewModel.objRootAppPaymentDetail.settlementDate =
                                response.settlementDate
                            sharedViewModel.objRootAppPaymentDetail.rrn = response.rrn
                            sharedViewModel.objRootAppPaymentDetail.hostAuthCode =
                                response.hostAuthCode
                            sharedViewModel.objRootAppPaymentDetail.originalDateTime =
                                response.originalDateTime
                            sharedViewModel.objRootAppPaymentDetail.stan = response.stan
                            sharedViewModel.objRootAppPaymentDetail.hostRespCode = response.hostRespCode

                            CustomDialogBuilder.composeProgressDialog(false)

                            sharedViewModel.objRootAppPaymentDetail.hostResMessage =
                                BuilderConstants.getIsoResponseMessage(response.hostRespCode.toString())
                            sharedViewModel.objRootAppPaymentDetail.txnStatus =
                                if (response.txnStatus == TxnStatus.APPROVED.toString())
                                    TxnStatus.APPROVED
                                else
                                    TxnStatus.DECLINED

                            updateTransResult(
                                sharedViewModel,
                                emvStatusToTransStatus(response.hostRespCode),
                                sharedViewModel.objRootAppPaymentDetail.originalDateTime.toString(),
                                sharedViewModel.objRootAppPaymentDetail.hostAuthCode.toString(),
                                sharedViewModel.objRootAppPaymentDetail.posCondition.toString()
                            )
                            navHostController.navigate(AppNavigationItems.ApprovedScreen.route)
                        }

                        override fun onApiServiceError(error: ApiServiceError) {
                            navHostController.navigate(AppNavigationItems.ApprovedScreen.route)
                        }

                        override fun onApiServiceTimeout(apiServiceTimeout: ApiServiceTimeout) {
                            CustomDialogBuilder.Companion.composeAlertDialog(
                                title = navHostController.context.resources?.getString(
                                    R.string.default_alert_title_error
                                ),
                                message = apiServiceTimeout.message
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                Log.e("ApiCallException", e.message ?: "Unknown error")
            }
        }
    }
    /**
     * This function updates the transaction result both in:
     * 1. SharedViewModel (in-memory state for UI usage)
     * 2. Local database (persistent storage)
     *
     * Flow:
     * - Update txnStatus in sharedViewModel for immediate UI reflection
     * - Fetch the existing transaction from DB using txnId
     * - Update important transaction fields such as:
     *      - txnStatus (approved/declined)
     *      - originalDateTime (host/original txn time)
     *      - hostAuthCode (authorization code from server)
     *      - stan (System Trace Audit Number)
     *      - voucherNumber (receipt reference)
     *      - rrn (Retrieval Reference Number)
     *      - settlementDate (used for batch settlement)
     *      - approvalCode (final approval reference)
     *      - posConditionCode (transaction type indicator)
     * - Save the updated transaction back into the database
     *
     * Notes:
     * - Runs inside viewModelScope to avoid blocking UI thread
     * - Handles null txnStatus safely
     * - Logs error if transaction is not found in DB
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun updateTransResult(sharedViewModel: SharedViewModel, txnStatus: TxnStatus?, originalDateTime: String, AuthCode: String, posCondition: String) {
        sharedViewModel.objRootAppPaymentDetail.txnStatus = txnStatus
        viewModelScope.launch {
            val txnId = sharedViewModel.objRootAppPaymentDetail.id
            dbRepository.fetchTxnById(txnId)?.let { txn ->
                txn.txnStatus = txnStatus?.toString() ?: ""
                txn.originalDateTime = originalDateTime
                txn.hostAuthCode = AuthCode
                txn.stan = sharedViewModel.objRootAppPaymentDetail.stan
                txn.VoucherNumber = sharedViewModel.objRootAppPaymentDetail.voucherNumber
                txn.rrn = sharedViewModel.objRootAppPaymentDetail.rrn
                txn.settlementDate = sharedViewModel.objRootAppPaymentDetail.settlementDate
                txn.ApprovalCode = sharedViewModel.objRootAppPaymentDetail.approvalCode
                txn.posConditionCode = posCondition
                Log.d("DATABASE","Txn Update Amount Viewmodel")
                dbRepository.updateTxn(txn)
            } ?: run {
                Log.e("AmountView", "Transaction NOT FOUND for txnId: $txnId")
            }
        }
    }
}
