package com.analogics.tpaymentsapos.rootUiScreens.utility

import android.content.Context
import android.util.Log
import com.analogics.builder_core.model.PaymentServiceTxnDetails
import com.analogics.tpaymentsapos.R
import com.analogics.tpaymentsapos.rootUiScreens.activity.SharedViewModel

class ReceiptBuilder {

    // Define alignment options
    enum class Alignment {
        LEFT,
        CENTER,
        RIGHT,
        NONE
    }

    // Function to build a receipt
    fun createReceipt(context: Context, sharedViewModel: SharedViewModel, paymentDetails: PaymentServiceTxnDetails?): Receipt {
        return Receipt.Builder()
            .apply {

                Log.d("PaymentDetail hello", "Batch Id: ${sharedViewModel.objRootAppPaymentDetail.batchId}")
                addField(sharedViewModel.objPosConfig?.header1.toString(), "", "", Alignment.CENTER)
                addField(context.getString(R.string.receipt_address), sharedViewModel.objPosConfig?.header2.toString(), "", Alignment.LEFT)
                addField("", "", "", Alignment.CENTER)
                addField("", "", "", Alignment.CENTER)
                addField(context.getString(R.string.receipt_date), paymentDetails?.dateTime, "", Alignment.LEFT)
                addField(context.getString(R.string.receipt_merchant_id), paymentDetails?.merchantId, context.getString(R.string.receipt_terminal_id) + paymentDetails?.merchantId, Alignment.NONE)
                addField(context.getString(R.string.receipt_batch_no), paymentDetails?.batchId, context.getString(R.string.receipt_invoice_no) + paymentDetails?.invoiceNo, Alignment.NONE)
                addField(paymentDetails?.txnType,"" , "", Alignment.CENTER)
                addField(context.getString(R.string.receipt_card_no), "**** **** **** 1234", "", Alignment.CENTER)
                addField(context.getString(R.string.receipt_card_type), "", "CREDIT", Alignment.NONE)
                addField(context.getString(R.string.receipt_auth_code), "", paymentDetails?.hostAuthCode, Alignment.NONE)
                addField(context.getString(R.string.receipt_ref_no), "", "56789", Alignment.NONE)
                addField(context.getString(R.string.receipt_subtotal), "", paymentDetails?.txnAmount, Alignment.NONE)
                addField(context.getString(R.string.receipt_tip), "", paymentDetails?.tip, Alignment.NONE)
                addField(context.getString(R.string.receipt_gray_line), "", "", Alignment.CENTER)
                addField(context.getString(R.string.receipt_total), "", paymentDetails?.ttlAmount, Alignment.NONE)
                addField(context.getString(R.string.receipt_gray_line), "", "", Alignment.CENTER)
                addField(context.getString(R.string.receipt_sign), "", "", Alignment.LEFT)
                addField(context.getString(R.string.receipt_gray_line), "", "", Alignment.CENTER)
                addField(context.getString(R.string.receipt_txn_status), "", paymentDetails?.txnStatus, Alignment.NONE)
                addField("CARDHOLDER NAME", "", "", Alignment.CENTER)
                addField(" TRANSACTION ACCEPTED & LIABILITY" + " OF CARDHOLDER TO PAY IS" + " CONFIRMED", "", "", Alignment.CENTER)
                addField("******MERCHANT COPY******", "", "", Alignment.CENTER)

            }
            .build()
    }

    // Function to build a summary report
    fun createSummaryReport(context: Context,sharedViewModel: SharedViewModel,paymentDetails: PaymentServiceTxnDetails?): SummaryReport {
        return SummaryReport.Builder()
            .addSummaryField("", sharedViewModel.objPosConfig?.header1.toString(), "")
            .addSummaryField("", context.getString(R.string.summary_header), "")
            .addSummaryField("", sharedViewModel.objPosConfig?.header2.toString(), sharedViewModel.objPosConfig?.header3.toString())
            .addSummaryField(context.getString(R.string.summary_dot_line), context.getString(R.string.summary_dot_line), context.getString(R.string.summary_dot_line))
            .addSummaryField(context.getString(R.string.summary_purchase),paymentDetails?.ttlPurchaseCount?.toString() ?: "N/A" , paymentDetails?.ttlTxnAmount?.toString() ?: "N/A")
            .addSummaryField(context.getString(R.string.summary_refund),paymentDetails?.ttlRefundCount?.toString() ?: "N/A" , paymentDetails?.ttlRefundAmount?.toString() ?: "N/A")
            .addSummaryField(context.getString(R.string.summary_void), "2", "10.00")
            .addSummaryField(context.getString(R.string.summary_tip), "10", "10.00")
            .addSummaryField(context.getString(R.string.summary_cashback),"5" , "50.00")
            .addSummaryField(context.getString(R.string.summary_total), paymentDetails?.ttlTxnCount?.toString() ?: "N/A", paymentDetails?.ttlTxnAmount?.toString() ?: "N/A")
            .addSummaryField(context.getString(R.string.summary_txn_breakdown), "", "")
            .addSummaryField(context.getString(R.string.summary_credit_txn), "", "")
            .addSummaryField(context.getString(R.string.summary_debit_txn), "", "")
            .addSummaryField("", context.getString(R.string.summary_footer), "")
            .build()
    }

