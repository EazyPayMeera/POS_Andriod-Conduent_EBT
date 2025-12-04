package com.eazypaytech.paymentservicecore.utils

import android.content.Context
import android.util.Log
import com.eazypaytech.builder_core.model.Symbol
import com.eazypaytech.builder_core.utils.formatAmount
import com.eazypaytech.securityframework.handler.SecureKeyHandler
import com.eazypaytech.tpaymentcore.utils.HardwareUtils
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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

    suspend fun injectKeys(ipek: String?, ksn: String?, kcv: String?, context : Context?=null) : Boolean {
        if (ipek?.isNotEmpty() == true && ksn?.isNotEmpty() == true && kcv?.isNotEmpty() == true) {
            /* Here DUKPT Pin Key serves as main key as well. Used for encryption of track data */
            return HardwareUtils.injectTMK(ipek, kcv, context) && HardwareUtils.injectDukptPinKey(ipek,ksn, context)
        }
        return false
    }

    suspend fun injectWorkingPinKey(
        workingKeyHex: String,
        context: Context? = null
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            HardwareUtils.injectWorkingPinKey(workingKeyHex, context)
        } catch (exception: Exception) {
            Log.e("HARDWARE_UTILS", exception.message.toString())
            false
        }
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