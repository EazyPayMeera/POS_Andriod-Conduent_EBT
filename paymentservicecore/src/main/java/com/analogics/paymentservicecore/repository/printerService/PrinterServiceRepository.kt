package com.analogics.tpaymentsapos.rootUtils.genericComposeUI

import android.content.Context
import android.os.Bundle
import android.util.Log
import com.analogics.paymentservicecore.listeners.requestListener.PrinterRequestListener
import com.analogics.paymentservicecore.listeners.responseListener.IPrinterResultProviderListener
import com.analogics.tpaymentcore.handler.PrinterHandler
import com.analogics.tpaymentcore.listener.IPrinterHandlerListener
import javax.inject.Inject

class PrinterServiceRepository @Inject constructor() : PrinterRequestListener, IPrinterHandlerListener {

    private val TAG = "PrinterServiceRepo"

    private lateinit var iPrinterResultProviderListener: IPrinterResultProviderListener

    data class Receipt(
        val fields: List<Pair<String, String>>, // List of dynamic fields
        val items: List<ReceiptItem>,
        val barcode: String? = null, // Add Barcode
        val qrcode: String? = null   // Add QR Code
    ) {
        class Builder {
            private val fields: MutableList<Pair<String, String>> = mutableListOf()
            private val items: MutableList<ReceiptItem> = mutableListOf()
            private var barcode: String? = null
            private var qrcode: String? = null

            fun addField(label: String, value: String) = apply {
                fields.add(label to value)
            }

            fun addItem(item: ReceiptItem) = apply {
                items.add(item)
            }

            fun setBarcode(barcode: String?) = apply {
                this.barcode = barcode
            }

            fun setQRCode(qrcode: String?) = apply {
                this.qrcode = qrcode
            }

            fun build(): Receipt {
                return Receipt(fields, items, barcode, qrcode)
            }
        }
    }

    data class ReceiptItem(
        val name: String,
        val price: Double
    )


    val receipt = Receipt.Builder()
        .addField("STORE NAME:", "Awesome Store")
        .addField("STORE ADDRESS:", "1234 Market St, San Francisco, CA")
        .addField("STORE PHONE:", "(123) 456-7890")
        .addField("ORDER DETAILS", "Order Details")
        .addField("ORDER DATE/TIME:", "2024-08-29 12:34")
        .addField("ORDER #:", "12345")
        .addField("POS TERMINAL #:", "67890")
        .addField("PURCHASED ITEMS", "")
        .addItem(PrinterServiceRepository.ReceiptItem("Item 1", 19.99))
        .addItem(PrinterServiceRepository.ReceiptItem("Item 2", 9.99))
        .addField("SUBTOTAL:", "$29.98")
        .addField("SALES TAX:", "$2.40")
        .addField("TOTAL AMOUNT:", "$32.38")
        .addField("PAYMENT:", "Credit Card")
        .addField("CARD:", "**** **** **** 1234")
        .addField("AUTHORIZATION:", "6789")
        .addField("THANK YOU FOR YOUR PURCHASE!\nWE APPRECIATE YOUR BUSINESS!", "")
        .addField("CUSTOMER SUPPORT", "")
        .addField("SUPPORT PHONE:", "(987) 654-3210")
        .addField("SUPPORT EMAIL:", "support@example.com")
        .addField("BARCODE", "123456789012")
        .addField("QR CODE", "https://example.com/qrcode")
        .build()

    override fun printReceiptDetails(format: Bundle, iPrinterResultProviderListener: IPrinterResultProviderListener) {
        val barcodeString = receipt.fields.find { it.first == "BARCODE" }?.second ?: ""

        val receiptDetails = receipt.fields.map { (label, value) ->
            "$label: $value"
        } + receipt.items.mapIndexed { index, item ->
            "${index + 1}. ${item.name}              $${item.price}"
        }

        // Append additional details if they exist
        val additionalDetails = mutableListOf<String>()
        receipt.qrcode?.let { qrcode ->
            additionalDetails.add("BARCODE $qrcode")
        }

        val allDetails = receiptDetails + additionalDetails

        this.iPrinterResultProviderListener = iPrinterResultProviderListener
        try {
            PrinterHandler.addReceiptDetails(format, barcodeString, receiptDetails, this) // Pass this as the listener
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
