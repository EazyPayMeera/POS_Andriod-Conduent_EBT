package com.analogics.tpaymentsapos.rootModel

import com.google.gson.annotations.SerializedName

data class ObjRootAppPaymentDetails(
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

    )
