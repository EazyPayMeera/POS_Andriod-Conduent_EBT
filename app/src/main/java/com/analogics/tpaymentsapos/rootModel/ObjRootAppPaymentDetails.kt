package com.analogics.tpaymentsapos.rootModel


import com.analogics.paymentservicecore.model.emv.CardEntryMode
import com.analogics.paymentservicecore.model.emv.PosConditionCode
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
    @SerializedName("cashierId") var cashierId: String? = null,
    @SerializedName("loginId") var loginId: String? = null,
    @SerializedName("loginPassword") var loginPassword: String? = null,
    @SerializedName("deviceSN") var deviceSN: String? = null,
    @SerializedName("deviceMake") var deviceMake: String? = null,
    @SerializedName("deviceModel") var deviceModel: String? = null,

    /* Host Authorization */
    @SerializedName("authAmount") var authAmount: Double? = 0.00,
    @SerializedName("hostAuthCode") var hostAuthCode: String? = null,
    @SerializedName("hostRespCode") var hostRespCode: String? = null,
    @SerializedName("hostAuthResult") var hostAuthResult: String? = null,
    @SerializedName("hostTxnRef") var hostTxnRef: String? = null,

    /* Batch Totals */
    @SerializedName("ttlPurchaseAmount") var ttlPurchaseAmount:String? = null,
    @SerializedName("ttlRefundAmount") var ttlRefundAmount:String? = null,
    @SerializedName("ttlTxnAmount") var ttlTxnAmount:String? = null,
    @SerializedName("ttlTipAmount") var ttlTipAmount: String? = null,
    @SerializedName("ttlPurchaseCount") var ttlPurchaseCount:Int? = null,
    @SerializedName("ttlRefundCount") var ttlRefundCount:Int? = null,
    @SerializedName("ttlTxnCount") var ttlTxnCount:Int? = null,
    @SerializedName("ttlTipCount") var ttlTipCount:Int? = null,

    /* Card Details */
    @SerializedName("emvData")          var emvData: String? = null,
    @SerializedName("trackData")        var trackData: String? = null,
    @SerializedName("pinBlock")         var pinBlock: String? = null,
    @SerializedName("ksn")              var ksn: String? = null,
    @SerializedName("posConditionCode") var posConditionCode: PosConditionCode? = null,
    @SerializedName("cardEntryMode")    var cardEntryMode: CardEntryMode? = null,
    @SerializedName("cardPan")          var cardPan: String? = null,
    @SerializedName("cardMaskedPan")    var cardMaskedPan: String? = null,
    @SerializedName("cardBrand")        var cardBrand: String? = null,
    @SerializedName("cardSeqNum")       var cardSeqNum: String? = null,
    @SerializedName("cardAuthMethod")   var cardAuthMethod: String? = null,
    @SerializedName("cardAuthResult")   var cardAuthResult: String? = null,
    @SerializedName("cardCountryCode")  var cardCountryCode: String? = null,
    @SerializedName("cardLanguagePref") var cardLanguagePref: String? = null,
    @SerializedName("receiptEmvData")   var receiptEmvData: String? = null,
    @SerializedName("signatureData")    var signatureData: String? = null,

    /* Transaction Details */
    @SerializedName("batchId") var batchId: String? = null,
    @SerializedName("invoiceNo") var invoiceNo: String? = null,
    @SerializedName("stan") var stan: String? = null,
    @SerializedName("purchaseOrderNo") var purchaseOrderNo: String? = null,
    @SerializedName("dateTime") var dateTime: String? = null,
    @SerializedName("timeZone") var timeZone: String? = null,
    @SerializedName("txnType") var txnType:TxnType?=null,
    @SerializedName("accountType") var accountType: String? = null,
    @SerializedName("txnCurrencyCode") var txnCurrencyCode: String? = null,
    @SerializedName("txnAmount") var txnAmount: Double? = 0.00,
    @SerializedName("tip") var tip: Double? = 0.00,
    @SerializedName("cashback") var cashback: Double? = 0.00,
    @SerializedName("CGST") var CGST: Double? = 0.00,
    @SerializedName("SGST") var SGST: Double? = 0.00,
    @SerializedName("ttlAmount") var ttlAmount: Double? = 0.00,
    @SerializedName("refundableAmount") var refundableAmount: String? = null,
    @SerializedName("txnStatus") var txnStatus: TxnStatus? = null,
    @SerializedName("acquirerName") var acquirerName:String?=BuildConfig.ACQUIRER_NAME,

    /* Original Txn data for Void Refund Capture */
    @SerializedName("originalHostTxnRef")  var originalHostTxnRef: String? = null,
    @SerializedName("originalTxnType")  var originalTxnType: TxnType? = null,
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
