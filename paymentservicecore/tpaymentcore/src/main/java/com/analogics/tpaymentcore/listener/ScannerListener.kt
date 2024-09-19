/*
package com.analogics.tpaymentcore.listener

import android.content.Context
import android.os.Bundle

interface ScannerListener {
    fun initScanner(context: Context, scannerHandlerListener: IScannerHandlerListener)
    fun startScanner(
        data: Bundle,
        onScanned: (qrCode: String) -> Unit,
        onError: (errorCode: Int, message: String) -> Unit,
        onTimeout: () -> Unit,
        onCancel: () -> Unit,
        scannerHandlerListener: IScannerHandlerListener
    )
    fun stopScanner(
        context: Context,
        scannerHandlerListener: IScannerHandlerListener
    )
}*/
package com.analogics.tpaymentcore.listener

import android.content.Context
import com.google.mlkit.vision.common.InputImage

interface ScannerListener {
    // Initializes the scanner
    fun initScanner(
        context: Context,
        scannerHandlerListener: IScannerHandlerListener
    )

    // Starts the scanner with an InputImage and callbacks for scanned data or errors
    fun startScanner(
        image: InputImage,
        onScanned: (qrCode: String) -> Unit,
        onError: (errorCode: Int, message: String) -> Unit
    )

    // Stops the scanner
    fun stopScanner(
        context: Context,
        scannerHandlerListener: IScannerHandlerListener
    )
}
