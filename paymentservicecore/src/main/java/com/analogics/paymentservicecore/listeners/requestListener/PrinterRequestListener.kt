package com.analogics.paymentservicecore.listeners.requestListener

import android.content.Context
import android.os.Bundle
import com.analogics.paymentservicecore.listeners.responseListener.IPrinterResultProviderListener

interface PrinterRequestListener {
    suspend fun initPrinter(context: Context, iPrinterResultProviderListener: IPrinterResultProviderListener)
    suspend fun printReceiptDetails(format: Bundle,
                                    barcodeString: String,
                                    receiptDetails: List<String>,
                                    descriptionList: List<String>,
                                    alignment: List<Int>, // Assuming alignment is of type Int; adjust as necessary
                                    iPrinterResultProviderListener: IPrinterResultProviderListener)

    suspend fun printLeftCenterRightDetails(
        Transaction: List<String>,
        Count: List<String>,
        Total: List<String>, // Assuming alignment is of type Int; adjust as necessary
        iPrinterResultProviderListener: IPrinterResultProviderListener
    )
    suspend fun printImage(format: Bundle,imageData: ByteArray, iPrinterResultProviderListener: IPrinterResultProviderListener)
    suspend fun printLeftRightDetails(label: List<String>, description: List<String>, iPrinterResultProviderListener: IPrinterResultProviderListener)
    suspend fun getStatus(iPrinterResultProviderListener: IPrinterResultProviderListener)
}