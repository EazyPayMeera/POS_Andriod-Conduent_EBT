package com.analogics.tpaymentcore.listener.requestListener

import com.analogics.tpaymentcore.listener.responseListener.IEmvSdkResponseListener
import com.analogics.tpaymentcore.model.emv.CAPKey

interface IEmvWrapperRequestListener {
    var iEmvSdkResponseListener: IEmvSdkResponseListener

    fun initializeSdk(listCAPKeys: List<CAPKey>?)
}