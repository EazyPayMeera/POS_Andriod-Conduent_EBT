package com.eazypaytech.paymentservicecore.listeners.responseListener

import com.eazypaytech.paymentservicecore.model.PaymentServiceTxnDetails
import com.eazypaytech.paymentservicecore.model.error.ApiServiceError

interface IApiServiceResponseListener {
    fun onApiServiceSuccess(paymentServiceTxnDetails: PaymentServiceTxnDetails)
    fun onApiServiceError(apiServiceError: ApiServiceError)
    fun onApiServiceDisplayProgress(show: Boolean, title: String? = null, subTitle: String? = null, message: String? = null){}
}