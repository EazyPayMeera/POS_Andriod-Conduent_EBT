package com.eazypaytech.posafrica.rootUtils.miscellaneous

import android.content.Context
import android.util.Log
import com.eazypaytech.paymentservicecore.constants.AppConstants
import com.eazypaytech.paymentservicecore.listeners.responseListener.IPrinterServiceResponseListener
import com.eazypaytech.paymentservicecore.model.emv.PrinterServiceResult
import com.eazypaytech.posafrica.rootModel.ObjRootAppPaymentDetails
import com.eazypaytech.posafrica.rootUiScreens.dialogs.CustomDialogBuilder
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.PrinterServiceRepository
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.PrinterServiceRepository.PrintFormat
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.convertDateTime
import com.eazypaytech.posafrica.R
import com.eazypaytech.posafrica.rootModel.Symbol
import com.eazypaytech.posafrica.rootModel.Symbol.Type
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.PrinterServiceRepository.Align
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.PrinterServiceRepository.FontSize
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.PrinterServiceRepository.Style
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.convertReceiptDateTime
//import com.eazypaytech.posafrica.rootUtils.genericComposeUI.formatReceiptDateTime
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.getCardEntryStringId
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.getCurrentDateTime
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.getTxnStatusStringId
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.getTxnTypeStringId
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.toAmountFormat
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.toDecimalFormat

object PrinterUtils {

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

        val date = convertReceiptDateTime(data.dateTime, outputFormat = "MM/dd/yy")
        val time = convertReceiptDateTime(data.dateTime, outputFormat = "hh:mm:ssa")

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
            repo.addText(
                context.getString(R.string.receipt_settlement_date) + " " + data.settlementDate,
                format = PrintFormat().fontSize(FontSize.MEDIUM)
            )
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

