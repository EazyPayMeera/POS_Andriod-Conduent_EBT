package com.analogics.tpaymentcore.listener.requestListener

import android.content.Context
import com.analogics.tpaymentcore.listener.responseListener.IEmvSdkResponseListener

interface IEmvSdkRequestListener {
    fun initPaymentSDK(context: Context, iEmvSdkResponseListener: IEmvSdkResponseListener)
    fun startPayment(context: Context, iEmvSdkResponseListener: IEmvSdkResponseListener)
    fun onEmvSdkResponse(response: Any)
}