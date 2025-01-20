package com.eazypaytech.paymentservicecore.model.emv

import com.google.gson.annotations.SerializedName

data class CAPKey(
    @SerializedName("rid")      val rid : String? = null,
    @SerializedName("index")    val index : String? = null,
    @SerializedName("exponent") val exponent : String? = null,
    @SerializedName("modulus")  val modulus : String? = null,
    @SerializedName("checksum") val checksum : String? = null
)
