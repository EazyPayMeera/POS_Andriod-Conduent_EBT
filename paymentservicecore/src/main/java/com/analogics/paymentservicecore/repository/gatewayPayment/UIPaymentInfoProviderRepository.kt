package com.analogics.paymentservicecore.repository.gatewayPayment

import android.content.Context
import com.analogics.tpaymentcore.handler.PaymentConfigurationHandler
import com.analogics.tpaymentcore.listener.IPaymentCoreHandlerListener

object UIPaymentInfoProviderRepository:IPaymentCoreHandlerListener {
    override fun onPMTRespHandler(uiData:String) {
        //PaymentConfigurationHandler.initPayment(this, )
    }
}