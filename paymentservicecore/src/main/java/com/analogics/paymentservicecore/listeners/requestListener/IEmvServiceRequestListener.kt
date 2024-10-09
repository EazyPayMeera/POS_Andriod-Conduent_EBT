package com.analogics.paymentservicecore.listeners.requestListener

import android.content.Context
import com.analogics.paymentservicecore.listeners.responseListener.IApiServiceResponseListener
import com.analogics.paymentservicecore.listeners.responseListener.IEmvServiceResponseListener

interface IEmvServiceRequestListener {
    fun initPaymentSDK(context: Context, iEmvServiceResponseListener: IEmvServiceResponseListener)
    fun startPayment(
        context: Context,
        iEmvServiceResponseListener: IEmvServiceResponseListener
    )

    fun onEmvServiceResponse(response: Any)
}