package com.analogics.tpaymentcore.listener

import android.content.Context

interface IPaymentConfigListener {

    fun initPaymentSDK(context: Context, iPaymentSDKListener: IPaymentSDKListener)
    fun startPayment(context: Context, iPaymentSDKListener: IPaymentSDKListener)

    fun handlePaymentEvent()

    fun onConfigPMTRes()

}