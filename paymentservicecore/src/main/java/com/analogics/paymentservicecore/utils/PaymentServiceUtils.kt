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

    fun String.batchClosingTtlAmt(): String {


        return ""
    }

    inline fun <reified T> jsonStringToObjectList(response: String): List<T> {
        return Gson().fromJson(response, object : TypeToken<T>() {}.type)
    }

    fun generateRsaKey(): KeyPair {
        var keyPair = SecureKeyHandler.generateRsaKey()
        return keyPair
    }

    fun getDeviceSN(): String {
        return HardwareUtils.getDeviceSN()
    }


    suspend fun injectKeys(
        tmk: String,
        pinKey: String,
        kcv: String,
        context: Context? = null
    ): Boolean {

        Log.d("KEY_INJECT", "----- Inject Keys Start -----")
        Log.d("KEY_INJECT", "TMK: $tmk")
        Log.d("KEY_INJECT", "PIN Key: $pinKey")
        Log.d("KEY_INJECT", "KCV: $kcv")
        val pin = pinKey.take(32)
        if (tmk.isNotEmpty() && pinKey.isNotEmpty() && kcv.isNotEmpty()) {

            val tmkResult = HardwareUtils.injectTMKKey(tmk, kcv, context)
            Log.d("KEY_INJECT", "TMK Injection Result: $tmkResult")

            val pinResult = HardwareUtils.injectWorkingKey(pin, context)
            Log.d("KEY_INJECT", "Working Key Injection Result: $pinResult")

            val finalResult = tmkResult && pinResult
            Log.d("KEY_INJECT", "Final Result: $finalResult")

            return finalResult
        }

        Log.e("KEY_INJECT", "Keys are empty!")
        return false
    }



}

fun Double?.toDecimalFormat(decimalPlaces: Int = 2, symbol: Symbol?=Symbol(type = Symbol.Type.NONE), withSeparator: Boolean = false): String
{
    return formatAmount(this?:0.00,decimalPlaces,symbol,withSeparator)
}

fun maskPAN(pan: String): String {
    if (pan.length <= 4) {
        return pan
    }
    val maskedPart = "*".repeat(pan.length - 4) // Mask all but the last 4 characters
    val lastPart = pan.takeLast(4)             // Last 4 characters

    return "$maskedPart$lastPart".chunked(4)
        .joinToString(" ")
}

fun maskPANReceiptStyle(pan: String): String {
    if (pan.length <= 10) return pan // too short to mask middle

    val first6 = pan.take(6)                 // first 6 digits
    val last4 = pan.takeLast(4)              // last 4 digits
    val middleMask = "x".repeat(pan.length - 10) // middle digits as x

    return first6 + middleMask + last4
}