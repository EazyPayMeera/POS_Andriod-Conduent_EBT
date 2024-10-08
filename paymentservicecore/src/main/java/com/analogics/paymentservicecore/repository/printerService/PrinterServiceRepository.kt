package com.analogics.tpaymentsapos.rootUtils.genericComposeUI

import android.content.Context
import android.os.Bundle
import android.util.Log
import com.analogics.builder_core.model.PaymentServiceTxnDetails
import com.analogics.paymentservicecore.listeners.requestListener.PrinterRequestListener
import com.analogics.paymentservicecore.listeners.responseListener.IPrinterResultProviderListener
import com.analogics.paymentservicecore.repository.utility.ReceiptBuilder
import com.analogics.tpaymentcore.handler.PrinterHandler
import com.analogics.tpaymentcore.listener.IPrinterHandlerListener
import javax.inject.Inject

class PrinterServiceRepository @Inject constructor(
    receiptBuilder: ReceiptBuilder,
    paymentServiceTxnDetails: PaymentServiceTxnDetails?,// Inject the ReceiptBuilder
) : PrinterRequestListener, IPrinterHandlerListener {

    private val TAG = "PrinterServiceRepo"
/*    val pdetails = paymentServiceTxnDetails()
    Log.d("Object Details","Transaction Details ${pdetails}")*/
    private lateinit var iPrinterResultProviderListener: IPrinterResultProviderListener

    private val receipt: ReceiptBuilder.Receipt = receiptBuilder.createReceipt(paymentServiceTxnDetails) // Use the correct reference

    override fun printReceiptDetails(format: Bundle, iPrinterResultProviderListener: IPrinterResultProviderListener) {
        val barcodeString = receipt.fields.find { it.first == "BARCODE" }?.second ?: ""

        val receiptDetails = receipt.fields.map { (label, value) ->
            "$label: $value"
        } + receipt.items.mapIndexed { index, item ->
            "${index + 1}. ${item.name}              $${item.price}"
        }

        // Map alignment values to integers
        val alignment: List<Int> = receipt.fields.map { field ->
            when (field.third) {
                ReceiptBuilder.Alignment.LEFT -> 0
                ReceiptBuilder.Alignment.CENTER -> 1
                ReceiptBuilder.Alignment.RIGHT -> 2
            }
        }

// Log or process the integer alignment values
        alignment.forEach { alignmentInt ->
            Log.d(TAG, "Alignment as Int: $alignmentInt")  // Log each integer alignment value
        }

        // Append additional details if they exist
        val additionalDetails = mutableListOf<String>()
        receipt.qrcode?.let { qrcode ->
            additionalDetails.add("BARCODE $qrcode")
        }

        val allDetails = receiptDetails + additionalDetails

        this.iPrinterResultProviderListener = iPrinterResultProviderListener
        try {
            PrinterHandler.addReceiptDetails(format, barcodeString, receiptDetails,alignment, this) // Pass this as the listener
            Log.d(TAG, "Receipt printed successfully.")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to print receipt: ${e.message}")
            iPrinterResultProviderListener.onSuccess(false)
        }
    }

    override suspend fun initPrinter(
        context: Context,
        iPrinterResultProviderListener: IPrinterResultProviderListener
    ) {
        Log.d(TAG, "Initializing printer in Payment Service Repository...")
        this.iPrinterResultProviderListener = iPrinterResultProviderListener
        try {
            PrinterHandler.initPrinter(context, this) // Pass this as the listener
            Log.d(TAG, "Printer initialized successfully in Payment Service Repository...")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize printer: ${e.message}")
            iPrinterResultProviderListener.onSuccess(false)
        }
    }

    override fun onPrinterRespHandler(uiData: String) {
        Log.d(TAG, "Received printer response: $uiData")
        if (uiData == "SUCCESS") {
            Log.d(TAG, "Printer response is SUCCESS.")
            iPrinterResultProviderListener.onSuccess(true)
        } else {
            Log.d(TAG, "Printer response is FAILURE.")
            iPrinterResultProviderListener.onSuccess(false)
        }
    }
}
