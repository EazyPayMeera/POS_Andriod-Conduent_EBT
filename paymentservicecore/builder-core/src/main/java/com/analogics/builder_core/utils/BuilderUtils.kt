package com.analogics.builder_core.utils

import android.content.Context
import android.os.Build
import android.util.Log
import com.analogics.builder_core.constants.BuilderConstants
import com.analogics.networkservicecore.serviceutils.NetworkConstants
import com.analogics.securityframework.preferences.SecuredSharedPrefManager
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

object BuilderUtils {

    fun prepareApiRequestBody(requestObj: Any): RequestBody {
        return Gson().toJson(requestObj).toByteArray()
            .toRequestBody("application/json".toMediaTypeOrNull())
    }

    fun generateNonce(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val date = LocalDateTime.now()
            date.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        } else {
            ""  //TODO : Implement whenever required
        }
    }

    fun generateSecret(nonce: String, appKey: String): String {
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

    fun getSTAN(context: Context, increment: Boolean? = true): Long {
        var stan: Long = 1

        /* Separate Preferences Maintained for Builder so that it doesn't interfere with POS Config */
        try {
            /* Load Existing Value */
            SecuredSharedPrefManager(context, BuilderConstants.SHARED_PREF_NAME).getLong(
                BuilderConstants.SHARED_PREF_KEY_STAN
            ).let {
                if (it in BuilderConstants.MIN_STAN_VAL..BuilderConstants.MAX_STAN_VAL)
                    stan = if (increment == true) it + 1 else it
            }

            /* Put Updated Value */
            SecuredSharedPrefManager(context, BuilderConstants.SHARED_PREF_NAME).putLong(
                BuilderConstants.SHARED_PREF_KEY_STAN,
                stan
            )
        } catch (exception: Exception) {
            Log.e(this.javaClass.name, exception.toString())
        }

        return stan
    }

    fun getCurrentDateTime(format : String?=BuilderConstants.DEFAULT_ISO8583_DATE_TIME_FORMAT): String {
        val sdf = SimpleDateFormat(format, Locale.getDefault())
        return sdf.format(Date())
    }
}