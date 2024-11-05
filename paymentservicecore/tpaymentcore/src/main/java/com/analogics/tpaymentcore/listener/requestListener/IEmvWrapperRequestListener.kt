package com.analogics.tpaymentcore.listener.requestListener

import android.content.Context
import com.analogics.tpaymentcore.listener.responseListener.IEmvSdkResponseListener
import com.analogics.tpaymentcore.model.emv.AidConfig
import com.analogics.tpaymentcore.model.emv.CAPKey

interface IEmvWrapperRequestListener {
    var iEmvSdkResponseListener: IEmvSdkResponseListener

    fun initializeSdk(aidConfig : AidConfig?,capKeys: List<CAPKey>?)
}