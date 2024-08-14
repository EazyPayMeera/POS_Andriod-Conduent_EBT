package com.analogics.tpaymentcore.listener

interface IPaymentConfigListener {
    fun initPayment(iPaymentCoreHandlerListener:IPaymentCoreHandlerListener)
    fun startPayment()

    fun handlePaymentEvent()

    fun onConfigPMTRes()

}