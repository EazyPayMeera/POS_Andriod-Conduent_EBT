package com.analogics.builder_core.listener.requestListener

import com.analogics.builder_core.listener.responseListener.IApiServiceResponseListener
import com.analogics.networkservicecore.nComponent.ResultProvider
import okhttp3.ResponseBody

interface IBuildApiRequestListener {
    suspend fun apiEmployeeDetails(iApiServiceResponseListener: IApiServiceResponseListener)
    fun onApiSerivceHandler(apiResultProvider: ResultProvider<ResponseBody>)

}