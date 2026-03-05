package com.eazypaytech.paymentservicecore.listeners.responseListener

import com.eazypaytech.paymentservicecore.model.PaymentServiceTxnDetails
import com.eazypaytech.paymentservicecore.model.error.ApiServiceError
import com.eazypaytech.paymentservicecore.model.error.ApiServiceTimeout

interface IApiServiceResponseListener {

    fun onApiServiceSuccess(paymentServiceTxnDetails: PaymentServiceTxnDetails)
    fun onApiServiceError(apiServiceError: ApiServiceError)
    fun onApiServiceTimeout(apiServiceTimeout: ApiServiceTimeout)
    fun onApiServiceDisplayProgress(show: Boolean, title: String? = null, subTitle: String? = null, message: String? = null){}
}