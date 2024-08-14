package com.analogics.paymentservicecore.listeners.responseListener
import com.analogics.networkservicecore.nComponent.ResultProvider
import com.analogics.paymentservicecore.models.TransactionData


interface IResultProviderListener {
    fun onSuccess(apiResultProvider: ResultProvider<String>)
    fun onFailure(apiResultProvider: ResultProvider<Error>)
}

interface ITransactionResultListener {
    fun onSuccess(transactionData: TransactionData)
    fun onFailure(exception: Exception)
}