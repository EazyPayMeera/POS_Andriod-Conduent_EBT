package com.analogics.paymentservicecore.listeners.responseListener
import com.analogics.networkservicecore.nComponent.ResultProvider


interface IResultProviderListener {
    fun onSuccess(result: Any?)
    fun onFailure(exception: Exception)
}
