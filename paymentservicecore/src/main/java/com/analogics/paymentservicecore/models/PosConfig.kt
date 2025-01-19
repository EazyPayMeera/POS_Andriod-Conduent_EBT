package com.analogics.paymentservicecore.models

import android.content.Context
import android.os.Build
import com.analogics.paymentservicecore.logger.AppLogger
import com.analogics.securityframework.handler.SharedPrefHandler
import com.google.gson.annotations.SerializedName
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class PosConfig @Inject constructor(@ApplicationContext val context: Context) {

    /* Merchant & Cashier Info */
    @SerializedName("merchantId") var merchantId: String? = null
    @SerializedName("merchantCategoryCode") var merchantCategoryCode: String? = null
    @SerializedName("merchantNameLocation") var merchantNameLocation: String? = null
    @SerializedName("batchId") var batchId: String? = null
    @SerializedName("terminalId") var terminalId: String? = null
    @SerializedName("cashierId") var cashierId: String? = null
    @SerializedName("loginId") var loginId: String? = null
    @SerializedName("loginPassword") var loginPassword: String? = null
    @SerializedName("language") var language: String? = null
    @SerializedName("customerCareNumber") var customerCareNumber: String? = null
    @SerializedName("inactivityTimeout") var inactivityTimeout: Int? = null

    /* Device Info */
    @SerializedName("deviceSN") var deviceSN: String? = null
    @SerializedName("deviceMake") var deviceMake: String? = null
    @SerializedName("deviceModel") var deviceModel: String? = null
    @SerializedName("timeZone") var timeZone: String? = null
    @SerializedName("devicePublicKey") var devicePublicKey: String? = null
    @SerializedName("devicePrivateKey") var devicePrivateKey: String? = null

    /* Transaction Specific Config */
    @SerializedName("currencyCode") var currencyCode: String? = null
    @SerializedName("tipPercent1") var tipPercent1: Double? = null
    @SerializedName("tipPercent2") var tipPercent2: Double? = null
    @SerializedName("tipPercent3") var tipPercent3: Double? = null
    @SerializedName("vatPercent")  var vatPercent: Double? = null
    @SerializedName("SGSTPercent") var SGSTPercent: Double? = null

    /* Receipt Specific Config */
    @SerializedName("header1") var header1: String? = null
    @SerializedName("header2") var header2: String? = null
    @SerializedName("header3") var header3: String? = null
    @SerializedName("header4") var header4: String? = null
    @SerializedName("footer1") var footer1: String? = null
    @SerializedName("footer2") var footer2: String? = null
    @SerializedName("footer3") var footer3: String? = null
    @SerializedName("footer4") var footer4: String? = null

    /* Toggles */
    @SerializedName("isDemoMode") var isDemoMode: Boolean? = false
    @SerializedName("isAutoPrintReport") var isAutoPrintReport: Boolean? = false
    @SerializedName("isAutoPrintMerchant") var isAutoPrintMerchant: Boolean? = false
    @SerializedName("isAutoPrintCustomer") var isAutoPrintCustomer: Boolean? = false
    @SerializedName("isPromptInvoiceNo") var isPromptInvoiceNo: Boolean? = false
    @SerializedName("isTipEnabled") var isTipEnabled: Boolean? = false
    @SerializedName("isTaxEnabled") var isTaxEnabled: Boolean? = false
    @SerializedName("isInactivityTimeout") var isInactivityTimeout: Boolean? = false
    @SerializedName("isBatchId") var isBatchId: Boolean? = false

    /* State Management */
    @SerializedName("isLoggedIn") var isLoggedIn: Boolean? = false
    @SerializedName("isPaymentSDKInit") var isPaymentSDKInit: Boolean? = false
    @SerializedName("isOnboardingComplete") var isOnboardingComplete: Boolean? = false
    @SerializedName("isActivationDone") var isActivationDone: Boolean? = false

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