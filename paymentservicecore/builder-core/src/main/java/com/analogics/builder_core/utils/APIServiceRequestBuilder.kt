package com.analogics.builder_core.utils

import com.analogics.builder_core.model.PaymentServiceTxnDetails
import com.analogics.builder_core.model.auth_capture.PostAuthRequest
import com.analogics.builder_core.model.auth_capture.PreAuthRequest
import com.analogics.builder_core.model.login.UserLoginRequest
import com.analogics.builder_core.model.purchase.PurchaseRequest
import com.analogics.builder_core.model.reund.RefundRequest
import com.analogics.builder_core.model.reversal.ReversalReqeust
import com.analogics.builder_core.model.void.VoidReqeust
import javax.inject.Inject

class APIServiceRequestBuilder @Inject constructor() {

    fun createLoginRequest(paymentServiceTxnDetails: PaymentServiceTxnDetails?): UserLoginRequest {
        return UserLoginRequest(
            merchantId = paymentServiceTxnDetails?.merchantId,
            terminalId = paymentServiceTxnDetails?.terminalId,
            loginId = paymentServiceTxnDetails?.loginId,
            loginPassword = paymentServiceTxnDetails?.loginPassword,
            deviceSN = paymentServiceTxnDetails?.deviceSN,
            deviceMake = paymentServiceTxnDetails?.deviceMake,
            deviceModel = paymentServiceTxnDetails?.deviceModel
        )
    }

