package com.analogics.tpaymentcore.listener.requestListener

import android.content.Context
import com.analogics.tpaymentcore.listener.responseListener.IEmvSdkResponseListener
import com.analogics.tpaymentcore.model.emv.AidConfig
import com.analogics.tpaymentcore.model.emv.CAPKey
import com.analogics.tpaymentcore.model.emv.TransConfig

interface IEmvSdkRequestListener {
    var iEmvSdkResponseListener : IEmvSdkResponseListener

    fun initPaymentSDK(aidConfig : AidConfig?,capKeys: List<CAPKey>?)
    fun startPayment(context: Context, transConfig: TransConfig?)
    fun abortPayment()
}