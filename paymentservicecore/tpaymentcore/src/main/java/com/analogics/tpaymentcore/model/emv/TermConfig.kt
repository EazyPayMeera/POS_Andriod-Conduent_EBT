package com.analogics.tpaymentcore.model.emv

import com.google.gson.annotations.SerializedName

data class TermConfig(
    @SerializedName("merchantCategoryCode") val merchantCategoryCode : String? = null,
    @SerializedName("merchantIdentifier")   val merchantIdentifier : String? = null,
    @SerializedName("merchantNameLocation") val merchantNameLocation : String? = null,
    @SerializedName("terminalIdentifier")   val terminalIdentifier : String? = null,
    @SerializedName("countryCode")          val countryCode : String? = null,
    @SerializedName("currencyCode")         val currencyCode : String? = null,
    @SerializedName("currencyExponent")     val currencyExponent : String? = null,
    @SerializedName("terminalType")         val terminalType : String? = null,
    @SerializedName("terminalCapabilities") val terminalCapabilities : String? = null,
    @SerializedName("addlTerminalCapabilities") val addlTerminalCapabilities : String? = null,
    @SerializedName("ifdSerialNumber")      val ifdSerialNumber : String? = null,
    @SerializedName("randomTransSwitch")    val randomTransSwitch : String? = null,
    @SerializedName("execFileCheckSwitch")  val execFileCheckSwitch : String? = null
)
