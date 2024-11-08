package com.analogics.builder_core.listener.requestListener

import com.analogics.builder_core.listener.responseListener.IBuilderServiceResponseListener
import com.analogics.networkservicecore.nComponent.ResultProvider
import okhttp3.RequestBody
import okhttp3.ResponseBody

interface IBuilderServiceRequestListener {
    suspend fun apiEmployeeDetails(iBuilderServiceResponseListener: IBuilderServiceResponseListener)
    suspend fun apiRefund(iBuilderServiceResponseListener: IBuilderServiceResponseListener, requestBody: RequestBody)
    suspend fun apiVoid(iBuilderServiceResponseListener: IBuilderServiceResponseListener, requestBody: RequestBody)
    suspend fun apiPurchase(iBuilderServiceResponseListener: IBuilderServiceResponseListener, requestBody: RequestBody)
    suspend fun apiPreAuth(iBuilderServiceResponseListener: IBuilderServiceResponseListener, requestBody: RequestBody)
    suspend fun apiPostAuth(iBuilderServiceResponseListener: IBuilderServiceResponseListener, requestBody: RequestBody)
    suspend fun apiReversal(iBuilderServiceResponseListener: IBuilderServiceResponseListener, requestBody: RequestBody)
    suspend fun apiDeviceLogin(iBuilderServiceResponseListener: IBuilderServiceResponseListener, requestBody: RequestBody)
    suspend fun apiGetAccessToken(iBuilderServiceResponseListener: IBuilderServiceResponseListener, requestBody: RequestBody)
    suspend fun apiRklRequest(iBuilderServiceResponseListener: IBuilderServiceResponseListener, requestBody: ByteArray)

    fun onNetworkServiceResponse(apiResultProvider: ResultProvider<ResponseBody>)

}