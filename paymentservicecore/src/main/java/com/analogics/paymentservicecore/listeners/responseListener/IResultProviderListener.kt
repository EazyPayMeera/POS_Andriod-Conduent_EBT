package com.analogics.paymentservicecore.listeners.responseListener

import com.analogics.networkservicecore.nComponent.ResultProvider


interface IResultProviderListener {
    fun getResultSuccess(apiResultProvider: ResultProvider<String>)
    fun getResultFailed(apiResultProvider: ResultProvider<Error>)
}