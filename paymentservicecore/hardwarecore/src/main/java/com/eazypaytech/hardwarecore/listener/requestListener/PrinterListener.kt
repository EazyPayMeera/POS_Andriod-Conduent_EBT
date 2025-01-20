package com.eazypaytech.tpaymentcore.listener.requestListener

import android.content.Context
import com.eazypaytech.tpaymentcore.listener.responseListener.IPrinterHandlerListener

interface PrinterListener {
    fun initPrinter(context: Context, IPrinterHandlerListener: IPrinterHandlerListener)
}