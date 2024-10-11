package com.analogics.tpaymentcore.listener.requestListener

import android.content.Context
import com.analogics.tpaymentcore.listener.responseListener.IPrinterHandlerListener

interface PrinterListener {
    fun initPrinter(context: Context, IPrinterHandlerListener: IPrinterHandlerListener)
}