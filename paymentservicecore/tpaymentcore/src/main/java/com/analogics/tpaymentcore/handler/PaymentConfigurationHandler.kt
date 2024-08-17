package com.analogics.tpaymentcore.handler

import android.content.Context
import com.analogics.networkservicecore.nComponent.ResultProvider
import com.analogics.tpaymentcore.EMV.EMV
import com.analogics.tpaymentcore.listener.IPaymentConfigListener
import com.analogics.tpaymentcore.listener.IPaymentCoreHandlerListener
import com.analogics.tpaymentcore.model.TError

object PaymentConfigurationHandler : IPaymentConfigListener{
    lateinit var iPaymentCoreHandlerListener:IPaymentCoreHandlerListener
    override fun  initPayment(iPaymentCoreHandlerListener:IPaymentCoreHandlerListener,
        context: Context
    ) {
        this.iPaymentCoreHandlerListener=iPaymentCoreHandlerListener
        EMV.initialize(context);
    }

    override fun startPayment(iPaymentCoreHandlerListener: IPaymentCoreHandlerListener, context : Context) {
        EMV.startPayment(context);
        iPaymentCoreHandlerListener.onPMTRespHandler("Hello this is the UI data")
    }

    override fun handlePaymentEvent() {
        TODO("Not yet implemented")
    }

    override fun onConfigPMTRes() {
        iPaymentCoreHandlerListener.onPMTRespHandler(TError("Network Error"))
    }

}