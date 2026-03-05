package com.eazypaytech.builder_core.repository

import android.util.Log
import com.eazypaytech.builder_core.listener.requestListener.IBuilderServiceRequestListenerLyra
import com.eazypaytech.builder_core.listener.responseListener.IBuilderServiceResponseListenerLyra
import com.eazypaytech.networkservicecore.nComponent.NetworkCallProvider
import com.eazypaytech.networkservicecore.nComponent.ResultProvider
import javax.inject.Inject

class BuilderServiceRepositoryLyra @Inject constructor():IBuilderServiceRequestListenerLyra{
    lateinit var iBuilderServiceResponseListener:IBuilderServiceResponseListenerLyra

    @OptIn(ExperimentalStdlibApi::class)
    override suspend fun networkServiceRequest(
        iBuilderServiceResponseListener: IBuilderServiceResponseListenerLyra,
        requestBody: ByteArray
    ) {
        this.iBuilderServiceResponseListener = iBuilderServiceResponseListener
        Log.d("NETWORK","REQUEST_HEX:"+requestBody.toHexString().uppercase())
        Log.d("NETWORK", "REQUEST_ASCII: ${String(requestBody, Charsets.US_ASCII)}")

        try {
            // Collect each message emitted by the flow
            NetworkCallProvider.safeApiCall(requestBody).collect { message ->
                // Wrap each message in ResultProvider.Success
                val result = ResultProvider.Success(message)
                onNetworkServiceResponse(result)
            }
        } catch (e: Exception) {
            onNetworkServiceResponse(ResultProvider.Error(e))
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