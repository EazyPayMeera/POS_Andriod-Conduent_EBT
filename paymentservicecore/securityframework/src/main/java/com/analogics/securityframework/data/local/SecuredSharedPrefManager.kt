package com.analogics.securityframework.data.local

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Wrapper over SharedPreferences for simplified key-value storage.
 *
 * ⚠️ NOTE:
 * This is NOT truly "secure storage".
 * Despite the name "SecuredSharedPrefManager",
 * SharedPreferences is plaintext unless encrypted.
 *
 * For sensitive data (PIN, tokens, keys), use:
 * - Android Keystore
 * - EncryptedSharedPreferences
 */
@Singleton
class SecuredSharedPrefManager @Inject constructor(
    @ApplicationContext context: Context,
    prefsName: String = "MyAppPrefs"
) {

    /**
     * Internal SharedPreferences instance
     */
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)

    /**
     * Stores a String value in preferences.
     */
    fun putString(key: String, value: String) {
        with(sharedPreferences.edit()) {
            putString(key, value)
            apply()
        }
    }

    /**
     * Retrieves a String value from preferences.
     */
    fun getString(key: String, defaultValue: String = ""): String {
        return sharedPreferences.getString(key, defaultValue) ?: defaultValue
    }

    /**
     * Stores an integer value.
     */
    fun putInt(key: String, value: Int) {
        with(sharedPreferences.edit()) {
            putInt(key, value)
            apply()
        }
    }

    /**
     * Retrieves an integer value.
     */
    fun getInt(key: String, defaultValue: Int = -1): Int {
        return sharedPreferences.getInt(key, defaultValue)
    }

    /**
     * Stores a boolean value.
     */
    fun putBoolean(key: String, value: Boolean) {
        with(sharedPreferences.edit()) {
            putBoolean(key, value)
            apply()
        }
    }

    /**
     * Retrieves a boolean value.
     */
    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean {
        return sharedPreferences.getBoolean(key, defaultValue)
    }

    /**
     * Stores a float value.
     */
    fun putFloat(key: String, value: Float) {
        with(sharedPreferences.edit()) {
            putFloat(key, value)
            apply()
        }
    }

    /**
     * Retrieves a float value.
     */
    fun getFloat(key: String, defaultValue: Float = -1f): Float {
        return sharedPreferences.getFloat(key, defaultValue)
    }

    /**
     * Stores a long value.
     */
    fun putLong(key: String, value: Long) {
        with(sharedPreferences.edit()) {
            putLong(key, value)
            apply()
        }
    }

    /**
     * Retrieves a long value.
     */
    fun getLong(key: String, defaultValue: Long = -1L): Long {
        return sharedPreferences.getLong(key, defaultValue)
    }

    /**
     * Returns all stored key-value pairs.
     */
    fun getAll() = sharedPreferences.all

    /**
     * Removes a specific key from preferences.
     */
    fun remove(key: String) {
        with(sharedPreferences.edit()) {
            remove(key)
            apply()
        }
    }

    //  val prefs = SecureSharePrefManager(context, "MyAppPrefs")
    //  prefs.putString("username", "JohnDoe")

}