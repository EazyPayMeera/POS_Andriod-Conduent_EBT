package com.analogics.builder_core.repository

import android.util.Log
import com.analogics.builder_core.listener.requestListener.IBuildApiRequestListener
import com.analogics.builder_core.listener.responseListener.IApiServiceResponseListener
import com.analogics.networkservicecore.nComponent.IAPIService
import com.analogics.networkservicecore.nComponent.NetworkCallProvider
import com.analogics.networkservicecore.nComponent.ResultProvider
import okhttp3.RequestBody
import okhttp3.ResponseBody
import javax.inject.Inject

class BuildApiRepository @Inject constructor(private val iApiService: IAPIService):IBuildApiRequestListener{
    lateinit var iApiServiceResponseListener:IApiServiceResponseListener


    override suspend fun apiEmployeeDetails(iApiServiceResponseListener: IApiServiceResponseListener) {
        this.iApiServiceResponseListener=iApiServiceResponseListener
        onNetworkServiceResponse(NetworkCallProvider.safeApiCall {
            iApiService.getEmployeeDetails()
        } )
    }

    override suspend fun apiRefund(
        iApiServiceResponseListener: IApiServiceResponseListener,
        requestBody: RequestBody
    ) {
        this.iApiServiceResponseListener=iApiServiceResponseListener
        onNetworkServiceResponse(NetworkCallProvider.safeApiCall {
            iApiService.getRefund(requestBody)
        } )
    }

    override suspend fun apiVoid(
        iApiServiceResponseListener: IApiServiceResponseListener,
        requestBody: RequestBody
    ) {
        this.iApiServiceResponseListener=iApiServiceResponseListener
        onNetworkServiceResponse(NetworkCallProvider.safeApiCall {
            iApiService.getVoid(requestBody)
        } )
    }

    override suspend fun apiPurchase(
        iApiServiceResponseListener: IApiServiceResponseListener,
        requestBody: RequestBody
    ) {
        this.iApiServiceResponseListener=iApiServiceResponseListener
        onNetworkServiceResponse(NetworkCallProvider.safeApiCall {
            iApiService.getPurchase(requestBody)
        } )
    }

    override suspend fun apiPreAuth(
        iApiServiceResponseListener: IApiServiceResponseListener,
        requestBody: RequestBody
    ) {
        this.iApiServiceResponseListener=iApiServiceResponseListener
        onNetworkServiceResponse(NetworkCallProvider.safeApiCall {
            iApiService.getPreAuth(requestBody)
        } )
    }

    override suspend fun apiPostAuth(
        iApiServiceResponseListener: IApiServiceResponseListener,
        requestBody: RequestBody
    ) {
        this.iApiServiceResponseListener=iApiServiceResponseListener
        onNetworkServiceResponse(NetworkCallProvider.safeApiCall {
            iApiService.getAuthCapture(requestBody)
        } )
    }

    override suspend fun apiReversal(
        iApiServiceResponseListener: IApiServiceResponseListener,
        requestBody: RequestBody
    ) {
        this.iApiServiceResponseListener=iApiServiceResponseListener
        onNetworkServiceResponse(NetworkCallProvider.safeApiCall {
            iApiService.getReversal(requestBody)
        } )
    }

    override suspend fun apiDeviceLogin(
        iApiServiceResponseListener: IApiServiceResponseListener,
        requestBody: RequestBody
    ) {
        this.iApiServiceResponseListener=iApiServiceResponseListener
        onNetworkServiceResponse(NetworkCallProvider.safeApiCall {
            iApiService.login(requestBody)
        } )
    }

    override suspend fun apiGetAccessToken(
        iApiServiceResponseListener: IApiServiceResponseListener,
        requestBody: RequestBody
    ) {
        this.iApiServiceResponseListener=iApiServiceResponseListener
        onNetworkServiceResponse(NetworkCallProvider.safeApiCall {
            iApiService.getAccessToken(requestBody)
        } )
    }

    override fun onNetworkServiceResponse(apiResultProvider: ResultProvider<ResponseBody>) {
        when (apiResultProvider) {
            is ResultProvider.Success -> {
                iApiServiceResponseListener.onApiSuccess(apiResultProvider.data.source().buffer.readUtf8())
            }

            is ResultProvider.Error -> {
                iApiServiceResponseListener.onApiFailure(apiResultProvider.exception)
            }
            else -> Unit
        }
    }
}