package com.analogics.paymentservicecore.repository.paymentService


import com.analogics.paymentservicecore.listeners.requestListener.IMakeReqeustListener
import com.analogics.paymentservicecore.listeners.responseListener.IResultProviderListener
import com.analogics.networkservicecore.nComponent.IAPIService
import com.analogics.networkservicecore.nComponent.NetworkCallProvider
import javax.inject.Inject

class MakeRequestRepository @Inject constructor(private val iApiService: IAPIService):
    IMakeReqeustListener {
    lateinit var iResultProviderListener: IResultProviderListener
    override suspend fun getUserListAPI(iResultProviderListener: IResultProviderListener) {
        this.iResultProviderListener = iResultProviderListener
        iResultProviderListener.getResultSuccess(NetworkCallProvider.safeApiCall {
            iApiService.getUserData()
        })
    }
}