package com.analogics.tpaymentcore.scanner

import android.content.Context
import android.os.Bundle
import com.urovo.sdk.scanner.InnerScannerImpl
import com.urovo.sdk.scanner.listener.ScannerListener

class Scanner private constructor() {

    companion object {
        @Volatile
        private var INSTANCE: Scanner? = null

        fun getInstance(): Scanner =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Scanner().also { INSTANCE = it }
            }
    }

    private var mScanner: InnerScannerImpl? = null

    // Initialize the Scanner with context
    fun initScanner(context: Context) {
        // Log initialization
        com.urovo.file.logfile.printLog("===initScanner in Scanner.kt")

        // If mScanner is null, initialize it with the given context
        if (this.mScanner == null) {
            this.mScanner = InnerScannerImpl.getInstance(context)
        }
    }

    // Start the scanner with the provided context and callback methods
    fun startScanner(
        context: Context,
        data: Bundle,
        cameraId: Int = 1,
        timeout: Long = 10000L, // default timeout set to 10 seconds
        onScanned: (qrCode: String) -> Unit,
        onError: (errorCode: Int, message: String) -> Unit,
        onTimeout: () -> Unit,
        onCancel: () -> Unit
    ) {
        val listener = object : ScannerListener {
            override fun onSuccess(qrCode: String) {
                onScanned(qrCode)
            }

            override fun onError(errorCode: Int, message: String) {
                onError(errorCode, message)
            }

            override fun onTimeout() {
                onTimeout()
            }

            override fun onCancel() {
                onCancel()
            }
        }

        try {
            // Start scanning using InnerScannerImpl with the provided parameters
            mScanner?.startScan(context, data, cameraId, timeout, listener)
        } catch (e: Exception) {
            e.printStackTrace()
            onError(-1, "Failed to start scanner: ${e.message}")
        }
    }

    // Function to stop the scanner safely
    fun stopScanner() {
        try {
            mScanner?.stopScan()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
