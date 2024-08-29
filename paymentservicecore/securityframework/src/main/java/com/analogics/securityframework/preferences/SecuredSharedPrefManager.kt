package com.analogics.securityframework.preferences

import android.content.Context
import android.content.SharedPreferences

class SecuredSharedPrefManager(context: Context, prefsName: String) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)

    fun putString(key: String, value: String) {
        with(sharedPreferences.edit()) {
            putString(key, value)
            apply()
        }
    }

    fun getString(key: String, defaultValue: String = ""): String {
        return sharedPreferences.getString(key, defaultValue) ?: defaultValue
    }

    fun putInt(key: String, value: Int) {
        with(sharedPreferences.edit()) {
            putInt(key, value)
            apply()
        }
    }

    fun getInt(key: String, defaultValue: Int = -1): Int {
        return sharedPreferences.getInt(key, defaultValue)
    }

    fun putBoolean(key: String, value: Boolean) {
        with(sharedPreferences.edit()) {
            putBoolean(key, value)
            apply()
        }
    }

    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean {
        return sharedPreferences.getBoolean(key, defaultValue)
    }

    fun putFloat(key: String, value: Float) {
        with(sharedPreferences.edit()) {
            putFloat(key, value)
            apply()
        }
    }

    fun getFloat(key: String, defaultValue: Float = -1f): Float {
        return sharedPreferences.getFloat(key, defaultValue)
    }

    fun putLong(key: String, value: Long) {
        with(sharedPreferences.edit()) {
            putLong(key, value)
            apply()
        }
    }

    fun getLong(key: String, defaultValue: Long = -1L): Long {
        return sharedPreferences.getLong(key, defaultValue)
    }

    fun getAll() = sharedPreferences.all

    //  val prefs = SecureSharePrefManager(context, "MyAppPrefs")
    //  prefs.putString("username", "JohnDoe")

}