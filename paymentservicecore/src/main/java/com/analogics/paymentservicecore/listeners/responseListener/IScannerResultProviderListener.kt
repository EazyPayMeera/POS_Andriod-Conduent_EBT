package com.analogics.paymentservicecore.listeners.responseListener

interface IScannerResultProviderListener {
    fun onSuccess(result: Any?)
    fun onFailure(exception: Exception)
}