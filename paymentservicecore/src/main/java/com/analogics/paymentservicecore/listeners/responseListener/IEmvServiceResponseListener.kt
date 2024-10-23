package com.analogics.paymentservicecore.listeners.responseListener

import com.analogics.paymentservicecore.model.emv.EmvServiceResult.DisplayMsgId

interface IEmvServiceResponseListener {
    fun onEmvServiceResponse(response: Any)
    fun onEmvServiceDisplayMessage(displayMsgId: DisplayMsgId){
        //Default implementation
    }
}