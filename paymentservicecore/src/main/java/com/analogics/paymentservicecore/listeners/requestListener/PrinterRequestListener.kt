package com.analogics.paymentservicecore.listeners.requestListener

import android.content.Context
import android.os.Bundle
import com.analogics.paymentservicecore.listeners.responseListener.IPrinterResultProviderListener

interface PrinterRequestListener {
    suspend fun initPrinter(context: Context, iPrinterResultProviderListener: IPrinterResultProviderListener)
    suspend fun printReceiptDetails(format: Bundle,
                            barcodeString: String,
                            receiptDetails: List<String>,
                            alignment: List<Int>, // Assuming alignment is of type Int; adjust as necessary
                            iPrinterResultProviderListener: IPrinterResultProviderListener)
}