package com.analogics.tpaymentsapos.rootUiScreens.login

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.analogics.paymentservicecore.constants.AppConstants
import com.analogics.paymentservicecore.listeners.responseListener.IScannerResultProviderListener
import com.analogics.paymentservicecore.models.Acquirer
import com.analogics.paymentservicecore.models.TxnType
import com.analogics.paymentservicecore.utils.PaymentServiceUtils
import com.analogics.securityframework.database.dbRepository.TxnDBRepository
import com.analogics.tpaymentsapos.R
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import com.analogics.tpaymentsapos.rootModel.ObjRootAppPaymentDetails
import com.analogics.tpaymentsapos.rootUiScreens.activity.SharedViewModel
import com.analogics.tpaymentsapos.rootUiScreens.dialogs.CustomDialogBuilder
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.ScannerServiceRepository
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.getAcquirer
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.navigateAndClean
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.toDecimalFormat
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InvoiceViewModel @Inject constructor(private val dbRepository: TxnDBRepository) : ViewModel() {
    private val TAG = "InvoiceViewModel"
    private val _invoiceno = MutableStateFlow("")
    val invoiceno: StateFlow<String> = _invoiceno
    private val _isInvoiceFound = MutableStateFlow(false)
    val isInvoiceFound: StateFlow<Boolean> get() = _isInvoiceFound

    private val scannerServiceRepository = ScannerServiceRepository() // Instantiate here

    fun updateInvoiceNo(newValue: String, sharedViewModel: SharedViewModel?=null): String {
        sharedViewModel?.takeIf {getAcquirer(it.objRootAppPaymentDetail) == Acquirer.LYRA}?.let {
            _invoiceno.value = newValue.take(AppConstants.LYRA_MAX_INVOICE_LENGTH)
        }?:
        {_invoiceno.value = newValue}
        return _invoiceno.value // Return the updated invoice number
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun onConfirm(navHostController: NavHostController, sharedViewModel: SharedViewModel) {
        sharedViewModel.objRootAppPaymentDetail.invoiceNo = invoiceno.value
        viewModelScope.launch {
            when (sharedViewModel.objRootAppPaymentDetail.txnType) {
                TxnType.AUTHCAP -> {
                    if (getTransactionByInvoiceNo(navHostController, sharedViewModel)) {
                        navHostController.navigate(AppNavigationItems.InfoConfirmScreen.route)
                    }
                }
                TxnType.REFUND, TxnType.VOID -> {
                    if (getTransactionByInvoiceNo(navHostController, sharedViewModel)) {
                        navHostController.navigate(AppNavigationItems.AmountScreen.route)
                    }
                }
                else -> {
                    navHostController.navigate(AppNavigationItems.AmountScreen.route)
                }
            }
        }
    }


    fun navigateToTrainingScreen(navHostController: NavHostController) {
        viewModelScope.launch {
            navHostController.navigateAndClean(AppNavigationItems.DashBoardScreen.route)
        }
    }

    suspend fun initScanner(context: Context, iScannerResultProviderListener: IScannerResultProviderListener) {
        Log.d(TAG, "Initializing scanner in viewModel...")
        scannerServiceRepository.initScanner(context, iScannerResultProviderListener)
    }

    suspend fun startScanner(
        context: Context,
        data: Bundle,
        iScannerResultProviderListener: IScannerResultProviderListener

    ) {

        try {
            scannerServiceRepository.startScanner(
                context,
                data,
                iScannerResultProviderListener
            )

            Log.d(TAG, "Scanner started successfully in viewModel")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start scanner in viewModel: ${e.message}")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)   // To check for Refund Transaction is Found or not
    fun isRRLFound(invoiceNo: String) {
        viewModelScope.launch {
            // Check if the invoice exists
            val isFound = dbRepository.isRRLFound(invoiceNo)
            _isInvoiceFound.value = isFound
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getTransactionByInvoiceNo(navHost: NavHostController?, sharedViewModel: SharedViewModel): Boolean {
        var isTransactionFound = true
        viewModelScope.launch {
            try {
                Log.d("getTransactionByInvoiceNo", "Fetching transaction for invoiceNo: ${sharedViewModel.objRootAppPaymentDetail.invoiceNo}")

                dbRepository.fetchTransactionByInvoiceNo(sharedViewModel.objRootAppPaymentDetail.invoiceNo.toString())?.let { transactions ->
                    if (transactions.isEmpty()) {
                        CustomDialogBuilder.composeAlertDialog(
                            title = navHost?.context?.resources?.getString(R.string.invalid_invoice),
                            subtitle = navHost?.context?.resources?.getString(R.string.invoice_not_found)
                        )
                        isTransactionFound = false
                        return@launch
                    }

                    PaymentServiceUtils.transformObject<ObjRootAppPaymentDetails>(transactions[0])?.let { transformedObject ->
                        sharedViewModel.objRootAppPaymentDetail = transformedObject.copy(
                            id = sharedViewModel.objRootAppPaymentDetail.id,
                            txnType = sharedViewModel.objRootAppPaymentDetail.txnType,
                            txnStatus = sharedViewModel.objRootAppPaymentDetail.txnStatus,
                            hostAuthResult = sharedViewModel.objRootAppPaymentDetail.hostAuthResult
                        )

                        sharedViewModel.objRootAppPaymentDetail.originalTxnType = transformedObject.txnType
                        sharedViewModel.objRootAppPaymentDetail.originalTip = transformedObject.tip?.toDecimalFormat() ?: "0.00"
                        sharedViewModel.objRootAppPaymentDetail.originalCGST = transformedObject.CGST?.toDecimalFormat() ?: "0.00"
                        sharedViewModel.objRootAppPaymentDetail.originalSGST = transformedObject.SGST?.toDecimalFormat() ?: "0.00"
                        sharedViewModel.objRootAppPaymentDetail.originalCashback = transformedObject.cashback?.toDecimalFormat() ?: "0.00"
                        sharedViewModel.objRootAppPaymentDetail.originalTtlAmount = transformedObject.ttlAmount?.toDecimalFormat() ?: "0.00"
                        sharedViewModel.objRootAppPaymentDetail.originalTxnAmount = transformedObject.txnAmount?.toDecimalFormat() ?: "0.00"
                        sharedViewModel.objRootAppPaymentDetail.originalHostTxnRef = transformedObject.hostTxnRef
                        isTransactionFound = true
                    } ?: Log.e("getTransactionByInvoiceNo", "Transformation failed for transaction: ${transactions[0]}")
                } ?: Log.e("getTransactionByInvoiceNo", "fetchTransactionByInvoiceNo returned null")
            } catch (e: Exception) {
                Log.e("getTransactionByInvoiceNo", "Error occurred: ${e.message}", e)
                isTransactionFound = false
            }
        }
        return isTransactionFound
    }


}
