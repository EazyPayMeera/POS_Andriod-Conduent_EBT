package com.analogics.builder_core.domain.repository

import android.util.Log
import com.analogics.builder_core.domain.listener.requestListener.IBuilderServiceRequestListenerLyra
import com.analogics.builder_core.domain.listener.responseListener.IBuilderServiceResponseListenerLyra
import com.analogics.networkservicecore.data.remote.NetworkCallProvider
import com.analogics.networkservicecore.data.remote.ResultProvider
import kotlinx.coroutines.TimeoutCancellationException
import java.net.SocketTimeoutException
import javax.inject.Inject

class BuilderServiceRepositoryLyra @Inject constructor(): IBuilderServiceRequestListenerLyra {
    lateinit var iBuilderServiceResponseListener: IBuilderServiceResponseListenerLyra

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
    override suspend fun handShakeRequest(iBuilderServiceResponseListener: IBuilderServiceResponseListenerLyra, requestBody: ByteArray)
    {
        this.iBuilderServiceResponseListener = iBuilderServiceResponseListener
        Log.d("NETWORK","REQUEST_HEX:"+requestBody.toHexString().uppercase())
        Log.d("NETWORK", "REQUEST_ASCII: ${String(requestBody, Charsets.US_ASCII)}")
        NetworkCallProvider.safeApiCall(requestBody).let {
            onNetworkServiceResponse(it)
        }
    }


    @OptIn(ExperimentalStdlibApi::class)
    fun networkServiceResponse(
        iBuilderServiceResponseListener: IBuilderServiceResponseListenerLyra,
        requestBody: ByteArray
    ) {
        this.iBuilderServiceResponseListener = iBuilderServiceResponseListener
        Log.d("NETWORK", "REQUEST_HEX: " + requestBody.toHexString().uppercase())
        Log.d("NETWORK", "REQUEST_ASCII: ${String(requestBody, Charsets.US_ASCII)}")
        NetworkCallProvider.safeApiResponse(requestBody)

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
                    is SocketTimeoutException,
                    is TimeoutCancellationException -> {

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