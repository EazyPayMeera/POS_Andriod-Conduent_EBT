package com.eazypaytech.posafrica.domain.model

import com.eazypaytech.posafrica.BuildConfig
import com.analogics.paymentservicecore.data.model.emv.CardEntryMode
import com.analogics.paymentservicecore.data.model.emv.PosConditionCode
import com.analogics.paymentservicecore.data.model.TxnStatus
import com.analogics.paymentservicecore.data.model.TxnType
import com.google.gson.annotations.SerializedName
import java.io.Serializable
import javax.inject.Singleton

@Singleton
data class ObjRootAppPaymentDetails(
    @SerializedName("id") var id: Long? = null,
    @SerializedName("procId") var procId: String? = null,
    @SerializedName("cashierId") var cashierId: String? = null,
    @SerializedName("loginId") var loginId: String? = null,
    @SerializedName("loginPassword") var loginPassword: String? = null,
    @SerializedName("deviceSN") var deviceSN: String? = null,
    @SerializedName("deviceMake") var deviceMake: String? = null,
    @SerializedName("deviceModel") var deviceModel: String? = null,

    @SerializedName("merchantNameLocation") var merchantNameLocation: String? = null,
    @SerializedName("merchantBankName") var merchantBankName: String? = null,
    @SerializedName("merchantType") var merchantType: String? = null,
    @SerializedName("fnsNumber") var fnsNumber: String? = null,
    @SerializedName("stateCode") var stateCode: String? = null,
    @SerializedName("countyCode") var countyCode: String? = null,
    @SerializedName("postalServiceCode") var postalServiceCode: String? = null,
    @SerializedName("voucherNumber") var voucherNumber: String? = null,

    /* Host Authorization */
    @SerializedName("hostAuthCode") var hostAuthCode: String? = null,
    @SerializedName("approvalCode") var approvalCode: String? = null,
    @SerializedName("hostRespCode") var hostRespCode: String? = null,
    @SerializedName("hostAuthResult") var hostAuthResult: String? = null,
    @SerializedName("hostTxnRef") var hostTxnRef: String? = null,
    @SerializedName("hostResMessage") var hostResMessage: String? = null,

    /* Batch Totals */
    @SerializedName("ttlPurchaseAmount") var ttlPurchaseAmount:String? = null,
    @SerializedName("ttlRefundAmount") var ttlRefundAmount:String? = null,
    @SerializedName("ttlTxnAmount") var ttlTxnAmount:String? = null,
    @SerializedName("ttlTipAmount") var ttlTipAmount: String? = null,
    @SerializedName("ttlPurchaseCount") var ttlPurchaseCount:Int? = null,
    @SerializedName("ttlRefundCount") var ttlRefundCount:Int? = null,
    @SerializedName("ttlTxnCount") var ttlTxnCount:Int? = null,
    @SerializedName("ttlTipCount") var ttlTipCount:Int? = null,

    /* Conduent */
    @SerializedName("cardPan") var cardPan: String? = null,
    @SerializedName("stan") var stan: String? = null,
    @SerializedName("authAmount") var authAmount: String? = null,
    @SerializedName("dateTime") var dateTime: String? = null,
    @SerializedName("masterKey") var masterKey: String? = null,
    @SerializedName("expiryDate") var expiryDate: String? = null,
    @SerializedName("processingCode") var processingCode: String? = null,
    @SerializedName("localTime") var localTime: String? = null,
    @SerializedName("localDate") var localDate: String? = null,
    @SerializedName("settlementDate") var settlementDate: String? = null,
    @SerializedName("captureDate") var captureDate: String? = null,
    @SerializedName("posEntryMode") var posEntryMode: String? = null,
    @SerializedName("acquirerId") var acquirerId: String? = null,
    @SerializedName("track2Data") var track2Data: String? = null,
    @SerializedName("rrn") var rrn: String? = null,
    @SerializedName("authId") var authId: String? = null,
    @SerializedName("responseCode") var responseCode: String? = null,
    @SerializedName("merchantId") var merchantId: String? = null,
    @SerializedName("terminalId") var terminalId: String? = null,
    @SerializedName("merchantName") var merchantName: String? = null,
    @SerializedName("merchantBank") var merchantBank: String? = null,
    @SerializedName("currencyCode") var currencyCode: String? = null,
    @SerializedName("additionalAmt") var additionalAmt: String? = null,
    @SerializedName("snapEndBalance") var snapEndBalance: Double? = 0.00,
    @SerializedName("cashEndBalance") var cashEndBalance: Double? = 0.00,
    @SerializedName("posCondition") var posCondition: String? = null,
    @SerializedName("privateData") var privateData: String? = null,
    @SerializedName("acquirerTrace") var acquirerTrace: String? = null,
    @SerializedName("reservedPrivate") var reservedPrivate: String? = null,
    @SerializedName("originalData") var originalData: String? = null,
    @SerializedName("originalDateTime") var originalDateTime: String? = null,
    @SerializedName("snapBeginBal") var snapBeginBal: Double? = 0.00,
    @SerializedName("cashBeginBal") var cashBeginBal: Double? = 0.00,
    @SerializedName("settlementCode") var settlementCode: String? = null,
    @SerializedName("creditsNumber") var creditsNumber: String? = null,
    @SerializedName("creditsReversalNumber") var creditsReversalNumber: String? = null,
    @SerializedName("debitsNumber") var debitsNumber: String? = null,
    @SerializedName("debitsReversalNumber") var debitsReversalNumber: String? = null,
    @SerializedName("inquiriesNumber") var inquiriesNumber: String? = null,
    @SerializedName("authorizationsNumber") var authorizationsNumber: String? = null,
    @SerializedName("creditsAmount") var creditsAmount: String? = null,
    @SerializedName("creditsReversalAmount") var creditsReversalAmount: String? = null,
    @SerializedName("debitsAmount") var debitsAmount: String? = null,
    @SerializedName("debitsReversalAmount") var debitsReversalAmount: String? = null,
    @SerializedName("netSettlementAmount") var netSettlementAmount: String? = null,
    @SerializedName("settlementInstitutionId") var settlementInstitutionId: String? = null,

    @SerializedName("workKey") var workKey: String? = null,

    /* Card Details */
    @SerializedName("emvData")          var emvData: String? = null,
    @SerializedName("trackData")        var trackData: String? = null,
    @SerializedName("pinBlock")         var pinBlock: String? = null,
    @SerializedName("ternNameLoc")        var ternNameLoc: String? = null,
    @SerializedName("ksn")              var ksn: String? = null,
    @SerializedName("posConditionCode") var posConditionCode: PosConditionCode? = null,
    @SerializedName("cardEntryMode")    var cardEntryMode: CardEntryMode? = null,
    @SerializedName("cardMaskedPan")    var cardMaskedPan: String? = null,
    @SerializedName("cardBrand")        var cardBrand: String? = null,
    @SerializedName("cardSeqNum")       var cardSeqNum: String? = null,
    @SerializedName("cardAuthMethod")   var cardAuthMethod: String? = null,
    @SerializedName("cardAuthResult")   var cardAuthResult: String? = null,
    @SerializedName("cardCountryCode")  var cardCountryCode: String? = null,
    @SerializedName("cardLanguagePref") var cardLanguagePref: String? = null,
    @SerializedName("receiptEmvData")   var receiptEmvData: String? = null,


    /* Transaction Details */
    @SerializedName("batchId") var batchId: String? = null,
    @SerializedName("invoiceNo") var invoiceNo: String? = null,
    @SerializedName("purchaseOrderNo") var purchaseOrderNo: String? = null,
    @SerializedName("timeZone") var timeZone: String? = null,
    @SerializedName("txnType") var txnType:TxnType?=null,
    @SerializedName("accountType") var accountType: String? = null,
    @SerializedName("txnCurrencyCode") var txnCurrencyCode: String? = null,
    @SerializedName("txnAmount") var txnAmount: Double? = 0.00,
    @SerializedName("tip") var tip: Double? = 0.00,
    @SerializedName("cashback") var cashback: Double? = 0.00,
    @SerializedName("VAT") var VAT: Double? = 0.00,
    @SerializedName("serviceCharge") var serviceCharge: Double? = 0.00,
    @SerializedName("ttlAmount") var ttlAmount: Double? = 0.00,
    @SerializedName("refundableAmount") var refundableAmount: String? = null,
    @SerializedName("txnStatus") var txnStatus: TxnStatus? = null,
    @SerializedName("acquirerName") var acquirerName:String?= BuildConfig.ACQUIRER_NAME,
    @SerializedName("signatureData")    var signatureData: String? = null,

    /* Original Txn data for Void Refund Capture */
    @SerializedName("originalHostTxnRef")  var originalHostTxnRef: String? = null,
    @SerializedName("originalTxnType")  var originalTxnType: TxnType? = null,
    @SerializedName("originalTxnAmount")    var originalTxnAmount: String? = null,
    @SerializedName("originalTip")      var originalTip: String? = null,
    @SerializedName("originalCashback") var originalCashback: String? = null,
    @SerializedName("originalVat")     var originalVat: String? = null,
    @SerializedName("originalServiceCharge")   var originalServiceCharge: String? = null,
    @SerializedName("originalTtlAmount")    var originalTtlAmount: String? = null,
    @SerializedName("originalTxnRef")   var originalTxnRef: String? = null,

    /* Other flags */
    @SerializedName("isFallback")       var isFallback: Boolean? = false,
    @SerializedName("isCaptured")       var isCaptured: Boolean? = false,
    @SerializedName("isVoided")         var isVoided: Boolean? = false,
    @SerializedName("isRefunded")       var isRefunded: Boolean? = false,
    @SerializedName("isDemoMode")       var isDemoMode: Boolean? = false,

    @SerializedName("isPurchase")       var isPurchase: Boolean? = false,
    @SerializedName("isReturn")       var isReturn: Boolean? = false,

    @SerializedName("isTapEnable") var isTapEnable: Boolean? = false,
    @SerializedName("isEMVEnable") var isEMVEnable: Boolean? = false,

    /* Receipt Specific Config */
    @SerializedName("header1") var header1: String? = null,
    @SerializedName("header2") var header2: String? = null,
    @SerializedName("header3") var header3: String? = null,
    @SerializedName("header4") var header4: String? = null,
    @SerializedName("footer1") var footer1: String? = null,
    @SerializedName("footer2") var footer2: String? = null,
    @SerializedName("footer3") var footer3: String? = null,
    @SerializedName("footer4") var footer4: String? = null
    ):Serializable
