package com.eazypaytech.builder_core.requestBuilder

import android.content.Context
import com.eazypaytech.builder_core.constants.BuilderConstants
import com.eazypaytech.builder_core.model.BuilderServiceTxnDetails
import com.eazypaytech.builder_core.model.auth_capture.PostAuthRequest
import com.eazypaytech.builder_core.model.auth_capture.PreAuthRequest
import com.eazypaytech.builder_core.model.auth_token.AuthTokenRequest
import com.eazypaytech.builder_core.model.login.UserLoginRequest
import com.eazypaytech.builder_core.model.purchase.PurchaseRequest
import com.eazypaytech.builder_core.model.reund.RefundRequest
import com.eazypaytech.builder_core.model.reversal.ReversalReqeust
import com.eazypaytech.builder_core.model.void.VoidReqeust
import com.eazypaytech.builder_core.utils.BuilderUtils
import com.eazypaytech.networkservicecore.serviceutils.NetworkConstants
import com.solab.iso8583.IsoMessage
import com.solab.iso8583.IsoType
import com.solab.iso8583.MessageFactory
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlin.io.encoding.ExperimentalEncodingApi

class ApiRequestBuilder @Inject constructor(@ApplicationContext val context: Context) {
    var messageFactory = MessageFactory<IsoMessage>()

    @OptIn(ExperimentalEncodingApi::class, ExperimentalStdlibApi::class)
    fun createRklRequest(builderServiceTxnDetails: BuilderServiceTxnDetails?): ByteArray? {
        var message = messageFactory.newMessage(0x0800)

        /* Set binary encoding instead of ASCII encoding */
        message.setBinary(true)

        /* Field 3, Processing Code, N6, Mandatory */
        message.setValue(BuilderConstants.ISO_FIELD_PROC_CODE, BuilderConstants.PROC_CODE_RKL_FULL_SN, IsoType.NUMERIC,BuilderConstants.ISO_FIELD_PROC_CODE_LENGTH)

        /* Field 11, STAN, N6, Mandatory */
        message.setValue(BuilderConstants.ISO_FIELD_STAN,
            BuilderUtils.getSTAN(context), IsoType.NUMERIC,BuilderConstants.ISO_FIELD_STAN_LENGTH)

        /* Field 12, Time, N6, Mandatory */
        message.setValue(BuilderConstants.ISO_FIELD_TIME,
            BuilderUtils.getCurrentDateTime(BuilderConstants.DEFAULT_ISO8583_TIME_FORMAT), IsoType.TIME,BuilderConstants.ISO_FIELD_TIME_LENGTH)

        /* Field 13, Date, N4, Mandatory */
        message.setValue(BuilderConstants.ISO_FIELD_DATE,
            BuilderUtils.getCurrentDateTime(BuilderConstants.DEFAULT_ISO8583_DATE_FORMAT), IsoType.DATE4,BuilderConstants.ISO_FIELD_DATE_LENGTH)

        /* Field 24, NII, N3, Mandatory */
        message.setValue(BuilderConstants.ISO_FIELD_NII, BuilderConstants.DEFAULT_ISO8583_NII, IsoType.NUMERIC,BuilderConstants.ISO_FIELD_NII_LENGTH)

        /* Field 41, TID, ANS8, Mandatory */
        message.setValue(BuilderConstants.ISO_FIELD_TID, builderServiceTxnDetails?.terminalId, IsoType.ALPHA,BuilderConstants.ISO_FIELD_TID_LENGTH)

        /* Field 42, MID, ANS15, Mandatory */
        message.setValue(BuilderConstants.ISO_FIELD_MID, builderServiceTxnDetails?.merchantId, IsoType.ALPHA,BuilderConstants.ISO_FIELD_MID_LENGTH)

        /* Field 60, Serial No, ANS...999, Mandatory */
        message.setValue(BuilderConstants.ISO_FIELD_TERM_SR_NO, builderServiceTxnDetails?.deviceSN, IsoType.LLLVAR,builderServiceTxnDetails?.deviceSN?.length?:0)

        /* Field 62, Working Key, ANS...999, Mandatory */
        message.setValue(BuilderConstants.ISO_FIELD_WORKING_KEY, builderServiceTxnDetails?.devicePublicKey, IsoType.LLLVAR,builderServiceTxnDetails?.devicePublicKey?.length?:0)

        return message.writeData()
    }

