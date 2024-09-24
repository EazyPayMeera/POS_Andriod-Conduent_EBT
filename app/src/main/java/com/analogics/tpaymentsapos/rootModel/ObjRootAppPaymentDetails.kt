package com.analogics.tpaymentsapos.rootModel


import com.analogics.paymentservicecore.models.TxnType
import com.google.gson.annotations.SerializedName
import java.io.Serializable
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
data class ObjRootAppPaymentDetails (
    @SerializedName("MerchantId") var merchantId: String? = null,
    @SerializedName("TerminalId") var terminalId: String? = null,
    @SerializedName("LoginId") var loginId: String? = null,
    @SerializedName("LoginPassword") var loginPassword: String? = null,
    @SerializedName("DeviceSN") var deviceSN: String? = null,
    @SerializedName("DeviceMake") var deviceMake: String? = null,
    @SerializedName("DeviceModel") var deviceModel: String? = null,

    @SerializedName("AuthAmount") var authAmount: String? = null,
    @SerializedName("HostAuthCode") var hostAuthCode: String? = null,
    @SerializedName("HostRespCode") var hostRespCode: String? = null,
    @SerializedName("HostAuthResult") var hostAuthResult: String? = null,
    @SerializedName("HostTxnRef") var hostTxnRef: String? = null,

    @SerializedName("RefundableAmount") var refundableAmount: String? = null,
    /* Card Details */
    @SerializedName("EmvData") var emvData: String? = null,

    @SerializedName("TxnType") var txnType:TxnType?=null,
    @SerializedName("BatchId") var batchId: String? = null,
    @SerializedName("InvoiceNo") var invoiceNo: String? = null,
    @SerializedName("PurchaseOrderNo") var purchaseOrderNo: String? = null,
    @SerializedName("DateTime") var dateTime: String? = null,
    @SerializedName("TimeZone") var timeZone: String? = null,
    @SerializedName("AccountType") var accountType: String? = null,
    @SerializedName("TxnCurrencyCode") var txnCurrencyCode: String? = null,
    @SerializedName("TxnAmount") var txnAmount: Double? = null,
    @SerializedName("Tip") var tip: String? = null,
    @SerializedName("Cashback") var cashback: String? = null,
    @SerializedName("CGST") var CGST: String? = null,
    @SerializedName("SGST") var SGST: String? = null,
    @SerializedName("TtlAmount") var ttlAmount: String? = null,
    @SerializedName("TxnStatus") var txnStatus: String? = null,

    ):Serializable
