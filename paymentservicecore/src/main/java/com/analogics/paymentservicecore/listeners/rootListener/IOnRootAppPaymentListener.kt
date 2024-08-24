package com.analogics.paymentservicecore.listeners.rootListener

import com.analogics.paymentservicecore.model.error.PaymentServiceError

interface IOnRootAppPaymentListener {

    fun onPaymentSuccess(response: Any)
    fun onPaymentError(tError: PaymentServiceError)
}