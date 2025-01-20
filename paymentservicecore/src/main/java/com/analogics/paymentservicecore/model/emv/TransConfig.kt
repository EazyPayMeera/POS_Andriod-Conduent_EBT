package com.eazypaytech.paymentservicecore.model.emv

import com.google.gson.annotations.SerializedName

data class TransConfig(
    @SerializedName("amount")               val amount : String? = null,
    @SerializedName("cashbackAmount")       val cashbackAmount : String? = null,
    @SerializedName("currencyCode")         val currencyCode : String? = null,
    @SerializedName("transactionType")      val transactionType : String? = null,
    @SerializedName("cardCheckMode")        val cardCheckMode : CardCheckMode? = null,
    @SerializedName("cardCheckTimeout")     var cardCheckTimeout : String? = null,
    @SerializedName("enableBeeper")         var enableBeeper : Boolean? = null,
    @SerializedName("supportFallback")      val supportFallback : Boolean? = null,
    @SerializedName("supportDRL")           val supportDRL : Boolean? = null,
    @SerializedName("forceOnline")          val forceOnline : Boolean? = null
)
