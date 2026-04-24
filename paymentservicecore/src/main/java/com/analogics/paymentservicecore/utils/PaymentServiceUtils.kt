package com.analogics.paymentservicecore.utils

import android.content.Context
import android.util.Log
import com.analogics.builder_core.data.model.Symbol
import com.analogics.builder_core.utils.formatAmount
import com.eazypaytech.hardwarecore.utils.HardwareUtils
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.security.KeyPair

/**
 * Utility object for Payment Service layer.
 *
 * Provides:
 * 1. JSON serialization/deserialization helpers
 * 2. Object transformation utilities
 * 3. Security key injection utilities
 * 4. Common formatting extensions (amount formatting)
 *
 * Acts as a shared helper between SDK, repository, and domain layers.
 */
object PaymentServiceUtils {

    /**
     * Converts JSON string → Kotlin object
     *
     * @param response JSON string
     * @return Parsed object of type T or null if parsing fails
     */
    inline fun <reified T> jsonStringToObject(response: String): T? {
        return Gson().fromJson(response, object : TypeToken<T>() {}.type)
    }

    /**
     * Converts Kotlin object → JSON string
     *
     * @param response object to serialize
     * @return JSON string representation
     */
    inline fun <reified T> objectToJsonString(response: T): String {
        val gson = Gson()
        return gson.toJson(response, object : TypeToken<T>() {}.type)
    }

    /**
     * Transforms one object type → another using JSON bridge conversion.
     *
     * ⚠️ Note:
     * This is a runtime conversion (not compile-time safe).
     * Useful for mapping SDK models ↔ app models.
     *
     * @param input source object
     * @return transformed object of type T or null
     */
    inline fun <reified T> transformObject(input: Any?): T? {
        return Gson().fromJson(Gson().toJson(input), object : TypeToken<T>() {}.type)
    }

//    fun getDeviceSN(): String {
//        return HardwareUtils.getDeviceSN()
//    }

    /**
     * Injects encryption keys into hardware module.
     *
     * Flow:
     * 1. Inject TMK (Terminal Master Key)
     * 2. Inject PIN working key
     * 3. Validate both operations
     *
     * @param tmk Terminal Master Key
     * @param pinKey PIN encryption key (trimmed to 32 chars)
     * @param kcv Key Check Value for validation
     * @param context Optional Android context for hardware access
     * @return true if both key injections succeed, false otherwise
     */
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

/**
 * Extension function: Formats Double into currency/amount string.
 *
 * Features:
 * - Decimal precision control
 * - Optional currency symbol
 * - Thousand separator support
 *
 * @param decimalPlaces Number of decimal places (default = 2)
 * @param symbol Currency symbol wrapper
 * @param withSeparator Adds thousand separators if true
 */
fun Double?.toDecimalFormat(decimalPlaces: Int = 2, symbol: Symbol?=Symbol(type = Symbol.Type.NONE), withSeparator: Boolean = false): String
{
    return formatAmount(this?:0.00,decimalPlaces,symbol,withSeparator)
}
