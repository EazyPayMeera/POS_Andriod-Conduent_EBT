package com.analogics.tpaymentsapos.rootUiScreens.login

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.analogics.paymentservicecore.listeners.responseListener.IScannerResultProviderListener
import com.analogics.paymentservicecore.models.TxnType
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import com.analogics.tpaymentsapos.rootUiScreens.activity.SharedViewModel
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.ScannerServiceRepository
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.navigateAndClean
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class InvoiceViewModel : ViewModel() {
    private val TAG = "InvoiceViewModel"
    private val _invoiceno = MutableStateFlow("")
    val invoiceno: StateFlow<String> = _invoiceno

    private val scannerServiceRepository = ScannerServiceRepository() // Instantiate here

    fun updateInvoiceNo(newValue: String): String {
        _invoiceno.value = newValue
        return _invoiceno.value // Return the updated invoice number
    }

    fun onConfirm(navHostController: NavHostController, sharedViewModel: SharedViewModel)
    {
        sharedViewModel.objRootAppPaymentDetail.invoiceNo = invoiceno.value
        Log.d("Invoice No", "Invoice No in onConfirm: ${sharedViewModel.objRootAppPaymentDetail.invoiceNo}")
        navigateToAmountScreen(navHostController,sharedViewModel)
    }

    fun navigateToAmountScreen(navHostController: NavHostController,sharedViewModel: SharedViewModel) {
        sharedViewModel.objRootAppPaymentDetail.invoiceNo = invoiceno.value
        Log.d("Invoice No", "Invoice No in Amount: ${sharedViewModel.objRootAppPaymentDetail.invoiceNo}")
        viewModelScope.launch {
            if (sharedViewModel.objRootAppPaymentDetail.txnType == TxnType.AUTHCAP) {
                navHostController.navigate(AppNavigationItems.InfoConfirmScreen.route)
            } else {
                navHostController.navigate(AppNavigationItems.AmountScreen.route)
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
}
