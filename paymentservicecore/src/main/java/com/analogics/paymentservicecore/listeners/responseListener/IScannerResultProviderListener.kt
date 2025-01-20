package com.eazypaytech.paymentservicecore.listeners.responseListener

interface IScannerResultProviderListener {
    fun onSuccess(result: Any?)
    fun onFailure(exception: Exception)
}