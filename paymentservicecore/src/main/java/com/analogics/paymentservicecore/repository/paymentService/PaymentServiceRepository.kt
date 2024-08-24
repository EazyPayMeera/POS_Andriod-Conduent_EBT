package com.analogics.paymentservicecore.repository.paymentService


import android.content.Context
import com.analogics.builder_core.listener.responseListener.IBuilderCoreResponseListener
import com.analogics.builder_core.repository.MakeRequestRepository

import com.analogics.paymentservicecore.listeners.requestListener.IPaymentService
import com.analogics.paymentservicecore.listeners.rootListener.IOnRootAppPaymentListener
import com.analogics.paymentservicecore.model.error.PaymentServiceError
import com.analogics.tpaymentcore.handler.PaymentConfigurationHandler
import com.analogics.tpaymentcore.listener.IPaymentCoreHandlerListener
import javax.inject.Inject

class PaymentServiceRepository @Inject constructor(private var makeRequestRepository: MakeRequestRepository) :
    IPaymentService,
    IPaymentCoreHandlerListener, IBuilderCoreResponseListener {
    lateinit var iOnRootAppPaymentListener: IOnRootAppPaymentListener


    override fun onTPaymentSDKHandler(uiData: String) {
        /* Just for testing comparing with uiData value */
        if (uiData == "SUCCESS")
            iOnRootAppPaymentListener.onPaymentSuccess(true)
        else
            iOnRootAppPaymentListener.onPaymentError(PaymentServiceError("Error"))
    }

    override fun onApiSuccessRes(respone: Any) {
        iOnRootAppPaymentListener.onPaymentSuccess(respone)
    }

    override fun onApiFailureRes(error: Any) {
        iOnRootAppPaymentListener.onPaymentError(PaymentServiceError(error.toString()))
    }

    override suspend fun apiEmpDetails(  iOnRootAppPaymentListener: IOnRootAppPaymentListener) {
        this.iOnRootAppPaymentListener = iOnRootAppPaymentListener
        makeRequestRepository.apiEmployeeDetails(this)
    }

    override fun initPaymentSDK(
        context: Context,
        iOnRootAppPaymentListener: IOnRootAppPaymentListener
    ) {
        this.iOnRootAppPaymentListener = iOnRootAppPaymentListener
        PaymentConfigurationHandler.initPaymentSDK(context, this)
    }

    override fun startPayment(
        context: Context,
        iOnRootAppPaymentListener: IOnRootAppPaymentListener
    ) {
        this.iOnRootAppPaymentListener = iOnRootAppPaymentListener
        PaymentConfigurationHandler.startPayment(context, this)
    }


}