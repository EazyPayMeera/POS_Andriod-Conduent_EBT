package com.analogics.paymentservicecore.repository.paymentService


import android.content.Context
import com.analogics.builder_core.listener.responseListener.IApiServiceResponseListener
import com.analogics.builder_core.repository.BuildApiRepository
import com.analogics.paymentservicecore.constants.ConfigConstants

import com.analogics.paymentservicecore.listeners.requestListener.IPaymentServiceRequestListener
import com.analogics.paymentservicecore.listeners.rootListener.IOnRootAppPaymentListener
import com.analogics.paymentservicecore.model.error.PaymentServiceError
import com.analogics.paymentservicecore.models.PosConfig
import com.analogics.securityframework.handler.SharedPrefHandler
import com.analogics.tpaymentcore.handler.PaymentConfigurationHandler
import com.analogics.tpaymentcore.listener.IPaymentSDKListener
import javax.inject.Inject

class PaymentServiceRepository @Inject constructor(private var buildApiRepository: BuildApiRepository) :
    IPaymentServiceRequestListener,
    IPaymentSDKListener, IApiServiceResponseListener {
    lateinit var iOnRootAppPaymentListener: IOnRootAppPaymentListener
    lateinit var context: Context

    override fun onTPaymentSDKInit(uiData: String) {
        /* Just for testing comparing with uiData value */
        if (uiData == "SUCCESS") {
            PosConfig.apply { isPaymentSDKInit = true }.saveToPrefs(context)
            iOnRootAppPaymentListener.onPaymentSuccess(true)
        } else {
            PosConfig.apply { isPaymentSDKInit = false }.saveToPrefs(context)
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
        buildApiRepository.apiEmployeeDetails(this)
    }

    override fun initPaymentSDK(
        context: Context,
        iOnRootAppPaymentListener: IOnRootAppPaymentListener
    ) {
        this.iOnRootAppPaymentListener = iOnRootAppPaymentListener
        this.context = context
        if (PosConfig.isPaymentSDKInit != true)
            PaymentConfigurationHandler.initPaymentSDK(context, this)
    }

    override fun startPayment(
        context: Context,
        iOnRootAppPaymentListener: IOnRootAppPaymentListener
    ) {
        this.iOnRootAppPaymentListener = iOnRootAppPaymentListener
        PaymentConfigurationHandler.startPayment(context, this)
    }

    override fun getPosConfig(): PosConfig {
        return PosConfig
    }
}