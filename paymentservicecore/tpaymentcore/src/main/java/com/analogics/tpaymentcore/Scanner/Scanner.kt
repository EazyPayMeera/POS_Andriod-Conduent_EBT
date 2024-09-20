package com.analogics.tpaymentcore.Scanner

import android.content.Context
import android.os.Bundle
import android.util.Log
import com.analogics.tpaymentcore.listener.IScannerHandlerListener
import com.urovo.file.logfile
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

    private var innerScanner: InnerScannerImpl? = null

    // Initialize the scanner
    fun initScanner(context: Context) {
        logfile.printLog("===initScanner in Scanner kt")
        if (innerScanner == null) {
            innerScanner = InnerScannerImpl.getInstance(context)
        }
    }

    // Start scanning
    fun startScan(data: Bundle, cameraId: Int, timeout: Long, scannerHandlerListener: IScannerHandlerListener) {
        try {
            innerScanner?.startScan(innerScanner?.mContext ?: return, data, cameraId, timeout, object : ScannerListener {
                override fun onSuccess(result: String) {
                    scannerHandlerListener.onScannerRespHandler(result)
                }

                override fun onError(code: Int, message: String) {
                    scannerHandlerListener.onScannerRespHandler(message)
                }

                override fun onCancel() {
                    scannerHandlerListener.onScannerRespHandler("1")
                }

                override fun onTimeout() {
                    scannerHandlerListener.onScannerRespHandler("1")
                }
            })
        } catch (e: Exception) {
            Log.e("Scanner", "Error starting scan: ${e.message}")
        }
    }

    // Stop scanning
    fun stopScan() {
        try {
            innerScanner?.stopScan()
        } catch (e: Exception) {
            Log.e("Scanner", "Error stopping scan: ${e.message}")
        }
    }

}
