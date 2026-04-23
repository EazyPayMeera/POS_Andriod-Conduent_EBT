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

    /**
     * Generates or retrieves the System Trace Audit Number (STAN).
     *
     * STAN is a unique 6-digit number used to identify transactions (ISO8583 DE11).
     *
     * Features:
     * - Persists STAN in secured shared preferences
     * - Auto-increments within defined range
     * - Prevents overflow beyond MAX_STAN_VAL
     *
     * @param context Application context
     * @param increment Whether to increment STAN or just fetch current value
     * @return STAN as Long
     */
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

    /**
     * Returns current date-time formatted for ISO8583 fields.
     *
     * Default format: MMddHHmmss (Transmission Date & Time - DE7)
     *
     * @param format Optional custom date format
     * @return Formatted date-time string
     */
    fun getCurrentDateTime(format : String?=BuilderConstants.DEFAULT_ISO8583_DATE_TIME_FORMAT): String {
        val sdf = SimpleDateFormat(format, Locale.getDefault())
        return sdf.format(Date())
    }

    /**
     * Returns local device time formatted for ISO8583.
     *
     * Format: HHmmss (Local Transaction Time - DE12)
     *
     * Requires API level 26+
     *
     * @return Local time as string
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun getLocalTime(): String {
        val currentTime = LocalTime.now()  // device local time
        val formatter = DateTimeFormatter.ofPattern("HHmmss")  // ISO8583 DE012 format
        return currentTime.format(formatter)
    }

    /**
     * Returns local device date formatted for ISO8583.
     *
     * Format: MMdd (Local Transaction Date - DE13)
     *
     * Requires API level 26+
     *
     * @return Local date as string
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun getLocalDate(): String {
        val currentDate = LocalDate.now()  // device local date
        val formatter = DateTimeFormatter.ofPattern("MMdd")
        return currentDate.format(formatter)
    }

    /**
     * Calculates Key Check Value (KCV) for a given encryption key.
     *
     * KCV is used to verify integrity of cryptographic keys.
     *
     * Logic:
     * - Encrypts 8-byte zero block using provided key
     * - Returns first 3 bytes of encrypted output (6 hex characters)
     *
     * Supported Keys:
     * - 8 bytes  → DES
     * - 16/24 bytes → 3DES (DESede)
     *
     * @param keyHex HEX string of key (e.g., "0123456789ABCDEF")
     * @return 6-character KCV string
     * @throws IllegalArgumentException if key length is invalid
     */
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