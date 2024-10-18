package com.analogics.paymentservicecore.model.emv

import com.google.gson.annotations.SerializedName

data class TransConfig(
    @SerializedName("amount")               val amount : String? = null,
    @SerializedName("cashbackAmount")       val cashbackAmount : String? = null,
    @SerializedName("transactionType")      val transactionType : String? = null,
    @SerializedName("cardCheckMode")        val cardCheckMode : String? = null
)