    data class Receipt(
        val fields: List<Field>,  // Replace Triple with Field class for four parameters
        val items: List<ReceiptItem>,
        val barcode: String? = null,
        val qrcode: String? = null
    ) {
        // Define a class to hold four parameters
        data class Field(
            val label: String?,
            val value: String?,
            val description: String?,
            val alignment: Alignment
        )

        class Builder {
            private val fields: MutableList<Field> = mutableListOf()
            private val items: MutableList<ReceiptItem> = mutableListOf()
            private var barcode: String? = null
            private var qrcode: String? = null

            // Add method to accept nullable label, value, description, and alignment
            fun addField(label: String? = null, value: String? = null, description: String? = null, alignment: Alignment) = apply {
                fields.add(Field(label, value, description, alignment))
            }

            // Method to add barcode
            fun addBarcode(barcode: String?) = apply {
                this.barcode = barcode
            }

            // Method to add QR code
            fun addQrCode(qrcode: String?) = apply {
                this.qrcode = qrcode
            }

            // Build method to create the final Receipt object
            fun build(): Receipt {
                return Receipt(fields, items, barcode, qrcode)
            }
        }
    }


    // Data class for SummaryReport
    data class SummaryReport(
        val summaryFields: List<Triple<String, String, String>> // Now has label, value, and description
    ) {
        class Builder {
            private val summaryFields: MutableList<Triple<String, String, String>> = mutableListOf()

            // Removed alignment and added description as third parameter
            fun addSummaryField(label: String, value: String, description: String) = apply {
                summaryFields.add(Triple(label, value, description))
            }

            fun build(): SummaryReport {
                return SummaryReport(summaryFields)
            }
        }
    }


    fun createDetailReport(context: Context,sharedViewModel: SharedViewModel, paymentDetails: PaymentServiceTxnDetails?, transactionList: List<TransactionDetails> ): DetailedReport {
        val reportBuilder = DetailedReport.Builder()

        reportBuilder
            .addDetailField("", sharedViewModel.objPosConfig?.header1.toString(), "")
            .addDetailField("", context.getString(R.string.summary_debit_txn), "")
            .addDetailField(context.getString(R.string.detail_txn), context.getString(R.string.detail_count), context.getString(R.string.summary_total))
            .addDetailField(context.getString(R.string.summary_dot_line), context.getString(R.string.summary_dot_line), context.getString(R.string.summary_dot_line))
            .addDetailField(context.getString(R.string.summary_purchase), paymentDetails?.ttlPurchaseCount?.toString() ?: "N/A", paymentDetails?.ttlPurchaseAmount?.toString() ?: "N/A")
            .addDetailField(context.getString(R.string.summary_refund), paymentDetails?.ttlRefundCount?.toString() ?: "N/A", paymentDetails?.ttlRefundAmount?.toString() ?: "N/A")
            .addDetailField(context.getString(R.string.summary_dot_line), context.getString(R.string.summary_dot_line), context.getString(R.string.summary_dot_line))
            .addDetailField("", context.getString(R.string.detail_txn_details), "")
            .addDetailField(context.getString(R.string.detail_txn_Type), "", context.getString(R.string.detail_txn_Status))
            .addDetailField(context.getString(R.string.detail_invoice_no), "", context.getString(R.string.detail_auth_code))
            .addDetailField(context.getString(R.string.detail_txn_amt), "", context.getString(R.string.detail_total_amt))
            .addDetailField("", paymentDetails?.txnStatus.toString(), "")


        // Add transaction details from the transaction list
        transactionList.forEach { transaction ->
            reportBuilder.addDetailField(
                "",
                transaction.timedate,
                ""
            )
            reportBuilder.addDetailField(
                transaction.TxnType,
                "",
                transaction.Status
            )
            reportBuilder.addDetailField(
                transaction.InvoiceNo,
                "",
                transaction.AuthCode
            )
            reportBuilder.addDetailField(
                transaction.txnAmount,
                "",
                transaction.ttlAmount
            )
            reportBuilder.addDetailField(
                context.getString(R.string.summary_dot_line),
                context.getString(R.string.summary_dot_line),
                context.getString(R.string.summary_dot_line)
            )
        }

        // Build and return the detailed report
        return reportBuilder.build()
    }

    // Data class for DetailedReport
    data class DetailedReport(
        val detailFields: List<Triple<String, String, String>> // Three fields: label, quantity, price
    ) {
        class Builder {
            private val detailFields: MutableList<Triple<String, String, String>> = mutableListOf()

            fun addDetailField(label: String, quantity: String, price: String) = apply {
                detailFields.add(Triple(label, quantity, price))
            }

            fun build(): DetailedReport {
                return DetailedReport(detailFields)
            }
        }
    }

    data class TransactionDetails(
        val TxnType: String,
        val Status: String,
        val InvoiceNo: String,
        val AuthCode: String,
        val txnAmount: String,
        val ttlAmount: String,
        val timedate: String
    )

    // Data class for ReceiptItem
    data class ReceiptItem(
        val name: String,
        val price: Double
    )
}
