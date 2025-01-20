package com.eazypaytech.tpaymentcore.utils

import android.util.Log
import java.util.Hashtable
import kotlin.experimental.and

class TlvUtils {
    val TAG_TLV_UTILS = "TLV_UTILS"
    var tlvMap = HashMap<String, String>()

    constructor()
    {
    }

    constructor(tlvMap : HashMap<String, String>)
    {
        this.tlvMap = tlvMap
    }

    constructor(tlvMap : Hashtable<String, String>?)
    {
        this.tlvMap = tlvMap?.toMap() as HashMap<String, String>
    }

    constructor(tlvString : String?)
    {
        parseTlv(tlvString)
    }

    @OptIn(ExperimentalStdlibApi::class)
    fun addTagValAscii(tag: String, value: String?, minLen: Int=0, maxLen: Int=255) : TlvUtils {
        try {
            value?.padEnd(minLen)?.toByteArray()?.toHexString()?.take(maxLen*2)?.let { tlvMap[tag.uppercase()] = it.uppercase() }
        }catch (exception : Exception)
        {
            Log.e(TAG_TLV_UTILS, ""+exception.message)
        }
        return this
    }

    @OptIn(ExperimentalStdlibApi::class)
    fun addTagValHex(tag: String, value: String?, minLen: Int=0, maxLen: Int=255) : TlvUtils {
        try {
            value?.let {
                var hexBytes = (if(it.length % 2 == 0) it else "${it}F").hexToByteArray()
                var hexString = hexBytes.toHexString().padStart(minLen*2, '0').take(maxLen*2)
                tlvMap[tag.uppercase()] = hexString.uppercase()
            }
        }catch (exception : Exception)
        {
            Log.e(TAG_TLV_UTILS, ""+exception.message)
        }
        return this
    }

    @OptIn(ExperimentalStdlibApi::class)
    fun addTagValBoolean(tag: String, value: Boolean?) : TlvUtils {
        try {
            value?.let {
                var hexString = if(value==true) "01" else "00"
                tlvMap[tag.uppercase()] = hexString.uppercase()
            }
        }catch (exception : Exception)
        {
            Log.e(TAG_TLV_UTILS, ""+exception.message)
        }
        return this
    }

    @OptIn(ExperimentalStdlibApi::class)
    fun parseTlv(tlv : String?): TlvUtils {
        try {
            tlv?.let {
                var hexBytes = (if(it.length % 2 == 0) it else "${it}F").hexToByteArray()
                var tagLen : Int
                var lenLen : Int
                var valLen : Int
                Log.d(TAG_TLV_UTILS, "------------- Parsed Tag List -------------")
                while(hexBytes.isNotEmpty())
                {
                    tagLen = getTagLen(hexBytes);
                    lenLen = getLenLen(hexBytes.sliceArray(tagLen..hexBytes.size-1))
                    valLen = getValLen(hexBytes.sliceArray(tagLen..hexBytes.size-1))
                    var tag = hexBytes.sliceArray(0..tagLen-1).toHexString().uppercase()
                    var value = hexBytes.sliceArray(tagLen+lenLen..tagLen+lenLen+valLen-1).toHexString().uppercase()
                    tlvMap[tag] = value

                    hexBytes = hexBytes.sliceArray(tagLen+lenLen+valLen..hexBytes.size-1)
                    Log.d(TAG_TLV_UTILS, "Tag : $tag, Value : $value")
                }
            }
        }catch ( exception : Exception)
        {
            Log.e(TAG_TLV_UTILS, ""+exception.message)
        }
        return this
    }

    @OptIn(ExperimentalStdlibApi::class)
    fun toTlvLen(len : Int) : String
    {
        return when(len) {
            in 0..0x7F -> len.toByte().toHexString()
            in 0x80..0xFF -> "81"+len.toByte().toHexString()
            in 0x100..0xFFFF -> "82"+len.toByte().toHexString()    /* No complex logic. 2 bytes more than enough */
            else -> "83"+len.toByte().toHexString()
        }.uppercase()
    }

    fun toTlvString(tags : List<String>?=null): String {
        var result = ""
        try {
            if(tags!=null)
            {
                for(tag in tags)
                {
                    if(tlvMap.containsKey(tag.uppercase()) && tlvMap[tag.uppercase()]?.isNotEmpty() == true)
                        result += "${tag.uppercase()}${toTlvLen((tlvMap[tag.uppercase()]?.length ?: 0) / 2)}${tlvMap[tag.uppercase()]}"
                }
            }
            else
            {
                for(tlv in tlvMap)
                {
                    result += "${tlv.key}${toTlvLen(tlv.value.length/2)}${tlv.value}"
                }
            }
        }catch ( exception : Exception)
        {
            Log.e(TAG_TLV_UTILS, ""+exception.message)
        }
        return result
    }

    fun getTagLen(tlv : ByteArray) : Int
    {
        var result = 1
        try {
            if(tlv[0].and(0x1F.toByte()) == 0x1F.toByte())
            {
                do {result++}
                while(result < tlv.size && tlv[result-1].and(0x80.toByte()) == 0x80.toByte())
            }
        }catch (exception : Exception)
        {
            Log.e(TAG_TLV_UTILS, ""+exception.message)
        }
        return result
    }

    fun getLenLen(lv : ByteArray) : Int
    {
        var result = 1
        try {
            if(lv[0].and(0x80.toByte()) == 0x80.toByte())
                result += lv[0].and(0x7F.toByte()).toInt()
        }catch (exception : Exception)
        {
            Log.e(TAG_TLV_UTILS, ""+exception.message)
        }
        return result
    }

    fun getValLen(lv : ByteArray) : Int
    {
        var result = 0
        var lenLen = getLenLen(lv)
        try {
            if(lenLen>1) {
                val lenBytes = lv.sliceArray(1..lenLen)
                while (lenBytes.isNotEmpty()) {
                    result = (result shl 8) + lenBytes[0].toInt()
                    lenBytes.drop(1)
                }
            }
            else {
                result = lv[0].toInt()
            }
        }catch (exception : Exception) {
            Log.e(TAG_TLV_UTILS, "" + exception.message)
        }
        return result
    }
}