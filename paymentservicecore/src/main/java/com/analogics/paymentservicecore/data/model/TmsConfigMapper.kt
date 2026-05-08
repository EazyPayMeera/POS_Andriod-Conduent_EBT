package com.analogics.paymentservicecore.models

import android.content.Context
import com.analogics.paymentservicecore.data.model.PosConfig

/**
 * Mapper responsible for converting raw TMS response map
 * into strongly typed PosConfig model.
 *
 * This is used during terminal configuration download.
 */
object TmsConfigMapper {

    /**
     * Converts key-value TMS response into PosConfig object.
     *
     * @param context Application context (used by PosConfig)
     * @param map Raw configuration received from TMS server
     * @return Fully populated PosConfig object
     */
    fun mapToPosConfig(context: Context, map: Map<String, String>): PosConfig {

        val config = PosConfig(context)
        // ---------------------------
        // Network configuration
        // ---------------------------
        config.baseUrl = map["Host_URL"]
        config.port = map["Port"]?.toIntOrNull()
        config.hosttimeout = map["Timeout"]?.toIntOrNull()

        // ---------------------------
        // Merchant identifiers
        // ---------------------------
        config.merchantId = map["MerchantID"]
        config.terminalId = map["TerminalID"]
        config.procId = map["ProcId"]

        // ---------------------------
        // Merchant display details
        // ---------------------------
        config.merchantNameLocation = map["MerchNameLoc"]
        config.merchantBankName = map["MerchBankname"]

        // ---------------------------
        // Merchant classification
        // ---------------------------
        config.merchantCategoryCode = map["MCC"]
        config.merchantType = map["MCC"]

        // ---------------------------
        // Regulatory / regional info
        // ---------------------------
        config.fnsNumber = map["FNSNumber"]
        config.stateCode = map["StateCode"]
        config.countyCode = map["CountyCode"]
        config.postalServiceCode = map["PostalServiceCode"]

        // ---------------------------
        //  EMV CONFIG
        // ---------------------------
        map["EMV_CONFIG"]?.let {
            config.emvConfigJson = it
        }
        // ---------------------------
        //  CAP KEYS
        // ---------------------------
        map["CAP_KEYS"]?.let {
            config.capKeysJson = it
        }

        return config
    }
}