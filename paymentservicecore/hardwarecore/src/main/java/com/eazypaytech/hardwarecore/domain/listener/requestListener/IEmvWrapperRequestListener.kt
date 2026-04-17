package com.eazypaytech.hardwarecore.domain.listener.requestListener

import com.eazypaytech.hardwarecore.domain.listener.responseListener.IEmvSdkResponseListener
import com.eazypaytech.hardwarecore.data.model.AidConfig
import com.eazypaytech.hardwarecore.data.model.CAPKey

interface IEmvWrapperRequestListener {
    var iEmvSdkResponseListener: IEmvSdkResponseListener

    fun initializeSdk(aidConfig : AidConfig?,capKeys: List<CAPKey>?)
}