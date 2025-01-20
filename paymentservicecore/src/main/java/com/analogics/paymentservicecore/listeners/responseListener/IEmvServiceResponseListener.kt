package com.eazypaytech.paymentservicecore.listeners.responseListener

import com.eazypaytech.paymentservicecore.constants.EmvConstants
import com.eazypaytech.paymentservicecore.model.emv.EmvServiceResult.DisplayMsgId

interface IEmvServiceResponseListener {
    fun onEmvServiceResponse(response: Any)
    fun onEmvServiceDisplayMessage(displayMsgId: DisplayMsgId){/*Default implementation*/}
}