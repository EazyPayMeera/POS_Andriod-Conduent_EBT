package com.eazypaytech.posafrica.rootUtils.miscellaneous

import android.content.Context
import com.eazypaytech.paymentservicecore.constants.AppConstants
import com.eazypaytech.paymentservicecore.listeners.responseListener.IPrinterServiceResponseListener
import com.eazypaytech.paymentservicecore.model.emv.PrinterServiceResult
import com.eazypaytech.paymentservicecore.models.toEmvTransType
import com.eazypaytech.posafrica.rootModel.ObjRootAppPaymentDetails
import com.eazypaytech.posafrica.rootUiScreens.dialogs.CustomDialogBuilder
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.PrinterServiceRepository
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.PrinterServiceRepository.PrintFormat
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.convertDateTime
import com.eazypaytech.posafrica.R
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.PrinterServiceRepository.Align
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.PrinterServiceRepository.FontSize
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.PrinterServiceRepository.Style

object PrinterUtils {

    fun printReceipt(context: Context, objRootAppPaymentDetails: ObjRootAppPaymentDetails, isCustomer : Boolean = false)
    {
        PrinterServiceRepository().init(context, object : IPrinterServiceResponseListener {
              override fun onPrinterServiceResponse(response: Any) {
                  when (response) {
                      is PrinterServiceResult.Result ->{
                          when(response.status)
                          {
                              PrinterServiceResult.Status.PRINTING-> CustomDialogBuilder.composePrintingDialog()
                              else->CustomDialogBuilder.hideProgress()
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

            /* Transaction Type */
            .addText(objRootAppPaymentDetails.txnType.toString(),
                format = PrintFormat().align(Align.CENTER).fontSize(FontSize.LARGE).style(Style.BOLD)
            )

            /* Transaction Status */
            .addText(context.getString(R.string.receipt_txn_status), objRootAppPaymentDetails.txnStatus.toString(),
                format = PrintFormat().align(Align.CENTER).fontSize(FontSize.LARGE).style(Style.BOLD)
            )

            .feedLine()

            /* Card Brand & Number */
            .addText(context.getString(R.string.receipt_card_no),objRootAppPaymentDetails.cardBrand.toString() + " " + objRootAppPaymentDetails.cardMaskedPan,
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

            /*  Footers */
            .addText(objRootAppPaymentDetails.footer1,
                format = PrintFormat().align(Align.CENTER))
            .addText(objRootAppPaymentDetails.footer2,
                format = PrintFormat().align(Align.CENTER))
            .addText(objRootAppPaymentDetails.footer3,
                format = PrintFormat().align(Align.CENTER))
            .addText(objRootAppPaymentDetails.footer4,
                format = PrintFormat().align(Align.CENTER))
            .feedLine(2)
            .print()
    }
}