package com.analogics.networkservicecore.tms.repository

import android.content.Context
import android.util.Log
import com.analogics.networkservicecore.tms.TmsRequestSigner
import com.analogics.networkservicecore.tms.api.TmsApiService
import com.eazypaytech.networkservicecore.BuildConfig
//import com.eazypaytech.paymentservicecore.models.PosConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TmsRepository @Inject constructor(
    private val api: TmsApiService,
    @ApplicationContext private val context: Context
) {

    suspend fun fetchConfig(sn: String): Map<String, String>? {
        return try {

            Log.d("TMS", "SN: $sn")

            val params = mapOf(
                "sn" to sn,
                "access_key" to BuildConfig.TMS_ACCESS_KEY,
                "timestamp" to System.currentTimeMillis().toString(),
                "nonce" to UUID.randomUUID().toString()
            )

            val sign = TmsRequestSigner.generateSignature(params, BuildConfig.TMS_SECRET)

            val response = api.getDeviceParams(
                sn = sn,
                accessKey = params["access_key"]!!,
                sign = sign,
                isFullUpdate = 1
            )

            Log.d("TMS", "Full Response: $response")

            val terminalParam = response.data?.terminal_param
            val finalMap = mutableMapOf<String, String>()

            // PRIMARY: new structure
            val setupConfig = terminalParam?.ebt_device_setup

            // 1) Base device/host config
            val baseConfig = setupConfig?.get("")
                ?: terminalParam?.config_xml?.get("")
                ?: terminalParam?.ebt_device_config?.values?.firstOrNull()

            baseConfig?.let { finalMap.putAll(it) }

            // 2) EMV config
            val emvJson = setupConfig
                ?.get("EMV_AIDs")
                ?.get("AID Config")
                ?: terminalParam?.config_xml?.get("EMV_AIDs")?.get("AID Config")
                ?: terminalParam?.config_xml?.get("EMV")?.get("EMV Config")

            if (!emvJson.isNullOrEmpty()) {
                finalMap["EMV_CONFIG"] = emvJson
            }

            // 3) CAP keys
            val capkJson = setupConfig
                ?.get("EMV_CAPKeys")
                ?.get("CAPKeys")
                ?: terminalParam?.config_xml?.get("EMV_CAPKeys")?.get("CAPKeys")

            if (!capkJson.isNullOrEmpty()) {
                finalMap["CAP_KEYS"] = capkJson
            }

            return finalMap

        } catch (e: Exception) {
            Log.e("TMS", "API FAILED", e)
            null
        }
    }
}