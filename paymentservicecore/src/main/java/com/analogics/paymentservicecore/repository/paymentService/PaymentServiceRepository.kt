package com.analogics.paymentservicecore.repository.paymentService


import android.content.Context
import com.analogics.paymentservicecore.listeners.requestListener.IMakeReqeustListener
import com.analogics.paymentservicecore.listeners.responseListener.IResultProviderListener
import com.analogics.networkservicecore.nComponent.IAPIService
import com.analogics.networkservicecore.nComponent.NetworkCallProvider
import com.analogics.paymentservicecore.listeners.rootListener.IOnRootAppListener
import com.analogics.tpaymentcore.handler.PaymentConfigurationHandler
import com.analogics.tpaymentcore.listener.IPaymentCoreHandlerListener
import com.analogics.tpaymentcore.model.TError
import javax.inject.Inject

class MakeRequestRepository @Inject constructor(private val iApiService: IAPIService) :
    IMakeReqeustListener,IPaymentCoreHandlerListener {
    lateinit var iResultProviderListener: IResultProviderListener
    lateinit var iOnRootAppListener: IOnRootAppListener
    override suspend fun getUserListAPI(iResultProviderListener: IResultProviderListener) {
        this.iResultProviderListener = iResultProviderListener
        iResultProviderListener.getResultSuccess(NetworkCallProvider.safeApiCall {
            iApiService.getUserData()
        })
    }

    override suspend fun initPayment(iOnRootAppListener: IOnRootAppListener, context: Context) {
        this.iOnRootAppListener = iOnRootAppListener
        PaymentConfigurationHandler.initPayment(this,context)
    }

    override fun onPMTRespHandler(paymentResponse: Any) {
        if(paymentResponse is TError) {
            iOnRootAppListener.onPaymentError(paymentResponse)
        }else {
            iOnRootAppListener.onSuccess(paymentResponse as String)
        }
    }
}