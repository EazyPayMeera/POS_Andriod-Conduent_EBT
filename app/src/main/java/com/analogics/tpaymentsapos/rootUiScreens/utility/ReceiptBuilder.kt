package com.analogics.tpaymentsapos.rootUiScreens.utility

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
            .addField("GLOBAL PAYMENTS", "", Alignment.CENTER)
            .addField("STORE ADDRESS:", "1234 Market St, San Francisco, CA", Alignment.LEFT)
            .addField("Merchant Id:", paymentDetails?.merchantId ?: "N/A", Alignment.LEFT)
            .addField("Terminal Id:", paymentDetails?.terminalId ?: "N/A", Alignment.LEFT)
            .addField("-----------------------", "", Alignment.LEFT)
            .addField("CARD TYPE:  ", "CREDIT", Alignment.LEFT)
            .addField("CARD:", "**** **** **** 1234", Alignment.LEFT)
            .addField("TRANS TYPE:", "\n" + (paymentDetails?.txnType ?: "N/A"), Alignment.LEFT)
            .addField("BATCH NO:", (paymentDetails?.batchId ?: "N/A"), Alignment.LEFT)
            .addField("TRACE NO:", "123456", Alignment.LEFT)
            .addField("DATE/TIME:", paymentDetails?.dateTime ?: "N/A", Alignment.LEFT)
            .addField("REF NO:", "56789", Alignment.LEFT)
            .addField("APP CODE:", "1112", Alignment.LEFT)
            .addField("Txn ID:", paymentDetails?.invoiceNo ?: "N/A", Alignment.LEFT)
            .addField("APP:", paymentDetails?.cardBrand ?: "N/A", Alignment.LEFT)
            .addField("SUBTOTAL:", paymentDetails?.txnAmount.toString(), Alignment.LEFT)
            .addField("SALES TAX:", paymentDetails?.ttlAmount.toString(), Alignment.LEFT)
            .addField("POS TERMINAL #:", paymentDetails?.terminalId ?: "N/A", Alignment.LEFT)
            .addField("Card Entry Mode:", paymentDetails?.cardEntryMode ?: "N/A", Alignment.LEFT)
            .addField("AID:", "123456789123456", Alignment.LEFT)
            .addField("TC:", paymentDetails?.purchaseOrderNo ?: "N/A", Alignment.LEFT)
            .addField("TOTAL:", paymentDetails?.ttlAmount.toString(), Alignment.LEFT)
            .addField("PAYMENT:", paymentDetails?.accountType.toString(), Alignment.LEFT)
            .addField("AUTHORIZATION:", paymentDetails?.hostAuthCode.toString(), Alignment.LEFT)
            .addField("I AGREE TO PAY ABOVE TOTAL", "\n" + "AMOUNT ACCORDING TO CARD\n" + "ISSUER AGREEMENT", Alignment.CENTER)
            .addField("CUSTOMER SUPPORT", "", Alignment.LEFT)
            .addField("SUPPORT PHONE:", "(987) 654-3210", Alignment.LEFT)
            .addField("SUPPORT EMAIL:", "support@example.com", Alignment.LEFT)
            .addField("BARCODE", paymentDetails?.hostTxnRef ?: "N/A", Alignment.LEFT)
            .addField("QR CODE", "https://example.com/qrcode", Alignment.CENTER)
            .addField("******CUSTOMER COPY******", "", Alignment.CENTER)
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