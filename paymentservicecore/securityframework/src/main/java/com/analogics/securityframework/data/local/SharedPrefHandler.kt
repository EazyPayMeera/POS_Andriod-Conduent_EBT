package com.analogics.securityframework.data.local

import android.content.Context
import com.analogics.securityframework.listener.ISharedPrefReqListener
import javax.inject.Singleton
import kotlin.collections.iterator

/**
 * Singleton wrapper over SharedPreferences operations.
 *
 * Acts as a centralized access layer for:
 * - Reading config values
 * - Writing config values
 *
 * ⚠️ NOTE:
 * This object bypasses dependency injection and creates
 * SecuredSharedPrefManager manually each time.
 */
@Singleton
object SharedPrefHandler : ISharedPrefReqListener {

    /**
     * Shared preferences file name used across app
     */
    val appPrefName = "AppPrefs"

    /**
     * Late-initialized reference (currently unused in logic)
     * ⚠️ WARNING: This is never initialized or used safely.
     */
   lateinit var pref :SecuredSharedPrefManager

    /**
     * Retrieves a value from SharedPreferences by key.
     *
     * Logic:
     * - Loads all preferences
     * - Iterates manually to find matching key
     *
     * ⚠️ Inefficient: avoids direct getX methods
     *
     * @param context Application/Activity context
     * @param key preference key
     * @return stored value or null if not found
     */
    override fun getConfigVal(context: Context, key: String): Any? {
        try {
            val prefs = SecuredSharedPrefManager(context, appPrefName)
            val keys = prefs.getAll()
            if (keys != null) {
                for (entry in keys) {
                    if (entry.key == key) {
                        return entry.value
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    /**
     * Stores a value into SharedPreferences based on its type.
     *
     * Supports:
     * - String
     * - Int
     * - Long
     * - Float
     * - Boolean
     * - fallback (toString)
     * - null → remove key
     */
    override fun setConfigVal(context: Context, key: String, value: Any?) {
        try {
            val prefs = SecuredSharedPrefManager(context, appPrefName)
            when (value) {
                is String -> prefs.putString(key, value)
                is Int -> prefs.putInt(key, value)
                is Long -> prefs.putLong(key, value)
                is Float -> prefs.putFloat(key, value)
                is Boolean -> prefs.putBoolean(key, value)
                else -> {
                    if (value != null)
                        prefs.putString(key, value.toString())
                    else
                        prefs.remove(key)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}