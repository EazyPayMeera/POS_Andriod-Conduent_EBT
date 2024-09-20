package com.analogics.tpaymentsapos.rootUiScreens.login

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.analogics.paymentservicecore.listeners.responseListener.IScannerResultProviderListener
import com.analogics.paymentservicecore.models.TxnInfo
import com.analogics.paymentservicecore.models.TxnType
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.ScannerServiceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class InvoiceViewModel : ViewModel() {
    private val TAG = "InvoiceViewModel"
    private val _invoiceno = MutableStateFlow("")
    val invoiceno: StateFlow<String> = _invoiceno

    private val scannerServiceRepository = ScannerServiceRepository() // Instantiate here

    fun updateInvoiceNo(newValue: String) {
        _invoiceno.value = newValue
    }

    fun navigateToAmountScreen(navHostController: NavHostController) {
        viewModelScope.launch {
            if (TxnInfo.txnType == TxnType.AUTHCAP) {
                navHostController.navigate(AppNavigationItems.InfoConfirmScreen.route)
            } else {
                navHostController.navigate(AppNavigationItems.AmountScreen.route)
            }
        }
    }

    fun navigateToTrainingScreen(navHostController: NavHostController) {
        viewModelScope.launch {
            navHostController.navigate(AppNavigationItems.TrainingScreen.route)
        }
    }

    suspend fun initScanner(context: Context, iScannerResultProviderListener: IScannerResultProviderListener) {
        Log.d(TAG, "Initializing scanner in viewModel...")
        scannerServiceRepository.initScanner(context, iScannerResultProviderListener)
    }

    suspend fun startScanner(
        context: Context,
        data: Bundle,
        onSuccess: (String) -> Unit,
        onError: (Int, String) -> Unit,
        onTimeout: () -> Unit,
        onCancel: () -> Unit
    ) {
        Log.d(TAG, "Starting scanner in viewModel...")
        try {
            scannerServiceRepository.startScanner(
                context,
                data,
                onSuccess,
                onError,
                onTimeout,
                onCancel
            )
            Log.d(TAG, "Scanner started successfully in viewModel")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start scanner in viewModel: ${e.message}")
        }
    }
}