        if (isVoid) {

           /* SNAP BEGIN BALANCE */
            data.snapBeginBal?.let {
                repo.addText(
                    context.getString(R.string.receipt_snap_begin_balance) + " " +
                            it.toDecimalFormat(symbol = Symbol(type = Type.CURRENCY))
                )
            }

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
            val voidEndbal = data.snapBeginBal?.plus(data.txnAmount!!)
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
                var cshbeginBal = data.cashEndBalance?.plus(data.txnAmount!!)
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
            repo.addText(context.getString(R.string.receipt_result)+ " " + txnStatusStr + " - "+ data.hostRespCode)
        else
            repo.addText(context.getString(R.string.receipt_result)+ " " + txnStatusStr)


        if (!isReturn && !isBalanceInquiry) {
            repo.addText(context.getString(R.string.receipt_auth)+ " " + data.hostAuthCode)
        }

        repo.addText(context.getString(R.string.receipt_trace_no)+ " " + "${data.stan}-${data.rrn}")

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

    fun printSummary(context: Context, listObjRootAppPaymentDetails: List<ObjRootAppPaymentDetails>?)
    {
        var _report = ReportBuilder(listObjRootAppPaymentDetails)
        PrinterServiceRepository().init(context, object : IPrinterServiceResponseListener {
            override fun onPrinterServiceResponse(response: Any) {
                when (response) {
                    is PrinterServiceResult.Result ->{
                        when(response.status) {
                            PrinterServiceResult.Status.PRINTING -> CustomDialogBuilder.composePrintingDialog(
                                title = context.resources.getString(R.string.printing),
                                subtitle = context.resources.getString(
                                        R.string.receipt_printing_summary
                                ),
                                message = context.resources.getString(R.string.plz_wait)
                            )
                            PrinterServiceResult.Status.INIT_FAILURE, PrinterServiceResult.Status.ERROR, PrinterServiceResult.Status.PRINT_FAILURE -> CustomDialogBuilder.composeAlertDialog(
                                title = context.resources.getString(R.string.printer_error_title),
                                message = context.resources.getString(R.string.printer_printing_failed)
                            )
                            PrinterServiceResult.Status.OUT_OF_PAPER -> CustomDialogBuilder.composeAlertDialog(
                                title = context.resources.getString(R.string.printer_error_title),
                                message = context.resources.getString(R.string.printer_out_of_paper)
                            )
                            else -> CustomDialogBuilder.hideProgress()
                        }
                    }
                }
            }
        })

            /* Headers */
            .addText(
                listObjRootAppPaymentDetails?.get(0)?.header1,
                format = PrintFormat().align(Align.CENTER).style(Style.BOLD))
            .addText(listObjRootAppPaymentDetails?.get(0)?.header2,
                format = PrintFormat().align(Align.CENTER).style(Style.BOLD))
            .addText(listObjRootAppPaymentDetails?.get(0)?.header3,
                format = PrintFormat().align(Align.CENTER).style(Style.BOLD))
            .addText(listObjRootAppPaymentDetails?.get(0)?.header4,
                format = PrintFormat().align(Align.CENTER).style(Style.BOLD))

            .feedLine()

            /* Summary Title */
            .addText(context.getString(R.string.receipt_printing_summary),
                format = PrintFormat().fontSize(FontSize.LARGE).align(Align.CENTER)
            )

            .feedLine()

            /* Date Time */
            .addText(convertDateTime(getCurrentDateTime(), outputFormat = AppConstants.DEFAULT_RECEIPT_DATE_FORMAT),
                convertDateTime(getCurrentDateTime(), outputFormat = AppConstants.DEFAULT_RECEIPT_TIME_FORMAT),
                format = PrintFormat().fontSize(FontSize.MEDIUM)
            )

            /* MID & TID */
            .addText(listObjRootAppPaymentDetails?.get(0)?.merchantId,
                listObjRootAppPaymentDetails?.get(0)?.procId,
                format = PrintFormat().fontSize(FontSize.MEDIUM)
            )

            /* Demo Mode Text */
            .addText(context.getString(R.string.receipt_gray_line),
                format = PrintFormat().fontSize(FontSize.MEDIUM).align(Align.CENTER),
                condition = listObjRootAppPaymentDetails?.get(0)?.isDemoMode == true)
            .addText(context.getString(R.string.receipt_train_mode),
                format = PrintFormat().fontSize(FontSize.MEDIUM).align(Align.CENTER),
                condition = listObjRootAppPaymentDetails?.get(0)?.isDemoMode == true)
            .addText(context.getString(R.string.receipt_gray_line),
                format = PrintFormat().fontSize(FontSize.MEDIUM).align(Align.CENTER),
                condition = listObjRootAppPaymentDetails?.get(0)?.isDemoMode == true)

            /* Transaction Counts & Totals */
            .addText(context.getString(R.string.receipt_gray_line),
                format = PrintFormat().fontSize(FontSize.MEDIUM).align(Align.CENTER)
            )

            .addText(context.getString(R.string.summary_purchase),"x"+_report.getPurchaseCount(), _report.getPurchaseTotal().toAmountFormat(),
                format = PrintFormat().fontSize(FontSize.MEDIUM)
            )
            .addText(" " + context.getString(R.string.summary_subtotal), "x"+_report.getPurchaseCount(), _report.getSubTotal().toAmountFormat(),
                format = PrintFormat().fontSize(FontSize.MEDIUM),
                condition = _report.getSubTotal()!=_report.getPurchaseTotal()
            )
            .addText(" " + context.getString(R.string.summary_vat),"x"+_report.getVATCount(), _report.getVAT().toAmountFormat(),
                format = PrintFormat().fontSize(FontSize.MEDIUM),
                condition = _report.getVAT()>0.00
            )
            .addText(" " + context.getString(R.string.summary_tip),"x"+_report.getTipCount(), _report.getTip().toAmountFormat(),
                format = PrintFormat().fontSize(FontSize.MEDIUM),
                condition = _report.getTip()>0.00
            )
            .addText(" " + context.getString(R.string.summary_service_charge),"x"+_report.getServiceChargeCount(), _report.getServiceCharge().toAmountFormat(),
                format = PrintFormat().fontSize(FontSize.MEDIUM),
                condition = _report.getServiceCharge()>0.00
            )
            .feedLine()

            .addText(context.getString(R.string.summary_refund), "x"+_report.getRefundCount(), _report.getRefundTotal().toAmountFormat(),
                format = PrintFormat().fontSize(FontSize.MEDIUM),
                condition = _report.getRefundTotal()>0.00
            )

            .addText(context.getString(R.string.receipt_gray_line),
                format = PrintFormat().fontSize(FontSize.MEDIUM).align(Align.CENTER)
            )

            .addText(context.getString(R.string.summary_total), _report.getNetTotal().toAmountFormat(),
                format = PrintFormat().fontSize(FontSize.MEDIUM)
            )


            .feedLine()
            .print()
    }

    fun printDetailed(context: Context, listObjRootAppPaymentDetails: List<ObjRootAppPaymentDetails>?)
    {
        var _report = ReportBuilder(listObjRootAppPaymentDetails)
        PrinterServiceRepository().init(context, object : IPrinterServiceResponseListener {
            override fun onPrinterServiceResponse(response: Any) {
                when (response) {
                    is PrinterServiceResult.Result ->{
                        when(response.status) {
                            PrinterServiceResult.Status.PRINTING -> CustomDialogBuilder.composePrintingDialog(
                                title = context.resources.getString(R.string.printing),
                                subtitle = context.resources.getString(
                                    R.string.receipt_printing_detail
                                ),
                                message = context.resources.getString(R.string.plz_wait)
                            )
                            PrinterServiceResult.Status.INIT_FAILURE, PrinterServiceResult.Status.ERROR, PrinterServiceResult.Status.PRINT_FAILURE -> CustomDialogBuilder.composeAlertDialog(
                                title = context.resources.getString(R.string.printer_error_title),
                                message = context.resources.getString(R.string.printer_printing_failed)
                            )
                            PrinterServiceResult.Status.OUT_OF_PAPER -> CustomDialogBuilder.composeAlertDialog(
                                title = context.resources.getString(R.string.printer_error_title),
                                message = context.resources.getString(R.string.printer_out_of_paper)
                            )
                            else -> CustomDialogBuilder.hideProgress()
                        }
                    }
                }
            }
        })

            /* Headers */
            .addText(
                listObjRootAppPaymentDetails?.get(0)?.header1,
                format = PrintFormat().align(Align.CENTER).style(Style.BOLD))
            .addText(listObjRootAppPaymentDetails?.get(0)?.header2,
                format = PrintFormat().align(Align.CENTER).style(Style.BOLD))
            .addText(listObjRootAppPaymentDetails?.get(0)?.header3,
                format = PrintFormat().align(Align.CENTER).style(Style.BOLD))
            .addText(listObjRootAppPaymentDetails?.get(0)?.header4,
                format = PrintFormat().align(Align.CENTER).style(Style.BOLD))

            .feedLine()

            /* Detail Title */
            .addText(context.getString(R.string.receipt_printing_detail),
                format = PrintFormat().fontSize(FontSize.LARGE).align(Align.CENTER)
            )

            .feedLine()

            /* Date Time */
            .addText(convertDateTime(getCurrentDateTime(), outputFormat = AppConstants.DEFAULT_RECEIPT_DATE_FORMAT),
                convertDateTime(getCurrentDateTime(), outputFormat = AppConstants.DEFAULT_RECEIPT_TIME_FORMAT),
                format = PrintFormat().fontSize(FontSize.MEDIUM)
            )

            /* MID & TID */
            .addText(listObjRootAppPaymentDetails?.get(0)?.merchantId,
                listObjRootAppPaymentDetails?.get(0)?.procId,
                format = PrintFormat().fontSize(FontSize.MEDIUM)
            )

            /* Demo Mode Text */
            .addText(context.getString(R.string.receipt_gray_line),
                format = PrintFormat().fontSize(FontSize.MEDIUM).align(Align.CENTER),
                condition = listObjRootAppPaymentDetails?.get(0)?.isDemoMode == true)
            .addText(context.getString(R.string.receipt_train_mode),
                format = PrintFormat().fontSize(FontSize.MEDIUM).align(Align.CENTER),
                condition = listObjRootAppPaymentDetails?.get(0)?.isDemoMode == true)
            .addText(context.getString(R.string.receipt_gray_line),
                format = PrintFormat().fontSize(FontSize.MEDIUM).align(Align.CENTER),
                condition = listObjRootAppPaymentDetails?.get(0)?.isDemoMode == true)

            /* Transaction Counts & Totals */
            .addText(context.getString(R.string.receipt_gray_line),
                format = PrintFormat().fontSize(FontSize.MEDIUM).align(Align.CENTER)
            )

            .addText(context.getString(R.string.summary_purchase),"x"+_report.getPurchaseCount(), _report.getPurchaseTotal().toAmountFormat(),
                format = PrintFormat().fontSize(FontSize.MEDIUM)
            )
            .addText(" " + context.getString(R.string.summary_subtotal), "x"+_report.getPurchaseCount(), _report.getSubTotal().toAmountFormat(),
                format = PrintFormat().fontSize(FontSize.MEDIUM),
                condition = _report.getSubTotal()!=_report.getPurchaseTotal()
            )
            .addText(" " + context.getString(R.string.summary_vat),"x"+_report.getVATCount(), _report.getVAT().toAmountFormat(),
                format = PrintFormat().fontSize(FontSize.MEDIUM),
                condition = _report.getVAT()>0.00
            )
            .addText(" " + context.getString(R.string.summary_tip),"x"+_report.getTipCount(), _report.getTip().toAmountFormat(),
                format = PrintFormat().fontSize(FontSize.MEDIUM),
                condition = _report.getTip()>0.00
            )
            .addText(" " + context.getString(R.string.summary_service_charge),"x"+_report.getServiceChargeCount(), _report.getServiceCharge().toAmountFormat(),
                format = PrintFormat().fontSize(FontSize.MEDIUM),
                condition = _report.getServiceCharge()>0.00
            )
            .feedLine()

            .addText(context.getString(R.string.summary_refund), "x"+_report.getRefundCount(), _report.getRefundTotal().toAmountFormat(),
                format = PrintFormat().fontSize(FontSize.MEDIUM),
                condition = _report.getRefundTotal()>0.00
            )

            .addText(context.getString(R.string.receipt_gray_line),
                format = PrintFormat().fontSize(FontSize.MEDIUM).align(Align.CENTER)
            )

            .addText(context.getString(R.string.summary_total), _report.getNetTotal().toAmountFormat(),
                format = PrintFormat().fontSize(FontSize.MEDIUM)
            )

            .addText(context.getString(R.string.receipt_gray_line),
                format = PrintFormat().fontSize(FontSize.MEDIUM).align(Align.CENTER)
            )


            .feedLine()

            .addText(context.getString(R.string.detail_txn_details),
                format = PrintFormat().fontSize(FontSize.LARGE).align(Align.CENTER)
            )

            /* Headers for Transaction Record */
            .addText(context.getString(R.string.receipt_gray_line),
                format = PrintFormat().fontSize(FontSize.MEDIUM).align(Align.CENTER)
            )
            /* Transaction Type, Card Brand Last4 & Transaction Status */
            .addText(context.getString(R.string.detail_txn_Type), context.getString(R.string.detail_card), context.getString(R.string.detail_txn_Status),
                format = PrintFormat().fontSize(FontSize.SMALL)
            )
            /* Txn Ref Number, Invoice Number & Auth Code */
            .addText(context.getString(R.string.detail_txn_ref), context.getString(R.string.detail_invoice_no), context.getString(R.string.detail_auth_code),
                format = PrintFormat().fontSize(FontSize.SMALL)
            )
            /* TIP, Service Charge, VAT */
            .addText(context.getString(R.string.detail_tip), context.getString(R.string.detail_service_charge),context.getString(R.string.detail_vat),
                format = PrintFormat().fontSize(FontSize.SMALL)
            )
            /* Txn Amount & Total Amount */
            .addText(context.getString(R.string.detail_txn_amt), context.getString(R.string.detail_total_amt),
                format = PrintFormat().fontSize(FontSize.SMALL)
            )
            .addText(context.getString(R.string.receipt_gray_line),
                format = PrintFormat().fontSize(FontSize.MEDIUM).align(Align.CENTER)
            ).apply {

                for (item in listObjRootAppPaymentDetails!!) {
                    /* Transaction Type, Card Brand Last4 & Transaction Status */
                    addText(
                        item.txnType.toString(), (item.cardBrand?.take(6)?.plus(" ")?:"").plus(item.cardMaskedPan?.takeLast(4)?:"-"), item.txnStatus.toString(),
                        format = PrintFormat().fontSize(FontSize.SMALL)
                    )
                        /* Txn Ref Number, Invoice Number & Auth Code */
                        .addText(
                            item.hostTxnRef?:"-", item.invoiceNo.toString(), item.hostAuthCode?:"-",
                            format = PrintFormat().fontSize(FontSize.SMALL)
                        )
                        /* TIP, Service Charge & VAT */
                        .addText(
                            item.tip.toAmountFormat(), item.serviceCharge.toAmountFormat(), item.VAT.toAmountFormat(),
                            format = PrintFormat().fontSize(FontSize.SMALL),
                            condition = (item.tip?:0.00)>0.00 || (item.serviceCharge?:0.00)>0.00 || (item.VAT?:0.00)>0.00
                        )
                        /* Txn Amount & Total Amount */
                        .addText(
                            item.txnAmount.toAmountFormat(), item.ttlAmount.toAmountFormat(),
                            format = PrintFormat().fontSize(FontSize.SMALL)
                        )
                        .addText(context.getString(R.string.receipt_gray_line),
                            format = PrintFormat().fontSize(FontSize.MEDIUM).align(Align.CENTER)
                        )
                }
            }
            .feedLine()
            .print()
    }
}