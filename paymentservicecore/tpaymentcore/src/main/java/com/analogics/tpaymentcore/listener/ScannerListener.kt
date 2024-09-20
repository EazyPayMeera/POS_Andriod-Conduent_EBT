package com.analogics.tpaymentcore.listener

import android.content.Context
import android.os.Bundle

interface ScannerListener {
    fun initScanner(context: Context, scannerHandlerListener: IScannerHandlerListener)
    fun startScan(data: Bundle, cameraId: Int, timeout: Long, listener: IScannerHandlerListener)
    fun stopScan()
}
