package com.analogics.paymentservicecore.repository.scannerService

import android.content.Context
import android.util.Log
import com.analogics.paymentservicecore.listeners.requestListener.ScannerRequestListener
import com.analogics.paymentservicecore.listeners.responseListener.IScannerResultProviderListener
import com.analogics.tpaymentcore.handler.ScannerHandler
import com.analogics.tpaymentcore.listener.IScannerHandlerListener
import com.google.mlkit.vision.common.InputImage
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
        image: InputImage,
        onScanned: (qrCode: String) -> Unit,
        onError: (errorCode: Int, message: String) -> Unit
    ) {
        Log.d(TAG, "Start scanner in Payment Service Repository...")
        try {
            // Start the scanner with the provided image
            ScannerHandler.startScanner(image, onScanned, onError)
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
