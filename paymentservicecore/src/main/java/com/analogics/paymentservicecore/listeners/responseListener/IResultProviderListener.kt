package com.analogics.paymentservicecore.listeners.responseListener
import com.analogics.networkservicecore.nComponent.ResultProvider


interface IResultProviderListener {
    fun onSuccess(apiResultProvider: ResultProvider<String>)
    fun onFailure(apiResultProvider: ResultProvider<Error>)
}

interface IPaymentServiceResultListener {
    fun onSuccess(result: Any?)
    fun onFailure(exception: Exception)
}