package com.analogics.networkservicecore.tms

import android.util.Log
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * Utility object responsible for generating HMAC-SHA256 signatures
 * for TMS API request authentication.
 *
 * This ensures:
 * - Request integrity
 * - Server-side validation
 * - Tamper-proof API communication
 */
object TmsRequestSigner {

    private const val HMAC_SHA256 = "HmacSHA256"

    /**
     * Generates HMAC-SHA256 signature from request parameters.
     *
     * Steps:
     * 1. Filter empty/null values
     * 2. Sort parameters lexicographically (ASCII order)
     * 3. Build query string (key=value&key=value...)
     * 4. Generate HMAC-SHA256 hash using secret key
     * 5. Convert result to uppercase HEX string
     *
     * @param params Request parameters map
     * @param secret Shared secret key for HMAC generation
     * @return Uppercase hexadecimal signature string
     */
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