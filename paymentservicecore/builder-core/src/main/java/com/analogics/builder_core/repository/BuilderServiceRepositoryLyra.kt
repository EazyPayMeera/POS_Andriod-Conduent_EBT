package com.analogics.builder_core.repository

import android.util.Log
import com.analogics.builder_core.listener.requestListener.IBuilderServiceRequestListenerLyra
import com.analogics.builder_core.listener.responseListener.IBuilderServiceResponseListenerLyra
import com.analogics.networkservicecore.nComponent.NetworkCallProvider
import com.analogics.networkservicecore.nComponent.ResultProvider
import javax.inject.Inject

class BuilderServiceRepositoryLyra @Inject constructor():IBuilderServiceRequestListenerLyra{
    lateinit var iBuilderServiceResponseListener:IBuilderServiceResponseListenerLyra

    @OptIn(ExperimentalStdlibApi::class)
    override suspend fun networkServiceRequest(iBuilderServiceResponseListener: IBuilderServiceResponseListenerLyra, requestBody: ByteArray)
    {
        this.iBuilderServiceResponseListener = iBuilderServiceResponseListener
        Log.d("NETWORK","REQUEST_HEX:"+requestBody.toHexString().uppercase())
        NetworkCallProvider.safeApiCall(requestBody).let {
            onNetworkServiceResponse(it)
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    override fun onNetworkServiceResponse(apiResultProvider: ResultProvider<ByteArray>) {
        when (apiResultProvider) {
            is ResultProvider.Success -> {
                Log.d("NETWORK","RESPONSE_HEX:"+apiResultProvider.data.toHexString().uppercase())
                iBuilderServiceResponseListener.onBuilderSuccess(apiResultProvider.data)
            }

            is ResultProvider.Error -> {
                Log.d("NETWORK","RESPONSE_HEX:"+apiResultProvider.exception.message)
                iBuilderServiceResponseListener.onBuilderFailure(apiResultProvider.exception)
            }
            else -> Unit
        }
    }
}