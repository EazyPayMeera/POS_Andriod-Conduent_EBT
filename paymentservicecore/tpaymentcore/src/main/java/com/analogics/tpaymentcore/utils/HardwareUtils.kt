package com.analogics.tpaymentcore.utils

import android.device.DeviceManager

object HardwareUtils {
    fun getDeviceSN() : String
    {
        return DeviceManager().tidsn
    }
}