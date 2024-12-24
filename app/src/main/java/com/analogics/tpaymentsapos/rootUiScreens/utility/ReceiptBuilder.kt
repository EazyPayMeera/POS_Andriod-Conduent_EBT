package com.analogics.tpaymentsapos.rootUiScreens.utility

import android.content.Context
import com.analogics.paymentservicecore.model.PaymentServiceTxnDetails
import com.analogics.tpaymentsapos.R
import com.analogics.tpaymentsapos.rootUiScreens.activity.SharedViewModel
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.toDecimalFormat

class ReceiptBuilder {
    // Define alignment options
    enum class Alignment {
        LEFT,
        CENTER,
        RIGHT,
        NONE
    }

    enum class FontSize {
        Small,
        Medium,
        Big
    }

    fun createSummaryReport(context: Context, sharedViewModel: SharedViewModel, paymentDetails: PaymentServiceTxnDetails?): SummaryReport {
        val reportBuilder = SummaryReport.Builder()

        reportBuilder
            .addSummaryField("", sharedViewModel.objPosConfig?.header1.toString(), "",FontSize.Small)
            .addSummaryField("", context.getString(R.string.summary_header), "",FontSize.Small)
            .addSummaryField("", sharedViewModel.objPosConfig?.header2.toString(), sharedViewModel.objPosConfig?.header3.toString(),FontSize.Small)

        // Conditionally add the "Training Mode" field if in demo mode
        if (sharedViewModel.objPosConfig?.isDemoMode == true) {
            reportBuilder.addSummaryField(context.getString(R.string.summary_dot_line), context.getString(R.string.summary_dot_line), context.getString(R.string.summary_dot_line),FontSize.Small)
            reportBuilder.addSummaryField("", context.getString(R.string.receipt_train_mode), "",FontSize.Big)
            reportBuilder.addSummaryField(context.getString(R.string.summary_dot_line), context.getString(R.string.summary_dot_line), context.getString(R.string.summary_dot_line),FontSize.Small)
        }

        reportBuilder
            .addSummaryField(context.getString(R.string.summary_dot_line), context.getString(R.string.summary_dot_line), context.getString(R.string.summary_dot_line),FontSize.Small)
            .addSummaryField(context.getString(R.string.summary_purchase), paymentDetails?.ttlPurchaseCount?.toString() ?: "N/A", (paymentDetails?.ttlTxnAmount?.toDoubleOrNull()?:0.00).toDecimalFormat(),FontSize.Small)
            .addSummaryField(context.getString(R.string.summary_refund), paymentDetails?.ttlRefundCount?.toString() ?: "N/A", (paymentDetails?.ttlRefundAmount?.toDoubleOrNull()?:0.00).toDecimalFormat(),FontSize.Small)
            .addSummaryField(context.getString(R.string.summary_void), paymentDetails?.ttlVoidCount?.toString() ?: "N/A", (paymentDetails?.ttlVoidAmount?.toDoubleOrNull()?:0.00).toDecimalFormat(),FontSize.Small)
            .addSummaryField(context.getString(R.string.summary_tip), paymentDetails?.ttlTipCount?.toString() ?: "N/A", (paymentDetails?.ttlTipAmount?.toDoubleOrNull()?:0.00).toDecimalFormat(),FontSize.Small)
            .addSummaryField(context.getString(R.string.summary_total), paymentDetails?.ttlTxnCount?.toString() ?: "N/A", (paymentDetails?.ttlTxnAmount?.toDoubleOrNull()?:0.00).toDecimalFormat(),FontSize.Small)
            .addSummaryField(context.getString(R.string.summary_txn_breakdown), "", "",FontSize.Small)
            .addSummaryField(context.getString(R.string.summary_credit_txn), "", "",FontSize.Small)
            .addSummaryField(context.getString(R.string.summary_debit_txn), "", "",FontSize.Small)
            .addSummaryField("", context.getString(R.string.summary_footer), "",FontSize.Small)

        // Finally, build the report
        return reportBuilder.build()
    }

