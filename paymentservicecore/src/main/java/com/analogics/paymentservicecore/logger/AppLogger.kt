// AppLogger.kt
package com.analogics.paymentservicecore.logger

import android.util.Log

object AppLogger {
    // Define log levels
    const val LEVEL_NONE = 0
    const val LEVEL_ERROR = 1
    const val LEVEL_WARN = 2
    const val LEVEL_INFO = 3
    const val LEVEL_DEBUG = 4

    // Set the current log level (e.g., controlled by BuildConfig or any other mechanism)
    var currentLogLevel: Int = LEVEL_DEBUG // Default to the most verbose level

    // Method to log an error
    fun e(tag: String, message: String, throwable: Throwable? = null) {
        if (currentLogLevel <= LEVEL_ERROR) {
            if (throwable != null) {
                Log.e(tag, message, throwable)
            } else {
                Log.e(tag, message)
            }
        }
    }

    // Method to log a warning
    fun w(tag: String, message: String) {
        if (currentLogLevel <= LEVEL_WARN) {
            Log.w(tag, message)
        }
    }

    // Method to log an informational message
    fun i(tag: String, message: String) {
        if (currentLogLevel <= LEVEL_INFO) {
            Log.i(tag, message)
        }
    }

    // Method to log a debug message
    fun d(tag: String, message: String) {
        if (currentLogLevel <= LEVEL_DEBUG) {
            Log.d(tag, message)
        }
    }
}
