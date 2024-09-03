package com.analogics.paymentservicecore.listeners.requestListener

import android.content.Context
import android.os.Bundle
import com.analogics.paymentservicecore.listeners.responseListener.IPrinterResultProviderListener

interface PrinterRequestListener {
    suspend fun initPrinter(context: Context, iPrinterResultProviderListener: IPrinterResultProviderListener)
    fun printReceiptDetails(format: Bundle,iPrinterResultProviderListener: IPrinterResultProviderListener)
}