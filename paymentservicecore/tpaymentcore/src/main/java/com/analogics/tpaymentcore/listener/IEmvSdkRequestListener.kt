package com.analogics.tpaymentcore.listener

import android.content.Context

interface IEmvSdkRequestListener {
    fun initPaymentSDK(context: Context, iEmvSdkResponseListener: IEmvSdkResponseListener)
    fun startPayment(context: Context, iEmvSdkResponseListener: IEmvSdkResponseListener)
    fun onEmvSdkResponse(response: Any)
}