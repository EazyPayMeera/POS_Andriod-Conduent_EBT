package com.analogics.builder_core.listener.requestListener

import com.analogics.builder_core.listener.responseListener.IBuilderServiceResponseListenerLyra
import com.analogics.networkservicecore.nComponent.ResultProvider

interface IBuilderServiceRequestListenerLyra {
    suspend fun networkServiceRequest(iBuilderServiceResponseListener: IBuilderServiceResponseListenerLyra, requestBody: ByteArray)
    fun onNetworkServiceResponse(apiResultProvider: ResultProvider<ByteArray>)
}