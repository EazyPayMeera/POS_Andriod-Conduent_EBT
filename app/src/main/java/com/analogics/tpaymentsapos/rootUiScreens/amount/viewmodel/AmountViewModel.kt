package com.analogics.tpaymentsapos.rootUiScreens.amount.viewmodel

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.analogics.paymentservicecore.listeners.responseListener.IApiServiceResponseListener
import com.analogics.paymentservicecore.model.PaymentServiceTxnDetails
import com.analogics.paymentservicecore.model.error.ApiServiceError
import com.analogics.paymentservicecore.models.TxnStatus
import com.analogics.paymentservicecore.models.TxnType
import com.analogics.paymentservicecore.repository.apiService.ApiServiceRepository
import com.analogics.paymentservicecore.utils.PaymentServiceUtils
import com.analogics.securityframework.database.dbRepository.TxnDBRepository
import com.analogics.securityframework.database.entity.TxnEntity
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import com.analogics.tpaymentsapos.rootModel.ObjRootAppPaymentDetails
import com.analogics.tpaymentsapos.rootUiScreens.activity.SharedViewModel
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.emvStatusToTransStatus
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.formatAmount
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.getCurrentDateTime
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.navigateAndClean
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.transformToAmountDouble
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AmountViewModel @Inject constructor(private  var apiServiceRepository: ApiServiceRepository,private val dbRepository: TxnDBRepository) : ViewModel() {

    var transAmount by mutableStateOf("")
        private set

    private val _totalAmount = MutableStateFlow<String?>(null)
    val totalAmount: StateFlow<String?> = _totalAmount

    private val _timeDate = MutableStateFlow<String?>(null)
    val timeDate: StateFlow<String?> = _timeDate

    @RequiresApi(Build.VERSION_CODES.O)
    fun onLoad(sharedViewModel: SharedViewModel)
    {
        transAmount.ifEmpty { transAmount = formatAmount(sharedViewModel.objRootAppPaymentDetail.txnAmount?:0.00) }
        getTotalAmountByInvoiceNo(sharedViewModel.objRootAppPaymentDetail.invoiceNo.toString())
        getTimeDateByInvoiceNo(sharedViewModel.objRootAppPaymentDetail.invoiceNo.toString())
    }

    fun onAmountChange(newValue: String) :String{
        transAmount = formatAmount(newValue)
        return transformToAmountDouble(newValue).toString()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun onConfirm(navHostController: NavHostController, sharedViewModel: SharedViewModel) {
        calculateTotal(sharedViewModel)
        when(sharedViewModel.objRootAppPaymentDetail.txnType) {
            TxnType.REFUND,TxnType.PREAUTH -> {
                navHostController.navigate(AppNavigationItems.CardScreen.route)
            }
            TxnType.VOID -> {
                Log.d("Database","Go to update when void")
                updateTransResult(sharedViewModel.objRootAppPaymentDetail)
                //navHostController.navigate(AppNavigationItems.PleaseWaitScreen.route)
                authenticateTransaction(sharedViewModel,navHostController)
            }
            else -> {
                navHostController.navigate(AppNavigationItems.ConfirmationScreen.route)
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
            TxnType.REFUND -> {
                sharedViewModel.objRootAppPaymentDetail.ttlAmount = transformToAmountDouble(totalAmount.value.toString())
                sharedViewModel.objRootAppPaymentDetail.txnAmount = transformToAmountDouble(_totalAmount.value.toString())
            }
            TxnType.VOID -> {
                sharedViewModel.objRootAppPaymentDetail.dateTime = getCurrentDateTime()
                sharedViewModel.objRootAppPaymentDetail.ttlAmount = transformToAmountDouble(totalAmount.value.toString())
                sharedViewModel.objRootAppPaymentDetail.txnAmount = transformToAmountDouble(_totalAmount.value.toString())
            }
            else -> {
                // Handle non-REFUND and non-PREAUTH cases
                sharedViewModel.objRootAppPaymentDetail.txnAmount = transformToAmountDouble(transAmount)

                if (sharedViewModel.objPosConfig?.isTaxEnabled == true) {
                    sharedViewModel.objRootAppPaymentDetail.CGST = calculateTax(
                        sharedViewModel.objRootAppPaymentDetail.txnAmount ?: 0.00,
                        sharedViewModel.objPosConfig?.CGSTPercent ?: 0.00
                    )
                    sharedViewModel.objRootAppPaymentDetail.SGST = calculateTax(
                        sharedViewModel.objRootAppPaymentDetail.txnAmount ?: 0.00,
                        sharedViewModel.objPosConfig?.SGSTPercent ?: 0.00
                    )
                }

                sharedViewModel.objRootAppPaymentDetail.ttlAmount =
                    (sharedViewModel.objRootAppPaymentDetail.txnAmount ?: 0.00) +
                            (sharedViewModel.objRootAppPaymentDetail.CGST ?: 0.00) +
                            (sharedViewModel.objRootAppPaymentDetail.SGST ?: 0.00)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getTotalAmountByInvoiceNo(invoiceNo: String) {
        viewModelScope.launch {
            val totalAmountString = dbRepository.fetchTotalAmountByInvoiceNo(invoiceNo)
            val totalAmount = totalAmountString?.toDoubleOrNull() ?: 0.0
            val formattedAmount = "%.2f".format(totalAmount)
            Log.d("TotalAmountDebug", "Total Amount: $formattedAmount")
            _totalAmount.value = formattedAmount
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getTimeDateByInvoiceNo(invoiceNo: String) {
        viewModelScope.launch {
            val totalDateTimeString = dbRepository.fetchTimeDateByInvoiceNo(invoiceNo)
            _timeDate.value = totalDateTimeString
            Log.d("TimeDateDebug", "Fetched TimeDate: ${_timeDate.value}")
        }
    }

    fun generateInvoiceNumber(): String {
        val randomNumber = (10000000..99999999).random()
        return "INVC$randomNumber"
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
                        // Handle successful response
                        Log.d("ApiServiceSuccess", "Response: $response")
                        // Transform the response if necessary and update your ViewModel state
                        updateTransResult(sharedViewModel = sharedViewModel,
                            emvStatusToTransStatus(response.txnStatus)
                        )
                        navHostController.navigate(AppNavigationItems.ApprovedScreen.route)
                    }

                    override fun onApiServiceError(error: ApiServiceError) {
                        // Handle error response
                        // This could be used to display an error message to the user
                        Log.e("AuthenticationError", error.errorMessage)
                        navHostController.navigate(AppNavigationItems.DeclineScreen.route)
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

}