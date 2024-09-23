package com.analogics.paymentservicecore.listeners.requestListener

import android.content.Context
import android.os.Bundle
import com.analogics.paymentservicecore.listeners.responseListener.IScannerResultProviderListener

interface ScannerRequestListener {
    suspend fun initScanner(
        context: Context,
        iScannerResultProviderListener: IScannerResultProviderListener
    )

    // Starts the scanner with an InputImage and callbacks
    suspend fun startScanner(
        context: Context,
        data: Bundle,
        iScannerResultProviderListener: IScannerResultProviderListener
    )
}