package com.analogics.paymentservicecore.data.listeners.responseListener

import com.analogics.paymentservicecore.data.model.emv.EmvServiceResult.DisplayMsgId

interface IEmvServiceResponseListener {
    fun onEmvServiceResponse(response: Any)
    fun onEmvServiceDisplayMessage(displayMsgId: DisplayMsgId){/*Default implementation*/}
}