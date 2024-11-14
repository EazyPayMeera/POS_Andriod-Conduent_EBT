package com.analogics.paymentservicecore.listeners.responseListener

import com.analogics.builder_core.model.PaymentServiceTxnDetails
import com.analogics.paymentservicecore.model.error.ApiServiceError

interface IApiServiceResponseListener {
    fun onApiServiceSuccess(paymentServiceTxnDetails: PaymentServiceTxnDetails)
    fun onApiServiceError(apiServiceError: ApiServiceError)
    fun onApiServiceDisplayProgress(show: Boolean, title: String? = null, subTitle: String? = null, message: String? = null){}
}