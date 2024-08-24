package com.analogics.builder_core.listener.requestListener

import com.analogics.builder_core.listener.responseListener.IBuilderCoreResponseListener
import com.analogics.networkservicecore.nComponent.ResultProvider
import okhttp3.ResponseBody

interface IMakeRequestListener {
    suspend fun apiEmployeeDetails(iBuilderCoreResponseListener: IBuilderCoreResponseListener)
    fun onApiSerivceHandler(apiResultProvider: ResultProvider<ResponseBody>)

}