package com.analogics.builder_core.domain.listener.requestListener

import com.analogics.builder_core.domain.listener.responseListener.IBuilderServiceResponseListenerLyra
import com.analogics.networkservicecore.data.remote.ResultProvider

interface IBuilderServiceRequestListenerLyra {
    suspend fun networkServiceRequest(iBuilderServiceResponseListener: IBuilderServiceResponseListenerLyra, requestBody: ByteArray)
    suspend fun networkServiceFinancialRequest(iBuilderServiceResponseListener: IBuilderServiceResponseListenerLyra, requestBody: ByteArray)
    suspend fun handShakeRequest(iBuilderServiceResponseListener: IBuilderServiceResponseListenerLyra, requestBody: ByteArray)
    fun onNetworkServiceResponse(apiResultProvider: ResultProvider<ByteArray>)
}