    fun createPurchaseRequest(paymentServiceTxnDetails: PaymentServiceTxnDetails?): PurchaseRequest {
        return PurchaseRequest(
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

    fun createVoidRequest(paymentServiceTxnDetails: PaymentServiceTxnDetails?): VoidReqeust {
        return VoidReqeust(
            merchantId = paymentServiceTxnDetails?.merchantId,
            terminalId = paymentServiceTxnDetails?.terminalId,
            cashierId = paymentServiceTxnDetails?.loginId,
            deviceSN = paymentServiceTxnDetails?.deviceSN,
            deviceMake = paymentServiceTxnDetails?.deviceMake,
            deviceModel = paymentServiceTxnDetails?.deviceModel,

            batchId = paymentServiceTxnDetails?.batchId,
            dateTime = paymentServiceTxnDetails?.dateTime,
            timeZone = paymentServiceTxnDetails?.timeZone,
            txnType = paymentServiceTxnDetails?.txnType,
            txnCurrencyCode = paymentServiceTxnDetails?.cardCountryCode,

            originalTxnType = paymentServiceTxnDetails?.originalTxnType,
            originalTxnAmount = paymentServiceTxnDetails?.originalTxnAmount,
            originalTip = paymentServiceTxnDetails?.originalTip,
            originalCashback = paymentServiceTxnDetails?.originalCashback,
            originalCGST = paymentServiceTxnDetails?.originalCGST,
            originalSGST = paymentServiceTxnDetails?.originalSGST,
            originalTtlAmount = paymentServiceTxnDetails?.originalTtlAmount,
            originalTxnRef = paymentServiceTxnDetails?.originalTxnRef,
            originalHostTxnRef = paymentServiceTxnDetails?.originalHostTxnRef
        )
    }

    fun createRefundRequest(paymentServiceTxnDetails: PaymentServiceTxnDetails?): RefundRequest {
        return RefundRequest(

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
            txnAmount = paymentServiceTxnDetails?.txnAmount,


            cardEntryMode = paymentServiceTxnDetails?.cardEntryMode,
            cardMaskedPan = paymentServiceTxnDetails?.cardMaskedPan,
            cardBrand = paymentServiceTxnDetails?.cardBrand,
            cardAuthMethod = paymentServiceTxnDetails?.cardAuthMethod,
            cardAuthResult = paymentServiceTxnDetails?.cardAuthResult,
            cardCountryCode = paymentServiceTxnDetails?.cardCountryCode,
            cardLanguagePref = paymentServiceTxnDetails?.cardLanguagePref,
            emvData = paymentServiceTxnDetails?.emvData,

            /* Original Txn data for Void Refund Capture */
            originalTxnRef = paymentServiceTxnDetails?.originalTxnRef
        )
    }

    fun createAuthCaptureRequest(paymentServiceTxnDetails: PaymentServiceTxnDetails?): PostAuthRequest {
        return PostAuthRequest(
            merchantId = paymentServiceTxnDetails?.merchantId,
            terminalId = paymentServiceTxnDetails?.terminalId,
            cashierId = paymentServiceTxnDetails?.loginId,
            deviceSN = paymentServiceTxnDetails?.deviceSN,
            deviceMake = paymentServiceTxnDetails?.deviceMake,
            deviceModel = paymentServiceTxnDetails?.deviceModel,

            batchId = paymentServiceTxnDetails?.batchId,
            dateTime = paymentServiceTxnDetails?.dateTime,
            timeZone = paymentServiceTxnDetails?.timeZone,
            txnType = paymentServiceTxnDetails?.txnType,
            txnCurrencyCode = paymentServiceTxnDetails?.txnCurrencyCode,

            originalTxnType = paymentServiceTxnDetails?.originalTxnType,
            originalTxnAmount = paymentServiceTxnDetails?.originalTxnAmount,
            originalTip = paymentServiceTxnDetails?.tip,
            originalCashback = paymentServiceTxnDetails?.cashback,
            originalCGST = paymentServiceTxnDetails?.originalCGST,
            originalSGST = paymentServiceTxnDetails?.originalSGST,
            originalTtlAmount = paymentServiceTxnDetails?.originalTtlAmount,
            originalTxnRef = paymentServiceTxnDetails?.originalTxnRef,
            originalHostTxnRef = paymentServiceTxnDetails?.originalHostTxnRef

        )
    }

    fun createPre_AuthRequest(paymentServiceTxnDetails: PaymentServiceTxnDetails?): PreAuthRequest {
        return PreAuthRequest(

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
            txnAmount = paymentServiceTxnDetails?.txnAmount,


            cardEntryMode = paymentServiceTxnDetails?.cardEntryMode,
            cardMaskedPan = paymentServiceTxnDetails?.cardMaskedPan,
            cardBrand = paymentServiceTxnDetails?.cardBrand,
            cardAuthMethod = paymentServiceTxnDetails?.cardAuthMethod,
            cardAuthResult = paymentServiceTxnDetails?.cardAuthResult,
            cardCountryCode = paymentServiceTxnDetails?.cardCountryCode,
            cardLanguagePref = paymentServiceTxnDetails?.cardLanguagePref,
            emvData = paymentServiceTxnDetails?.emvData
        )
    }

    fun createReversalRequest(paymentServiceTxnDetails: PaymentServiceTxnDetails?): ReversalReqeust {
        return ReversalReqeust(
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
            txnAmount = paymentServiceTxnDetails?.txnAmount,
            tip = paymentServiceTxnDetails?.tip,
            cashback = paymentServiceTxnDetails?.cashback,
            CGST = paymentServiceTxnDetails?.CGST,
            SGST = paymentServiceTxnDetails?.SGST,
            ttlAmount = paymentServiceTxnDetails?.ttlAmount,


            cardEntryMode = paymentServiceTxnDetails?.cardEntryMode,
            cardMaskedPan = paymentServiceTxnDetails?.cardMaskedPan,
            cardBrand = paymentServiceTxnDetails?.cardBrand,
            cardAuthMethod = paymentServiceTxnDetails?.cardAuthMethod,
            cardAuthResult = paymentServiceTxnDetails?.hostAuthResult,
            cardCountryCode = paymentServiceTxnDetails?.cardCountryCode,
            cardLanguagePref = paymentServiceTxnDetails?.cardLanguagePref,
            emvData = paymentServiceTxnDetails?.emvData,


            originalTxnType = paymentServiceTxnDetails?.originalTxnType,
            originalTxnAmount = paymentServiceTxnDetails?.originalTxnAmount,
            originalTip = paymentServiceTxnDetails?.originalTip,
            originalCashback = paymentServiceTxnDetails?.originalCashback,
            originalCGST = paymentServiceTxnDetails?.originalCGST,
            originalSGST = paymentServiceTxnDetails?.originalSGST,
            originalTtlAmount = paymentServiceTxnDetails?.originalTtlAmount,
            originalTxnRef = paymentServiceTxnDetails?.originalTxnRef
        )
    }


}