package com.analogics.paymentservicecore.repository.scannerService

import android.util.Log
import com.analogics.paymentservicecore.listeners.requestListener.ScannerRequestListener
import com.analogics.paymentservicecore.listeners.responseListener.IScannerResultProviderListener
import com.analogics.tpaymentcore.listener.IScannerHandlerListener
import javax.inject.Inject

class ScannerServiceRepository @Inject constructor() : ScannerRequestListener, IScannerHandlerListener {

    private val TAG = "ScannerServiceRepo"

    private lateinit var iScannerResultProviderListener: IScannerResultProviderListener

    override fun onScannerRespHandler(uiData: String) {
        // Your logic for handling the scanner response goes here
        Log.d(TAG, "Received printer response: $uiData")
        if (uiData == "SUCCESS") {
            Log.d(TAG, "Printer response is SUCCESS.")
            iScannerResultProviderListener.onSuccess(true)
        } else {
            Log.d(TAG, "Printer response is FAILURE.")
            iScannerResultProviderListener.onSuccess(false)
        }
    }
}