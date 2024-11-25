package com.analogics.tpaymentcore.repository

import android.content.Context
import android.os.Bundle
import com.analogics.tpaymentcore.Printer.Printer
import com.analogics.tpaymentcore.listener.requestListener.PrinterListener
import com.analogics.tpaymentcore.listener.responseListener.IPrinterHandlerListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


object PrinterHandler : PrinterListener {

    private const val TAG = "PrinterHandler"
    data class Receipt(val details: String)

    override fun initPrinter(
        context: Context,
        printerHandlerListener: IPrinterHandlerListener
    ) {
        try {
            // Initialize the printer
            Printer.getInstance().initPrint(context)
            printerHandlerListener.onPrinterRespHandler("SUCCESS")
        } catch (exception: Exception) {
            printerHandlerListener.onPrinterRespHandler("FAILURE")
        }
    }

    suspend fun getStatus(printerHandlerListener: IPrinterHandlerListener): Int? {
        return withContext(Dispatchers.IO) { // Switch to IO context for blocking calls
            try {
                // Get the printer status
                val response = Printer.getInstance().getPrinterStatus()
                response // Return the response here (Int?)
            } catch (e: Exception) {
                printerHandlerListener.onPrinterRespHandler("FAILURE")
                null // Return null or a failure status if an exception occurs
            }
        }
    }


    fun addReceiptDetails(
        barcodeFormat: Bundle,
        barcode:String,
        receipt: List<String>,
        descriptionList: List<String>,
        alignment: List<Int>,
        fontsize: List<Int>,
        listener: IPrinterHandlerListener
    ) {
        try {
            Printer.getInstance().printMultipleTextsAndStartPrinting(barcodeFormat,barcode,receipt,descriptionList,alignment,fontsize)
            Printer.getInstance().feedLine(3)
            listener.onPrinterRespHandler("SUCCESS")
        } catch (exception: Exception) {
            listener.onPrinterRespHandler("FAILURE")
        }
    }

    fun stopPrinting(
        listener: IPrinterHandlerListener
    ) {
        try {
            Printer.getInstance().stopPrinting()
            listener.onPrinterRespHandler("SUCCESS")
        } catch (exception: Exception) {
            listener.onPrinterRespHandler("FAILURE")
        }
    }


    fun addImage(
        format: Bundle,imageData: ByteArray,
        listener: IPrinterHandlerListener
    ) {
        try {
            Printer.getInstance().addImage(format,imageData)
            listener.onPrinterRespHandler("SUCCESS")
        } catch (exception: Exception) {
            listener.onPrinterRespHandler("FAILURE")
        }
    }

    fun addLeftCenterRightDetails(
        Transaction: List<String>,
        Count: List<String>,
        Total: List<String>,
        fontsize: List<Int>,
        listener: IPrinterHandlerListener
    ) {
        try {

            Printer.getInstance().printLeftCenterRightPrinting(Transaction,Count,Total,fontsize)
            Printer.getInstance().feedLine(3)
            listener.onPrinterRespHandler("SUCCESS")
        } catch (exception: Exception) {
            listener.onPrinterRespHandler("FAILURE")
        }
    }

    fun addLeftRightDetails(
        label: List<String>,
        description: List<String>,
        fontsize: List<Int>,
        listener: IPrinterHandlerListener
    ) {
        try {
            Printer.getInstance().addRightLeftDetails(label,description,fontsize)
            listener.onPrinterRespHandler("SUCCESS")
        } catch (exception: Exception) {
            listener.onPrinterRespHandler("FAILURE")
        }
    }
}
