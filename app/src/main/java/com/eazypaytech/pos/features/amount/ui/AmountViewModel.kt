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

    fun onAmountChange(newValue: String): String {
        transAmount = formatAmount(newValue)
        return transformToAmountDouble(newValue).toString()
    }


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

    fun onCancel(navHostController: NavHostController) {
        navHostController.navigateAndClean(AppNavigationItems.DashBoardScreen.route)
    }

    private fun calculateTax(txnAmount: Double, percent: Double): Double {
        return txnAmount * percent / 100.00
    }

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
                    sharedViewModel.objRootAppPaymentDetail.emvData = it.emvData

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
                dbRepository.updateTxn(txn)
            } ?: run {
                Log.e("AmountView", "Transaction NOT FOUND for txnId: $txnId")
            }
        }
    }
}
