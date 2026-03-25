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
            NetworkCallProvider.safeApiNetworkCall(requestBody).collect { message ->
                val result = ResultProvider.Success(message)
                onNetworkServiceResponse(result)
            }
        } catch (e: Exception) {
            onNetworkServiceResponse(ResultProvider.Error(e))
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    override suspend fun networkServiceFinancialRequest(iBuilderServiceResponseListener: IBuilderServiceResponseListenerLyra, requestBody: ByteArray)
    {
        this.iBuilderServiceResponseListener = iBuilderServiceResponseListener
        Log.d("NETWORK","REQUEST_HEX:"+requestBody.toHexString().uppercase())
        Log.d("NETWORK", "REQUEST_ASCII: ${String(requestBody, Charsets.US_ASCII)}")
        NetworkCallProvider.safeApiCall(requestBody).let {
            onNetworkServiceResponse(it)
        }
    }


    @OptIn(ExperimentalStdlibApi::class)
    override fun onNetworkServiceResponse(apiResultProvider: ResultProvider<ByteArray>) {
        when (apiResultProvider) {

            is ResultProvider.Success -> {
                Log.d("NETWORK", String(apiResultProvider.data))
                Log.d("NETWORK","RESPONSE_HEX:"+apiResultProvider.data.toHexString().uppercase())
                Log.d("NETWORK", "REQUEST_ASCII: ${String(apiResultProvider.data, Charsets.US_ASCII)}")
                iBuilderServiceResponseListener.onBuilderSuccess(apiResultProvider.data)
            }

            is ResultProvider.Error -> {
                when (apiResultProvider.exception) {
                    is java.net.SocketTimeoutException,
                    is kotlinx.coroutines.TimeoutCancellationException -> {

                        Log.e("NETWORK", "Timeout occurred: ${apiResultProvider.exception.message}")

                        // You can send a custom error back
                        iBuilderServiceResponseListener.onBuilderFailure(
                            Exception("Request timed out. Please try again.")
                        )
                    }

                    else -> {
                        Log.e("NETWORK", "Error: ${apiResultProvider.exception.message}")
                        iBuilderServiceResponseListener.onBuilderFailure(apiResultProvider.exception)
                    }
                }
            }
            else -> Unit
        }
    }
}