package com.eazypaytech.posafrica.device

import android.content.Context
import android.util.Log
import com.analogics.paymentservicecore.domain.repository.emvService.EmvServiceRepository
import com.eazypaytech.tpaymentcore.repository.EmvWrapperRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceInfoProvider @Inject constructor() {

    suspend fun getSerialNumber(context: Context): String {
        return try {
            EmvWrapperRepository.getDeviceSerialNumberSafe(context)
                .ifEmpty { "UNKNOWN" }
        } catch (e: Exception) {
            "UNKNOWN"
        }
    }
}

