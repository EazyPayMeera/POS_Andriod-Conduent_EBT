package com.analogics.tpaymentcore.repository

import android.content.Context
import android.os.Bundle
import android.util.Log
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
            Log.d(TAG, "Printer initialized successfully.")

            // Notify success
            //printerHandlerListener.onPrinterRespHandler("SUCCESS")
        } catch (exception: Exception) {
            Log.e(TAG, "Failed to initialize printer: ${exception.message}")

            // Notify failure
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
        alignment: List<Int>,
        listener: IPrinterHandlerListener
    ) {
        try {

            Printer.getInstance().printMultipleTextsAndStartPrinting(barcodeFormat,barcode,receipt,alignment)
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

    fun addLeftCenterRightDetails(
        Transaction: List<String>,
        Count: List<String>,
        Total: List<String>,
        listener: IPrinterHandlerListener
    ) {
        try {

            Printer.getInstance().printLeftCenterRightPrinting(Transaction,Count,Total)
            Printer.getInstance().feedLine(3)
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
