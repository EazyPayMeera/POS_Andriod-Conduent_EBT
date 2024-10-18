package com.analogics.tpaymentsapos.rootUiScreens.utility

import android.content.Context
import com.analogics.builder_core.model.PaymentServiceTxnDetails
import com.analogics.tpaymentsapos.R
import com.analogics.tpaymentsapos.rootUiScreens.activity.SharedViewModel

class ReceiptBuilder {

    // Define alignment options
    enum class Alignment {
        LEFT,
        CENTER,
        RIGHT
    }

    // Function to build a receipt
    fun createReceipt(context: Context,paymentDetails: PaymentServiceTxnDetails?): Receipt {
        return Receipt.Builder()
            .addField(context.getString(R.string.receipt_header), "", Alignment.CENTER)
            .addField(context.getString(R.string.receipt_address), "1234 Market St, San Francisco, CA", Alignment.LEFT)
            .addField(context.getString(R.string.receipt_merchant_id), paymentDetails?.merchantId ?: "N/A", Alignment.LEFT)
            .addField(context.getString(R.string.receipt_terminal_id), paymentDetails?.terminalId ?: "N/A", Alignment.LEFT)
            .addField(context.getString(R.string.receipt_gray_line), "", Alignment.LEFT)
            .addField(context.getString(R.string.receipt_card_type), "CREDIT", Alignment.LEFT)
            .addField(context.getString(R.string.receipt_card_no), "**** **** **** 1234", Alignment.LEFT)
            .addField(context.getString(R.string.receipt_trans_type), "\n" + (paymentDetails?.txnType ?: "N/A"), Alignment.LEFT)
            .addField(context.getString(R.string.receipt_batch_no), (paymentDetails?.batchId ?: "N/A"), Alignment.LEFT)
            .addField(context.getString(R.string.receipt_trace_no), "123456", Alignment.LEFT)
            .addField(context.getString(R.string.receipt_date_time), paymentDetails?.dateTime ?: "N/A", Alignment.LEFT)
            .addField(context.getString(R.string.receipt_ref_no), "56789", Alignment.LEFT)
            .addField(context.getString(R.string.receipt_aap_code), "1112", Alignment.LEFT)
            .addField(context.getString(R.string.receipt_txn_id), paymentDetails?.invoiceNo ?: "N/A", Alignment.LEFT)
            .addField(context.getString(R.string.receipt_app), paymentDetails?.cardBrand ?: "N/A", Alignment.LEFT)
            .addField(context.getString(R.string.receipt_subtotal), paymentDetails?.txnAmount.toString(), Alignment.LEFT)
            .addField(context.getString(R.string.receipt_sale_tax), paymentDetails?.ttlAmount.toString(), Alignment.LEFT)
            .addField(context.getString(R.string.receipt_pos_terminal), paymentDetails?.terminalId ?: "N/A", Alignment.LEFT)
            .addField(context.getString(R.string.receipt_card_entry_mode), paymentDetails?.cardEntryMode ?: "N/A", Alignment.LEFT)
            .addField(context.getString(R.string.receipt_aid), "123456789123456", Alignment.LEFT)
            .addField(context.getString(R.string.receipt_tc), paymentDetails?.purchaseOrderNo ?: "N/A", Alignment.LEFT)
            .addField(context.getString(R.string.receipt_total), paymentDetails?.ttlAmount.toString(), Alignment.LEFT)
            .addField(context.getString(R.string.receipt_payment), paymentDetails?.accountType.toString(), Alignment.LEFT)
            .addField(context.getString(R.string.receipt_authorization), paymentDetails?.hostAuthCode.toString(), Alignment.LEFT)
            .addField("I AGREE TO PAY ABOVE TOTAL", "\n" + "AMOUNT ACCORDING TO CARD\n" + "ISSUER AGREEMENT", Alignment.CENTER)
            .addField("CUSTOMER SUPPORT", "", Alignment.LEFT)
            .addField("SUPPORT PHONE:", "(987) 654-3210", Alignment.LEFT)
            .addField("SUPPORT EMAIL:", "support@example.com", Alignment.LEFT)
            .addField("BARCODE", paymentDetails?.hostTxnRef ?: "N/A", Alignment.LEFT)
            .addField("QR CODE", "https://example.com/qrcode", Alignment.CENTER)
            .addField("******CUSTOMER COPY******", "", Alignment.CENTER)
            .build()
    }

    // Function to build a summary report
    fun createSummaryReport(context: Context,sharedViewModel: SharedViewModel,paymentDetails: PaymentServiceTxnDetails?): SummaryReport {
        return SummaryReport.Builder()
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

    // Data class for Receipt
    data class Receipt(
        val fields: List<Triple<String, String, Alignment>>,
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


    fun createDetailReport(context: Context, paymentDetails: PaymentServiceTxnDetails?, transactionList: List<TransactionDetails> ): DetailedReport {
        val reportBuilder = DetailedReport.Builder()

        reportBuilder
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
            .addDetailField(context.getString(R.string.summary_dot_line), context.getString(R.string.summary_dot_line), context.getString(R.string.summary_dot_line))

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
