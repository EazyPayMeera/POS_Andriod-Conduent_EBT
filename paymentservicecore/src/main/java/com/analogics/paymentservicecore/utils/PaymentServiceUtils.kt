package com.analogics.paymentservicecore.utils

import android.content.Context
import android.util.Log
import com.analogics.builder_core.data.model.Symbol
import com.analogics.builder_core.utils.formatAmount
import com.analogics.securityframework.data.local.SecureKeyHandler
import com.eazypaytech.hardwarecore.utils.HardwareUtils
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.security.KeyPair

object PaymentServiceUtils {

    inline fun <reified T> jsonStringToObject(response: String): T? {
        return Gson().fromJson(response, object : TypeToken<T>() {}.type)
    }

    inline fun <reified T> objectToJsonString(response: T): String {
        val gson = Gson()
        return gson.toJson(response, object : TypeToken<T>() {}.type)
    }

    inline fun <reified T> transformObject(input: Any?): T? {
        return Gson().fromJson(Gson().toJson(input), object : TypeToken<T>() {}.type)
    }

//    fun getDeviceSN(): String {
//        return HardwareUtils.getDeviceSN()
//    }

    suspend fun injectKeys(tmk: String, pinKey: String, kcv: String, context: Context? = null): Boolean {
        val pin = pinKey.take(32)
        if (tmk.isNotEmpty() && pinKey.isNotEmpty() && kcv.isNotEmpty()) {

            val tmkResult = HardwareUtils.injectTMKKey(tmk, kcv, context)
            val pinResult = HardwareUtils.injectWorkingKey(pin, context)
            val finalResult = tmkResult && pinResult
            return finalResult
        }
        return false
    }
}

fun Double?.toDecimalFormat(decimalPlaces: Int = 2, symbol: Symbol?=Symbol(type = Symbol.Type.NONE), withSeparator: Boolean = false): String
{
    return formatAmount(this?:0.00,decimalPlaces,symbol,withSeparator)
}
