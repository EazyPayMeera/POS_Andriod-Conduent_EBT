package com.analogics.paymentservicecore.listeners.requestListener

import android.content.Context
import com.analogics.paymentservicecore.listeners.responseListener.IScannerResultProviderListener
import com.google.mlkit.vision.common.InputImage

interface ScannerRequestListener {
    suspend fun initScanner(
        context: Context,
        iScannerResultProviderListener: IScannerResultProviderListener
    )

    // Starts the scanner with an InputImage and callbacks
    suspend fun startScanner(
        image: InputImage,
        onScanned: (qrCode: String) -> Unit,
        onError: (errorCode: Int, message: String) -> Unit
    )
}