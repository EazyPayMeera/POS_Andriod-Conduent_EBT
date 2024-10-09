package com.analogics.paymentservicecore.repository.emvService

import android.content.Context
import com.analogics.paymentservicecore.listeners.requestListener.IEmvServiceRequestListener
import com.analogics.paymentservicecore.listeners.responseListener.IEmvServiceResponseListener
import com.analogics.paymentservicecore.model.error.EmvServiceError
import com.analogics.tpaymentcore.handler.PaymentConfigurationHandler
import com.analogics.tpaymentcore.listener.IPaymentSDKListener
import javax.inject.Inject

class EmvServiceRepository @Inject constructor() :
    IEmvServiceRequestListener,
    IPaymentSDKListener {
    lateinit var iEmvServiceResponseListener: IEmvServiceResponseListener
    lateinit var context: Context


    override fun onTPaymentSDKInit(uiData: String) {
        /* Just for testing comparing with uiData value */
        if (uiData == "SUCCESS") {
            iEmvServiceResponseListener.onEmvSuccess(true)
        } else {
            iEmvServiceResponseListener.onEmvError(EmvServiceError("Error"))
        }
    }

    override fun onTPaymentSDKHandler(uiData: String) {
        /* Just for testing comparing with uiData value */
        iEmvServiceResponseListener.onDisplayProgress(false)
        if (uiData == "SUCCESS")
            iEmvServiceResponseListener.onEmvSuccess(true)
        else
            iEmvServiceResponseListener.onEmvError(EmvServiceError("Error"))
    }

    override fun onTPaymentDisplayMessage(uiData: String?) {
        iEmvServiceResponseListener.onDisplayProgress(!uiData.isNullOrBlank(), message = uiData)
    }


    override fun initPaymentSDK(
        context: Context,
        iEmvServiceResponseListener: IEmvServiceResponseListener
    ) {
        this.iEmvServiceResponseListener = iEmvServiceResponseListener
        this.context = context
        iEmvServiceResponseListener.onDisplayProgress(false)
        PaymentConfigurationHandler.initPaymentSDK(context, this)
    }

    override fun startPayment(
        context: Context,
        iEmvServiceResponseListener: IEmvServiceResponseListener
    ) {
        this.iEmvServiceResponseListener = iEmvServiceResponseListener
        iEmvServiceResponseListener.onDisplayProgress(false)
        PaymentConfigurationHandler.startPayment(context, this)
    }

    override fun onEmvServiceResponse(response: Any) {
        iEmvServiceResponseListener.onDisplayProgress(false)
        when (response) {
            is EmvServiceError -> {
                iEmvServiceResponseListener.onEmvError(EmvServiceError(response.toString()))
            }
            else ->
            {
                iEmvServiceResponseListener.onEmvSuccess(response)
            }
        }
    }
}