package com.analogics.tpaymentcore.handler

import android.content.Context
import android.os.Bundle
import android.util.Log
import com.analogics.tpaymentcore.listener.IScannerHandlerListener
import com.analogics.tpaymentcore.scanner.Scanner

object ScannerHandler {

    private const val TAG = "ScannerHandler"

    fun initScanner(
        context: Context,
        scannerHandlerListener: IScannerHandlerListener
    ) {
        try {
            // Initialize the scanner
            Scanner.getInstance().initScanner(context)
            Log.d(TAG, "Scanner initialized successfully.")

            // Notify success
            scannerHandlerListener.onScannerRespHandler("SUCCESS")
        } catch (exception: Exception) {
            Log.e(TAG, "Failed to initialize scanner: ${exception.message}")

            // Notify failure
            scannerHandlerListener.onScannerRespHandler("FAILURE")
        }
    }

    fun startScanner(
        context: Context,
        data: Bundle,
        onScanned: (qrCode: String) -> Unit,
        onError: (errorCode: Int, message: String) -> Unit,
        onTimeout: () -> Unit,
        onCancel: () -> Unit,
        scannerHandlerListener: IScannerHandlerListener
    ) {
        try {
            // Start scanning using Scanner class
            Scanner.getInstance().startScanner(
                context = context,
                data = data,
                cameraId = 1,
                timeout = 5000L, // 5 seconds timeout, adjust as needed
                onScanned = onScanned,
                onError = onError,
                onTimeout = onTimeout,
                onCancel = onCancel
            )
            Log.d(TAG, "Scanner started successfully.")

            // Notify success
            scannerHandlerListener.onScannerRespHandler("SUCCESS")
        } catch (exception: Exception) {
            Log.e(TAG, "Failed to start scanner: ${exception.message}")

            // Notify failure
            scannerHandlerListener.onScannerRespHandler("FAILURE")
        }
    }

    fun stopScanner(
        scannerHandlerListener: IScannerHandlerListener
    ) {
        try {
            // Stop scanning using Scanner class
            Scanner.getInstance().stopScanner()
            Log.d(TAG, "Scanner stopped successfully.")

            // Notify success
            scannerHandlerListener.onScannerRespHandler("SUCCESS")
        } catch (exception: Exception) {
            Log.e(TAG, "Failed to stop scanner: ${exception.message}")

            // Notify failure
            scannerHandlerListener.onScannerRespHandler("FAILURE")
        }
    }
}
