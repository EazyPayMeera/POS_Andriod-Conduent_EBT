package com.analogics.builder_core.repository

import com.analogics.builder_core.listener.requestListener.IMakeRequestListener
import com.analogics.builder_core.listener.responseListener.IBuilderCoreResponseListener
import com.analogics.builder_core.utils.BuilderUtils
import com.analogics.networkservicecore.nComponent.IAPIService
import com.analogics.networkservicecore.nComponent.NetworkCallProvider
import com.analogics.networkservicecore.nComponent.ResultProvider
import com.example.example.ObjEmployeeResponse
import com.google.gson.Gson
import okhttp3.ResponseBody
import javax.inject.Inject

class MakeRequestRepository @Inject constructor(private val iApiService: IAPIService,private var  builderUtils: BuilderUtils):IMakeRequestListener{
    lateinit var iBuilderCoreResponseListener:IBuilderCoreResponseListener


    override suspend fun apiEmployeeDetails(iBuilderCoreResponseListener: IBuilderCoreResponseListener) {
        this.iBuilderCoreResponseListener=iBuilderCoreResponseListener
        onApiSerivceHandler(NetworkCallProvider.safeApiCall {
            iApiService.getEmployeeDetails()
        } )
    }

    override fun onApiSerivceHandler(apiResultProvider: ResultProvider<ResponseBody>) {
        when (apiResultProvider) {
            is ResultProvider.Success -> {
                iBuilderCoreResponseListener.onApiSuccessRes(builderUtils.formatedGsonObject(apiResultProvider.data.source().buffer.readUtf8()))
            }

            is ResultProvider.Error -> {
                iBuilderCoreResponseListener.onApiFailureRes(apiResultProvider.exception)
            }
            else -> Unit
        }
    }
}