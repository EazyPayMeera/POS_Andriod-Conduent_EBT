package com.analogics.tpaymentcore.handler

import android.content.Context
import android.os.Bundle
import android.util.Log
import com.analogics.tpaymentcore.Scanner.Scanner
import com.analogics.tpaymentcore.listener.IScannerHandlerListener
import com.analogics.tpaymentcore.listener.ScannerListener

object ScannerHandler : ScannerListener {

    private const val TAG = "ScannerHandler"

    override fun initScanner(
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

    override fun startScan(data: Bundle, cameraId: Int, timeout: Long, scannerHandlerListener: IScannerHandlerListener) {
        try {
            // Start the scanning process
            Log.d(TAG, "Starting scan with camera ID: $cameraId and timeout: $timeout")

            Scanner.getInstance().startScan(data,cameraId,timeout,scannerHandlerListener)
        } catch (exception: Exception) {
            Log.e(TAG, "Failed to start scan: ${exception.message}")

            // Notify failure
            scannerHandlerListener.onScannerRespHandler("Scan failed")
        }
    }

    override fun stopScan() {
        Scanner.getInstance().stopScan()
        Log.d(TAG, "Stopping scan.")
        // Notify about cancellation if necessary
    }
}
