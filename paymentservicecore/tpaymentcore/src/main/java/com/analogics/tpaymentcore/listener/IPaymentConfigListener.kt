package com.analogics.tpaymentcore.listener

import android.content.Context
import com.analogics.networkservicecore.nComponent.ResultProvider

interface IPaymentConfigListener {
    fun<T> initPayment(context: Context) : ResultProvider<T>
    fun startPayment(iPaymentCoreHandlerListener: IPaymentCoreHandlerListener, context: Context)

    fun handlePaymentEvent()

    fun onConfigPMTRes()

}