// AppLogger.kt
package com.analogics.paymentservicecore.logger

import android.util.Log

object AppLogger {
    // Define log levels
     enum class LOG_LEVEL{
        NONE,ERROR,WARN,INFO,DEBUG,VERBOSE
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
    fun e(tag: String, message: String, throwable: Throwable? = null) {
        if (currentLogLevel >= LOG_LEVEL.ERROR) {
            if (throwable != null) {
                Log.e(tag, message, throwable)
            } else {
                Log.e(tag, message)
            }
        }
    }

    // Method to log a warning
    fun w(tag: String, message: String) {
        if (currentLogLevel >= LOG_LEVEL.WARN) {
            Log.w(tag, message)
        }
    }

    // Method to log an informational message
    fun i(tag: String, message: String) {
        if (currentLogLevel >= LOG_LEVEL.INFO) {
            Log.i(tag, message)
        }
    }

    // Method to log a debug message
    fun d(tag: String, message: String) {
        if (currentLogLevel >= LOG_LEVEL.DEBUG) {
            Log.d(tag, message)
        }
    }

    // Method to log a verbose/trace message
    fun v(tag: String, message: String) {
        if (currentLogLevel >= LOG_LEVEL.VERBOSE) {
            Log.v(tag, message)
        }
    }
}
