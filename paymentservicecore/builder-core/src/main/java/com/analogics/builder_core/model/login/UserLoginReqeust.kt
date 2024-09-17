package com.analogics.builder_core.model.login

import com.google.gson.annotations.SerializedName

data class UserLoginReqeust (
@SerializedName("MerchantId") var merchantId: String? = null,
@SerializedName("TerminalId") var terminalId: String? = null,
@SerializedName("LoginId") var loginId: String? = null,
@SerializedName("LoginPassword") var loginPassword: String? = null,
@SerializedName("DeviceSN") var deviceSN: String? = null,
@SerializedName("DeviceMake") var deviceMake: String? = null,
@SerializedName("DeviceModel") var deviceModel: String? = null)