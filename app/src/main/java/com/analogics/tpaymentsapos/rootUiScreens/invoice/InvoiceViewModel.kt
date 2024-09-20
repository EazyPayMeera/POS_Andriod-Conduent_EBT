package com.analogics.tpaymentsapos.rootUiScreens.login

import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.analogics.paymentservicecore.listeners.responseListener.IScannerResultProviderListener
import com.analogics.paymentservicecore.models.TxnInfo
import com.analogics.paymentservicecore.models.TxnType
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class InvoiceViewModel : ViewModel() {
    private val _invoiceno = MutableStateFlow("")
    val invoiceno: StateFlow<String> = _invoiceno

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

    suspend fun initScanner(context: Context, iScannerResultProviderListener: IScannerResultProviderListener)
    {
        Log.d(TAG, "Initializing printer in viewModel...")
    }

    /*suspend fun startScanner(
        data: Bundle,
        onScanned: (String) -> Unit,
        onError: (Int, String) -> Unit,
        onTimeout: () -> Unit,
        onCancel: () -> Unit,
        scannerHandlerListener: IScannerHandlerListener
    ) {
        Log.d(TAG, "Start scanner in viewModel...")
        try {
            // Assuming ScannerServiceRepository is already instantiated
            ScannerServiceRepository().startScanner(
                data = data,
                onScanned = onScanned,
                onError = onError,
                onTimeout = onTimeout,
                onCancel = onCancel,
                scannerHandlerListener = scannerHandlerListener
            )
            Log.d(TAG, "Scanner started successfully in viewModel")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start scanner in viewModel: ${e.message}")
        }
    }*/


}
