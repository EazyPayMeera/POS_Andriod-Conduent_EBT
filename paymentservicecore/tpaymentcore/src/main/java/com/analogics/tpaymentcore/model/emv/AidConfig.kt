package com.analogics.tpaymentcore.model.emv

import com.google.gson.annotations.SerializedName

data class AidConfig(
    /* Common */
    @SerializedName("acquirerId")           val acquirerId : String?=null,
    @SerializedName("appSelIndicator")      val appSelIndicator : String? = null,
    @SerializedName("terminalCountryCode")  val terminalCountryCode : String? = null,
    @SerializedName("terminalCapabilities") val terminalCapabilities : String? = null,
    @SerializedName("terminalFloorLimit")   val terminalFloorLimit : String? = null,
    @SerializedName("terminalAppPriority")  val terminalAppPriority : String? = null,
    @SerializedName("tacDefault")           val tacDefault : String? = null,
    @SerializedName("tacDenial")            val tacDenial : String? = null,
    @SerializedName("tacOnline")            val tacOnline : String? = null,
    @SerializedName("defaultDDOL")          val defaultDDOL : String? = null,
    @SerializedName("defaultTDOL")          val defaultTDOL : String? = null,
    @SerializedName("threshold")            val threshold : String? = null,
    @SerializedName("targetPercentage")     val targetPercentage : String? = null,
    @SerializedName("maxTargetPercentage")  val maxTargetPercentage : String? = null,
    @SerializedName("additionalTags")       val additionalTags : String? = null,

    /* AID Specific */
    @SerializedName("aid")                  val aid : String? = null,
    @SerializedName("appVersion")           val appVersion : String? = null,

    @SerializedName("aidList")              val aidList : List<AidConfig>? = null,

    /* Contact override */
    @SerializedName("contact")              val contact : AidConfig? = null,

    /* Contactless override */
    @SerializedName("contactless")          val contactless : AidConfig? = null
)
