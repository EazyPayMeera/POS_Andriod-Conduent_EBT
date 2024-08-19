package com.analogics.tpaymentcore.listener

import android.content.Context
import com.analogics.tpaymentcore.components.ResultProvider

interface IPaymentConfigListener {
    fun<T> initPaymentSDK(context: Context) : ResultProvider<T>
    fun startPayment(iPaymentCoreHandlerListener: IPaymentCoreHandlerListener, context: Context)

    fun handlePaymentEvent()

    fun onConfigPMTRes()

}