package com.eazypaytech.pos.core.utils.miscellaneous

import android.content.Context
import android.util.Log
import com.eazypaytech.paymentservicecore.constants.AppConstants
import com.analogics.paymentservicecore.data.listeners.responseListener.IPrinterServiceResponseListener
import com.analogics.paymentservicecore.data.model.printer.PrinterServiceResult
import com.eazypaytech.pos.domain.model.ObjRootAppPaymentDetails
import com.eazypaytech.pos.features.dialogs.ui.CustomDialogBuilder
import com.analogics.paymentservicecore.domain.repository.printerService.PrinterServiceRepository
import com.analogics.paymentservicecore.domain.repository.printerService.PrinterServiceRepository.PrintFormat
import com.eazypaytech.pos.core.utils.convertDateTime
import com.eazypaytech.pos.R
import com.eazypaytech.pos.domain.model.Symbol
import com.eazypaytech.pos.domain.model.Symbol.Type
import com.analogics.paymentservicecore.domain.repository.printerService.PrinterServiceRepository.Align
import com.analogics.paymentservicecore.domain.repository.printerService.PrinterServiceRepository.FontSize
import com.analogics.paymentservicecore.domain.repository.printerService.PrinterServiceRepository.Style
import com.eazypaytech.pos.core.utils.convertReceiptDateTime
import com.eazypaytech.pos.core.utils.getCurrentDateTime
import com.eazypaytech.pos.core.utils.getTxnStatusStringId
import com.eazypaytech.pos.core.utils.getTxnTypeStringId
import com.eazypaytech.pos.core.utils.toAmountFormat
import com.eazypaytech.pos.core.utils.toDecimalFormat

object PrinterUtils {

