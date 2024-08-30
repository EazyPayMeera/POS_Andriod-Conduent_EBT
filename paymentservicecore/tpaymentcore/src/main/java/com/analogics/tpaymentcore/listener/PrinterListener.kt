package com.analogics.tpaymentcore.listener

import android.content.Context

interface PrinterListener {
    fun initPrinter(context: Context, IPrinterHandlerListener: IPrinterHandlerListener)

}