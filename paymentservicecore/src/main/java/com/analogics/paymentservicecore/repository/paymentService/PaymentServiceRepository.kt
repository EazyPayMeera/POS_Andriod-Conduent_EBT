package com.analogics.paymentservicecore.repository.paymentService


import android.content.Context
import com.analogics.builder_core.listener.responseListener.IBuilderCoreResponseListener
import com.analogics.builder_core.repository.MakeRequestRepository
import com.analogics.paymentservicecore.constants.ConfigConst

import com.analogics.paymentservicecore.listeners.requestListener.IPaymentService
import com.analogics.paymentservicecore.listeners.rootListener.IOnRootAppPaymentListener
import com.analogics.paymentservicecore.model.error.PaymentServiceError
import com.analogics.securityframework.handler.SharedPrefHandler
import com.analogics.tpaymentcore.handler.PaymentConfigurationHandler
import com.analogics.tpaymentcore.listener.IPaymentCoreHandlerListener
import javax.inject.Inject

class PaymentServiceRepository @Inject constructor(private var makeRequestRepository: MakeRequestRepository) :
    IPaymentService,
    IPaymentCoreHandlerListener, IBuilderCoreResponseListener {
    lateinit var iOnRootAppPaymentListener: IOnRootAppPaymentListener
    lateinit var context: Context
    override fun onTPaymentSDKInit(uiData: String) {
        /* Just for testing comparing with uiData value */
        if (uiData == "SUCCESS") {
            isPaymentSDKInit(context, true)
            iOnRootAppPaymentListener.onPaymentSuccess(true)
        }
        else {
            isPaymentSDKInit(context, false)
            iOnRootAppPaymentListener.onPaymentError(PaymentServiceError("Error"))
        }
    }

    override fun onTPaymentSDKHandler(uiData: String) {
        /* Just for testing comparing with uiData value */
        if (uiData == "SUCCESS")
            iOnRootAppPaymentListener.onPaymentSuccess(true)
        else
            iOnRootAppPaymentListener.onPaymentError(PaymentServiceError("Error"))
    }

    override fun onApiSuccessRes(response: Any) {
        iOnRootAppPaymentListener.onPaymentSuccess(response)
    }

    override fun onApiFailureRes(error: Any) {
        iOnRootAppPaymentListener.onPaymentError(PaymentServiceError(error.toString()))
    }

    override suspend fun apiEmpDetails(iOnRootAppPaymentListener: IOnRootAppPaymentListener) {
        this.iOnRootAppPaymentListener = iOnRootAppPaymentListener
        makeRequestRepository.apiEmployeeDetails(this)
    }

    override fun initPaymentSDK(
        context: Context,
        iOnRootAppPaymentListener: IOnRootAppPaymentListener
    ) {
        this.iOnRootAppPaymentListener = iOnRootAppPaymentListener
        this.context = context
        if(!isPaymentSDKInit(context))
            PaymentConfigurationHandler.initPaymentSDK(context, this)
    }

    override fun startPayment(
        context: Context,
        iOnRootAppPaymentListener: IOnRootAppPaymentListener
    ) {
        this.iOnRootAppPaymentListener = iOnRootAppPaymentListener
        PaymentConfigurationHandler.startPayment(context, this)
    }

    override fun isPaymentSDKInit(context: Context): Boolean {
        try {
            return SharedPrefHandler.getConfigVal(context, ConfigConst.CONFIG_KEY_IS_PAYMENT_SDK_INIT) == true
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    override fun isPaymentSDKInit(context: Context, isInit: Boolean) {
        try {
            SharedPrefHandler.setConfigVal(context, ConfigConst.CONFIG_KEY_IS_PAYMENT_SDK_INIT, isInit)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}