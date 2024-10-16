package com.analogics.paymentservicecore.listeners.responseListener

import com.analogics.paymentservicecore.model.error.EmvServiceException

interface IEmvServiceResponseListener {
    fun onEmvServiceResponse(response: Any)
/*    fun onEmvServiceException(emvServiceException: EmvServiceException)*/
    fun onEmvServiceDisplayProgress(show: Boolean, title: String? = null, subTitle: String? = null, message: String? = null){
        //Default implementation
    }
}