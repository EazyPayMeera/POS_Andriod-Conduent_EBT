package com.analogics.builder_core.requestBuilder

import android.content.Context
import com.analogics.builder_core.constants.BuilderConstants
import com.analogics.builder_core.model.PaymentServiceTxnDetails
import com.analogics.builder_core.model.auth_capture.PostAuthRequest
import com.analogics.builder_core.model.auth_capture.PreAuthRequest
import com.analogics.builder_core.model.auth_token.AuthTokenRequest
import com.analogics.builder_core.model.login.UserLoginRequest
import com.analogics.builder_core.model.purchase.PurchaseRequest
import com.analogics.builder_core.model.reund.RefundRequest
import com.analogics.builder_core.model.reversal.ReversalReqeust
import com.analogics.builder_core.model.void.VoidReqeust
import com.analogics.builder_core.utils.BuilderUtils
import com.analogics.networkservicecore.serviceutils.NetworkConstants
import com.github.kpavlov.jreactive8583.iso.ISO8583Version
import com.github.kpavlov.jreactive8583.iso.J8583MessageFactory
import com.github.kpavlov.jreactive8583.iso.MessageClass
import com.github.kpavlov.jreactive8583.iso.MessageFunction
import com.github.kpavlov.jreactive8583.iso.MessageOrigin
import com.solab.iso8583.IsoMessage
import com.solab.iso8583.IsoType
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlin.io.encoding.ExperimentalEncodingApi

class ApiRequestBuilderLyra @Inject constructor(@ApplicationContext val context: Context) {
    var messageFactory = J8583MessageFactory<IsoMessage>(ISO8583Version.V1987, MessageOrigin.ACQUIRER)

    private fun appendIsoLength(request : ByteArray?) : ByteArray
    {
        var isoPacket = ByteArray(request?.size?.plus(2)?:2)
        isoPacket[0] = ((request?.size?:0)/256).toByte()
        isoPacket[1] = ((request?.size?:0)%256).toByte()
        request?.copyInto(isoPacket,2,0, request.size)
        return isoPacket
    }

    @OptIn(ExperimentalEncodingApi::class, ExperimentalStdlibApi::class)
    fun createRklRequest(paymentServiceTxnDetails: PaymentServiceTxnDetails?): ByteArray {
        var message = messageFactory.newMessage(
            messageClass = MessageClass.NETWORK_MANAGEMENT,
            messageFunction = MessageFunction.REQUEST,
            messageOrigin = MessageOrigin.ACQUIRER
        )

        /* Set binary encoding instead of ASCII encoding */
        message.setBinary(true)

        /* TPDU, N10, Mandatory */
        message.binaryIsoHeader = NetworkConstants.ISO_HEADER

        /* Field 3, Processing Code, N6, Mandatory */
        message.setValue(NetworkConstants.ISO_FIELD_PROC_CODE, NetworkConstants.PROC_CODE_RKL_FULL_SN, IsoType.NUMERIC,NetworkConstants.ISO_FIELD_PROC_CODE_LENGTH)

        /* Field 11, STAN, N6, Mandatory */
        message.setValue(NetworkConstants.ISO_FIELD_STAN,
            BuilderUtils.getSTAN(context), IsoType.NUMERIC,NetworkConstants.ISO_FIELD_STAN_LENGTH)

        /* Field 12, Time, N6, Mandatory */
        message.setValue(NetworkConstants.ISO_FIELD_TIME,
            BuilderUtils.getCurrentDateTime(BuilderConstants.DEFAULT_ISO8583_TIME_FORMAT), IsoType.TIME,NetworkConstants.ISO_FIELD_TIME_LENGTH)

        /* Field 13, Date, N4, Mandatory */
        message.setValue(NetworkConstants.ISO_FIELD_DATE,
            BuilderUtils.getCurrentDateTime(BuilderConstants.DEFAULT_ISO8583_DATE_FORMAT), IsoType.DATE4,NetworkConstants.ISO_FIELD_DATE_LENGTH)

        /* Field 24, NII, N3, Mandatory */
        message.setValue(NetworkConstants.ISO_FIELD_NII, BuilderConstants.DEFAULT_ISO8583_NII, IsoType.NUMERIC,NetworkConstants.ISO_FIELD_NII_LENGTH)

        /* Field 41, TID, ANS8, Mandatory */
        message.setValue(NetworkConstants.ISO_FIELD_TID, paymentServiceTxnDetails?.terminalId, IsoType.ALPHA,NetworkConstants.ISO_FIELD_TID_LENGTH)

        /* Field 42, MID, ANS15, Mandatory */
        message.setValue(NetworkConstants.ISO_FIELD_MID, paymentServiceTxnDetails?.merchantId, IsoType.ALPHA,NetworkConstants.ISO_FIELD_MID_LENGTH)

        /* Field 60, Serial No, ANS...999, Mandatory */
        message.setValue(NetworkConstants.ISO_FIELD_TERM_SR_NO, paymentServiceTxnDetails?.deviceSN, IsoType.LLLVAR,paymentServiceTxnDetails?.deviceSN?.length?:0)

        /* Field 62, Working Key, ANS...999, Mandatory */
        message.setValue(NetworkConstants.ISO_FIELD_WORKING_KEY, paymentServiceTxnDetails?.devicePublicKey, IsoType.LLLVAR,paymentServiceTxnDetails?.devicePublicKey?.length?:0)

        return appendIsoLength(message.writeData())
    }

    fun createAccessTokenRequest(paymentServiceTxnDetails: PaymentServiceTxnDetails?): AuthTokenRequest {
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