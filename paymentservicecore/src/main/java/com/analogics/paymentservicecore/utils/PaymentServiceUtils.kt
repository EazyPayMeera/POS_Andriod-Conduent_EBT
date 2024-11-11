package com.analogics.paymentservicecore.utils

import android.R
import com.analogics.securityframework.handler.SecureKeyHandler
import com.analogics.tpaymentcore.utils.HardwareUtils
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.security.KeyPair
import java.security.PrivateKey

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

    fun injectTMK(ipek: String?, ksn: String?, kcv: String?) : Boolean {
        if (ipek?.isNotEmpty() == true && ksn?.isNotEmpty() == true) {
            HardwareUtils.injectTMK(ipek, ksn)
            return true
        }
        return false
    }

}