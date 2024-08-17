package com.analogics.paymentservicecore.listeners.rootListener

import com.analogics.tpaymentcore.model.TError

interface IOnRootAppListener {

    fun onSuccess(string: String)
    fun onPaymentError(tError: TError)
}