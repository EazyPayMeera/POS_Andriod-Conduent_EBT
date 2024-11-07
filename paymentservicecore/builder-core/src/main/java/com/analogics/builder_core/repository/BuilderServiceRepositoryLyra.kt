package com.analogics.builder_core.repository

import com.analogics.builder_core.listener.requestListener.IBuilderServiceRequestListenerLyra
import com.analogics.builder_core.listener.responseListener.IBuilderServiceResponseListenerLyra
import com.analogics.networkservicecore.nComponent.ResultProvider
import javax.inject.Inject

class BuilderServiceRepositoryLyra @Inject constructor():IBuilderServiceRequestListenerLyra{
    lateinit var iBuilderServiceResponseListener:IBuilderServiceResponseListenerLyra

    override suspend fun networkServiceRequest(iBuilderServiceResponseListener: IBuilderServiceResponseListenerLyra, requestBody: ByteArray)
    {
        this.iBuilderServiceResponseListener = iBuilderServiceResponseListener
        onNetworkServiceResponse(ResultProvider.Error(exception = Exception("Not yet implemented")))
    }

    override fun onNetworkServiceResponse(apiResultProvider: ResultProvider<ByteArray>) {
        when (apiResultProvider) {
            is ResultProvider.Success -> {
                iBuilderServiceResponseListener.onBuilderSuccess(apiResultProvider.data)
            }

            is ResultProvider.Error -> {
                iBuilderServiceResponseListener.onBuilderFailure(apiResultProvider.exception)
            }
            else -> Unit
        }
    }
}