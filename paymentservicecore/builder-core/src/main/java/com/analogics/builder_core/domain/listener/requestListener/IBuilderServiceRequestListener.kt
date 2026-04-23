package com.analogics.builder_core.domain.listener.requestListener

import com.analogics.builder_core.domain.listener.responseListener.IBuilderServiceResponseListener
import com.analogics.networkservicecore.data.remote.ResultProvider

interface IBuilderServiceRequestListener {
    suspend fun networkServiceRequest(iBuilderServiceResponseListener: IBuilderServiceResponseListener, requestBody: ByteArray)
    suspend fun networkServiceFinancialRequest(iBuilderServiceResponseListener: IBuilderServiceResponseListener, requestBody: ByteArray)
    suspend fun handShakeRequest(iBuilderServiceResponseListener: IBuilderServiceResponseListener, requestBody: ByteArray)
    fun onNetworkServiceResponse(apiResultProvider: ResultProvider<ByteArray>)
}