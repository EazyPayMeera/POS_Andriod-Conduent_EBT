package com.eazypaytech.tpaymentcore.utils

import android.device.DeviceManager
import android.device.SEManager
import android.util.Log
import com.eazypaytech.tpaymentcore.constants.EncryptionConstants
import com.urovo.sdk.pinpad.PinPadProviderImpl

object HardwareUtils {
    fun getDeviceSN() : String
    {
        return DeviceManager().deviceId
    }

    @OptIn(ExperimentalStdlibApi::class)
    fun injectTMK(tmk: String, kcv: String) : Boolean
    {
        try {
            return PinPadProviderImpl.getInstance().loadMainKey(EncryptionConstants.KEY_INDEX_MAIN_KEY, tmk.hexToByteArray(), kcv.hexToByteArray())
        }catch (exception : Exception)
        {
            Log.e("HARDWARE_UTILS", exception.message.toString())
            return false
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    fun injectDukptPinKey(ipek: String, ksn: String) : Boolean
    {
        try {
            var ipekBytes = ipek.hexToByteArray()
            var ksnBytes = ksn.hexToByteArray()
            return PinPadProviderImpl.getInstance().downloadKeyDukpt(EncryptionConstants.DUKPT_KEY_SET_PIN, null,0, ksnBytes, ksnBytes.size, ipekBytes, ipekBytes.size) == 0 &&
                    PinPadProviderImpl.getInstance().downloadKeyDukpt(EncryptionConstants.DUKPT_KEY_SET_TDK, null,0, ksnBytes, ksnBytes.size, ipekBytes, ipekBytes.size) == 0 &&
                    PinPadProviderImpl.getInstance().downloadKeyDukpt(EncryptionConstants.DUKPT_KEY_SET_EMV, null,0, ksnBytes, ksnBytes.size, ipekBytes, ipekBytes.size) == 0
        }catch (exception : Exception)
        {
            Log.e("HARDWARE_UTILS", exception.message.toString())
            return false
        }
    }
}