// InfoConfirmViewModel.kt
package com.eazypaytech.posafrica.rootUiScreens.isinfo

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
import com.eazypaytech.paymentservicecore.repository.apiService.ApiServiceRepository
import com.eazypaytech.paymentservicecore.utils.PaymentServiceUtils
import com.eazypaytech.paymentservicecore.utils.toDecimalFormat
import com.eazypaytech.posafrica.R
import com.eazypaytech.securityframework.database.dbRepository.TxnDBRepository
import com.eazypaytech.securityframework.database.entity.TxnEntity
import com.eazypaytech.posafrica.navigation.AppNavigationItems
import com.eazypaytech.posafrica.rootModel.ObjRootAppPaymentDetails
import com.eazypaytech.posafrica.rootUiScreens.activity.SharedViewModel
import com.eazypaytech.posafrica.rootUiScreens.dialogs.CustomDialogBuilder
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.formatAmount
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.getCurrentDateTime
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.getFormattedDateTime
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.navigateAndClean
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.transformToAmountDouble
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InfoConfirmViewModel @Inject constructor(private  var apiServiceRepository: ApiServiceRepository, private val dbRepository: TxnDBRepository) : ViewModel() {

    private val _totalAmount = MutableStateFlow<String?>(null)
    val totalAmount: StateFlow<String?> = _totalAmount

    var rawInput by mutableStateOf("")
        private set

    var formattedAmount by mutableStateOf("0.00")
        private set

    val transactionDateTime: String = getFormattedDateTime()


    fun onAmountChange(newValue: String) {
        if (newValue.all { it.isDigit() || it == '.' }) {
            rawInput = newValue
            formattedAmount = formatAmount(newValue)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun onConfirm(
        Amount: String,
        sharedViewModel: SharedViewModel,
        navHostController: NavHostController
    ) {

        sharedViewModel.objRootAppPaymentDetail.dateTime = getCurrentDateTime()
        sharedViewModel.objRootAppPaymentDetail.ttlAmount = transformToAmountDouble(Amount)
        navHostController.navigate(AppNavigationItems.PleaseWaitScreen.route)
        //updateTransResult(sharedViewModel.objRootAppPaymentDetail)
        authenticateTransaction(sharedViewModel,navHostController)
    }

    fun onCancel(navHostController: NavHostController) {
        navHostController.navigateAndClean(AppNavigationItems.DashBoardScreen.route)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getTotalAmountByInvoiceNo(invoiceNo: String) {
        viewModelScope.launch {
            val totalAmountString = dbRepository.fetchTotalAmountByInvoiceNo(invoiceNo)
            val totalAmount = totalAmountString?.toDoubleOrNull() ?: 0.0
            val formattedAmount = "%.2f".format(totalAmount)
            _totalAmount.value = formattedAmount
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun updateTransResult(objRootAppPaymentDetails: ObjRootAppPaymentDetails)
    {
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
                        CustomDialogBuilder.composeAlertDialog(title = navHostController.context.resources?.getString(
                            R.string.default_alert_title_error),message = apiServiceTimeout.message)
                    }
                })
            } catch (e: Exception) {
                // Handle any exceptions that may occur
                Log.e("ApiCallException", e.message ?: "Unknown error")
                navHostController.navigate(AppNavigationItems.DeclineScreen.route)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getTransactionByInvoiceNo(sharedViewModel: SharedViewModel) {
        viewModelScope.launch {
            dbRepository.fetchTransactionByInvoiceNo(sharedViewModel.objRootAppPaymentDetail.invoiceNo.toString())?.let {
                PaymentServiceUtils.transformObject<ObjRootAppPaymentDetails>(it[0])?.let {
                    sharedViewModel.objRootAppPaymentDetail = it.copy(
                        id = sharedViewModel.objRootAppPaymentDetail.id,
                        txnType = sharedViewModel.objRootAppPaymentDetail.txnType,
                        txnStatus = sharedViewModel.objRootAppPaymentDetail.txnStatus,
                        hostAuthResult = sharedViewModel.objRootAppPaymentDetail.hostAuthResult
                    )
                    sharedViewModel.objRootAppPaymentDetail.originalTxnType = it.txnType
                    sharedViewModel.objRootAppPaymentDetail.originalTip = it.tip.toDecimalFormat()
                    sharedViewModel.objRootAppPaymentDetail.originalVat = it.VAT.toDecimalFormat()
                    sharedViewModel.objRootAppPaymentDetail.originalServiceCharge = it.serviceCharge.toDecimalFormat()
                    sharedViewModel.objRootAppPaymentDetail.originalCashback =it.cashback.toDecimalFormat()
                    sharedViewModel.objRootAppPaymentDetail.originalTtlAmount = it.ttlAmount.toDecimalFormat()
                    sharedViewModel.objRootAppPaymentDetail.originalTxnAmount = it.txnAmount.toDecimalFormat()
                    sharedViewModel.objRootAppPaymentDetail.originalHostTxnRef = it.hostTxnRef
                }
            }
        }
    }


}
