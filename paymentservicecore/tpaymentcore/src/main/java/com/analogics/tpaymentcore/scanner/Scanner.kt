package com.analogics.tpaymentcore.scanner

import android.content.Context
import android.device.ScanManager
import android.os.Bundle
import com.urovo.file.logfile
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

    fun startScanning(                // For Start Scanning
        context: Context,
        data: Bundle?,
        cameraId: Int,
        timeout: Long,
        listener: ScannerListener?
    ) {
        // Calling startScan on mScanner object with the passed parameters
        mScanner?.startScan(context, data, cameraId, timeout, listener)
    }




}