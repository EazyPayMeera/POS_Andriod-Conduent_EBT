package com.eazypaytech.tpaymentcore.listener.requestListener

import android.content.Context
import com.eazypaytech.tpaymentcore.listener.responseListener.IEmvSdkResponseListener
import com.eazypaytech.tpaymentcore.model.emv.AidConfig
import com.eazypaytech.tpaymentcore.model.emv.CAPKey

interface IEmvWrapperRequestListener {
    var iEmvSdkResponseListener: IEmvSdkResponseListener

    fun initializeSdk(aidConfig : AidConfig?,capKeys: List<CAPKey>?)
}