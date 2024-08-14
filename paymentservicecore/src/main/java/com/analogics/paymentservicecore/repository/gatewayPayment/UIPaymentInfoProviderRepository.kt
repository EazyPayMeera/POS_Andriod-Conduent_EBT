package com.analogics.paymentservicecore.repository.gatewayPayment

import android.content.Context
import com.analogics.paymentservicecore.listeners.responseListener.IResultProviderListener
import com.analogics.tpaymentcore.handler.PaymentConfigurationHandler
import com.analogics.tpaymentcore.listener.IPaymentCoreHandlerListener

object UIPaymentInfoProviderRepository:IPaymentCoreHandlerListener{
    lateinit var iResultProviderListener: IResultProviderListener
    override fun onPMTRespHandler(uiData:String) {

    }
    fun initPayment(context: Context, iResultProviderListener: IResultProviderListener) {
        this.iResultProviderListener = iResultProviderListener

        UIPaymentInfoProviderRepository.iResultProviderListener.getResultSuccess(PaymentConfigurationHandler.initPayment(context))
    }
}