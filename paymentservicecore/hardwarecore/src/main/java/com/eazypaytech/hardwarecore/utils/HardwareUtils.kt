package com.eazypaytech.tpaymentcore.utils

import android.content.Context
import android.device.DeviceManager
import android.device.SEManager
import android.util.Log
import com.eazypaytech.tpaymentcore.constants.EncryptionConstants
import com.eazypaytech.tpaymentcore.repository.EmvSdkRequestRepository
import com.eazypaytech.tpaymentcore.repository.EmvWrapperRepository
import com.urovo.sdk.pinpad.PinPadProviderImpl

object HardwareUtils {
    fun getDeviceSN() : String
    {
        //return DeviceManager().deviceId
        return "80042414067304" // TODO:Hardcoded as of now for testing with EP000101 - EPTEST000000101
    }

    @OptIn(ExperimentalStdlibApi::class)
    suspend fun injectTMK(tmk: String, kcv: String, context: Context?=null) : Boolean
    {
        try {
            return EmvWrapperRepository.injectTMK(tmk, kcv, context)
        }catch (exception : Exception)
        {
            Log.e("HARDWARE_UTILS", exception.message.toString())
            return false
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    suspend fun injectDukptPinKey(ipek: String, ksn: String,context: Context?=null) : Boolean
    {
        try {
            return EmvWrapperRepository.injectDukptPinKey(ipek, ksn, context)
        }catch (exception : Exception)
        {
            Log.e("HARDWARE_UTILS", exception.message.toString())
            return false
        }
    }
}