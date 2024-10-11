package com.analogics.tpaymentcore.listener.requestListener

import com.analogics.tpaymentcore.listener.responseListener.IEmvWrapperResponseListener

interface IEmvWrapperRequestListener {
    fun initializeSdk(emvWrapperResponseListener: IEmvWrapperResponseListener)
}