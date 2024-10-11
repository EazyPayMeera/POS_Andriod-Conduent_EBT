package com.analogics.tpaymentcore.listener.requestListener

interface IEmvWrapperRequestListener {
    var onEmvSdkResponse: (Any)->Unit

    fun initializeSdk()
}