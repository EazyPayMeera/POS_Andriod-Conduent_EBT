package com.analogics.tpaymentcore.scanner

import android.content.Context
import android.device.ScanManager
import android.os.Bundle
import com.urovo.file.logfile
import com.urovo.i9000s.api.emv.Funs.context
import com.urovo.sdk.scanner.InnerScannerImpl
import com.urovo.sdk.scanner.listener.ScannerListener



class Scanner constructor() {

    companion object {
        @Volatile
        private var INSTANCE: Scanner? = null

        fun getInstance(): Scanner =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Scanner().also { INSTANCE = it }
            }
    }


    private var mScanner: InnerScannerImpl? = null
    private var mScannerMan:ScanManager? = null

    // To Initialize the Printer
    fun initScanner(context: Context) {
        // Log initialization attempt
        logfile.printLog("===initScanner in Scanner.kt")

        // Check if mScanner is null, then initialize it using ScannerProviderImpl
        if (this.mScanner == null) {
            // Ensure ScannerProviderImpl.getInstance returns a ScannerManager or compatible type
            this.mScanner = InnerScannerImpl.getInstance(context)
        }

    }

    fun startScanner(
        data: Bundle,
        cameraId: Int = 1,
        timeout: Long = 5000L, // default timeout set to 5 seconds
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
            // Call the startScan method from InnerScannerImpl
            mScanner?.startScan(context, data, cameraId, timeout, listener)
        } catch (e: Exception) {
            e.printStackTrace()
            onError(-1, "Failed to start scanner: ${e.message}")
        }
    }

    // Function to stop the scanner
    fun stopScanner() {
        try {
            // Safely call stopScan if scannerImpl is not null
            mScanner?.stopScan()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}