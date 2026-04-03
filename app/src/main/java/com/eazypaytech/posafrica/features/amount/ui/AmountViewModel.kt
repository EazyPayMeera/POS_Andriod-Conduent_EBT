package com.eazypaytech.posafrica.features.amount.ui

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
import com.eazypaytech.posafrica.R
import com.eazypaytech.posafrica.features.activity.ui.SharedViewModel
import com.eazypaytech.posafrica.features.dialogs.ui.CustomDialogBuilder
import com.eazypaytech.posafrica.core.navigation.routes.AppNavigationItems
import com.eazypaytech.posafrica.domain.model.ObjRootAppPaymentDetails
import com.eazypaytech.posafrica.core.utils.formatAmount
import com.eazypaytech.posafrica.core.utils.getCurrentDateTime
import com.eazypaytech.posafrica.core.utils.navigateAndClean
import com.eazypaytech.posafrica.core.utils.transformToAmountDouble
import com.analogics.securityframework.data.repository.TxnDBRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AmountViewModel @Inject constructor(private  var apiServiceRepository: ApiServiceRepository, private val dbRepository: TxnDBRepository) : ViewModel() {

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
    fun onLoad(navHostController: NavHostController, context: Context, sharedViewModel: SharedViewModel)
    {
        transAmount.ifEmpty {
            when (sharedViewModel.objRootAppPaymentDetail.txnType) {
                TxnType.VOID_LAST -> {
                    transAmount =
                        formatAmount(
                            sharedViewModel.objRootAppPaymentDetail.originalTxnAmount?.toDoubleOrNull()
                                ?: 0.00
                        )
                    _origTotalAmount.value = formatAmount(
                        sharedViewModel.objRootAppPaymentDetail.originalTtlAmount?.toDoubleOrNull()
                            ?: 0.00
                    )
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


    fun navigateToAmountScreen(navHostController: NavHostController, sharedViewModel: SharedViewModel) {
        viewModelScope.launch {
            navHostController.navigate(AppNavigationItems.AmountScreen.route)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun onConfirm(navHostController: NavHostController, sharedViewModel: SharedViewModel) {
        sharedViewModel.objRootAppPaymentDetail.isReturn = false
        sharedViewModel.objRootAppPaymentDetail.isPurchase = false
        if (sharedViewModel.objRootAppPaymentDetail.txnType == TxnType.PURCHASE_CASHBACK) {
            sharedViewModel.objPosConfig?.apply { isCashback = false }
        }
        if(transformToAmountDouble(transAmount) <0.01) {
            CustomDialogBuilder.Companion.composeAlertDialog(
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
                TxnType.VOID_LAST, TxnType.E_VOUCHER -> {
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
                sharedViewModel.objRootAppPaymentDetail.ttlAmount =
                    transformToAmountDouble(transAmount)
                sharedViewModel.objRootAppPaymentDetail.txnAmount = sharedViewModel.objRootAppPaymentDetail.ttlAmount?.
                minus(sharedViewModel.objRootAppPaymentDetail.tip?:0.00)?.
                minus(sharedViewModel.objRootAppPaymentDetail.serviceCharge?:0.00)?.
                minus(sharedViewModel.objRootAppPaymentDetail.VAT?:0.00)
            }
            TxnType.VOID_LAST -> {
                sharedViewModel.objRootAppPaymentDetail.dateTime = getCurrentDateTime()
                sharedViewModel.objRootAppPaymentDetail.ttlAmount =
                    transformToAmountDouble(transAmount)
                sharedViewModel.objRootAppPaymentDetail.txnAmount = sharedViewModel.objRootAppPaymentDetail.ttlAmount?.
                minus(sharedViewModel.objRootAppPaymentDetail.tip?:0.00)?.
                minus(sharedViewModel.objRootAppPaymentDetail.serviceCharge?:0.00)?.
                minus(sharedViewModel.objRootAppPaymentDetail.VAT?:0.00)
            }
            else -> {
                // Handle non-REFUND and non-PREAUTH cases
                sharedViewModel.objRootAppPaymentDetail.txnAmount =
                    transformToAmountDouble(transAmount)

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
        Log.d("TXN_DEBUG", "Last Transaction: $lastTxn")
        lastTxn?.let {

            if (it.isVoided == true || it.txnType == TxnType.VOID_LAST.toString()) {

                CustomDialogBuilder.Companion.composeAlertDialog(
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
                        procId = sharedViewModel.objPosConfig?.procId

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
                    sharedViewModel.objRootAppPaymentDetail.originalDateTime = it.originalDateTime
                    sharedViewModel.objRootAppPaymentDetail.hostAuthCode = it.hostAuthCode
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

            CustomDialogBuilder.Companion.composeAlertDialog(
                title = context.getString(R.string.default_alert_title_error),
                message = context.getString(R.string.err_txn_not_found)
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun authenticateTransaction(sharedViewModel: SharedViewModel, navHostController: NavHostController) {
        viewModelScope.launch {
            try {
                CustomDialogBuilder.Companion.composeProgressDialog(true)
                apiServiceRepository.apiServiceRequestOnlineAuth(paymentServiceTxnDetails = PaymentServiceUtils.transformObject<PaymentServiceTxnDetails>(sharedViewModel.objRootAppPaymentDetail), object :
                    IApiServiceResponseListener {

                    override fun onApiServiceSuccess(response: PaymentServiceTxnDetails) {
                        CustomDialogBuilder.Companion.composeProgressDialog(false)
                        sharedViewModel.objRootAppPaymentDetail.hostResMessage = BuilderConstants.getIsoResponseMessage(response.hostRespCode.toString())
                        sharedViewModel.objRootAppPaymentDetail.txnStatus = if(response.txnStatus == TxnStatus.APPROVED.toString()) TxnStatus.APPROVED else TxnStatus.DECLINED
                        navHostController.navigate(AppNavigationItems.ApprovedScreen.route)
                    }

                    override fun onApiServiceError(error: ApiServiceError) {
                        navHostController.navigate(AppNavigationItems.ApprovedScreen.route)
                    }
                    override  fun onApiServiceTimeout(apiServiceTimeout: ApiServiceTimeout) {
                        CustomDialogBuilder.Companion.composeAlertDialog(title = navHostController.context.resources?.getString(
                            R.string.default_alert_title_error),message = apiServiceTimeout.message)
                    }

                })
            } catch (e: Exception) {

                Log.e("ApiCallException", e.message ?: "Unknown error")
                navHostController.navigate(AppNavigationItems.DeclineScreen.route)
            }
        }
    }

}