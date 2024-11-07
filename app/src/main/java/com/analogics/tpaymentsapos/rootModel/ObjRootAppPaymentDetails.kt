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
    @SerializedName("devicePublicKey") var devicePublicKey: String? = null,
    @SerializedName("devicePrivateKey") var devicePrivateKey: String? = null,

    @SerializedName("authAmount") var authAmount: Double? = 0.00,
    @SerializedName("hostAuthCode") var hostAuthCode: String? = null,
    @SerializedName("hostRespCode") var hostRespCode: String? = null,
    @SerializedName("hostAuthResult") var hostAuthResult: String? = null,
    @SerializedName("hostTxnRef") var hostTxnRef: String? = null,

    @SerializedName("ttlPurchaseAmount") var ttlPurchaseAmount:String? = null,
    @SerializedName("ttlRefundAmount") var ttlRefundAmount:String? = null,
    @SerializedName("ttlTxnAmount") var ttlTxnAmount:String? = null,

    @SerializedName("ttlPurchaseCount") var ttlPurchaseCount:Int? = null,
    @SerializedName("ttlRefundCount") var ttlRefundCount:Int? = null,
    @SerializedName("ttlTxnCount") var ttlTxnCount:Int? = null,

    @SerializedName("refundableAmount") var refundableAmount: String? = null,
    /* Card Details */
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
    @SerializedName("ACQUIRER_TYPE") var ACQUIRER_TYPE:String?=BuildConfig.ACQUIRER_TYPE


):Serializable
