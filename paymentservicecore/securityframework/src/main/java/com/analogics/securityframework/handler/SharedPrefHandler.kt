package com.eazypaytech.securityframework.handler

import android.content.Context
import com.eazypaytech.securityframework.listener.ISharedPrefReqListener
import com.eazypaytech.securityframework.preferences.SecuredSharedPrefManager
import javax.inject.Singleton

@Singleton
object SharedPrefHandler : ISharedPrefReqListener {
    val appPrefName = "AppPrefs"

   lateinit var pref :SecuredSharedPrefManager

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