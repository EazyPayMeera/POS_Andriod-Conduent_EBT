package com.analogics.tpaymentsapos.rootUtils.genericComposeUI

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
            ScannerHandler.initScanner(context, this) // Pass this as the listener
            Log.d(TAG, "Scanner initialized successfully in Payment Service Repository...")
            iScannerResultProviderListener.onSuccess("SUCCESS")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize scanner: ${e.message}")
            iScannerResultProviderListener.onFailure(e)
        }
    }

    override suspend fun startScanner(
        context: Context,
        data: Bundle,
        iScannerResultProviderListener: IScannerResultProviderListener
    ) {
        try {
            // Start the scanner
            this.iScannerResultProviderListener = iScannerResultProviderListener
            ScannerHandler.startScan(data, /* cameraId */ 1, /* timeout */ 10000, this) // Example call
        } catch (e: Exception) {
            Log.e(TAG, "Error starting scanner: ${e.message}")
            //onError(1, "Failed to start scanner")
        }
    }

    fun stopScan() {
        try {
            ScannerHandler.stopScan() // Call to stop the scan
            Log.d(TAG, "Scan stopped successfully.")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop scan: ${e.message}")
        }
    }

    override fun onScannerRespHandler(uiData: String) {
        Log.d(TAG, "Received printer response: $uiData")
        if (uiData != "FAIL") {
            Log.d(TAG, "Printer response is SUCCESS.")
            iScannerResultProviderListener.onSuccess(uiData)
        } else {
            Log.d(TAG, "Printer response is FAILURE.")
            iScannerResultProviderListener.onSuccess(false)
        }
    }


}
