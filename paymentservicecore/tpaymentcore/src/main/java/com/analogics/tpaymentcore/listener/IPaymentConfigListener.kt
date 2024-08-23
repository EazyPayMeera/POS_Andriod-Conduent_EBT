package com.analogics.tpaymentcore.listener

import android.content.Context

interface IPaymentConfigListener {

    fun initPaymentSDK(context: Context, iPaymentCoreHandlerListener: IPaymentCoreHandlerListener)
    fun startPayment(context: Context, iPaymentCoreHandlerListener: IPaymentCoreHandlerListener)

    fun handlePaymentEvent()

    fun onConfigPMTRes()

}