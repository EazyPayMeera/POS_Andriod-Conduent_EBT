package com.analogics.builder_core.utils

import android.os.Build
import android.util.Log
import com.analogics.networkservicecore.serviceutils.NetworkConstants
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.security.MessageDigest
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object BuilderUtils  {
     fun prepareApiRequestBody(requestObj:Any): RequestBody {
        return Gson().toJson(requestObj).toByteArray()
            .toRequestBody("application/json".toMediaTypeOrNull())
    }

    fun generateNonce() : String
    {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val date = LocalDateTime.now()
            date.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        } else {
            ""  //TODO : Implement whenever required
        }
    }

    fun generateSecret(nonce : String, appKey : String) : String {
        var secret = ""
        try {
            var digest = MessageDigest.getInstance(NetworkConstants.DIGEST_ALGORITHM)
                .digest(nonce.toByteArray() + appKey.toByteArray())
            secret = printableHexString(digest)
        } catch (e: Exception) {
            Log.e("API_ERROR", e.toString())
        }
        return secret
    }

    fun printableHexString(digestedHash: ByteArray): String {
        return digestedHash.map { Integer.toHexString(0xFF and it.toInt()) }
            .map { if (it.length < 2) "0$it" else it }
            .fold("", { acc, d -> acc + d })
    }
}