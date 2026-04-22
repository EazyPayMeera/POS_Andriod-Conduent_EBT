package com.analogics.paymentservicecore.models

import android.content.Context
import com.analogics.paymentservicecore.data.model.PosConfig


object TmsConfigMapper {

    fun mapToPosConfig(context: Context, map: Map<String, String>): PosConfig {

        val config = PosConfig(context)

        config.baseUrl = map["Host_URL"]
        config.port = map["Port"]?.toIntOrNull()
        config.hosttimeout = map["Timeout"]?.toIntOrNull()

        config.merchantId = map["MerchantID"]
        config.terminalId = map["TerminalID"]
        config.procId = map["ProcId"]

        config.merchantNameLocation = map["MerchNameLoc"]
        config.merchantBankName = map["MerchBankname"]

        config.merchantCategoryCode = map["MCC"]
        config.merchantType = map["MCC"]

        config.fnsNumber = map["FNSNumber"]
        config.stateCode = map["StateCode"]
        config.countyCode = map["CountyCode"]
        config.postalServiceCode = map["PostalServiceCode"]

        return config
    }
}