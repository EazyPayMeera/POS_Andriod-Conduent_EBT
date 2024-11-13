package com.analogics.paymentservicecore.listeners.responseListener

import com.analogics.paymentservicecore.model.error.ApiServiceError

interface IApiServiceResponseListener {
    fun onApiServiceSuccess(response: Any)
    fun onApiServiceError(apiServiceError: ApiServiceError)
    fun onApiServiceDisplayProgress(show: Boolean, title: String? = null, subTitle: String? = null, message: String? = null){}
}