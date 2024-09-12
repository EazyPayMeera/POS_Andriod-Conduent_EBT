package com.analogics.tpaymentcore.handler

import android.content.Context
import android.os.Bundle
import android.util.Log
import com.analogics.tpaymentcore.Printer.Printer
import com.analogics.tpaymentcore.listener.IPrinterHandlerListener
import com.analogics.tpaymentcore.listener.PrinterListener


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
            Log.d(TAG, "Printer initialized successfully.")

            // Notify success
            printerHandlerListener.onPrinterRespHandler("SUCCESS")
        } catch (exception: Exception) {
            Log.e(TAG, "Failed to initialize printer: ${exception.message}")

            // Notify failure
            printerHandlerListener.onPrinterRespHandler("FAILURE")
        }
    }

    fun addReceiptDetails(
        barcodeFormat: Bundle,
        barcode:String,
        receipt: List<String>,
        listener: IPrinterHandlerListener
    ) {
        try {

            Printer.getInstance().printMultipleTextsAndStartPrinting(barcodeFormat,barcode,receipt)
            Printer.getInstance().feedLine(3)
            //Printer.getInstance().qrCodePrinting(barcodeFormat,"123456")

            Log.d(TAG, "Receipt details added successfully.")

            // Notify success
            listener.onPrinterRespHandler("SUCCESS")
        } catch (exception: Exception) {
            Log.e(TAG, "Failed to add receipt details: ${exception.message}")

            // Notify failure
            listener.onPrinterRespHandler("FAILURE")
        }
    }
}
