package com.analogics.paymentservicecore.repository.paymentService


import android.content.Context
import com.analogics.paymentservicecore.listeners.requestListener.IMakeRequestListener
import com.analogics.paymentservicecore.listeners.responseListener.IResultProviderListener
import com.analogics.tpaymentcore.handler.PaymentConfigurationHandler
import com.analogics.tpaymentcore.listener.IPaymentCoreHandlerListener
import javax.inject.Inject

class PaymentServiceRepository @Inject constructor():
    IMakeRequestListener, IPaymentCoreHandlerListener {
    lateinit var iResultProviderListener: IResultProviderListener
    lateinit var iPaymentCoreHandlerListener: IPaymentCoreHandlerListener
    override suspend fun getUserListAPI(iResultProviderListener: IResultProviderListener) {
        this.iResultProviderListener = iResultProviderListener
        /*iResultProviderListener.onSuccess(NetworkCallProvider.safeApiCall {
            iApiService.getUserData()
        })*/
    }

    override suspend fun initPaymentSDK(
        context: Context,
        iResultProviderListener: IResultProviderListener
    ) {
        this.iResultProviderListener = iResultProviderListener
        PaymentConfigurationHandler.initPaymentSDK(context,this)
    }

    override fun onPMTRespHandler(uiData: String) {
        /* Just for testing comparing with uiData value */
        if(uiData=="SUCCESS")
            iResultProviderListener.onSuccess(true)
        else
            iResultProviderListener.onSuccess(false)
    }
}