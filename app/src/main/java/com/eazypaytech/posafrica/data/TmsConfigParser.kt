package com.eazypaytech.posafrica.data


import android.content.Context
import android.util.Log
import com.analogics.paymentservicecore.data.model.PosConfig
//import com.eazypaytech.paymentservicecore.models.PosConfig
import org.w3c.dom.Document
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

object TmsConfigParser {

    private const val TAG = "TMS_CONFIG"
    private const val FILE_PATH = "/sdcard/Download/config_parameters.xml"

    /*fun loadConfig(context: Context): PosConfig? {
        return try {
            val file = File("/sdcard/Download/config_parameters.xml")

            if (!file.exists()) return null

            val doc = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder()
                .parse(file)

            fun getTag(tag: String): String? {
                val node = doc.getElementsByTagName(tag)
                return if (node.length > 0) node.item(0).textContent else null
            }

            val baseUrl = getTag("PrimaryURL")
            val port = getTag("Port")?.toIntOrNull()
            val timeout = getTag("Timeout")?.toIntOrNull()
            val merchantId = getTag("MerchantID")
            val terminalId = getTag("TerminalID")
            val procid = getTag("ProcId")
            val merchNameLoc = getTag("MerchNameLoc")
            val merchbankName = getTag("MerchBankname")
            val mcc = getTag("MCC")
            val fnsNumber = getTag("FNSNumber")

            // ✅ Correct object creation
            val config = PosConfig(context)

            // ✅ Map values
            config.merchantId = merchantId
            config.terminalId = terminalId
            config.procId = procid
            config.merchantNameLocation = merchNameLoc
            config.merchantBankName = merchbankName
            config.merchantCategoryCode = mcc
            config.fnsNumber = fnsNumber

            // 👉 Store host config (you can map to existing fields or extend)
            config.baseUrl = baseUrl        // TEMP mapping (or create new field)
            config.port = port
            config.hosttimeout = timeout

            // Optional logging
            Log.d("TMS", "Config Loaded: $config")

            config

        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }*/
    fun loadFromAssets(context: Context): PosConfig? {
        return try {
            val inputStream = context.assets.open("config_parameters.xml")

            val doc = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder()
                .parse(inputStream)

            val baseUrl = doc.getElementsByTagName("PrimaryURL").item(0).textContent
            val port = doc.getElementsByTagName("Port").item(0).textContent.toInt()
            val timeout = doc.getElementsByTagName("Timeout").item(0).textContent.toInt()
            val merchantId = doc.getElementsByTagName("MerchantID").item(0).textContent
            val terminalId = doc.getElementsByTagName("TerminalID").item(0).textContent
            val procIdd = doc.getElementsByTagName("ProcId").item(0).textContent
            val merchNameLoc = doc.getElementsByTagName("MerchNameLoc").item(0).textContent
            val merchbankName = doc.getElementsByTagName("MerchBankname").item(0).textContent
            val mcc = doc.getElementsByTagName("MCC").item(0).textContent
            val fnsNumber = doc.getElementsByTagName("FNSNumber").item(0).textContent
            val stateCode = doc.getElementsByTagName("StateCode").item(0).textContent
            val countyCode = doc.getElementsByTagName("CountyCode").item(0).textContent
            val postalServiceCode = doc.getElementsByTagName("PostalServiceCode").item(0).textContent

            val config = PosConfig(context)
            config.baseUrl = baseUrl
            config.port = port
            config.hosttimeout = timeout
            config.merchantId = merchantId
            config.terminalId = terminalId
            config.procId = procIdd
            config.merchantNameLocation = merchNameLoc
            config.merchantBankName = merchbankName
            config.merchantType = mcc
            config.merchantCategoryCode = mcc
            config.fnsNumber = fnsNumber
            config.stateCode = stateCode
            config.countyCode = countyCode
            config.postalServiceCode = postalServiceCode

            Log.d("TMS", "Config Loaded: $config")

            config

        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

