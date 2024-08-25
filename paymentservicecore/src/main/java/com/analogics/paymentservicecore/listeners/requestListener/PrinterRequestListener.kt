package com.analogics.paymentservicecore.listeners.requestListener

import android.content.Context
import com.analogics.paymentservicecore.listeners.responseListener.IPrinterResultProviderListener

interface PrinterRequestListener {
    suspend fun initPrinter(context: Context, iPrinterResultProviderListener: IPrinterResultProviderListener)
}