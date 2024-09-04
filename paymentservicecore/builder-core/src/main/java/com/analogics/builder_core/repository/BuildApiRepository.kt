package com.analogics.builder_core.repository

import com.analogics.builder_core.listener.requestListener.IBuildApiRequestListener
import com.analogics.builder_core.listener.responseListener.IApiServiceResponseListener
import com.analogics.builder_core.utils.BuilderUtils
import com.analogics.networkservicecore.nComponent.IAPIService
import com.analogics.networkservicecore.nComponent.NetworkCallProvider
import com.analogics.networkservicecore.nComponent.ResultProvider
import okhttp3.ResponseBody
import javax.inject.Inject

class BuildApiRepository @Inject constructor(private val iApiService: IAPIService, private var  builderUtils: BuilderUtils):IBuildApiRequestListener{
    lateinit var iApiServiceResponseListener:IApiServiceResponseListener


    override suspend fun apiEmployeeDetails(iApiServiceResponseListener: IApiServiceResponseListener) {
        this.iApiServiceResponseListener=iApiServiceResponseListener
        onApiSerivceHandler(NetworkCallProvider.safeApiCall {
            iApiService.getEmployeeDetails()
        } )
    }

    override fun onApiSerivceHandler(apiResultProvider: ResultProvider<ResponseBody>) {
        when (apiResultProvider) {
            is ResultProvider.Success -> {
                iApiServiceResponseListener.onApiSuccessRes(builderUtils.formatedGsonObject(apiResultProvider.data.source().buffer.readUtf8()))
            }

            is ResultProvider.Error -> {
                iApiServiceResponseListener.onApiFailureRes(apiResultProvider.exception)
            }
            else -> Unit
        }
    }
}