package com.eazypaytech.posafrica.rootUtils.miscellaneous

import android.content.Context
import androidx.compose.ui.res.stringResource
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
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.getCardEntryStringId
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.getCurrentDateTime
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.getTxnStatusStringId
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.getTxnTypeStringId
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.toAmountFormat
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.toDecimalFormat

object PrinterUtils {

    fun printReceipt(context: Context, objRootAppPaymentDetails: ObjRootAppPaymentDetails, isCustomer : Boolean = false)
    {
        PrinterServiceRepository().init(context, object : IPrinterServiceResponseListener {
              override fun onPrinterServiceResponse(response: Any) {
                  when (response) {
                      is PrinterServiceResult.Result ->{
                          when(response.status) {
                              PrinterServiceResult.Status.PRINTING -> CustomDialogBuilder.composePrintingDialog(
                                  title = context.resources.getString(R.string.printing),
                                  subtitle = context.resources.getString(
                                      if (isCustomer)
                                          R.string.receipt_printing_customer
                                      else
                                          R.string.receipt_printing_merchant
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
            .addText(objRootAppPaymentDetails.header1,
                format = PrintFormat().align(Align.CENTER).style(Style.BOLD))
            .addText(objRootAppPaymentDetails.header2,
                format = PrintFormat().align(Align.CENTER).style(Style.BOLD))
            .addText(objRootAppPaymentDetails.header3,
                format = PrintFormat().align(Align.CENTER).style(Style.BOLD))
            .addText(objRootAppPaymentDetails.header4,
                format = PrintFormat().align(Align.CENTER).style(Style.BOLD))

            .feedLine(1)

            /* Date Time */
            .addText(context.getString(R.string.receipt_date)+convertDateTime(objRootAppPaymentDetails.dateTime, outputFormat = AppConstants.DEFAULT_RECEIPT_DATE_FORMAT),
                context.getString(R.string.receipt_time) + convertDateTime(objRootAppPaymentDetails.dateTime, outputFormat = AppConstants.DEFAULT_RECEIPT_TIME_FORMAT),
                format = PrintFormat().fontSize(FontSize.SMALL)
            )

            /* MID & TID */
            .addText(context.getString(R.string.receipt_merchant_id)+objRootAppPaymentDetails.merchantId,
                context.getString(R.string.receipt_terminal_id)+objRootAppPaymentDetails.terminalId,
                format = PrintFormat().fontSize(FontSize.SMALL)
            )

            /* Batch & Invoice No */
            .addText(context.getString(R.string.receipt_batch_no)+objRootAppPaymentDetails.batchId,
                context.getString(R.string.receipt_invoice_no)+objRootAppPaymentDetails.invoiceNo,
                format = PrintFormat().fontSize(FontSize.SMALL)
            )

            /* Demo Mode Text */
            .addText(context.getString(R.string.receipt_gray_line),
                format = PrintFormat().fontSize(FontSize.MEDIUM).align(Align.CENTER),
                condition = objRootAppPaymentDetails.isDemoMode == true)
            .addText(context.getString(R.string.receipt_train_mode),
                format = PrintFormat().fontSize(FontSize.MEDIUM).align(Align.CENTER),
                condition = objRootAppPaymentDetails.isDemoMode == true)
            .addText(context.getString(R.string.receipt_gray_line),
                format = PrintFormat().fontSize(FontSize.MEDIUM).align(Align.CENTER),
                condition = objRootAppPaymentDetails.isDemoMode == true)

            /* Transaction Type & Transaction Status */
            .addText(context.getString(getTxnTypeStringId(objRootAppPaymentDetails.txnType)),
                context.getString(getTxnStatusStringId(objRootAppPaymentDetails.txnStatus)),
                format = PrintFormat().fontSize(FontSize.LARGE).style(Style.BOLD)
            )

            /* Card Brand & Number */
            .addText(context.getString(R.string.receipt_card_no),objRootAppPaymentDetails.cardBrand.toString() + " " + objRootAppPaymentDetails.cardMaskedPan,
                format = PrintFormat().fontSize(FontSize.SMALL)
            )

            /* Card Entry Mode */
            .addText(context.getString(R.string.receipt_card_entry_mode), context.getString(getCardEntryStringId(objRootAppPaymentDetails.cardEntryMode)),
                format = PrintFormat().fontSize(FontSize.SMALL)
            )

            /* Auth Code */
            .addText(context.getString(R.string.receipt_auth_code),objRootAppPaymentDetails.hostAuthCode,
                format = PrintFormat().fontSize(FontSize.SMALL),
                condition = objRootAppPaymentDetails.hostAuthCode != null && objRootAppPaymentDetails.hostTxnRef != null
            )

            /* Transaction Reference Number */
            .addText(context.getString(R.string.receipt_ref_no),objRootAppPaymentDetails.hostTxnRef,
                format = PrintFormat().fontSize(FontSize.SMALL),
                condition = objRootAppPaymentDetails.hostAuthCode != null && objRootAppPaymentDetails.hostTxnRef != null
            )

            /* Sub Total & Other Amounts */
            .addText(context.getString(R.string.receipt_subtotal),objRootAppPaymentDetails.txnAmount?.toDecimalFormat(symbol = Symbol(type = Type.CURRENCY)),
                format = PrintFormat().fontSize(FontSize.MEDIUM)
            )
            .addText(context.getString(R.string.receipt_tip),objRootAppPaymentDetails.tip?.toDecimalFormat(symbol = Symbol(type = Type.CURRENCY)),
                format = PrintFormat().fontSize(FontSize.MEDIUM),
                condition = (objRootAppPaymentDetails.tip?:0.00)>0.00
            )
            .addText(context.getString(R.string.receipt_service_charge),objRootAppPaymentDetails.serviceCharge?.toDecimalFormat(symbol = Symbol(type = Type.CURRENCY)),
                format = PrintFormat().fontSize(FontSize.MEDIUM),
                condition = (objRootAppPaymentDetails.serviceCharge?:0.00)>0.00
            )
            .addText(context.getString(R.string.receipt_vat),objRootAppPaymentDetails.VAT?.toDecimalFormat(symbol = Symbol(type = Type.CURRENCY)),
                format = PrintFormat().fontSize(FontSize.MEDIUM),
                condition = (objRootAppPaymentDetails.VAT?:0.00)>0.00
            )

            /* Total Amount */
            .addText(context.getString(R.string.receipt_gray_line),
                format = PrintFormat().fontSize(FontSize.MEDIUM).align(Align.CENTER)
            )
            .addText(context.getString(R.string.receipt_total),objRootAppPaymentDetails.ttlAmount?.toDecimalFormat(symbol = Symbol(type = Type.CURRENCY)),
                format = PrintFormat().fontSize(FontSize.MEDIUM)
            )
            .addText(context.getString(R.string.receipt_gray_line),
                format = PrintFormat().fontSize(FontSize.MEDIUM).align(Align.CENTER)
            )

            /* Customer Note */
            .addText(context.getString(R.string.receipt_note),
                format = PrintFormat().fontSize(FontSize.SMALL).align(Align.CENTER)
            )

            .feedLine()

            /*  Footers */
            .addText(objRootAppPaymentDetails.footer1,
                format = PrintFormat().fontSize(FontSize.SMALL).align(Align.CENTER),
                condition = isCustomer
            )
            .addText(objRootAppPaymentDetails.footer2,
                format = PrintFormat().fontSize(FontSize.SMALL).align(Align.CENTER),
                condition = isCustomer)
            .addText(objRootAppPaymentDetails.footer3,
                format = PrintFormat().fontSize(FontSize.SMALL).align(Align.CENTER),
                condition = isCustomer)
            .addText(objRootAppPaymentDetails.footer4,
                format = PrintFormat().fontSize(FontSize.SMALL).align(Align.CENTER),
                condition = isCustomer)
            .feedLine(condition = isCustomer)

            /* Customer Copy / Merchant Copy */
            .addText(context.getString(R.string.receipt_custom_copy),
                format = PrintFormat().fontSize(FontSize.SMALL).align(Align.CENTER),
                condition = isCustomer
            )
            .addText(context.getString(R.string.receipt_merch_copy),
                format = PrintFormat().fontSize(FontSize.SMALL).align(Align.CENTER),
                condition = !isCustomer
            )
            .feedLine()
            .print()
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
                listObjRootAppPaymentDetails?.get(0)?.terminalId,
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
                listObjRootAppPaymentDetails?.get(0)?.terminalId,
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