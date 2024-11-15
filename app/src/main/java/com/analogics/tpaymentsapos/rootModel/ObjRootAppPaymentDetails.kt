package com.analogics.tpaymentsapos.rootModel


import com.analogics.paymentservicecore.models.TxnStatus
import com.analogics.paymentservicecore.models.TxnType
import com.analogics.tpaymentsapos.BuildConfig
import com.google.gson.annotations.SerializedName
import java.io.Serializable
import javax.inject.Singleton

@Singleton
data class ObjRootAppPaymentDetails(
    @SerializedName("id") var id: Long? = null,
    @SerializedName("merchantId") var merchantId: String? = null,
    @SerializedName("terminalId") var terminalId: String? = null,
    @SerializedName("loginId") var loginId: String? = null,
    @SerializedName("loginPassword") var loginPassword: String? = null,
    @SerializedName("deviceSN") var deviceSN: String? = null,
    @SerializedName("deviceMake") var deviceMake: String? = null,
    @SerializedName("deviceModel") var deviceModel: String? = null,

    /* Host Response */
    @SerializedName("authAmount") var authAmount: Double? = 0.00,
    @SerializedName("hostAuthCode") var hostAuthCode: String? = null,
    @SerializedName("hostRespCode") var hostRespCode: String? = null,
    @SerializedName("hostAuthResult") var hostAuthResult: String? = null,
    @SerializedName("hostTxnRef") var hostTxnRef: String? = null,

    @SerializedName("ttlPurchaseAmount") var ttlPurchaseAmount:String? = null,
    @SerializedName("ttlRefundAmount") var ttlRefundAmount:String? = null,
    @SerializedName("ttlTxnAmount") var ttlTxnAmount:String? = null,
    @SerializedName("refundableAmount") var refundableAmount: String? = null,
    @SerializedName("ttlTipAmount") var ttlTipAmount: String? = null,

    @SerializedName("ttlPurchaseCount") var ttlPurchaseCount:Int? = null,
    @SerializedName("ttlRefundCount") var ttlRefundCount:Int? = null,
    @SerializedName("ttlTxnCount") var ttlTxnCount:Int? = null,
    @SerializedName("ttlTipCount") var ttlTipCount:Int? = null,

    /* Card Details */
    @SerializedName("CardEntryMode")    var cardEntryMode: String? = null,
    @SerializedName("CardMaskedPan")    var cardMaskedPan: String? = null,
    @SerializedName("CardBrand")        var cardBrand: String? = null,
    @SerializedName("CardAuthMethod")   var cardAuthMethod: String? = null,
    @SerializedName("CardAuthResult")   var cardAuthResult: String? = null,
    @SerializedName("CardCountryCode")  var cardCountryCode: String? = null,
    @SerializedName("CardLanguagePref") var cardLanguagePref: String? = null,
    @SerializedName("ReceiptEmvData")   var receiptEmvData: String? = null,
    @SerializedName("SignatureData")    var signatureData: String? = null,
    @SerializedName("emvData") var emvData: String? = null,

    @SerializedName("txnType") var txnType:TxnType?=null,
    @SerializedName("batchId") var batchId: String? = null,
    @SerializedName("invoiceNo") var invoiceNo: String? = null,
    @SerializedName("purchaseOrderNo") var purchaseOrderNo: String? = null,
    @SerializedName("dateTime") var dateTime: String? = null,
    @SerializedName("timeZone") var timeZone: String? = null,
    @SerializedName("accountType") var accountType: String? = null,
    @SerializedName("txnCurrencyCode") var txnCurrencyCode: String? = null,
    @SerializedName("txnAmount") var txnAmount: Double? = 0.00,
    @SerializedName("tip") var tip: Double? = 0.00,
    @SerializedName("cashback") var cashback: Double? = 0.00,
    @SerializedName("CGST") var CGST: Double? = 0.00,
    @SerializedName("SGST") var SGST: Double? = 0.00,
    @SerializedName("ttlAmount") var ttlAmount: Double? = 0.00,
    @SerializedName("txnStatus") var txnStatus: TxnStatus? = null,
    @SerializedName("ACQUIRER_TYPE") var ACQUIRER_TYPE:String?=BuildConfig.ACQUIRER_TYPE,

    /* Original Txn data for Void Refund Capture */
    @SerializedName("originalTxnType")  var originalTxnType: String? = null,
    @SerializedName("originalTxnAmount")    var originalTxnAmount: String? = null,
    @SerializedName("originalTip")      var originalTip: String? = null,
    @SerializedName("originalCashback") var originalCashback: String? = null,
    @SerializedName("originalCGST")     var originalCGST: String? = null,
    @SerializedName("originalSGST")     var originalSGST: String? = null,
    @SerializedName("originalTtlAmount")    var originalTtlAmount: String? = null,
    @SerializedName("originalTxnRef")   var originalTxnRef: String? = null,

    /* Other flags */
    @SerializedName("isFallback")       var isFallback: Boolean? = false,
    @SerializedName("isCaptured")       var isCaptured: Boolean? = false,
    @SerializedName("isVoided")         var isVoided: Boolean? = false,
    @SerializedName("isRefunded")       var isRefunded: Boolean? = false,
    @SerializedName("isDemoMode")       var isDemoMode: Boolean? = false
):Serializable
