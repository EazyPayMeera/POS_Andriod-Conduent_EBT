package com.analogics.paymentservicecore.model.emv

import com.google.gson.annotations.SerializedName

data class TransConfig(
    @SerializedName("amount")               val amount : String? = null,
    @SerializedName("cashbackAmount")       val cashbackAmount : String? = null,
    @SerializedName("currencyCode")         val currencyCode : String? = null,
    @SerializedName("transactionType")      val transactionType : String? = null,
    @SerializedName("cardCheckMode")        val cardCheckMode : String? = null,
    @SerializedName("cardCheckTimeout")     val cardCheckTimeout : Int? = null,
    @SerializedName("forceOnline")          val forceOnline : Boolean? = null,
    @SerializedName("supportFallback")      val supportFallback : Boolean? = null,
    @SerializedName("supportDRL")           val supportDRL : Boolean? = null,
    @SerializedName("enableBeeper")         val enableBeeper : Boolean? = null,
    @SerializedName("enableRefundCVM")      val enableRefundCVM : Boolean? = null,
    @SerializedName("forceInputPIN")        val forceInputPIN : Boolean? = null
)
