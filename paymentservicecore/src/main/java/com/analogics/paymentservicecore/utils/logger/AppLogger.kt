// AppLogger.kt
package com.eazypaytech.paymentservicecore.logger

import android.util.Log

object AppLogger {
    // Define log levels
     enum class LOG_LEVEL{
        NONE,ERROR,WARN,INFO,DEBUG,VERBOSE
    }

    enum class MODULE{
        APP_UI,APP_LOGIC,PAYMENT_LIB,SECURITY_LIB,BUILDER_LIB,POS_CONFIG
    }

    // Set the current log level (e.g., controlled by BuildConfig or any other mechanism)
    var currentLogLevel: LOG_LEVEL = LOG_LEVEL.DEBUG // Default to the most verbose level

    fun setLogLevel(level: LOG_LEVEL) {
        currentLogLevel = level
    }

    fun setLogLevel(level: Int) {
        currentLogLevel = when(level) {
            LOG_LEVEL.ERROR.ordinal -> LOG_LEVEL.ERROR
            LOG_LEVEL.WARN.ordinal -> LOG_LEVEL.WARN
            LOG_LEVEL.INFO.ordinal -> LOG_LEVEL.INFO
            LOG_LEVEL.DEBUG.ordinal -> LOG_LEVEL.DEBUG
            LOG_LEVEL.VERBOSE.ordinal -> LOG_LEVEL.VERBOSE
            else -> LOG_LEVEL.NONE
        }
    }

    // Method to log an error
    fun e(tag: MODULE, message: String, throwable: Throwable? = null) {
        if (currentLogLevel >= LOG_LEVEL.ERROR) {
            if (throwable != null) {
                Log.e(tag.toString(), message, throwable)
            } else {
                Log.e(tag.toString(), message)
            }
        }
    }

    // Method to log a warning
    fun w(tag: MODULE, message: String) {
        if (currentLogLevel >= LOG_LEVEL.WARN) {
            Log.w(tag.toString(), message)
        }
    }

    // Method to log an informational message
    fun i(tag: MODULE, message: String) {
        if (currentLogLevel >= LOG_LEVEL.INFO) {
            Log.i(tag.toString(), message)
        }
    }

    // Method to log a debug message
    fun d(tag: MODULE, message: String) {
        if (currentLogLevel >= LOG_LEVEL.DEBUG) {
            Log.d(tag.toString(), message)
        }
    }

    // Method to log a verbose/trace message
    fun v(tag: MODULE, message: String) {
        if (currentLogLevel >= LOG_LEVEL.VERBOSE) {
            Log.v(tag.toString(), message)
        }
    }
}
