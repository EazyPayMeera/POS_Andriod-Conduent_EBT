package com.analogics.tpaymentcore.listener.requestListener

import android.content.Context
import com.analogics.tpaymentcore.listener.responseListener.IEmvSdkResponseListener

interface IEmvSdkRequestListener {
    var iEmvSdkResponseListener : IEmvSdkResponseListener

    fun initPaymentSDK(context: Context)
    fun startPayment(context: Context)
    fun onEmvSdkResponse(response: Any)
}