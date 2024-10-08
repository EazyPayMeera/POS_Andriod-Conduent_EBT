package com.analogics.paymentservicecore.listeners.rootListener

import com.analogics.paymentservicecore.model.error.PaymentServiceError

interface IOnRootAppPaymentListener {
    fun onPaymentSuccess(response: Any)
    fun onPaymentError(tError: PaymentServiceError)
    fun onDisplayProgress(show: Boolean, title: String? = null, subTitle: String? = null, message: String? = null)
}