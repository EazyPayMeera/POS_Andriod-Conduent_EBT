package com.analogics.paymentservicecore.listeners.responseListener

import com.analogics.paymentservicecore.model.error.EmvServiceError

interface IEmvServiceResponseListener {
    fun onEmvSuccess(response: Any)
    fun onEmvError(emvServiceError: EmvServiceError)
    fun onDisplayProgress(show: Boolean, title: String? = null, subTitle: String? = null, message: String? = null)
}