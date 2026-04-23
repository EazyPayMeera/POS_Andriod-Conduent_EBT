package com.analogics.builder_core.domain.repository

import android.util.Log
import com.analogics.builder_core.domain.listener.requestListener.IBuilderServiceRequestListener
import com.analogics.builder_core.domain.listener.responseListener.IBuilderServiceResponseListener
import com.analogics.networkservicecore.data.remote.NetworkCallProvider
import com.analogics.networkservicecore.data.remote.ResultProvider
import javax.inject.Inject

class BuilderServiceRepository @Inject constructor(): IBuilderServiceRequestListener {
    lateinit var iBuilderServiceResponseListener: IBuilderServiceResponseListener


    override suspend fun networkServiceRequest(
        iBuilderServiceResponseListener: IBuilderServiceResponseListener,
        requestBody: ByteArray
    ) {
        this.iBuilderServiceResponseListener = iBuilderServiceResponseListener
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


    override suspend fun handShakeRequest(iBuilderServiceResponseListener: IBuilderServiceResponseListener, requestBody: ByteArray)
    {
        this.iBuilderServiceResponseListener = iBuilderServiceResponseListener
        Log.d("NETWORK", "REQUEST_ASCII: ${String(requestBody, Charsets.US_ASCII)}")
        NetworkCallProvider.safeApiCall(requestBody).let {
            onNetworkServiceResponse(it)
        }
    }

    fun networkServiceResponse(
        iBuilderServiceResponseListener: IBuilderServiceResponseListener,
        requestBody: ByteArray
    ) {
        this.iBuilderServiceResponseListener = iBuilderServiceResponseListener
        Log.d("NETWORK", "REQUEST_ASCII: ${String(requestBody, Charsets.US_ASCII)}")
        NetworkCallProvider.safeApiResponse(requestBody)

    }

    override suspend fun networkServiceFinancialRequest(iBuilderServiceResponseListener: IBuilderServiceResponseListener, requestBody: ByteArray)
    {
        this.iBuilderServiceResponseListener = iBuilderServiceResponseListener
        Log.d("NETWORK", "REQUEST_ASCII: ${String(requestBody, Charsets.US_ASCII)}")
        NetworkCallProvider.safeApiCall(requestBody).let {
            onNetworkServiceResponse(it)
        }
    }


    override fun onNetworkServiceResponse(apiResultProvider: ResultProvider<ByteArray>) {
        when (apiResultProvider) {

            is ResultProvider.Success -> {
                Log.d("NETWORK", "RESPONSE_ASCII: ${String(apiResultProvider.data, Charsets.US_ASCII)}")
                iBuilderServiceResponseListener.onBuilderSuccess(apiResultProvider.data)
            }

            is ResultProvider.Error -> {
                when (apiResultProvider.exception) {
                    is java.net.SocketTimeoutException,
                    is kotlinx.coroutines.TimeoutCancellationException -> {
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