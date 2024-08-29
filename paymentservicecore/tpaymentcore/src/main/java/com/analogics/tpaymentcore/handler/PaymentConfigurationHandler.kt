package com.analogics.tpaymentcore.handler

import android.content.Context
import com.analogics.tpaymentcore.EMV.EMV
import com.analogics.tpaymentcore.listener.IPaymentConfigListener
import com.analogics.tpaymentcore.listener.IPaymentSDKListener

object PaymentConfigurationHandler : IPaymentConfigListener {
    override fun initPaymentSDK(
        context: Context,
        iPaymentSDKListener: IPaymentSDKListener
    ) {
        try {
            EMV.initialize(context);
            iPaymentSDKListener.onTPaymentSDKInit("SUCCESS")
        } catch (exception: Exception) {
            iPaymentSDKListener.onTPaymentSDKInit("FAILURE")
        }

    }

    override fun startPayment(
        context: Context,
        iPaymentSDKListener: IPaymentSDKListener
    ) {
        try {
            EMV.startPayment(context,iPaymentSDKListener);
        } catch (exception: Exception) {
            iPaymentSDKListener.onTPaymentSDKHandler("FAILURE")
        }
    }

    override fun handlePaymentEvent() {

    }

    override fun onConfigPMTRes() {

    }

}