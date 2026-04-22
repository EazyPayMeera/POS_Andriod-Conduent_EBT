package com.eazypaytech.posafrica.device

import android.content.Context
import android.util.Log
import com.analogics.paymentservicecore.domain.repository.emvService.EmvServiceRepository
import com.eazypaytech.tpaymentcore.repository.EmvWrapperRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceInfoProvider @Inject constructor(
    //private val emvWrapperRepository: EmvWrapperRepository
    //private val emvServiceRepository: EmvServiceRepository
) {

    suspend fun getSerialNumber(context: Context): String {
        return try {
            /*emvWrapperRepository.getDeviceSerialNumber(context)
                .ifEmpty { "UNKNOWN" }*/
            //emvServiceRepository.getDeviceSerialNumber(context).ifEmpty { "UNKNOWN" }
            EmvWrapperRepository.getDeviceSerialNumberSafe(context)
                .ifEmpty { "UNKNOWN" }
        } catch (e: Exception) {
            "UNKNOWN"
        }
    }
}

/*
@Singleton
class DeviceInfoProvider @Inject constructor() {

    suspend fun getSerialNumber(context: Context): String {
        return try {
            val deviceService = EmvWrapperRepository.getDeviceService(context)

            val devInfo = deviceService?.getDevInfo()

            val sn = devInfo?.getString("sn")  // 🔥 SAFE (no dependency on DeviceInfoConstants)

            Log.d("TMS", "Device SN from YSDK: $sn")

            sn ?: "UNKNOWN"
        } catch (e: Exception) {
            Log.e("TMS", "Error getting SN", e)
            "UNKNOWN"
        }
    }
}*/
