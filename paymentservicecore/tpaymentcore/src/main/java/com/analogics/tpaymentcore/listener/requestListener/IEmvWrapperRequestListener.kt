package com.analogics.tpaymentcore.listener.requestListener

import com.analogics.tpaymentcore.listener.responseListener.IEmvSdkResponseListener

interface IEmvWrapperRequestListener {
    var iEmvSdkResponseListener: IEmvSdkResponseListener

    fun initializeSdk()
}