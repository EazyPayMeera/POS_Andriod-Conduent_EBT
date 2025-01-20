package com.eazypaytech.builder_core.model.auth_token

import com.eazypaytech.networkservicecore.serviceutils.NetworkConstants
import com.google.gson.annotations.SerializedName

data class AuthTokenRequest(
    @SerializedName("app_id") var app_id: String? = null,
    @SerializedName("secret") var secret: String? = null,
    @SerializedName("grant_type") var grant_type: String? = NetworkConstants.VAL_GRANT_TYPE_CREDENTIALS,
    @SerializedName("nonce") var nonce: String? = null
)
