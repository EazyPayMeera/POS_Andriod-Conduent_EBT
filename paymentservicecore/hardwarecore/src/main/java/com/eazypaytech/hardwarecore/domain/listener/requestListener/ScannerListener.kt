package com.eazypaytech.hardwarecore.domain.listener.requestListener

import android.content.Context
import android.os.Bundle
import com.eazypaytech.hardwarecore.domain.listener.responseListener.IScannerHandlerListener

interface ScannerListener {
    fun initScanner(context: Context, scannerHandlerListener: IScannerHandlerListener)
    fun startScan(data: Bundle, cameraId: Int, timeout: Long, listener: IScannerHandlerListener)
    fun stopScan()
}