    // Function to build a receipt
    fun createReceipt(context: Context,customer: Boolean = false, sharedViewModel: SharedViewModel, paymentDetails: PaymentServiceTxnDetails?): Receipt {
        return Receipt.Builder()
            .apply {
                if(customer) {
                    sharedViewModel.objPosConfig?.header1?.let {
                        addField(it.toString(), "", "", Alignment.CENTER, FontSize.Small)
                    }
                    sharedViewModel.objPosConfig?.header2?.let {
                        addField(it.toString(), "", "", Alignment.CENTER, FontSize.Small)
                    }
                    sharedViewModel.objPosConfig?.header3?.let {
                        addField(it.toString(), "", "", Alignment.CENTER, FontSize.Small)
                    }
                    sharedViewModel.objPosConfig?.header4?.let {
                        addField(it.toString(), "", "", Alignment.CENTER, FontSize.Small)
                    }
                }
                addField(" ", "", "", Alignment.CENTER,FontSize.Medium)

                addField(context.getString(R.string.receipt_date), paymentDetails?.dateTime, "", Alignment.LEFT,FontSize.Small)
                addField(context.getString(R.string.receipt_merchant_id) + paymentDetails?.merchantId,"","", Alignment.NONE,FontSize.Small)
                addField(context.getString(R.string.receipt_terminal_id) + paymentDetails?.terminalId,"","", Alignment.NONE,FontSize.Small)
                addField(context.getString(R.string.receipt_batch_no), paymentDetails?.batchId, context.getString(R.string.receipt_invoice_no) + paymentDetails?.invoiceNo, Alignment.NONE,FontSize.Small)
                addField(" ", "", "", Alignment.CENTER,FontSize.Medium)
                if(sharedViewModel.objPosConfig?.isDemoMode == true)
                {
                    addField(context.getString(R.string.receipt_gray_line), "", "", Alignment.CENTER,FontSize.Small)
                    addField(context.getString(R.string.receipt_train_mode),"" , "", Alignment.CENTER,FontSize.Big)
                    addField(context.getString(R.string.receipt_gray_line), "", "", Alignment.CENTER,FontSize.Small)
                }
                addField(paymentDetails?.txnType.toString(),"" , "", Alignment.CENTER,FontSize.Big)
                addField(context.getString(R.string.receipt_txn_status), "", paymentDetails?.txnStatus, Alignment.NONE,FontSize.Big)
                addField(" ", "", "", Alignment.LEFT, FontSize.Medium)
                addField(context.getString(R.string.receipt_card_no), paymentDetails?.cardMaskedPan, "", Alignment.CENTER,FontSize.Small)
                addField(context.getString(R.string.receipt_card_type), "", paymentDetails?.cardBrand?.toString(), Alignment.NONE,FontSize.Small)
                addField(context.getString(R.string.receipt_auth_code), "", paymentDetails?.hostAuthCode, Alignment.NONE,FontSize.Small)
                addField(context.getString(R.string.receipt_ref_no), "", paymentDetails?.hostTxnRef, Alignment.NONE,FontSize.Small)
                addField(context.getString(R.string.receipt_subtotal), "", (paymentDetails?.txnAmount?.toDoubleOrNull()?:0.00).toDecimalFormat(), Alignment.NONE,FontSize.Medium)
                addField(context.getString(R.string.receipt_tip), "", (paymentDetails?.tip?.toDoubleOrNull()?:0.00).toDecimalFormat(), Alignment.NONE,FontSize.Small)
                addField(context.getString(R.string.receipt_gray_line), "", "", Alignment.CENTER,FontSize.Small)
                addField(context.getString(R.string.receipt_total), "", (paymentDetails?.ttlAmount?.toDoubleOrNull()?:0.00).toDecimalFormat(), Alignment.NONE,FontSize.Medium)
                addField(context.getString(R.string.receipt_gray_line), "", "", Alignment.CENTER,FontSize.Small)
                if(!customer) {
                    addField(" ", "", "", Alignment.LEFT, FontSize.Medium)
                    addField(context.getString(R.string.receipt_sign), "", "", Alignment.LEFT, FontSize.Small)
                    addField(context.getString(R.string.receipt_gray_line), "", "", Alignment.CENTER,FontSize.Small)
                    addField(context.getString(R.string.receipt_card_holder_name), "", "", Alignment.CENTER, FontSize.Small)
                    addField(" ", "", "", Alignment.LEFT, FontSize.Medium)
                    addField(context.getString(R.string.receipt_note), "", "", Alignment.CENTER, FontSize.Small)
                }

                if(customer) {
                    //addField(context.getString(R.string.receipt_card_holder_name), "", "", Alignment.CENTER, FontSize.Small)
                    addField(" ", "", "", Alignment.LEFT, FontSize.Medium)
                    addField(context.getString(R.string.receipt_custom_copy), "", "", Alignment.CENTER, FontSize.Small)
                }
                else
                {
                    addField(" ", "", "", Alignment.LEFT, FontSize.Medium)
                    addField(context.getString(R.string.receipt_merch_copy), "", "", Alignment.CENTER, FontSize.Small)
                }

                if(customer) {
                    addField(" ", "", "", Alignment.LEFT, FontSize.Medium)
                    sharedViewModel.objPosConfig?.footer1?.let {
                        addField(it.toString(), "", "", Alignment.CENTER, FontSize.Small)
                    }
                    sharedViewModel.objPosConfig?.footer2?.let {
                        addField(it.toString(), "", "", Alignment.CENTER, FontSize.Small)
                    }
                    sharedViewModel.objPosConfig?.footer3?.let {
                        addField(it.toString(), "", "", Alignment.CENTER, FontSize.Small)
                    }
                    sharedViewModel.objPosConfig?.footer4?.let {
                        addField(it.toString(), "", "", Alignment.CENTER, FontSize.Small)
                    }
                }

                addField(" ", "", "", Alignment.LEFT, FontSize.Medium)
            }
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
            val alignment: Alignment,
            val fontsize: FontSize
        )

        class Builder {
            private val fields: MutableList<Field> = mutableListOf()
            private val items: MutableList<ReceiptItem> = mutableListOf()
            private var barcode: String? = null
            private var qrcode: String? = null

