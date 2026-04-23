package com.eazypaytech.hardwarecore.utils

import android.content.Context
import android.util.Log
import com.eazypaytech.hardwarecore.BuildConfig
import com.eazypaytech.tpaymentcore.repository.EmvWrapperRepository


object HardwareUtils {
    /**
     * Injects Terminal Master Key (TMK) into the EMV device.
     *
     * @param tmk The Terminal Master Key in encrypted/hex format.
     * @param kcv Key Check Value used to verify TMK integrity.
     * @param context Optional Android context required by underlying hardware layer.
     * @return true if TMK injection is successful, false otherwise.
     */
    suspend fun injectTMKKey(tmk: String,kcv: String,context: Context? = null): Boolean {
        return try {
            EmvWrapperRepository.injectTMKKey(tmk, kcv, context)
        } catch (exception: Exception) {
            Log.e("HARDWARE_UTILS", exception.message.toString())
            false
        }
    }

    /**
     * Injects working (PIN) key into the EMV device.
     *
     * This key is used for PIN encryption/decryption during transaction processing.
     *
     * @param pinKey The working PIN key in encrypted/hex format.
     * @param context Optional Android context required by underlying hardware layer.
     * @return true if working key injection is successful, false otherwise.
     */
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