package com.analogics.tpaymentcore.listener

import android.content.Context

interface IPaymentConfigListener {
    fun initPayment(iPaymentCoreHandlerListener:IPaymentCoreHandlerListener, context: Context)
    fun startPayment(iPaymentCoreHandlerListener: IPaymentCoreHandlerListener, context: Context)

    fun handlePaymentEvent()

    fun onConfigPMTRes()

}