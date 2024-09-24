package com.analogics.tpaymentsapos.rootModel


import com.analogics.paymentservicecore.models.TxnType
import com.google.gson.annotations.SerializedName
import java.io.Serializable
import javax.inject.Singleton

@Singleton
data class ObjRootAppPaymentDetails (
    @SerializedName("merchantId") var merchantId: String? = null,
    @SerializedName("terminalId") var terminalId: String? = null,
    @SerializedName("loginId") var loginId: String? = null,
    @SerializedName("loginPassword") var loginPassword: String? = null,
    @SerializedName("deviceSN") var deviceSN: String? = null,
    @SerializedName("deviceMake") var deviceMake: String? = null,
    @SerializedName("deviceModel") var deviceModel: String? = null,

    @SerializedName("authAmount") var authAmount: String? = null,
    @SerializedName("hostAuthCode") var hostAuthCode: String? = null,
    @SerializedName("hostRespCode") var hostRespCode: String? = null,
    @SerializedName("hostAuthResult") var hostAuthResult: String? = null,
    @SerializedName("hostTxnRef") var hostTxnRef: String? = null,

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
    @SerializedName("txnAmount") var txnAmount: Double? = null,
    @SerializedName("tip") var tip: String? = null,
    @SerializedName("cashback") var cashback: String? = null,
    @SerializedName("CGST") var CGST: String? = null,
    @SerializedName("SGST") var SGST: String? = null,
    @SerializedName("ttlAmount") var ttlAmount: String? = null,
    @SerializedName("txnStatus") var txnStatus: String? = null,

    ):Serializable
