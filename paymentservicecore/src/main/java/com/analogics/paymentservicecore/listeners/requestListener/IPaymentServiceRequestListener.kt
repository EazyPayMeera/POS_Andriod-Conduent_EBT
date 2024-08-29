package com.analogics.paymentservicecore.listeners.requestListener

import android.content.Context
import com.analogics.paymentservicecore.listeners.rootListener.IOnRootAppPaymentListener

interface IPaymentServiceRequestListener {
    suspend fun apiEmpDetails(  iOnRootAppPaymentListener: IOnRootAppPaymentListener)
    fun initPaymentSDK(context: Context,iOnRootAppPaymentListener: IOnRootAppPaymentListener)
    fun startPayment(
    context: Context,
    iOnRootAppPaymentListener:IOnRootAppPaymentListener)
    fun isPaymentSDKInit(context: Context): Boolean
    fun isPaymentSDKInit(context: Context, isInit : Boolean)
}