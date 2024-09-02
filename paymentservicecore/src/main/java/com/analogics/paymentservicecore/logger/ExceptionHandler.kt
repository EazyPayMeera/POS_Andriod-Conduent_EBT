package com.analogics.paymentservicecore.logger

import android.util.Log
import java.io.IOException


/*
*   ExceptionHandler.safeExecute {
* TO DO
* }
* */
class ExceptionHandler {
val TAG="APP EXCEPTION"
    fun handleExeption(exception: Exception, customMessage: String? = null) {
        when (exception) {
            is IOException -> {
                // Handle network-related exceptions
                exception.message?.let { Log.e(TAG, it) }
                // Show network error UI or retry logic
            }
            is IllegalArgumentException -> {
                // Handle argument exceptions
                exception.message?.let { Log.e(TAG, it) }
                // Show user-friendly message or corrective action
            }
            else -> {
                // Handle general exceptions
                exception.message?.let { Log.e(TAG, it) }
                // Show generic error UI
            }
        }
    }

    fun <T> safeExecute(block: () -> T): T? {
        return try {
            block()
        } catch (exception: Exception) {
            handleExeption(exception)
            null
        }
    }

}