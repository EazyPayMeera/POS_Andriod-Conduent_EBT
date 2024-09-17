package com.analogics.builder_core.repository

import com.analogics.builder_core.listener.requestListener.IBuildApiRequestListener
import com.analogics.builder_core.listener.responseListener.IApiServiceResponseListener
import com.analogics.builder_core.utils.BuilderUtils
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
        onApiSerivceHandler(NetworkCallProvider.safeApiCall {
            iApiService.getEmployeeDetails()
        } )
    }

    override suspend fun apiRefund(
        iApiServiceResponseListener: IApiServiceResponseListener,
        requestBody: RequestBody
    ) {
        this.iApiServiceResponseListener=iApiServiceResponseListener
        onApiSerivceHandler(NetworkCallProvider.safeApiCall {
            iApiService.getRefund(requestBody)
        } )
    }

    override suspend fun apiVoid(
        iApiServiceResponseListener: IApiServiceResponseListener,
        requestBody: RequestBody
    ) {
        this.iApiServiceResponseListener=iApiServiceResponseListener
        onApiSerivceHandler(NetworkCallProvider.safeApiCall {
            iApiService.getVoid(requestBody)
        } )
    }

    override suspend fun apiPurchase(
        iApiServiceResponseListener: IApiServiceResponseListener,
        requestBody: RequestBody
    ) {
        this.iApiServiceResponseListener=iApiServiceResponseListener
        onApiSerivceHandler(NetworkCallProvider.safeApiCall {
            iApiService.getPurchase(requestBody)
        } )
    }

    override suspend fun apiPreAuth(
        iApiServiceResponseListener: IApiServiceResponseListener,
        requestBody: RequestBody
    ) {
        this.iApiServiceResponseListener=iApiServiceResponseListener
        onApiSerivceHandler(NetworkCallProvider.safeApiCall {
            iApiService.getPreAuth(requestBody)
        } )
    }

    override suspend fun apiPostAuth(
        iApiServiceResponseListener: IApiServiceResponseListener,
        requestBody: RequestBody
    ) {
        this.iApiServiceResponseListener=iApiServiceResponseListener
        onApiSerivceHandler(NetworkCallProvider.safeApiCall {
            iApiService.getAuthCapture(requestBody)
        } )
    }

    override suspend fun apiReversal(
        iApiServiceResponseListener: IApiServiceResponseListener,
        requestBody: RequestBody
    ) {
        this.iApiServiceResponseListener=iApiServiceResponseListener
        onApiSerivceHandler(NetworkCallProvider.safeApiCall {
            iApiService.getReversal(requestBody)
        } )
    }

    override suspend fun apiDeviceLogin(
        iApiServiceResponseListener: IApiServiceResponseListener,
        requestBody: RequestBody
    ) {
        this.iApiServiceResponseListener=iApiServiceResponseListener
        onApiSerivceHandler(NetworkCallProvider.safeApiCall {
            iApiService.login(requestBody)
        } )
    }


    override fun onApiSerivceHandler(apiResultProvider: ResultProvider<ResponseBody>) {
        when (apiResultProvider) {
            is ResultProvider.Success -> {
                iApiServiceResponseListener.onApiSuccessRes(apiResultProvider.data.source().buffer.readUtf8())
            }

            is ResultProvider.Error -> {
                iApiServiceResponseListener.onApiFailureRes(apiResultProvider.exception)
            }
            else -> Unit
        }
    }
}