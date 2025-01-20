package com.eazypaytech.paymentservicecore.model.emv

import com.google.gson.annotations.SerializedName

data class TermConfig(
    @SerializedName("merchantCategoryCode") val merchantCategoryCode : String? = null,
    @SerializedName("merchantIdentifier")   val merchantIdentifier : String? = null,
    @SerializedName("merchantNameLocation") val merchantNameLocation : String? = null,
    @SerializedName("terminalIdentifier")   val terminalIdentifier : String? = null,
    @SerializedName("ifdSerialNumber")      val ifdSerialNumber : String? = null,
    @SerializedName("cardCheckTimeout")     val cardCheckTimeout : String? = null,
    @SerializedName("enableBeeper")         val enableBeeper : Boolean? = null
)
