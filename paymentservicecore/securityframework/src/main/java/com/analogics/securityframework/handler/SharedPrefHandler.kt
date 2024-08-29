package com.analogics.securityframework.handler

import android.content.Context
import com.analogics.securityframework.listener.ISharedPrefReqListener
import com.analogics.securityframework.preferences.SecuredSharedPrefManager

object SharedPrefHandler : ISharedPrefReqListener {
    val appPrefName = "AppPrefs"
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

    override fun setConfigVal(context: Context, key: String, value: Any?) {
        try {
            val prefs = SecuredSharedPrefManager(context, appPrefName)
            when (value) {
                is String -> prefs.putString(key, value)
                is Int -> prefs.putInt(key, value)
                is Long -> prefs.putLong(key, value)
                is Float -> prefs.putFloat(key, value)
                is Boolean -> prefs.putBoolean(key, value)
                else -> prefs.putString(key, value.toString())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}