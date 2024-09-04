package com.analogics.paymentservicecore.listeners.responseListener

interface IPrinterResultProviderListener {
    fun onSuccess(result: Any?)
    fun onFailure(exception: Exception)
}