            // Add method to accept nullable label, value, description, and alignment
            fun addField(label: String? = null, value: String? = null, description: String? = null, alignment: Alignment,fontsize:FontSize) = apply {
                fields.add(Field(label, value, description, alignment,fontsize))
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


    // Data class for SummaryField, which holds label, value, description, and fontsize
    data class SummaryField(
        val label: String,
        val value: String,
        val description: String,
        val fontsize: FontSize
    )

    // Data class for SummaryReport
    data class SummaryReport(
        val summaryFields: List<SummaryField> // Now has label, value, description, and fontsize
    ) {
        class Builder {
            private val summaryFields: MutableList<SummaryField> = mutableListOf()

            // Added Int parameter for fontsize after description
            fun addSummaryField(label: String, value: String, description: String, fontsize: FontSize) = apply {
                summaryFields.add(SummaryField(label, value, description, fontsize))
            }

            fun build(): SummaryReport {
                return SummaryReport(summaryFields)
            }
        }
    }



    fun createDetailReport(
        context: Context,
        sharedViewModel: SharedViewModel, paymentDetails: PaymentServiceTxnDetails?, transactionList: List<TransactionDetails>?
    ): DetailedReport {
        val reportBuilder = DetailedReport.Builder()

        reportBuilder
            .addDetailField("", sharedViewModel.objPosConfig?.header1.toString(), "",FontSize.Small)
            .addDetailField("", context.getString(R.string.summary_debit_txn), "",FontSize.Small)
            .addDetailField(context.getString(R.string.detail_txn), context.getString(R.string.detail_count), context.getString(R.string.summary_total),FontSize.Small)
            if(sharedViewModel.objPosConfig?.isDemoMode == true)
            {
                reportBuilder.addDetailField(context.getString(R.string.summary_dot_line), context.getString(R.string.summary_dot_line), context.getString(R.string.summary_dot_line),FontSize.Small)
                reportBuilder.addDetailField("", context.getString(R.string.receipt_train_mode), "",FontSize.Big)
                reportBuilder.addDetailField(context.getString(R.string.summary_dot_line), context.getString(R.string.summary_dot_line), context.getString(R.string.summary_dot_line),FontSize.Small)
            }
        reportBuilder
            .addDetailField(context.getString(R.string.summary_dot_line), context.getString(R.string.summary_dot_line), context.getString(R.string.summary_dot_line),FontSize.Small)
            .addDetailField(context.getString(R.string.summary_purchase), paymentDetails?.ttlPurchaseCount?.toString() ?: "N/A", (paymentDetails?.ttlPurchaseAmount?.toDoubleOrNull()?:0.00).toDecimalFormat(),FontSize.Small)
            .addDetailField(context.getString(R.string.summary_refund), paymentDetails?.ttlRefundCount?.toString() ?: "N/A", (paymentDetails?.ttlRefundAmount?.toDoubleOrNull()?:0.00).toDecimalFormat(),FontSize.Small)
            .addDetailField(context.getString(R.string.summary_dot_line), context.getString(R.string.summary_dot_line), context.getString(R.string.summary_dot_line),FontSize.Small)
            .addDetailField("", context.getString(R.string.detail_txn_details), "",FontSize.Small)
            .addDetailField(context.getString(R.string.detail_txn_Type), "", context.getString(R.string.detail_txn_Status),FontSize.Small)
            .addDetailField(context.getString(R.string.detail_invoice_no), "", context.getString(R.string.detail_auth_code),FontSize.Small)
            .addDetailField(context.getString(R.string.detail_txn_amt), "", context.getString(R.string.detail_total_amt),FontSize.Small)
            .addDetailField("", paymentDetails?.txnStatus.toString(), "",FontSize.Small)


        // Add transaction details from the transaction list
        transactionList?.forEach { transaction ->
            reportBuilder.addDetailField(
                "",
                transaction.timedate,
                "",
                FontSize.Small
            )
            reportBuilder.addDetailField(
                transaction.TxnType,
                "",
                transaction.Status,
                FontSize.Small
            )
            reportBuilder.addDetailField(
                transaction.InvoiceNo,
                "",
                transaction.AuthCode,
                FontSize.Small
            )
            reportBuilder.addDetailField(
                transaction.txnAmount,
                "",
                transaction.ttlAmount,
                FontSize.Small
            )
            reportBuilder.addDetailField(
                context.getString(R.string.summary_dot_line),
                context.getString(R.string.summary_dot_line),
                context.getString(R.string.summary_dot_line),
                FontSize.Small
            )
        }

        // Build and return the detailed report
        return reportBuilder.build()
    }

    // Data class for DetailField with four fields
    data class DetailField(
        val label: String,
        val quantity: String,
        val price: String,
        val discount: FontSize // Example additional parameter
    )

    // Updated DetailedReport class
    data class DetailedReport(
        val detailFields: List<DetailField>
    ) {
        class Builder {
            private val detailFields: MutableList<DetailField> = mutableListOf()

            fun addDetailField(label: String, quantity: String, price: String, discount: FontSize) = apply {
                detailFields.add(DetailField(label, quantity, price, discount))
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
