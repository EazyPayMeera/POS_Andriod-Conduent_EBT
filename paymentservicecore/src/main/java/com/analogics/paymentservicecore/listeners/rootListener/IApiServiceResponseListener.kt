package com.analogics.paymentservicecore.listeners.rootListener

import com.analogics.paymentservicecore.model.error.ApiServiceError

interface IApiServiceResponseListener {
    fun onApiSuccess(response: Any)
    fun onApiError(apiServiceError: ApiServiceError)
    fun onDisplayProgress(show: Boolean, title: String? = null, subTitle: String? = null, message: String? = null)
}