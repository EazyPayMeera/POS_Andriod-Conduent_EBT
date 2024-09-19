package com.analogics.tpaymentsapos.rootUiScreens.login

import android.content.ContentValues.TAG
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.analogics.paymentservicecore.listeners.responseListener.IScannerResultProviderListener
import com.analogics.paymentservicecore.models.TxnInfo
import com.analogics.paymentservicecore.models.TxnType
import com.analogics.paymentservicecore.repository.scannerService.ScannerServiceRepository
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class InvoiceViewModel : ViewModel() {
    private val _invoiceno = MutableStateFlow("")
    val invoiceno: StateFlow<String> = _invoiceno

    private val scannerServiceRepository = ScannerServiceRepository() // Initialize repository

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

    suspend fun initScanner(
        context: Context,
        iScannerResultProviderListener: IScannerResultProviderListener
    ) {
        Log.d(TAG, "Initializing scanner in ViewModel...")
        scannerServiceRepository.initScanner(context, iScannerResultProviderListener)
    }


    suspend fun startScanner(
        context: Context,
        data: Bundle,
        onScanned: (String) -> Unit,
        onError: (Int, String) -> Unit,
        onTimeout: () -> Unit,
        onCancel: () -> Unit
    ) {
        Log.d(TAG, "Starting scanner with Bundle in ViewModel...")
        try {
            scannerServiceRepository.startScanner(
                context = context,
                data = data,
                onScanned = { qrCode ->
                    Log.d(TAG, "Scanned QR code: $qrCode")
                    onScanned(qrCode)
                },
                onError = { errorCode, message ->
                    Log.e(TAG, "Scanner error: $message")
                    onError(errorCode, message)
                },
                onTimeout = {
                    Log.d(TAG, "Scanner timeout.")
                    onTimeout()
                },
                onCancel = {
                    Log.d(TAG, "Scanner canceled.")
                    onCancel()
                }
            )
            Log.d(TAG, "Scanner started successfully with Bundle in ViewModel")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start scanner with Bundle in ViewModel: ${e.message}")
            onError(-1, "Failed to start scanner: ${e.message}")
        }
    }
}
