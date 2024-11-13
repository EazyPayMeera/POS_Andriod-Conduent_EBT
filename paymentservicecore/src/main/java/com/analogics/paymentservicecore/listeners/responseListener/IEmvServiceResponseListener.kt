package com.analogics.paymentservicecore.listeners.responseListener

import com.analogics.paymentservicecore.constants.EmvConstants
import com.analogics.paymentservicecore.model.emv.EmvServiceResult.DisplayMsgId

interface IEmvServiceResponseListener {
    fun onEmvServiceResponse(response: Any)
    fun onEmvServiceDisplayMessage(displayMsgId: DisplayMsgId){/*Default implementation*/}
    fun onEmvServiceRequestOnline(emvTags : HashMap<String,String>, onResponse : (HashMap<String,String>)->Unit){
        /* Default Implementation. App must should override this method to handle online request */
        var responseEmvTags = hashMapOf(EmvConstants.EMV_TAG_RESP_CODE to EmvConstants.EMV_TAG_VAL_UNABLE_TO_GO_ONLINE_DECLINE)  // Unable to go online, Decline
        onResponse(responseEmvTags)
    }
}