    fun createAccessTokenRequest(builderServiceTxnDetails: BuilderServiceTxnDetails?): AuthTokenRequest {
        var appId = "pcAz2HwAompQWPEF4MXUJt91ldqEnzQR"
        var appKey = "qqOKVz4gUATaoG6W"
        var nonce = BuilderUtils.generateNonce()
        var secret = BuilderUtils.generateSecret(nonce, appKey)
        return AuthTokenRequest(
            app_id = appId,
            secret = secret,
            grant_type = NetworkConstants.VAL_GRANT_TYPE_CREDENTIALS,
            nonce = nonce
        )
    }

    fun createLoginRequest(builderServiceTxnDetails: BuilderServiceTxnDetails?): UserLoginRequest {
        return UserLoginRequest(
            merchantId = builderServiceTxnDetails?.merchantId,
            terminalId = builderServiceTxnDetails?.terminalId,
            loginId = builderServiceTxnDetails?.loginId,
            loginPassword = builderServiceTxnDetails?.loginPassword,
            deviceSN = builderServiceTxnDetails?.deviceSN,
            deviceMake = builderServiceTxnDetails?.deviceMake,
            deviceModel = builderServiceTxnDetails?.deviceModel
        )
    }

    fun createPurchaseRequest(builderServiceTxnDetails: BuilderServiceTxnDetails?): PurchaseRequest {
        return PurchaseRequest(
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

    fun createVoidRequest(builderServiceTxnDetails: BuilderServiceTxnDetails?): VoidReqeust {
        return VoidReqeust(
            merchantId = builderServiceTxnDetails?.merchantId,
            terminalId = builderServiceTxnDetails?.terminalId,
            cashierId = builderServiceTxnDetails?.loginId,
            deviceSN = builderServiceTxnDetails?.deviceSN,
            deviceMake = builderServiceTxnDetails?.deviceMake,
            deviceModel = builderServiceTxnDetails?.deviceModel,

            batchId = builderServiceTxnDetails?.batchId,
            dateTime = builderServiceTxnDetails?.dateTime,
            timeZone = builderServiceTxnDetails?.timeZone,
            txnType = builderServiceTxnDetails?.txnType,
            txnCurrencyCode = builderServiceTxnDetails?.cardCountryCode,

            originalTxnType = builderServiceTxnDetails?.originalTxnType,
            originalTxnAmount = builderServiceTxnDetails?.originalTxnAmount,
            originalTip = builderServiceTxnDetails?.originalTip,
            originalCashback = builderServiceTxnDetails?.originalCashback,
            originalVat = builderServiceTxnDetails?.originalVat,
            originalSGST = builderServiceTxnDetails?.originalSGST,
            originalTtlAmount = builderServiceTxnDetails?.originalTtlAmount,
            originalTxnRef = builderServiceTxnDetails?.originalTxnRef,
            originalHostTxnRef = builderServiceTxnDetails?.originalHostTxnRef
        )
    }

    fun createRefundRequest(builderServiceTxnDetails: BuilderServiceTxnDetails?): RefundRequest {
        return RefundRequest(

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
            txnAmount = builderServiceTxnDetails?.txnAmount,


            cardEntryMode = builderServiceTxnDetails?.cardEntryMode,
            cardMaskedPan = builderServiceTxnDetails?.cardMaskedPan,
            cardBrand = builderServiceTxnDetails?.cardBrand,
            cardAuthMethod = builderServiceTxnDetails?.cardAuthMethod,
            cardAuthResult = builderServiceTxnDetails?.cardAuthResult,
            cardCountryCode = builderServiceTxnDetails?.cardCountryCode,
            cardLanguagePref = builderServiceTxnDetails?.cardLanguagePref,
            emvData = builderServiceTxnDetails?.emvData,

            /* Original Txn data for Void Refund Capture */
            originalTxnRef = builderServiceTxnDetails?.originalTxnRef
        )
    }

    fun createAuthCaptureRequest(builderServiceTxnDetails: BuilderServiceTxnDetails?): PostAuthRequest {
        return PostAuthRequest(
            merchantId = builderServiceTxnDetails?.merchantId,
            terminalId = builderServiceTxnDetails?.terminalId,
            cashierId = builderServiceTxnDetails?.loginId,
            deviceSN = builderServiceTxnDetails?.deviceSN,
            deviceMake = builderServiceTxnDetails?.deviceMake,
            deviceModel = builderServiceTxnDetails?.deviceModel,

            batchId = builderServiceTxnDetails?.batchId,
            dateTime = builderServiceTxnDetails?.dateTime,
            timeZone = builderServiceTxnDetails?.timeZone,
            txnType = builderServiceTxnDetails?.txnType,
            txnCurrencyCode = builderServiceTxnDetails?.txnCurrencyCode,

            originalTxnType = builderServiceTxnDetails?.originalTxnType,
            originalTxnAmount = builderServiceTxnDetails?.originalTxnAmount,
            originalTip = builderServiceTxnDetails?.tip,
            originalCashback = builderServiceTxnDetails?.cashback,
            originalVat = builderServiceTxnDetails?.originalVat,
            originalSGST = builderServiceTxnDetails?.originalSGST,
            originalTtlAmount = builderServiceTxnDetails?.originalTtlAmount,
            originalTxnRef = builderServiceTxnDetails?.originalTxnRef,
            originalHostTxnRef = builderServiceTxnDetails?.originalHostTxnRef

        )
    }

    fun createPre_AuthRequest(builderServiceTxnDetails: BuilderServiceTxnDetails?): PreAuthRequest {
        return PreAuthRequest(

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
            txnAmount = builderServiceTxnDetails?.txnAmount,


            cardEntryMode = builderServiceTxnDetails?.cardEntryMode,
            cardMaskedPan = builderServiceTxnDetails?.cardMaskedPan,
            cardBrand = builderServiceTxnDetails?.cardBrand,
            cardAuthMethod = builderServiceTxnDetails?.cardAuthMethod,
            cardAuthResult = builderServiceTxnDetails?.cardAuthResult,
            cardCountryCode = builderServiceTxnDetails?.cardCountryCode,
            cardLanguagePref = builderServiceTxnDetails?.cardLanguagePref,
            emvData = builderServiceTxnDetails?.emvData
        )
    }

    fun createReversalRequest(builderServiceTxnDetails: BuilderServiceTxnDetails?): ReversalReqeust {
        return ReversalReqeust(
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
            txnAmount = builderServiceTxnDetails?.txnAmount,
            tip = builderServiceTxnDetails?.tip,
            cashback = builderServiceTxnDetails?.cashback,
            VAT = builderServiceTxnDetails?.VAT,
            SGST = builderServiceTxnDetails?.SGST,
            ttlAmount = builderServiceTxnDetails?.ttlAmount,


            cardEntryMode = builderServiceTxnDetails?.cardEntryMode,
            cardMaskedPan = builderServiceTxnDetails?.cardMaskedPan,
            cardBrand = builderServiceTxnDetails?.cardBrand,
            cardAuthMethod = builderServiceTxnDetails?.cardAuthMethod,
            cardAuthResult = builderServiceTxnDetails?.cardAuthResult,
            cardCountryCode = builderServiceTxnDetails?.cardCountryCode,
            cardLanguagePref = builderServiceTxnDetails?.cardLanguagePref,
            emvData = builderServiceTxnDetails?.emvData,


            originalTxnType = builderServiceTxnDetails?.originalTxnType,
            originalTxnAmount = builderServiceTxnDetails?.originalTxnAmount,
            originalTip = builderServiceTxnDetails?.originalTip,
            originalCashback = builderServiceTxnDetails?.originalCashback,
            originalVat = builderServiceTxnDetails?.originalVat,
            originalSGST = builderServiceTxnDetails?.originalSGST,
            originalTtlAmount = builderServiceTxnDetails?.originalTtlAmount,
            originalTxnRef = builderServiceTxnDetails?.originalTxnRef
        )
    }
}