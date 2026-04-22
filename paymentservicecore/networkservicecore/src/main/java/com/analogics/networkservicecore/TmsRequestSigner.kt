package com.analogics.networkservicecore.tms

import android.util.Log
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object TmsRequestSigner {

    private const val HMAC_SHA256 = "HmacSHA256"

    fun generateSignature(
        params: Map<String, String>,
        secret: String
    ): String {

        //  1. Remove null/empty values
        val filtered = params
            .filterValues { !it.isNullOrEmpty() }

        //  2. Sort by ASCII (natural order)
        val sorted = filtered.toSortedMap()

        //  3. Build query string
        val stringToSign = sorted.entries.joinToString("&") {
            "${it.key}=${it.value}"
        }

        //  Debug log (VERY IMPORTANT during integration)
        println("TMS_STRING_TO_SIGN: $stringToSign")

        //  4. HMAC SHA256
        val mac = Mac.getInstance(HMAC_SHA256)
        val secretKey = SecretKeySpec(secret.toByteArray(Charsets.UTF_8), HMAC_SHA256)
        mac.init(secretKey)

        val rawHmac = mac.doFinal(stringToSign.toByteArray(Charsets.UTF_8))

        //  5. Convert to HEX (uppercase)
        val signature = rawHmac.joinToString("") { "%02X".format(it) }

        Log.d("TMS", "TMS_SIGNATURE: $signature")

        return signature
    }
}