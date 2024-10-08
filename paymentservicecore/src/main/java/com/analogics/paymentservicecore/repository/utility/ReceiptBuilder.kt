package com.analogics.paymentservicecore.repository.utility

import com.analogics.builder_core.model.PaymentServiceTxnDetails

class ReceiptBuilder {

    // Define alignment options
    enum class Alignment {
        LEFT,
        CENTER,
        RIGHT
    }

    fun createReceipt(paymentDetails: PaymentServiceTxnDetails?): Receipt {
        return Receipt.Builder()
            .addField("STORE NAME:", "Awesome Store", Alignment.CENTER)
            .addField("STORE ADDRESS:", "1234 Market St, San Francisco, CA", Alignment.LEFT)
            .addField("STORE PHONE:", "(123) 456-7890", Alignment.LEFT)
            .addField("Merchant Id:", paymentDetails?.merchantId ?: "N/A", Alignment.LEFT)
            .addField("Terminal Id:", paymentDetails?.terminalId ?: "N/A", Alignment.LEFT)
            .addField("Transaction Status:", paymentDetails?.hostAuthResult ?: "N/A",
                Alignment.LEFT
            )
            .addField("ORDER DETAILS", paymentDetails?.purchaseOrderNo ?: "N/A", Alignment.LEFT)
            .addField("ORDER DATE/TIME:", paymentDetails?.dateTime ?: "N/A", Alignment.LEFT)
            .addField("Invoice No:", paymentDetails?.invoiceNo ?: "N/A", Alignment.LEFT)
            .addField("POS TERMINAL #:", paymentDetails?.terminalId ?: "N/A", Alignment.LEFT)
            .addField("Card Entry Mode:", paymentDetails?.cardEntryMode ?: "N/A", Alignment.LEFT)
            .addField("SUBTOTAL:", paymentDetails?.txnAmount.toString(), Alignment.CENTER)
            .addField("SALES TAX:", paymentDetails?.ttlAmount.toString(), Alignment.CENTER)
            .addField("TOTAL AMOUNT:", paymentDetails?.ttlAmount.toString(), Alignment.CENTER)
            .addField("PAYMENT:", paymentDetails?.accountType.toString(), Alignment.CENTER)
            .addField("CARD:", "**** **** **** 1234", Alignment.CENTER)
            .addField("AUTHORIZATION:", paymentDetails?.hostAuthCode.toString(), Alignment.CENTER)
            .addField("THANK YOU FOR YOUR PURCHASE!\nWE APPRECIATE YOUR BUSINESS!", "",
                Alignment.CENTER
            )
            .addField("CUSTOMER SUPPORT", "", Alignment.CENTER)
            .addField("SUPPORT PHONE:", "(987) 654-3210", Alignment.CENTER)
            .addField("SUPPORT EMAIL:", "support@example.com", Alignment.CENTER)
            .addField("BARCODE", paymentDetails?.hostTxnRef ?: "N/A", Alignment.CENTER)
            .addField("QR CODE", "https://example.com/qrcode", Alignment.LEFT)
            .build()
    }

    data class Receipt(
        val fields: List<Triple<String, String, Alignment>>, // Use Triple to hold label, value, and alignment
        val items: List<ReceiptItem>,
        val barcode: String? = null,
        val qrcode: String? = null
    ) {
        class Builder {
            private val fields: MutableList<Triple<String, String, Alignment>> = mutableListOf()
            private val items: MutableList<ReceiptItem> = mutableListOf()
            private var barcode: String? = null
            private var qrcode: String? = null

            fun addField(label: String, value: String, alignment: Alignment) = apply {
                fields.add(Triple(label, value, alignment))
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
                return Receipt(fields, items, barcode, qrcode) // Return the fields directly
            }
        }
    }

    data class ReceiptItem(
        val name: String,
        val price: Double
    )
}
