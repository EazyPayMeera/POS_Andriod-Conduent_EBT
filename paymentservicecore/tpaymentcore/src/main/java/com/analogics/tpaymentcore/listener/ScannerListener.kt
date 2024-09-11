package com.analogics.tpaymentcore.listener

import android.content.Context

interface ScannerListener {
    fun StartScanner(context: Context, IPrinterHandlerListener: IPrinterHandlerListener)
}