package com.eazypaytech.builder_core.listener.requestListener

import com.eazypaytech.builder_core.listener.responseListener.IBuilderServiceResponseListenerLyra
import com.eazypaytech.networkservicecore.nComponent.ResultProvider
import okhttp3.ResponseBody

interface IBuilderServiceRequestListenerLyra {
    suspend fun networkServiceRequest(iBuilderServiceResponseListener: IBuilderServiceResponseListenerLyra, requestBody: ByteArray)
    suspend fun networkServiceFinancialRequest(iBuilderServiceResponseListener: IBuilderServiceResponseListenerLyra, requestBody: ByteArray)
    fun onNetworkServiceResponse(apiResultProvider: ResultProvider<ByteArray>)
}