package com.analogics.tpaymentcore.handler

import android.content.Context
import com.analogics.tpaymentcore.EMV.EMV
import com.analogics.tpaymentcore.listener.IPaymentConfigListener
import com.analogics.tpaymentcore.listener.IPaymentCoreHandlerListener
import com.analogics.tpaymentcore.model.TError

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
            EMV.startPayment(context,iPaymentCoreHandlerListener);
        } catch (exception: Exception) {
            iPaymentCoreHandlerListener.onPMTRespHandler("FAILURE")
        }
    }

    override fun handlePaymentEvent() {

    }

    override fun onConfigPMTRes() {

    }

}