package com.analogics.builder_core.utils

import com.analogics.builder_core.model.PaymentServiceTxnDetails
import com.analogics.builder_core.model.printer.PrinterRequest
import javax.inject.Inject

class PrinterRequestBuilder  @Inject constructor(){

    fun createPrinterRequest(paymentServiceTxnDetails: PaymentServiceTxnDetails?): PrinterRequest {
        return PrinterRequest(
            merchantId = paymentServiceTxnDetails?.merchantId,
            terminalId = paymentServiceTxnDetails?.terminalId,
            cashierId = paymentServiceTxnDetails?.loginId,
            deviceSN = paymentServiceTxnDetails?.deviceSN,
            deviceMake = paymentServiceTxnDetails?.deviceMake,
            deviceModel = paymentServiceTxnDetails?.deviceModel,


            batchId = paymentServiceTxnDetails?.batchId,
            invoiceNo = paymentServiceTxnDetails?.invoiceNo,
            purchaseOrderNo = paymentServiceTxnDetails?.purchaseOrderNo,
            dateTime = paymentServiceTxnDetails?.dateTime,
            timeZone = paymentServiceTxnDetails?.timeZone,
            txnType = paymentServiceTxnDetails?.txnType,
            accountType = paymentServiceTxnDetails?.accountType,
            txnCurrencyCode = paymentServiceTxnDetails?.txnCurrencyCode,
            txnAmount = paymentServiceTxnDetails?.authAmount,
            tip = paymentServiceTxnDetails?.tip,
            cashback = paymentServiceTxnDetails?.cashback,
            CGST = paymentServiceTxnDetails?.CGST,
            SGST = paymentServiceTxnDetails?.SGST,
            ttlAmount = paymentServiceTxnDetails?.ttlAmount,

            )
    }
}