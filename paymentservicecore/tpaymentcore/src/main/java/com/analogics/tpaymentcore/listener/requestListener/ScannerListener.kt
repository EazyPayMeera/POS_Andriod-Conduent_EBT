package com.analogics.tpaymentcore.listener.requestListener

import android.content.Context
import android.os.Bundle
import com.analogics.tpaymentcore.listener.responseListener.IScannerHandlerListener

interface ScannerListener {
    fun initScanner(context: Context, scannerHandlerListener: IScannerHandlerListener)
    fun startScan(data: Bundle, cameraId: Int, timeout: Long, listener: IScannerHandlerListener)
    fun stopScan()
}