    /**
     * Prints a full transaction receipt for merchant or customer copy.
     *
     * This function builds a detailed receipt using transaction data,
     * dynamically handling different transaction types (purchase, refund,
     * cashback, balance inquiry, void, voucher, etc.) and statuses.
     *
     * Features:
     * - Dynamic receipt title based on transaction type
     * - Balance calculations for SNAP/CASH accounts
     * - Special handling for VOID, RETURN, CASHBACK, and VOUCHER flows
     * - Supports merchant and customer copies
     * - Shows card, amounts, tips, VAT, service charge, and balances
     * - Adds printer status callbacks (printing, error, out of paper)
     *
     * @param context Android context used for resources and printing
     * @param data Transaction details model
     * @param isCustomer true for customer copy, false for merchant copy
     */
    fun printReceipt(
        context: Context,
        data: ObjRootAppPaymentDetails,
        isCustomer: Boolean = false
    ) {
        Log.d("PRINT_RECEIPT", "Receipt Data: $data")
        val repo = PrinterServiceRepository().init(context, object : IPrinterServiceResponseListener {
            override fun onPrinterServiceResponse(response: Any) {
                when (response) {
                    is PrinterServiceResult.Result -> {
                        when (response.status) {
                            PrinterServiceResult.Status.PRINTING ->
                                CustomDialogBuilder.composePrintingDialog(
                                    title = context.getString(R.string.printing),
                                    subtitle = context.getString(
                                        if (isCustomer) R.string.receipt_printing_customer
                                        else R.string.receipt_printing_merchant
                                    ),
                                    message = context.getString(R.string.plz_wait)
                                )

                            PrinterServiceResult.Status.ERROR,
                            PrinterServiceResult.Status.PRINT_FAILURE,
                            PrinterServiceResult.Status.INIT_FAILURE ->
                                CustomDialogBuilder.composeAlertDialog(
                                    title = context.getString(R.string.printer_error_title),
                                    message = context.getString(R.string.printer_printing_failed)
                                )

                            PrinterServiceResult.Status.OUT_OF_PAPER ->
                                CustomDialogBuilder.composeAlertDialog(
                                    title = context.getString(R.string.printer_error_title),
                                    message = context.getString(R.string.printer_out_of_paper)
                                )

                            else -> CustomDialogBuilder.hideProgress()
                        }
                    }
                }
            }
        })

        /* =========================
           🔹 FLAGS (CLEAN CONTROL)
           ========================= */

        val txnTypeStr = context.getString(getTxnTypeStringId(data.txnType))
        val txnStatusStr = context.getString(getTxnStatusStringId(data.txnStatus))

        val isApproved = txnStatusStr == context.getString(R.string.status_approved)
        val isDeclined = txnStatusStr == context.getString(R.string.status_declined)

        val isSnapPurchase = txnTypeStr == context.getString(R.string.ebt_food_purchase)
        val isCashPurchase = txnTypeStr == context.getString(R.string.ebt_cash_purchase)
        val isCashback = txnTypeStr == context.getString(R.string.ebt_purchase_cashback)
        val isReturn = txnTypeStr == context.getString(R.string.ebt_foodstamp_return)
        val isBalanceInquiry = txnTypeStr == context.getString(R.string.ebt_bal_inquiry)
        val isCashBalanceInquiry = txnTypeStr == context.getString(R.string.receipt_txntype_balance_inquiry_cash)
        val isCashWithdrawal = txnTypeStr == context.getString(R.string.receipt_txntype_cash_withdrawal)
        val isVoid = txnTypeStr == context.getString(R.string.ebt_void_last)
        val isVoucherSettlement = txnTypeStr == context.getString(R.string.ebt_e_voucher)

        val date = convertReceiptDateTime(data.dateTime, outputFormat = "MM/dd/yy")
        val time = convertReceiptDateTime(data.dateTime, outputFormat = "hh:mm:ssa")

        Log.d("PRINT_RECEIPT", "ObjRootAppPaymentDetails: $data")
        /* =========================
           🔹 HEADER
           ========================= */


        repo.addText(data.header1,
            format = PrintFormat().align(Align.CENTER).style(Style.BOLD))
        repo.addText(data.header2,
            format = PrintFormat().align(Align.CENTER).style(Style.BOLD))
        repo.addText(data.header3,
            format = PrintFormat().align(Align.CENTER).style(Style.BOLD))
        repo.addText(data.header4,
            format = PrintFormat().align(Align.CENTER).style(Style.BOLD))

        repo.feedLine()

        repo.feedLine()

        /* Date / Time / TID */
        repo.addText(
            context.getString(R.string.receipt_terminal_id) + data.terminalId,
            date,
            format = PrintFormat().fontSize(FontSize.MEDIUM)
        )

        repo.addText(
            context.getString(R.string.clerk_type_clerk) + data.loginId,
            time,
            format = PrintFormat().fontSize(FontSize.MEDIUM)
        )

        /* Add Line */
        repo.addText(context.getString(R.string.receipt_gray_line),
            format = PrintFormat().fontSize(FontSize.MEDIUM).align(Align.LEFT),)

        /* =========================
           🔹 TITLE (SPEC COMPLIANT)
           ========================= */

        val title = when {
            isSnapPurchase -> "EBT SNAP BENEFIT PURCHASE"
            isCashPurchase -> "EBT CASH BENEFIT PURCHASE"
            isCashback -> "EBT CASH BENEFIT PURCHASE W/ CASHBACK"
            isReturn -> "EBT SNAP BENEFIT RETURN"
            isBalanceInquiry -> "EBT BALANCE INQUIRY"
            isCashBalanceInquiry -> "EBT BALANCE INQUIRY"
            isCashWithdrawal -> "EBT CASH WITHDRAWAL"
            isVoid -> "EBT Void Last Tran"
            isVoucherSettlement -> "EBT Voucher Settlement"
            else -> txnTypeStr
        }

        repo.addText(title,
            format = PrintFormat().align(Align.LEFT).style(Style.BOLD))
        repo.feedLine()

        /* =========================
           🔹 CARD + AMOUNT
           ========================= */

        repo.addText(
            context.getString(R.string.receipt_card_no) + "  " +
            data.cardMaskedPan?.replace(Regex("\\d(?=\\d{4})"), "X")
        )

        /* Settlement Date (ONLY for Declined, NOT for Return) */
        if ( !isReturn && isApproved) {
            data.settlementDate?.let {
                repo.addText(
                    context.getString(R.string.receipt_settlement_date) + " " + it,
                    format = PrintFormat().fontSize(FontSize.MEDIUM)
                )
            }
            repo.feedLine()
        }

        /* Balance Summary (Non-Approved, Not Return) */
        if (isApproved ) {
            repo.addText(
                context.getString(R.string.receipt_balance_summary),
                format = PrintFormat().fontSize(FontSize.MEDIUM)
            )
        }

        /* Add Line */
        repo.addText(context.getString(R.string.receipt_gray_line),
            format = PrintFormat().fontSize(FontSize.MEDIUM).align(Align.LEFT),)

        if(isVoucherSettlement) {
            Log.d("Voucher Settlement PRINT_RECEIPT", "Voucher Number : ${data.voucherNumber}")
            data.voucherNumber?.let {
                repo.addText(context.getString(R.string.receipt_voucher_number) + " " + it)
            }
            Log.d("Voucher Settlement PRINT_RECEIPT", "Approval Code : ${data.approvalCode}")
            data.approvalCode?.let {
                repo.addText(context.getString(R.string.receipt_voucher_approval_code) + " " + it)
            }
            data.txnAmount?.let {
                repo.addText(context.getString(R.string.receipt_voucher_amount) + " " + it)
            }
        }
        if (isVoid) {
            Log.d("VOID PRINT_RECEIPT", "Snap Begin Bal: ${data.snapBeginBal}")
            Log.d("VOID PRINT_RECEIPT", "Snap Purchase: ${data.txnAmount}")
            Log.d("VOID PRINT_RECEIPT", "Snap End Bal: ${data.snapEndBalance}")
           /* SNAP BEGIN BALANCE */
            val voidBeginBal = data.snapEndBalance?.minus(data.txnAmount!!)
            voidBeginBal.let {
                repo.addText(
                    context.getString(R.string.receipt_snap_begin_balance) + " " +
                            it.toDecimalFormat(symbol = Symbol(type = Type.CURRENCY))
                )
            }
            /*data.snapBeginBal?.let {
                repo.addText(
                    context.getString(R.string.receipt_snap_begin_balance) + " " +
                            it.toDecimalFormat(symbol = Symbol(type = Type.CURRENCY))
                )
            }*/

            /* SNAP PURCHASE (VOIDED) */
            repo.addText(
                context.getString(R.string.receipt_snap_purchase) + " " +
                        "-" + data.txnAmount?.toDecimalFormat(symbol = Symbol(type = Type.CURRENCY)) +
                        "  VOIDED"
            )

            /* DOT LINE */
            repo.addText(context.getString(R.string.summary_dot_line),
                format = PrintFormat().align(Align.CENTER))

            /* SNAP END BALANCE */
            val voidEndbal = voidBeginBal?.plus(data.txnAmount!!)
            Log.d("VOID PRINT_RECEIPT", "Void End Bal: ${voidEndbal}")
            voidEndbal.let {
                repo.addText(
                    context.getString(R.string.receipt_snap_end_balance) + " " +
                            it.toDecimalFormat(symbol = Symbol(type = Type.CURRENCY))
                )
            }
            /*repo.addText(
                context.getString(R.string.receipt_snap_end_balance) + " " +
                        data.snapEndBalance?.toDecimalFormat(symbol = Symbol(type = Type.CURRENCY))
            )*/

            //repo.addText(context.getString(R.string.receipt_gray_line))
        }
        if (!isBalanceInquiry && isReturn && isDeclined) {
            repo.addText(
                context.getString(R.string.receipt_amount) + " " +
                data.txnAmount?.toDecimalFormat(symbol = Symbol(type = Type.CURRENCY))
            )
        }

        /* =========================
           🔹 BALANCE SECTION
           ========================= */

        if (isBalanceInquiry || isCashBalanceInquiry) {

            repo.addText(context.getString(R.string.receipt_snap_balance)+ " " +
                 data.snapEndBalance?.toDecimalFormat(symbol = Symbol(type = Type.CURRENCY)))
            repo.addText(context.getString(R.string.receipt_cash_balance)+ " " +
                data.cashEndBalance?.toDecimalFormat(symbol = Symbol(type = Type.CURRENCY)))

        } else {

            /* SNAP */
            if (isSnapPurchase || isReturn) {
                if(isSnapPurchase) {
                    var beginBal = data.snapEndBalance?.plus(data.txnAmount!!)
                    beginBal.let {
                        repo.addText(
                            context.getString(R.string.receipt_snap_begin_balance) + " " +
                                    it.toDecimalFormat(symbol = Symbol(type = Type.CURRENCY))
                        )
                    }
                }else{
                    var beginBal = data.snapEndBalance?.minus(data.txnAmount!!)
                    beginBal.let {
                        repo.addText(
                            context.getString(R.string.receipt_snap_begin_balance) + " " +
                                    it.toDecimalFormat(symbol = Symbol(type = Type.CURRENCY))
                        )
                    }
                }

                if(isSnapPurchase) {
                    repo.addText(
                        context.getString(R.string.receipt_snap_purchase) + " " +
                                "-" + data.txnAmount?.toDecimalFormat(symbol = Symbol(type = Type.CURRENCY))
                    )
                }else{
                    repo.addText(
                        context.getString(R.string.receipt_snap_purchase) + " " +
                                data.txnAmount?.toDecimalFormat(symbol = Symbol(type = Type.CURRENCY))
                    )
                }

                /* Add Line */
                repo.addText(context.getString(R.string.summary_dot_line),
                    format = PrintFormat().fontSize(FontSize.MEDIUM).align(Align.CENTER),)

                if(isSnapPurchase){
                    repo.addText(
                        context.getString(R.string.receipt_snap_end_balance) + " " +
                                data.snapEndBalance?.toDecimalFormat(symbol = Symbol(type = Type.CURRENCY))
                    )
                }else {
                    var endBal = data.snapEndBalance
                    endBal.let {
                        repo.addText(
                            context.getString(R.string.receipt_snap_end_balance) + " " +
                                    it.toDecimalFormat(symbol = Symbol(type = Type.CURRENCY))
                        )
                    }
                }
                repo.addText(context.getString(R.string.receipt_cash_balance)+ " " +
                    data.cashEndBalance?.toDecimalFormat(symbol = Symbol(type = Type.CURRENCY)))
            }

            /* CASH */
            if (isCashPurchase || isCashback) {
                val cshbeginBal = if (!isCashback) {
                    data.cashEndBalance?.plus(data.txnAmount!!)
                } else {
                    data.cashEndBalance?.plus(data.txnAmount!!)?.plus(data.cashback!!)
                }
                cshbeginBal?.let {
                    repo.addText(context.getString(R.string.receipt_cash_begin_balance)+ " " + it.toDecimalFormat(symbol = Symbol(type = Type.CURRENCY)))
                }

                repo.addText(
                    context.getString(R.string.receipt_cash_purchase) + " " +
                    "-" + data.txnAmount?.toDecimalFormat(symbol = Symbol(type = Type.CURRENCY))
                )
                if(isCashPurchase)
                    repo.addText(context.getString(R.string.summary_dot_line),
                        format = PrintFormat().fontSize(FontSize.MEDIUM).align(Align.CENTER))

                if (isCashback) {
                    repo.addText(context.getString(R.string.receipt_cash_back)+ " " + data.cashback?.toDecimalFormat(symbol = Symbol(type = Type.CURRENCY)))

                    /*repo.addText(
                        "Total Deduction",
                        ((data.txnAmount ?: 0.0) + (data.cashback ?: 0.0))
                            .toDecimalFormat(symbol = Symbol(type = Type.CURRENCY))
                    )*/
                    /* Add Line */
                    repo.addText(context.getString(R.string.summary_dot_line),
                        format = PrintFormat().fontSize(FontSize.MEDIUM).align(Align.CENTER))
                }

                repo.addText(context.getString(R.string.receipt_cash_end_balance)+ " " + data.cashEndBalance?.toDecimalFormat(symbol = Symbol(type = Type.CURRENCY)))

                repo.addText(context.getString(R.string.receipt_snap_balance)+ " " +
                        data.snapEndBalance?.toDecimalFormat(symbol = Symbol(type = Type.CURRENCY)))
            }
        }
        /* Cash Withdrawal */
        if (isCashWithdrawal) {
            var beginBal = data.cashEndBalance?.plus(data.txnAmount!!)
            beginBal?.let {
                repo.addText(context.getString(R.string.receipt_cash_begin_balance)+ " " + it.toDecimalFormat(symbol = Symbol(type = Type.CURRENCY)))
            }

            repo.addText(
                context.getString(R.string.receipt_cash_withdrawal)+ " " +
                        data.txnAmount?.toDecimalFormat(symbol = Symbol(type = Type.CURRENCY)))

            /* Add Line */
            repo.addText(context.getString(R.string.summary_dot_line),
                format = PrintFormat().fontSize(FontSize.MEDIUM).align(Align.CENTER),)

            repo.addText(
                context.getString(R.string.receipt_cash_end_balance) + " " +
                        data.cashEndBalance?.toDecimalFormat(symbol = Symbol(type = Type.CURRENCY)))


            repo.addText(context.getString(R.string.receipt_snap_balance)+ " " +
                    data.snapEndBalance?.toDecimalFormat(symbol = Symbol(type = Type.CURRENCY)))

        }
        /* Add Line */
        repo.addText(context.getString(R.string.receipt_gray_line),
            format = PrintFormat().fontSize(FontSize.MEDIUM).align(Align.LEFT),)

        /* =========================
           🔹 RESULT SECTION
           ========================= */


        if(isDeclined)
            repo.addText(
                context.getString(R.string.receipt_result) + " " +
                        txnStatusStr +
                        (data.hostRespCode?.let { " - $it" } ?: "")
            )
        else
            repo.addText(context.getString(R.string.receipt_result)+ " " + txnStatusStr)


        if (!isReturn && !isBalanceInquiry) {
            repo.addText(context.getString(R.string.receipt_auth)+ " " + data.hostAuthCode)
        }

        //repo.addText(context.getString(R.string.receipt_trace_no)+ " " + "${data.stan}-${data.rrn}")
        val trace = when {
            !data.stan.isNullOrEmpty() && !data.rrn.isNullOrEmpty() -> "${data.stan}-${data.rrn}"
            !data.stan.isNullOrEmpty() -> data.stan
            !data.rrn.isNullOrEmpty() -> data.rrn
            else -> null
        }

        trace?.let {
            repo.addText(context.getString(R.string.receipt_trace_no) + " $it")
        }

        repo.feedLine()

        /* =========================
           🔹 ACTION TEXT (CRITICAL)
           ========================= */

        when {
            isReturn && isApproved ->
                repo.addText(context.getString(R.string.receipt_accept_goods),
                    format = PrintFormat().style(Style.REVERSE).align(Align.LEFT)
                )

            isReturn && isDeclined ->
                repo.addText(context.getString(R.string.receipt_do_not_accept_goods),
                    format = PrintFormat().style(Style.REVERSE).align(Align.LEFT)
                )

            isApproved && !isBalanceInquiry ->
                repo.addText(context.getString(R.string.receipt_dispense_goods),
                    format = PrintFormat().style(Style.REVERSE).align(Align.LEFT)
                )

            isDeclined && !isBalanceInquiry ->
                repo.addText(context.getString(R.string.receipt_do_not_accept_goods),
                    format = PrintFormat().style(Style.REVERSE).align(Align.LEFT)
                )
        }

        /* Add Line */
        repo.addText(context.getString(R.string.receipt_gray_line),
            format = PrintFormat().fontSize(FontSize.MEDIUM).align(Align.LEFT),)

        /* =========================
           🔹 FOOTER
           ========================= */

        if (data.isDemoMode == true) {
            repo.addText(context.getString(R.string.receipt_train_mode),
                format =  PrintFormat().align(Align.CENTER))
        }

        if (isCustomer) {
            repo.addText(data.footer1,
                format = PrintFormat().align(Align.CENTER))
            repo.addText(data.footer2,
                format = PrintFormat().align(Align.CENTER))
            repo.addText(data.footer3,
                format = PrintFormat().align(Align.CENTER))
            repo.addText(data.footer4,
                format = PrintFormat().align(Align.CENTER))
            repo.addText(context.getString(R.string.receipt_custom_copy),
                format = PrintFormat().align(Align.CENTER))
        } else {
            repo.addText(context.getString(R.string.receipt_merch_copy),
                format = PrintFormat().align(Align.CENTER))
        }

        repo.feedLine()
        repo.print()
    }

}