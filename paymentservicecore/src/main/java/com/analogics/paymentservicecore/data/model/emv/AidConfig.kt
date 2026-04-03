package com.analogics.paymentservicecore.data.model.emv

import com.google.gson.annotations.SerializedName

data class AidConfig(
    /* Terminal Configuration */
    @SerializedName("merchantIdentifier")   var merchantIdentifier : String? = null,
    @SerializedName("merchantCategoryCode") var merchantCategoryCode : String? = null,
    @SerializedName("merchantNameLocation") var merchantNameLocation : String? = null,
    @SerializedName("terminalIdentifier")   var terminalIdentifier : String? = null,
    @SerializedName("ifdSerialNumber")      var ifdSerialNumber : String? = null,
    @SerializedName("cardCheckTimeout")     var cardCheckTimeout : String? = null,
    @SerializedName("enableBeeper")         var enableBeeper : Boolean? = null,

    /* Common EMV Parameters */
    @SerializedName("acquirerId")           val acquirerId : String?=null,
    @SerializedName("appSelIndicator")      val appSelIndicator : String? = null,
    @SerializedName("currencyCode")         val currencyCode : String? = null,
    @SerializedName("currencyExponent")     val currencyExponent : String? = null,
    @SerializedName("terminalCountryCode")  val terminalCountryCode : String? = null,
    @SerializedName("terminalType")         val terminalType : String? = null,
    @SerializedName("terminalCapabilities") val terminalCapabilities : String? = null,
    @SerializedName("addlTerminalCapabilities") val addlTerminalCapabilities : String? = null,
    @SerializedName("terminalFloorLimit")   val terminalFloorLimit : String? = null,
    @SerializedName("terminalFloorLimitCheck")  val terminalFloorLimitCheck : String? = null,
    @SerializedName("terminalAppPriority")  val terminalAppPriority : String? = null,
    @SerializedName("tacDefault")           val tacDefault : String? = null,
    @SerializedName("tacDenial")            val tacDenial : String? = null,
    @SerializedName("tacOnline")            val tacOnline : String? = null,
    @SerializedName("defaultDDOL")          val defaultDDOL : String? = null,
    @SerializedName("defaultTDOL")          val defaultTDOL : String? = null,
    @SerializedName("threshold")            val threshold : String? = null,
    @SerializedName("targetPercentage")     val targetPercentage : String? = null,
    @SerializedName("maxTargetPercentage")  val maxTargetPercentage : String? = null,
    @SerializedName("transactionType")      val transactionType : String? = null,
    @SerializedName("supportFallback")      val supportFallback : Boolean? = null,
    @SerializedName("supportDRL")           val supportDRL : Boolean? = null,
    @SerializedName("supportRandomTrans")   val enableRandomTrans : Boolean? = null,
    @SerializedName("supportExceptionFile") val supportExceptionFile : Boolean? = null,
    @SerializedName("supportSM")            val supportSM : Boolean? = null,
    @SerializedName("supportVelocityCheck") val supportVelocityCheck : Boolean? = null,
    @SerializedName("enableRefundCVM")      val enableRefundCVM : Boolean? = null,
    @SerializedName("forceInputPIN")        val forceInputPIN : Boolean? = null,

    /* AID Specific */
    @SerializedName("aid")                  val aid : String? = null,
    @SerializedName("appVersion")           val appVersion : String? = null,
    @SerializedName("aidList")              val aidList : List<AidConfig>? = null,

    /* Contact override */
    @SerializedName("contact")              val contact : AidConfig? = null,

    /* Contactless override */
    @SerializedName("contactless")                  val contactless : AidConfig? = null,
    @SerializedName("ttq")                          val ttq : String? = null,
    @SerializedName("rdrCtlsFloorLimit")            val rdrCtlsFloorLimit : String? = null,
    @SerializedName("rdrCtlsTransLimit")            val rdrCtlsTransLimit : String? = null,
    @SerializedName("rdrCVMRequiredLimit")          val rdrCVMRequiredLimit : String? = null,
    @SerializedName("statusCheckSupported")         val statusCheckSupported : String? = null,
    @SerializedName("zeroAmountAllowed")            val zeroAmountAllowed : String? = null,
    @SerializedName("zeroAmountOfflineAllowed")     val zeroAmountOfflineAllowed : String? = null,

    /* Visa Specific */
    @SerializedName("disableProcRestrictions")      val disableProcRestrictions : String? = null,
    @SerializedName("limitSwitch")                  val limitSwitch : String? = null,
    @SerializedName("programID")                    val programID : String? = null,

    /* Mastercard Specific */
    @SerializedName("cardDataInputCapability")      val cardDataInputCapability : String? = null,
    @SerializedName("kernelConfiguration")          val kernelConfiguration : String? = null,
    @SerializedName("cvmCapabilityCVMRequired")     val cvmCapabilityCVMRequired : String? = null,
    @SerializedName("cvmCapabilityNoCVMRequired")   val cvmCapabilityNoCVMRequired : String? = null,
    @SerializedName("magCVMCapabilityCVMRequired")  val magCVMCapabilityCVMRequired : String? = null,
    @SerializedName("magCVMCapabilityNoCVMRequired") val magCVMCapabilityNoCVMRequired : String? = null,
    @SerializedName("securityCapability")           val securityCapability : String? = null,
    @SerializedName("defaultUDOL")                  val defaultUDOL : String? = null,
    @SerializedName("rdrCtlsTransLimitODCVM")       val rdrCtlsTransLimitODCVM : String? = null,
    @SerializedName("rdrCtlsTransLimitNoODCVM")     val rdrCtlsTransLimitNoODCVM : String? = null,
    @SerializedName("riskManagementData")           val riskManagementData : String? = null,
    @SerializedName("dsvnTerm")                     val dsvnTerm : String? = null,

    /* Amex Specific */
    @SerializedName("ctlsRdrCapabilities")          val ctlsRdrCapabilities : String? = null,
    @SerializedName("enhancedCtlsRdrCapabilities")  val enhancedCtlsRdrCapabilities : String? = null,

    /* Rupay Specific */
    @SerializedName("addlTerminalCapabilitiesExtension") val addlTerminalCapabilitiesExtension : String? = null,
    @SerializedName("serviceDataFormat")                  val serviceDataFormat : String? = null,

    /* Supply additional EMV tags */
    @SerializedName("additionalTags")               val additionalTags : String? = null
)
