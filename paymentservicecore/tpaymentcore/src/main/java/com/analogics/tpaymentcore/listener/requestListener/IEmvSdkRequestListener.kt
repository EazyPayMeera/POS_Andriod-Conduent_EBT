package com.analogics.tpaymentcore.listener.requestListener

import android.content.Context
import com.analogics.tpaymentcore.listener.responseListener.IEmvSdkResponseListener
import com.analogics.tpaymentcore.model.emv.CAPKey

interface IEmvSdkRequestListener {
    var iEmvSdkResponseListener : IEmvSdkResponseListener

    fun initPaymentSDK(capKeys: List<CAPKey>?)
    fun startPayment(context: Context)
    fun onEmvSdkResponse(response: Any)
}