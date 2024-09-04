package com.analogics.paymentservicecore.listeners.requestListener

import android.content.Context
import com.analogics.paymentservicecore.listeners.rootListener.IOnRootAppPaymentListener
import com.analogics.paymentservicecore.models.PosConfig

interface IPaymentServiceRequestListener {
    suspend fun apiEmpDetails(  iOnRootAppPaymentListener: IOnRootAppPaymentListener)
    fun initPaymentSDK(context: Context,iOnRootAppPaymentListener: IOnRootAppPaymentListener)
    fun startPayment(
    context: Context,
    iOnRootAppPaymentListener:IOnRootAppPaymentListener)
    fun getPosConfig() : PosConfig
}