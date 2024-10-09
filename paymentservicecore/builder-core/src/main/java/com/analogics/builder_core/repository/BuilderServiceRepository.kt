package com.analogics.builder_core.repository

import com.analogics.builder_core.listener.requestListener.IBuilderServiceRequestListener
import com.analogics.builder_core.listener.responseListener.IBuilderServiceResponseListener
import com.analogics.networkservicecore.nComponent.IAPIService
import com.analogics.networkservicecore.nComponent.NetworkCallProvider
import com.analogics.networkservicecore.nComponent.ResultProvider
import okhttp3.RequestBody
import okhttp3.ResponseBody
import javax.inject.Inject

class BuilderServiceRepository @Inject constructor(private val iApiService: IAPIService):IBuilderServiceRequestListener{
    lateinit var iBuilderServiceResponseListener:IBuilderServiceResponseListener


    override suspend fun apiEmployeeDetails(iBuilderServiceResponseListener: IBuilderServiceResponseListener) {
        this.iBuilderServiceResponseListener=iBuilderServiceResponseListener
        onNetworkServiceResponse(NetworkCallProvider.safeApiCall {
            iApiService.getEmployeeDetails()
        } )
    }

    override suspend fun apiRefund(
        iBuilderServiceResponseListener: IBuilderServiceResponseListener,
        requestBody: RequestBody
    ) {
        this.iBuilderServiceResponseListener=iBuilderServiceResponseListener
        onNetworkServiceResponse(NetworkCallProvider.safeApiCall {
            iApiService.getRefund(requestBody)
        } )
    }

    override suspend fun apiVoid(
        iBuilderServiceResponseListener: IBuilderServiceResponseListener,
        requestBody: RequestBody
    ) {
        this.iBuilderServiceResponseListener=iBuilderServiceResponseListener
        onNetworkServiceResponse(NetworkCallProvider.safeApiCall {
            iApiService.getVoid(requestBody)
        } )
    }

    override suspend fun apiPurchase(
        iBuilderServiceResponseListener: IBuilderServiceResponseListener,
        requestBody: RequestBody
    ) {
        this.iBuilderServiceResponseListener=iBuilderServiceResponseListener
        onNetworkServiceResponse(NetworkCallProvider.safeApiCall {
            iApiService.getPurchase(requestBody)
        } )
    }

    override suspend fun apiPreAuth(
        iBuilderServiceResponseListener: IBuilderServiceResponseListener,
        requestBody: RequestBody
    ) {
        this.iBuilderServiceResponseListener=iBuilderServiceResponseListener
        onNetworkServiceResponse(NetworkCallProvider.safeApiCall {
            iApiService.getPreAuth(requestBody)
        } )
    }

    override suspend fun apiPostAuth(
        iBuilderServiceResponseListener: IBuilderServiceResponseListener,
        requestBody: RequestBody
    ) {
        this.iBuilderServiceResponseListener=iBuilderServiceResponseListener
        onNetworkServiceResponse(NetworkCallProvider.safeApiCall {
            iApiService.getAuthCapture(requestBody)
        } )
    }

    override suspend fun apiReversal(
        iBuilderServiceResponseListener: IBuilderServiceResponseListener,
        requestBody: RequestBody
    ) {
        this.iBuilderServiceResponseListener=iBuilderServiceResponseListener
        onNetworkServiceResponse(NetworkCallProvider.safeApiCall {
            iApiService.getReversal(requestBody)
        } )
    }

    override suspend fun apiDeviceLogin(
        iBuilderServiceResponseListener: IBuilderServiceResponseListener,
        requestBody: RequestBody
    ) {
        this.iBuilderServiceResponseListener=iBuilderServiceResponseListener
        onNetworkServiceResponse(NetworkCallProvider.safeApiCall {
            iApiService.login(requestBody)
        } )
    }

    override suspend fun apiGetAccessToken(
        iBuilderServiceResponseListener: IBuilderServiceResponseListener,
        requestBody: RequestBody
    ) {
        this.iBuilderServiceResponseListener=iBuilderServiceResponseListener
        onNetworkServiceResponse(NetworkCallProvider.safeApiCall {
            iApiService.getAccessToken(requestBody)
        } )
    }

    override fun onNetworkServiceResponse(apiResultProvider: ResultProvider<ResponseBody>) {
        when (apiResultProvider) {
            is ResultProvider.Success -> {
                iBuilderServiceResponseListener.onBuilderSuccess(apiResultProvider.data.source().buffer.readUtf8())
            }

            is ResultProvider.Error -> {
                iBuilderServiceResponseListener.onBuilderFailure(apiResultProvider.exception)
            }
            else -> Unit
        }
    }
}