package com.analogics.tpaymentcore.handler

import com.analogics.tpaymentcore.listener.IPaymentConfigListener
import com.analogics.tpaymentcore.listener.IPaymentCoreHandlerListener

object PaymentConfigurationHandler : IPaymentConfigListener{
    override fun initPayment(iPaymentCoreHandlerListener: IPaymentCoreHandlerListener) {
        iPaymentCoreHandlerListener.onPMTRespHandler("Hello this is the UI data")
    }

    override fun startPayment() {
        TODO("Not yet implemented")
    }

    override fun handlePaymentEvent() {
        TODO("Not yet implemented")
    }

    override fun onConfigPMTRes() {
        TODO("Not yet implemented")
    }

}