package com.eazypaytech.builder_core.utils

import com.eazypaytech.builder_core.model.BuilderServiceTxnDetails
import com.eazypaytech.builder_core.model.printer.PrinterRequest
import javax.inject.Inject

class PrinterRequestBuilder  @Inject constructor(){

    fun createPrinterRequest(builderServiceTxnDetails: BuilderServiceTxnDetails?): PrinterRequest {
        return PrinterRequest(
            merchantId = builderServiceTxnDetails?.merchantId,
            terminalId = builderServiceTxnDetails?.terminalId,
            cashierId = builderServiceTxnDetails?.loginId,
            deviceSN = builderServiceTxnDetails?.deviceSN,
            deviceMake = builderServiceTxnDetails?.deviceMake,
            deviceModel = builderServiceTxnDetails?.deviceModel,


            batchId = builderServiceTxnDetails?.batchId,
            invoiceNo = builderServiceTxnDetails?.invoiceNo,
            purchaseOrderNo = builderServiceTxnDetails?.purchaseOrderNo,
            dateTime = builderServiceTxnDetails?.dateTime,
            timeZone = builderServiceTxnDetails?.timeZone,
            txnType = builderServiceTxnDetails?.txnType,
            accountType = builderServiceTxnDetails?.accountType,
            txnCurrencyCode = builderServiceTxnDetails?.txnCurrencyCode,
            txnAmount = builderServiceTxnDetails?.authAmount,
            tip = builderServiceTxnDetails?.tip,
            cashback = builderServiceTxnDetails?.cashback,
            VAT = builderServiceTxnDetails?.VAT,
            SGST = builderServiceTxnDetails?.SGST,
            ttlAmount = builderServiceTxnDetails?.ttlAmount,

            )
    }
}