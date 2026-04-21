package com.eazypaytech.hardwarecore.utils

import android.content.Context
import android.util.Log
import com.eazypaytech.hardwarecore.BuildConfig
import com.eazypaytech.tpaymentcore.repository.EmvWrapperRepository


object HardwareUtils {
    fun getDeviceSN() : String
    {
        //return DeviceManager().deviceId
        return if(BuildConfig.HW_TYPE == "MOREFUN") "80042414067304" else "80042414067318"
        // TODO:Hardcoded as of now for testing with
        // 80042414067304 = EP000101 - EPTEST000000101
        // 80042414067318 = YL000002 - YLTEST000000002
    }

    suspend fun injectTMKKey(tmk: String,kcv: String,context: Context? = null): Boolean {
        return try {
            EmvWrapperRepository.injectTMKKey(tmk, kcv, context)
        } catch (exception: Exception) {
            Log.e("HARDWARE_UTILS", exception.message.toString())
            false
        }
    }

    suspend fun injectWorkingKey(pinKey: String, context: Context? = null) : Boolean
    {
        try {
            return EmvWrapperRepository.injectWorkingKey(pinKey, context)
        }catch (exception : Exception)
        {
            Log.e("HARDWARE_UTILS", exception.message.toString())
            return false
        }
    }
}