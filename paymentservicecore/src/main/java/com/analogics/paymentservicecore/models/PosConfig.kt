package com.analogics.paymentservicecore.models

import android.content.Context
import android.os.Build
import com.analogics.paymentservicecore.logger.AppLogger
import com.analogics.securityframework.handler.SharedPrefHandler
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class PosConfig @Inject constructor(val context: Context) {

    /* Merchant & Cashier Info */
    @SerializedName("MerchantId") var merchantId: String? = null
    @SerializedName("MerchantCategoryCode") var merchantCategoryCode: String? = null
    @SerializedName("MerchantNameLocation") var merchantNameLocation: String? = null
    @SerializedName("BatchId") var batchId: String? = null
    @SerializedName("TerminalId") var terminalId: String? = null
    @SerializedName("CashierId") var cashierId: String? = null
    @SerializedName("LoginId") var loginId: String? = null
    @SerializedName("LoginPassword") var loginPassword: String? = null
    @SerializedName("Language") var language: String? = null

    /* Device Info */
    @SerializedName("DeviceSN") var deviceSN: String? = null
    @SerializedName("DeviceMake") var deviceMake: String? = null
    @SerializedName("DeviceModel") var deviceModel: String? = null
    @SerializedName("TimeZone") var timeZone: String? = null

    /* Transaction Specific Config */
    @SerializedName("CurrencyCode") var currencyCode: String? = null
    @SerializedName("TipPercent1") var tipPercent1: Double? = null
    @SerializedName("TipPercent2") var tipPercent2: Double? = null
    @SerializedName("TipPercent3") var tipPercent3: Double? = null
    @SerializedName("CGSTPercent") var CGSTPercent: Double? = null
    @SerializedName("SGSTPercent") var SGSTPercent: Double? = null

    /* Receipt Specific Config */
    @SerializedName("Header1") var header1: String? = null
    @SerializedName("Header2") var header2: String? = null
    @SerializedName("Header3") var header3: String? = null
    @SerializedName("Header4") var header4: String? = null
    @SerializedName("Footer1") var footer1: String? = null
    @SerializedName("Footer2") var footer2: String? = null
    @SerializedName("Footer3") var footer3: String? = null
    @SerializedName("Footer4") var footer4: String? = null

    /* Toggles */
    @SerializedName("IsDemoMode") var isDemoMode: Boolean? = false
    @SerializedName("IsAutoPrintReport") var isAutoPrintReport: Boolean? = false
    @SerializedName("IsAutoPrintMerchant") var isAutoPrintMerchant: Boolean? = false
    @SerializedName("IsAutoPrintCustomer") var isAutoPrintCustomer: Boolean? = false
    @SerializedName("IsPromptInvoiceNo") var isPromptInvoiceNo: Boolean? = false
    @SerializedName("IsTipEnabled") var isTipEnabled: Boolean? = false
    @SerializedName("IsTaxEnabled") var isTaxEnabled: Boolean? = false

    /* State Management */
    @SerializedName("IsLoggedIn") var isLoggedIn: Boolean? = false
    @SerializedName("IsPaymentSDKInit") var isPaymentSDKInit: Boolean? = false
    @SerializedName("IsOnboardingComplete") var isOnboardingComplete: Boolean? = false
            
    fun loadFromPrefs() : PosConfig
    {
        try {
            for (field in this.javaClass.declaredFields) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    if (field.genericType.typeName == "java.lang.Double") {
                        /* Shared Preferences doesn't have explicit setter for Double */
                        try {
                            if(SharedPrefHandler.getConfigVal(context, field.name)!=null && SharedPrefHandler.getConfigVal(context, field.name)!="null")
                                field.set(this,SharedPrefHandler.getConfigVal(context, field.name)?.toString()?.toDouble())
                            else
                                field.set(this,0.0)
                        }catch (e : Exception)
                        {
                            AppLogger.e(AppLogger.MODULE.POS_CONFIG,e.toString())
                        }
                    } else if(field.genericType.typeName in listOf("java.lang.String","java.lang.Boolean","java.lang.Long","java.lang.Float","java.lang.Int")) {
                        field.set(this, SharedPrefHandler.getConfigVal(context, field.name))
                    }
                }
                else
                {
                    //TODO("VERSION.SDK_INT < P")
                }
            }
        }catch (e:Exception)
        {
            AppLogger.e(AppLogger.MODULE.POS_CONFIG,e.toString())
        }
        return this
    }

    fun saveToPrefs() : PosConfig {
        try {
            for (field in this.javaClass.declaredFields) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    if(field.genericType.typeName in listOf("java.lang.String","java.lang.Boolean","java.lang.Long","java.lang.Double","java.lang.Float","java.lang.Int"))
                        SharedPrefHandler.setConfigVal(context, field.name, field.get(this))
                } else {
                    //TODO("VERSION.SDK_INT < P")
                }
            }
        } catch (e: Exception) {
            AppLogger.e(AppLogger.MODULE.POS_CONFIG, e.toString())
        }
        return this
    }
}