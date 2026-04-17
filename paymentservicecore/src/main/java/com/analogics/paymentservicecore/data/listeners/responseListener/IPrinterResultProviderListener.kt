package com.analogics.paymentservicecore.data.listeners.responseListener

interface IPrinterResultProviderListener {
    fun onSuccess(result: Any?)
    fun onFailure(exception: Exception)
}