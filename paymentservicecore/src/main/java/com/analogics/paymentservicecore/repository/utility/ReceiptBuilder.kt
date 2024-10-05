package com.analogics.tpaymentsapos.rootUtils.genericComposeUI

import com.analogics.builder_core.model.PaymentServiceTxnDetails

class ReceiptBuilder {

    fun createReceipt(paymentDetails: PaymentServiceTxnDetails?): Receipt {
        return Receipt.Builder()
            .addField("STORE NAME:", "Awesome Store")
            .addField("STORE ADDRESS:", "1234 Market St, San Francisco, CA")
            .addField("STORE PHONE:", "(123) 456-7890")
            .addField("ORDER DETAILS", "Order Details")
            .addField("ORDER DATE/TIME:", paymentDetails?.dateTime ?: "N/A")
            .addField("Invoice No:", paymentDetails?.invoiceNo ?: "N/A")
            .addField("POS TERMINAL #:", paymentDetails?.terminalId ?: "N/A")
            .addField("PURCHASED ITEMS", "")
            // Assuming items would be dynamically passed
            .addItem(ReceiptItem("Item 1", 19.99)) // Example item, replace with actual data
            .addItem(ReceiptItem("Item 2", 9.99)) // Example item, replace with actual data
            .addField("SUBTOTAL:", paymentDetails?.txnAmount.toString())
            .addField("SALES TAX:", paymentDetails?.ttlAmount.toString())
            .addField("TOTAL AMOUNT:", paymentDetails?.ttlAmount.toString())
            .addField("PAYMENT:", paymentDetails?.accountType.toString())
            .addField("CARD:", "**** **** **** 1234") // Replace with actual masked card data if available
            .addField("AUTHORIZATION:", paymentDetails?.hostAuthCode.toString())
            .addField("THANK YOU FOR YOUR PURCHASE!\nWE APPRECIATE YOUR BUSINESS!", "")
            .addField("CUSTOMER SUPPORT", "")
            .addField("SUPPORT PHONE:", "(987) 654-3210")
            .addField("SUPPORT EMAIL:", "support@example.com")
            .addField("BARCODE", paymentDetails?.hostTxnRef ?: "N/A") // Assuming txnRef is used for barcode
            .addField("QR CODE", "https://example.com/qrcode") // You can generate a QR code based on txn details if needed
            .build()
    }

    data class Receipt(
        val fields: List<Pair<String, String>>,
        val items: List<ReceiptItem>,
        val barcode: String? = null,
        val qrcode: String? = null
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
}
