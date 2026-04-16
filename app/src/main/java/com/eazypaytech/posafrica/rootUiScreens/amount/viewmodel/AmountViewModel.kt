package com.eazypaytech.posafrica.rootUiScreens.amount.viewmodel

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
import com.eazypaytech.builder_core.constants.BuilderConstants
import com.eazypaytech.paymentservicecore.listeners.responseListener.IApiServiceResponseListener
import com.eazypaytech.paymentservicecore.model.PaymentServiceTxnDetails
import com.eazypaytech.paymentservicecore.model.error.ApiServiceError
import com.eazypaytech.paymentservicecore.model.error.ApiServiceTimeout
import com.eazypaytech.paymentservicecore.models.TxnStatus
import com.eazypaytech.paymentservicecore.models.TxnType
import com.eazypaytech.paymentservicecore.repository.apiService.ApiServiceRepository
import com.eazypaytech.paymentservicecore.utils.PaymentServiceUtils
import com.eazypaytech.paymentservicecore.utils.toDecimalFormat
import com.eazypaytech.securityframework.database.dbRepository.TxnDBRepository
import com.eazypaytech.securityframework.database.entity.TxnEntity
import com.eazypaytech.posafrica.navigation.AppNavigationItems
import com.eazypaytech.posafrica.rootModel.ObjRootAppPaymentDetails
import com.eazypaytech.posafrica.rootUiScreens.activity.SharedViewModel
import com.eazypaytech.posafrica.rootUiScreens.dialogs.CustomDialogBuilder
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.formatAmount
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.getCurrentDateTime
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.navigateAndClean
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.transformToAmountDouble
import com.eazypaytech.posafrica.R
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.emvStatusToTransStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AmountViewModel @Inject constructor(private  var apiServiceRepository: ApiServiceRepository,private val dbRepository: TxnDBRepository) : ViewModel() {

    var transAmount by mutableStateOf("")
        private set

    var isReadOnly by mutableStateOf(true)
        private set
    private val _totalAmount = MutableStateFlow<String?>(null)
    val totalAmount: StateFlow<String?> = _totalAmount
    private val _origTotalAmount = MutableStateFlow<String?>(null)
    val origTotalAmount: StateFlow<String?> = _origTotalAmount

    private val _timeDate = MutableStateFlow<String?>(null)
    private val _origDateTime = MutableStateFlow<String?>(null)
    val origDateTime: StateFlow<String?> = _origDateTime

    @RequiresApi(Build.VERSION_CODES.O)
    fun onLoad(navHostController: NavHostController,context: Context,sharedViewModel: SharedViewModel)
    {
        transAmount.ifEmpty {
            when (sharedViewModel.objRootAppPaymentDetail.txnType) {
                TxnType.VOID_LAST -> {
                    transAmount =
                        formatAmount(sharedViewModel.objRootAppPaymentDetail.originalTxnAmount?.toDoubleOrNull() ?: 0.00)
                    _origTotalAmount.value = formatAmount(sharedViewModel.objRootAppPaymentDetail.originalTtlAmount?.toDoubleOrNull() ?: 0.00)
                    _origDateTime.value = sharedViewModel.objRootAppPaymentDetail.dateTime
                    isReadOnly = false
                }
                else -> {
                    transAmount =
                        formatAmount(sharedViewModel.objRootAppPaymentDetail.txnAmount ?: 0.00)
                    isReadOnly = false
                }
            }
        }
    }

    fun onAmountChange(newValue: String) :String{
        transAmount = formatAmount(newValue)
        return transformToAmountDouble(newValue).toString()
    }


    fun navigateToAmountScreen(navHostController: NavHostController,sharedViewModel: SharedViewModel) {
        viewModelScope.launch {
            navHostController.navigate(AppNavigationItems.AmountScreen.route)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun onConfirm(navHostController: NavHostController, sharedViewModel: SharedViewModel) {
        if (sharedViewModel.objRootAppPaymentDetail.txnType == TxnType.PURCHASE_CASHBACK) {
            sharedViewModel.objPosConfig?.apply { isCashback = false }
        }
        if(transformToAmountDouble(transAmount)<0.01) {
            CustomDialogBuilder.composeAlertDialog(
                title = navHostController.context.getString(R.string.default_alert_title_error),
                message = navHostController.context.getString(R.string.err_zero_amt_not_allowed)
            )
        }
        else {
            calculateTotal(sharedViewModel)
            when (sharedViewModel.objRootAppPaymentDetail.txnType) {
                TxnType.FOODSTAMP_RETURN-> {
                    navHostController.navigate(AppNavigationItems.CardScreen.route)
                }
                TxnType.VOID_LAST,TxnType.E_VOUCHER -> {
                    Log.d("Database", "Go to update when void")
                    authenticateTransaction(sharedViewModel, navHostController)
                }
                TxnType.PURCHASE_CASHBACK -> {
                    Log.d("Database", "Go to update when void")
                    navHostController.navigate(AppNavigationItems.CashBackScreen.route)
                }
                TxnType.E_VOUCHER -> {
                    Log.d("Database", "Go to update when void")
                    authenticateTransaction(sharedViewModel,navHostController)
                }
                else -> {
                    navHostController.navigate(AppNavigationItems.CardScreen.route)
                }
            }
        }
    }

    fun onCancel(navHostController: NavHostController) {
        navHostController.navigateAndClean(AppNavigationItems.DashBoardScreen.route)
    }

    private fun calculateTax(txnAmount: Double, percent: Double) : Double {
        return txnAmount * percent / 100.00
    }

    @SuppressLint("SuspiciousIndentation")
    private fun calculateTotal(sharedViewModel: SharedViewModel) {
        when (sharedViewModel.objRootAppPaymentDetail.txnType) {
            TxnType.FOODSTAMP_RETURN -> {
                sharedViewModel.objRootAppPaymentDetail.ttlAmount = transformToAmountDouble(transAmount)
                sharedViewModel.objRootAppPaymentDetail.txnAmount = sharedViewModel.objRootAppPaymentDetail.ttlAmount?.
                minus(sharedViewModel.objRootAppPaymentDetail.tip?:0.00)?.
                minus(sharedViewModel.objRootAppPaymentDetail.serviceCharge?:0.00)?.
                minus(sharedViewModel.objRootAppPaymentDetail.VAT?:0.00)
            }
            TxnType.VOID_LAST -> {
                sharedViewModel.objRootAppPaymentDetail.dateTime = getCurrentDateTime()
                sharedViewModel.objRootAppPaymentDetail.ttlAmount = transformToAmountDouble(transAmount)
                sharedViewModel.objRootAppPaymentDetail.txnAmount = sharedViewModel.objRootAppPaymentDetail.ttlAmount?.
                minus(sharedViewModel.objRootAppPaymentDetail.tip?:0.00)?.
                minus(sharedViewModel.objRootAppPaymentDetail.serviceCharge?:0.00)?.
                minus(sharedViewModel.objRootAppPaymentDetail.VAT?:0.00)
            }
            else -> {
                // Handle non-REFUND and non-PREAUTH cases
                sharedViewModel.objRootAppPaymentDetail.txnAmount = transformToAmountDouble(transAmount)

                /* TAX on TOP of Service Charge */
                if (sharedViewModel.objPosConfig?.isTaxEnabled == true) {
                    sharedViewModel.objRootAppPaymentDetail.VAT = calculateTax(
                        (sharedViewModel.objRootAppPaymentDetail.txnAmount ?: 0.00) + (sharedViewModel.objRootAppPaymentDetail.serviceCharge?:0.00),
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


    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun fetchLastTransaction(
        navHostController: NavHostController,
        context: Context,
        sharedViewModel: SharedViewModel
    ) {

        val lastTxn = dbRepository.fetchLastTransactionByTxnType()
        Log.d("TXN_DEBUG", "lastTxn id = ${lastTxn?.id}")
        lastTxn?.let {

            if (it.isVoided == true || it.txnType == TxnType.VOID_LAST.toString()) {

                CustomDialogBuilder.composeAlertDialog(
                    title = context.getString(R.string.default_alert_title_error),
                    message = context.getString(R.string.err_txn_already_voided)
                )
                navHostController.navigateAndClean(AppNavigationItems.DashBoardScreen.route)
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
                    sharedViewModel.objRootAppPaymentDetail.processingCode = it.processingCode
                    sharedViewModel.objRootAppPaymentDetail.rrn = it.rrn
                    sharedViewModel.objRootAppPaymentDetail.localTime = it.localTime
                    sharedViewModel.objRootAppPaymentDetail.localDate = it.localDate
                    sharedViewModel.objRootAppPaymentDetail.dateTime = it.dateTime
                    sharedViewModel.objRootAppPaymentDetail.settlementDate = it.settlementDate
                    sharedViewModel.objRootAppPaymentDetail.posConditionCode = it.posConditionCode
                    sharedViewModel.objRootAppPaymentDetail.stan = it.stan
                    sharedViewModel.objRootAppPaymentDetail.posEntryMode = it.posEntryMode
                    sharedViewModel.objRootAppPaymentDetail.originalTxnType = it.txnType
                    sharedViewModel.objRootAppPaymentDetail.currencyCode = it.currencyCode
                    sharedViewModel.objRootAppPaymentDetail.originalDateTime = it.dateTime
                    sharedViewModel.objRootAppPaymentDetail.hostAuthCode = it.hostAuthCode
                    sharedViewModel.objRootAppPaymentDetail.emvData = it.emvData
                    Log.d("TXN_DETAILS", """
    ProcessingCode   = ${sharedViewModel.objRootAppPaymentDetail.processingCode}
    RRN              = ${sharedViewModel.objRootAppPaymentDetail.rrn}
    LocalTime        = ${sharedViewModel.objRootAppPaymentDetail.localTime}
    LocalDate        = ${sharedViewModel.objRootAppPaymentDetail.localDate}
    DateTime         = ${sharedViewModel.objRootAppPaymentDetail.dateTime}
    SettlementDate   = ${sharedViewModel.objRootAppPaymentDetail.settlementDate}
    POSConditionCode = ${sharedViewModel.objRootAppPaymentDetail.posConditionCode}
    STAN             = ${sharedViewModel.objRootAppPaymentDetail.stan}
    POSEntryMode     = ${sharedViewModel.objRootAppPaymentDetail.posEntryMode}
    OriginalTxnType  = ${sharedViewModel.objRootAppPaymentDetail.originalTxnType}
    CurrencyCode     = ${sharedViewModel.objRootAppPaymentDetail.currencyCode}
    OriginalDateTime = ${sharedViewModel.objRootAppPaymentDetail.originalDateTime}
    HostAuthCode     = ${sharedViewModel.objRootAppPaymentDetail.hostAuthCode}
    EMV Data         = ${sharedViewModel.objRootAppPaymentDetail.emvData}
""".trimIndent())
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

    @RequiresApi(Build.VERSION_CODES.O)
    fun authenticateTransaction(sharedViewModel: SharedViewModel, navHostController: NavHostController) {
        viewModelScope.launch {
            try {
                CustomDialogBuilder.composeProgressDialog(true)
                apiServiceRepository.apiServiceRequestOnlineAuth(paymentServiceTxnDetails = PaymentServiceUtils.transformObject<PaymentServiceTxnDetails>(sharedViewModel.objRootAppPaymentDetail), object :
                    IApiServiceResponseListener {

                    override fun onApiServiceSuccess(response: PaymentServiceTxnDetails) {
                        CustomDialogBuilder.composeProgressDialog(false)
                        val settlementDate = response.settlementDate

                        Log.d("EBT", "settlementDate: $settlementDate")
                        sharedViewModel.objRootAppPaymentDetail.settlementDate = response.settlementDate
                        sharedViewModel.objRootAppPaymentDetail.rrn = response.rrn
                        sharedViewModel.objRootAppPaymentDetail.hostAuthCode = response.hostAuthCode
                        sharedViewModel.objRootAppPaymentDetail.originalDateTime = response.dateTime
                        sharedViewModel.objRootAppPaymentDetail.hostResMessage = BuilderConstants.getIsoResponseMessage(response.hostRespCode.toString())
                        sharedViewModel.objRootAppPaymentDetail.txnStatus = if(response.txnStatus == TxnStatus.APPROVED.toString()) TxnStatus.APPROVED else TxnStatus.DECLINED
                        updateTransResult(sharedViewModel, emvStatusToTransStatus(response.hostRespCode),
                            sharedViewModel.objRootAppPaymentDetail.dateTime.toString(),sharedViewModel.objRootAppPaymentDetail.hostAuthCode.toString(),sharedViewModel.objRootAppPaymentDetail.posCondition.toString())
                        navHostController.navigate(AppNavigationItems.ApprovedScreen.route)
                    }

                    override fun onApiServiceError(error: ApiServiceError) {
                        navHostController.navigate(AppNavigationItems.ApprovedScreen.route)
                    }
                    override  fun onApiServiceTimeout(apiServiceTimeout: ApiServiceTimeout) {
                        CustomDialogBuilder.composeAlertDialog(title = navHostController.context.resources?.getString(R.string.default_alert_title_error),message = apiServiceTimeout.message)
                    }

                })
            } catch (e: Exception) {

                Log.e("ApiCallException", e.message ?: "Unknown error")

            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun updateTransResult(
        sharedViewModel: SharedViewModel,
        txnStatus: TxnStatus?,
        originalDateTime: String,
        AuthCode: String,
        posCondition: String
    ) {
        val TAG = "UPDATE_TXN_DEBUG"

        // Log incoming (NEW) values
        Log.d(TAG, "---- Incoming Values ----")
        Log.d(TAG, "txnStatus (new): $txnStatus")
        Log.d(TAG, "originalDateTime (new): $originalDateTime")
        Log.d(TAG, "AuthCode (new): $AuthCode")
        Log.d(TAG, "posCondition (new): $posCondition")
        Log.d(TAG, "txnId: ${sharedViewModel.objRootAppPaymentDetail.id}")

        // Update ViewModel value
        sharedViewModel.objRootAppPaymentDetail.txnStatus = txnStatus

        viewModelScope.launch {
            val txnId = sharedViewModel.objRootAppPaymentDetail.id

            dbRepository.fetchTxnById(txnId)?.let { txn ->

                // Log OLD values from DB
                Log.d(TAG, "---- Old DB Values ----")
                Log.d(TAG, "txnStatus (old): ${txn.txnStatus}")
                Log.d(TAG, "originalDateTime (old): ${txn.originalDateTime}")
                Log.d(TAG, "hostAuthCode (old): ${txn.hostAuthCode}")
                Log.d(TAG, "posConditionCode (old): ${txn.posConditionCode}")

                Log.d(TAG, "rrn (old): ${sharedViewModel.objRootAppPaymentDetail.rrn}")
                Log.d(TAG, "settlementDate (old): ${sharedViewModel.objRootAppPaymentDetail.settlementDate}")
                Log.d(TAG, "approvalCode (old): ${sharedViewModel.objRootAppPaymentDetail.approvalCode}")

                // Apply updates
                txn.txnStatus = txnStatus?.toString() ?: ""
                txn.originalDateTime = originalDateTime
                txn.hostAuthCode = AuthCode
                txn.rrn = sharedViewModel.objRootAppPaymentDetail.rrn
                txn.settlementDate = sharedViewModel.objRootAppPaymentDetail.settlementDate
                txn.ApprovalCode = sharedViewModel.objRootAppPaymentDetail.approvalCode
                txn.posConditionCode = posCondition


                // Log UPDATED values before saving
                Log.d(TAG, "---- Updated Values (Before Save) ----")
                Log.d(TAG, "txnStatus (updated): ${txn.txnStatus}")
                Log.d(TAG, "originalDateTime (updated): ${txn.originalDateTime}")
                Log.d(TAG, "hostAuthCode (updated): ${txn.hostAuthCode}")
                Log.d(TAG, "posConditionCode (updated): ${txn.posConditionCode}")

                dbRepository.updateTxn(txn)

                // Final confirmation log
                Log.d(TAG, "---- DB Update Completed for txnId: $txnId ----")
            } ?: run {
                Log.e(TAG, "Transaction NOT FOUND for txnId: $txnId")
            }
        }
    }

}