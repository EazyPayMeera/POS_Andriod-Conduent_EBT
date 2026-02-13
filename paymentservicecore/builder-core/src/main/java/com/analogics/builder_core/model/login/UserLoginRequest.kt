package com.eazypaytech.builder_core.model.login

import com.google.gson.annotations.SerializedName

data class UserLoginRequest (
@SerializedName("MerchantId") var merchantId: String? = null,
@SerializedName("procId") var procId: String? = null,
@SerializedName("LoginId") var loginId: String? = null,
@SerializedName("LoginPassword") var loginPassword: String? = null,
@SerializedName("DeviceSN") var deviceSN: String? = null,
@SerializedName("DeviceMake") var deviceMake: String? = null,
@SerializedName("DeviceModel") var deviceModel: String? = null)