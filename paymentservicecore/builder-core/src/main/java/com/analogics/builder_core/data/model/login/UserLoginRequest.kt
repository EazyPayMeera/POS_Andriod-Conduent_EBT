package com.analogics.builder_core.data.model.login

import com.google.gson.annotations.SerializedName

data class UserLoginRequest (
@SerializedName("MerchantId") var merchantId: String? = null,
@SerializedName("terminalId") var terminalId: String? = null,
@SerializedName("procId") var procId: String? = null,
@SerializedName("LoginId") var loginId: String? = null,
@SerializedName("LoginPassword") var loginPassword: String? = null,
@SerializedName("DeviceSN") var deviceSN: String? = null,
@SerializedName("DeviceMake") var deviceMake: String? = null,
@SerializedName("DeviceModel") var deviceModel: String? = null)
@SerializedName("merchantNameLocation") var merchantNameLocation: String? = null
@SerializedName("merchantBankName") var merchantBankName: String? = null
@SerializedName("merchantType") var merchantType: String? = null
@SerializedName("fnsNumber") var fnsNumber: String? = null
@SerializedName("stateCode") var stateCode: String? = null
@SerializedName("countyCode") var countyCode: String? = null
@SerializedName("postalServiceCode") var postalServiceCode: String? = null