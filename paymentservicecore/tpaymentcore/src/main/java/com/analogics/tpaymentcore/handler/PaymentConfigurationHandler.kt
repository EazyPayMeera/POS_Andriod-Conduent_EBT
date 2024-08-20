package com.analogics.tpaymentcore.handler

import android.content.Context
import com.analogics.tpaymentcore.EMV.EMV
import com.analogics.tpaymentcore.listener.IPaymentConfigListener
import com.analogics.tpaymentcore.listener.IPaymentCoreHandlerListener

object PaymentConfigurationHandler : IPaymentConfigListener {
    override fun initPaymentSDK(
        context: Context,
        iPaymentCoreHandlerListener: IPaymentCoreHandlerListener
    ) {
        try {
            EMV.initialize(context);
            iPaymentCoreHandlerListener.onPMTRespHandler("SUCCESS")
        } catch (exception: Exception) {
            iPaymentCoreHandlerListener.onPMTRespHandler("FAILURE")
        }
    }

    override fun startPayment(
        context: Context,
        iPaymentCoreHandlerListener: IPaymentCoreHandlerListener
    ) {
        try {
            EMV.startPayment(context);
            iPaymentCoreHandlerListener.onPMTRespHandler("SUCCESS")
        } catch (exception: Exception) {
            iPaymentCoreHandlerListener.onPMTRespHandler("FAILURE")
        }
    }

    override fun handlePaymentEvent() {
        TODO("Not yet implemented")
    }

    override fun onConfigPMTRes() {
        TODO("Not yet implemented")
    }

}