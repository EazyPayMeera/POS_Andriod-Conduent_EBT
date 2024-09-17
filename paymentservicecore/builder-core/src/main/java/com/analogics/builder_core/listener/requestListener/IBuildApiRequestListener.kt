package com.analogics.builder_core.listener.requestListener

import com.analogics.builder_core.listener.responseListener.IApiServiceResponseListener
import com.analogics.networkservicecore.nComponent.ResultProvider
import okhttp3.RequestBody
import okhttp3.ResponseBody

interface IBuildApiRequestListener {
    suspend fun apiEmployeeDetails(iApiServiceResponseListener: IApiServiceResponseListener)
    suspend fun apiRefund(iApiServiceResponseListener: IApiServiceResponseListener,requestBody: RequestBody)
    suspend fun apiVoid(iApiServiceResponseListener: IApiServiceResponseListener,requestBody: RequestBody)
    suspend fun apiPurchase(iApiServiceResponseListener: IApiServiceResponseListener,requestBody: RequestBody)
    suspend fun apiPreAuth(iApiServiceResponseListener: IApiServiceResponseListener,requestBody: RequestBody)
    suspend fun apiPostAuth(iApiServiceResponseListener: IApiServiceResponseListener,requestBody: RequestBody)
    suspend fun apiReversal(iApiServiceResponseListener: IApiServiceResponseListener,requestBody: RequestBody)
    suspend fun apiDeviceLogin(iApiServiceResponseListener: IApiServiceResponseListener,requestBody: RequestBody)

    fun onApiSerivceHandler(apiResultProvider: ResultProvider<ResponseBody>)

}