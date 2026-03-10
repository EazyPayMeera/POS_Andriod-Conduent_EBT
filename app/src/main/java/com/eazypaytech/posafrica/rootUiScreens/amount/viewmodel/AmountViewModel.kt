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
    val timeDate: StateFlow<String?> = _timeDate
    private val _origDateTime = MutableStateFlow<String?>(null)
    val origDateTime: StateFlow<String?> = _origDateTime

    @RequiresApi(Build.VERSION_CODES.O)
    fun onLoad(navHostController: NavHostController,context: Context,sharedViewModel: SharedViewModel)
    {
        transAmount.ifEmpty {
            when (sharedViewModel.objRootAppPaymentDetail.txnType) {
                TxnType.VOID_LAST -> {
                    Log.d(
                        "FETCH_TXN",
                        "OriginalAmount123: ${sharedViewModel.objRootAppPaymentDetail.originalTxnAmount}"
                    )
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

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun fetchLastTransaction(
        navHostController: NavHostController,
        context: Context,
        sharedViewModel: SharedViewModel
    ) {


            Log.d("FETCH_TXN", "Starting fetchLastTransaction")

            val lastTxn = dbRepository.fetchLastTransaction()

            Log.d("FETCH_TXN", "DB Result: $lastTxn")

            lastTxn?.let {

                Log.d("FETCH_TXN", "TxnType: ${it.txnType}")
                Log.d("FETCH_TXN", "IsVoided: ${it.isVoided}")
                Log.d("FETCH_TXN", "TTL Amount: ${it.ttlAmount}")
                Log.d("FETCH_TXN", "Txn Amount: ${it.txnAmount}")
                Log.d("FETCH_TXN", "Cashback: ${it.cashback}")
                Log.d("FETCH_TXN", "HostTxnRef: ${it.hostTxnRef}")

                if (it.isVoided == true || it.txnType == TxnType.VOID_LAST.toString()) {

                    Log.d("FETCH_TXN", "Transaction already voided")

                    CustomDialogBuilder.composeAlertDialog(
                        title = context.getString(R.string.default_alert_title_error),
                        message = context.getString(R.string.err_txn_already_voided)
                    )

                } else {

                    val transformedTxn =
                        PaymentServiceUtils.transformObject<ObjRootAppPaymentDetails>(it)

                    Log.d("FETCH_TXN", "Transformed Object: $transformedTxn")

                    transformedTxn?.let {

                        sharedViewModel.objRootAppPaymentDetail = it.copy(
                            id = sharedViewModel.objRootAppPaymentDetail.id,
                            txnType = sharedViewModel.objRootAppPaymentDetail.txnType,
                            txnStatus = sharedViewModel.objRootAppPaymentDetail.txnStatus,
                            hostAuthResult = sharedViewModel.objRootAppPaymentDetail.hostAuthResult
                        )

                        Log.d("FETCH_TXN", "Copied Object: ${sharedViewModel.objRootAppPaymentDetail}")

                        sharedViewModel.objRootAppPaymentDetail.originalTxnType = it.txnType
                        sharedViewModel.objRootAppPaymentDetail.originalCashback =
                            it.cashback.toDecimalFormat()
                        sharedViewModel.objRootAppPaymentDetail.originalTtlAmount =
                            it.ttlAmount.toDecimalFormat()
                        sharedViewModel.objRootAppPaymentDetail.originalTxnAmount =
                            it.txnAmount.toDecimalFormat()
                        sharedViewModel.objRootAppPaymentDetail.originalHostTxnRef = it.hostTxnRef

                        Log.d(
                            "FETCH_TXN",
                            "OriginalTxnType: ${sharedViewModel.objRootAppPaymentDetail.originalTxnType}"
                        )
                        Log.d(
                            "FETCH_TXN",
                            "OriginalAmount: ${sharedViewModel.objRootAppPaymentDetail.originalTxnAmount}"
                        )
                        Log.d(
                            "FETCH_TXN",
                            "OriginalTotal: ${sharedViewModel.objRootAppPaymentDetail.originalTtlAmount}"
                        )
                        Log.d(
                            "FETCH_TXN",
                            "OriginalHostRef: ${sharedViewModel.objRootAppPaymentDetail.originalHostTxnRef}"
                        )
                    }

                    Log.d("FETCH_TXN", "Navigating to Amount Screen")

                    //navigateToAmountScreen(navHostController, sharedViewModel)
                }

            } ?: run {

                Log.d("FETCH_TXN", "No transaction found in DB")

                CustomDialogBuilder.composeAlertDialog(
                    title = context.getString(R.string.default_alert_title_error),
                    message = context.getString(R.string.err_txn_not_found)
                )
            }
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
                TxnType.VOID_LAST -> {
                    Log.d("Database", "Go to update when void")
                    authenticateTransaction(sharedViewModel, navHostController)
                }
                TxnType.PURCHASE_CASHBACK -> {
                    Log.d("Database", "Go to update when void")
                    navHostController.navigate(AppNavigationItems.CashBackScreen.route)
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
    fun updateTransResult(objRootAppPaymentDetails: ObjRootAppPaymentDetails)
    {
        Log.d("Database","Go to update ")
        viewModelScope.launch {
            dbRepository.insertOrUpdateTxn(PaymentServiceUtils.transformObject<TxnEntity>(objRootAppPaymentDetails))
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun authenticateTransaction(sharedViewModel: SharedViewModel, navHostController: NavHostController) {
        viewModelScope.launch {
            try {
                Log.d("AuthTransaction","Going For Authenticate the transaction")
                apiServiceRepository.apiServiceRequestOnlineAuth(paymentServiceTxnDetails = PaymentServiceUtils.transformObject<PaymentServiceTxnDetails>(sharedViewModel.objRootAppPaymentDetail), object :
                    IApiServiceResponseListener {

                    override fun onApiServiceSuccess(response: PaymentServiceTxnDetails) {
                        sharedViewModel.objRootAppPaymentDetail.txnStatus = if(response.txnStatus == TxnStatus.APPROVED.toString()) TxnStatus.APPROVED else TxnStatus.DECLINED
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
                // Handle any exceptions that may occur
                Log.e("ApiCallException", e.message ?: "Unknown error")
                navHostController.navigate(AppNavigationItems.DeclineScreen.route)
            }
        }
    }
}