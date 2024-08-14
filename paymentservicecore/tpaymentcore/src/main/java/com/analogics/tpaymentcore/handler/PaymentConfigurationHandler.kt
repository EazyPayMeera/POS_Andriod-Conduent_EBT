package com.analogics.tpaymentcore.handler

import android.content.Context
import com.analogics.networkservicecore.nComponent.ResultProvider
import com.analogics.tpaymentcore.EMV.EMV
import com.analogics.tpaymentcore.listener.IPaymentConfigListener
import com.analogics.tpaymentcore.listener.IPaymentCoreHandlerListener

object PaymentConfigurationHandler : IPaymentConfigListener{
    override fun <T> initPayment(
        context: Context
    ): ResultProvider<T> {
        EMV.initialize(context);
        return ResultProvider.Success("true" as T)
    }

    override fun startPayment(iPaymentCoreHandlerListener: IPaymentCoreHandlerListener, context : Context) {
        EMV.startPayment(context);
        iPaymentCoreHandlerListener.onPMTRespHandler("Hello this is the UI data")
    }

    override fun handlePaymentEvent() {
        TODO("Not yet implemented")
    }

    override fun onConfigPMTRes() {
        TODO("Not yet implemented")
    }

}