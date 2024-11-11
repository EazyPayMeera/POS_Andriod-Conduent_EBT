package com.analogics.tpaymentcore.utils

import android.device.DeviceManager
import android.device.SEManager
import android.util.Log
import com.analogics.tpaymentcore.constants.EncryptionConstants

object HardwareUtils {
    fun getDeviceSN() : String
    {
        return DeviceManager().deviceId
    }

    @OptIn(ExperimentalStdlibApi::class)
    fun injectTMK(ipek: String, ksn: String) : Boolean
    {
        try {
            var ipekBytes = ipek.hexToByteArray()
            var ksnBytes = ksn.hexToByteArray()
            var sm = SEManager()
            sm.downloadKeyDukpt(EncryptionConstants.KEY_TYPE_TMK, null,0, ksnBytes, ksnBytes.size, ipekBytes, ipekBytes.size)
            return true
        }catch (exception : Exception)
        {
            Log.e("HARDWARE_UTILS", exception.message.toString())
            return false
        }
    }
}