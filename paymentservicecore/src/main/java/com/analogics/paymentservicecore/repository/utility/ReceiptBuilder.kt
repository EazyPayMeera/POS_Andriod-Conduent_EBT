package com.analogics.tpaymentsapos.rootUtils.genericComposeUI

class ReceiptBuilder {

    fun createReceipt(): Receipt {
        return Receipt.Builder()
            .addField("STORE NAME:", "Awesome Store")
            .addField("STORE ADDRESS:", "1234 Market St, San Francisco, CA")
            .addField("STORE PHONE:", "(123) 456-7890")
            .addField("ORDER DETAILS", "Order Details")
            .addField("ORDER DATE/TIME:", "2024-08-29 12:34")
            .addField("ORDER #:", "12345")
            .addField("POS TERMINAL #:", "67890")
            .addField("PURCHASED ITEMS", "")
            .addItem(ReceiptItem("Item 1", 19.99))
            .addItem(ReceiptItem("Item 2", 9.99))
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
