package com.analogics.paymentservicecore.listeners.requestListener

import android.content.Context
import com.analogics.builder_core.model.PaymentServiceTxnDetails
import com.analogics.paymentservicecore.listeners.responseListener.IEmvServiceResponseListener
import com.analogics.paymentservicecore.listeners.rootListener.IApiServiceResponseListener
import com.analogics.paymentservicecore.models.PosConfig

interface IEmvServiceRequestListener {
    fun initPaymentSDK(context: Context, iEmvServiceResponseListener: IEmvServiceResponseListener)
    fun startPayment(
        context: Context,
        iEmvServiceResponseListener: IEmvServiceResponseListener
    )

    fun onEmvServiceResponse(response: Any)
}