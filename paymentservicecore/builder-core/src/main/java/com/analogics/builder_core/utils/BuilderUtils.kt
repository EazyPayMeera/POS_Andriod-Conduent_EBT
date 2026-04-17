package com.analogics.builder_core.utils

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.analogics.builder_core.data.constants.BuilderConstants
import com.analogics.networkservicecore.data.serviceutils.NetworkConstants
import com.analogics.securityframework.data.local.SecuredSharedPrefManager
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

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

    @RequiresApi(Build.VERSION_CODES.O)
    fun getLocalTime(): String {
        val currentTime = LocalTime.now()  // device local time
        val formatter = DateTimeFormatter.ofPattern("HHmmss")  // ISO8583 DE012 format
        return currentTime.format(formatter)
    }

    fun formatDateTimeToISO8583(dateTime: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val date = inputFormat.parse(dateTime) ?: return ""

        val outputFormat = SimpleDateFormat("MMddHHmmss", Locale.getDefault())
        return outputFormat.format(date)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getLocalDate(): String {
        val currentDate = LocalDate.now()  // device local date
        val formatter = DateTimeFormatter.ofPattern("MMdd")
        return currentDate.format(formatter)
    }

    fun calculateKCV(keyHex: String?): String {
        // Convert HEX string to bytes
        val keyBytes = keyHex?.chunked(2)?.map { it.toInt(16).toByte() }?.toByteArray()

        // Data to encrypt: 8 bytes of zeros
        val zeroBlock = ByteArray(8) { 0x00 }

        // Choose DES or DESede based on key length
        val cipherAlgorithm = when (keyBytes?.size) {
            8 -> "DES/ECB/NoPadding"       // single-length DES
            16, 24 -> "DESede/ECB/NoPadding" // double or triple-length 3DES
            else -> throw IllegalArgumentException("Invalid key length: ${keyBytes?.size}")
        }

        val secretKey = when (keyBytes.size) {
            8 -> SecretKeySpec(keyBytes, "DES")
            16, 24 -> SecretKeySpec(keyBytes, "DESede")
            else -> throw IllegalArgumentException("Invalid key length: ${keyBytes.size}")
        }

        val cipher = Cipher.getInstance(cipherAlgorithm)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)

        val encrypted = cipher.doFinal(zeroBlock)
        return encrypted.take(3).joinToString("") { "%02X".format(it) }
    }
}