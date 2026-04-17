package com.analogics.paymentservicecore.data.listeners.responseListener

import com.analogics.paymentservicecore.data.model.PaymentServiceTxnDetails
import com.analogics.paymentservicecore.data.model.error.ApiServiceError
import com.analogics.paymentservicecore.data.model.error.ApiServiceTimeout

interface IApiServiceResponseListener {

    fun onApiServiceSuccess(paymentServiceTxnDetails: PaymentServiceTxnDetails)
    fun onApiServiceError(apiServiceError: ApiServiceError)
    fun onApiServiceTimeout(apiServiceTimeout: ApiServiceTimeout)
    fun onApiServiceDisplayProgress(show: Boolean, title: String? = null, subTitle: String? = null, message: String? = null){}
}