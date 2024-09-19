package com.analogics.paymentservicecore.repository.scannerService

import android.content.Context
import android.os.Bundle
import android.util.Log
import com.analogics.paymentservicecore.listeners.requestListener.ScannerRequestListener
import com.analogics.paymentservicecore.listeners.responseListener.IScannerResultProviderListener
import com.analogics.tpaymentcore.handler.ScannerHandler
import com.analogics.tpaymentcore.listener.IScannerHandlerListener
import javax.inject.Inject

class ScannerServiceRepository @Inject constructor() : ScannerRequestListener, IScannerHandlerListener {

    private val TAG = "ScannerServiceRepo"

    private lateinit var iScannerResultProviderListener: IScannerResultProviderListener

    override suspend fun initScanner(
        context: Context,
        iScannerResultProviderListener: IScannerResultProviderListener
    ) {
        Log.d(TAG, "Initializing scanner in Payment Service Repository...")
        this.iScannerResultProviderListener = iScannerResultProviderListener
        try {
            // Initialize the scanner
            ScannerHandler.initScanner(context, this)
            Log.d(TAG, "Scanner initialized successfully in Payment Service Repository...")
            iScannerResultProviderListener.onSuccess(true)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize scanner: ${e.message}")
            iScannerResultProviderListener.onSuccess(false)
        }
    }

    override suspend fun startScanner(
        context: Context,
        data: Bundle,
        onScanned: (qrCode: String) -> Unit,
        onError: (errorCode: Int, message: String) -> Unit,
        onTimeout: () -> Unit,
        onCancel: () -> Unit
    ) {
        Log.d(TAG, "Starting scanner in Payment Service Repository...")
        try {
            // Start the scanner with the provided parameters
            ScannerHandler.startScanner(
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
                },
                scannerHandlerListener = this
            )
            Log.d(TAG, "Scanner started successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start scanner: ${e.message}")
            onError(-1, "Failed to start scanner: ${e.message}")
        }
    }

    override fun onScannerRespHandler(uiData: String) {
        // Your logic for handling the scanner response goes here
        Log.d(TAG, "Received scanner response: $uiData")
        if (uiData == "SUCCESS") {
            Log.d(TAG, "Scanner response is SUCCESS.")
            iScannerResultProviderListener.onSuccess(true)
        } else {
            Log.d(TAG, "Scanner response is FAILURE.")
            iScannerResultProviderListener.onSuccess(false)
        }
    }
}
