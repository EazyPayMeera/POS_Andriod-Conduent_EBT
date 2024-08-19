package com.analogics.paymentservicecore.repository.gatewayPayment

import android.content.Context
import com.analogics.paymentservicecore.listeners.responseListener.IPaymentServiceResultListener
import com.analogics.tpaymentcore.components.ResultProvider
import com.analogics.tpaymentcore.handler.PaymentConfigurationHandler.initPaymentSDK
import java.lang.Exception

object UIPaymentInfoProviderRepository {

    fun initPaymentSDK(context: Context, iTransactionResultListener: IPaymentServiceResultListener) {
        when (val result =
            initPaymentSDK<Boolean>(context)) {
            is ResultProvider.Success -> {
                iTransactionResultListener.onSuccess(result.data)
            }
            is ResultProvider.Error -> {
                iTransactionResultListener.onFailure(result.exception)
            }
            else -> {
                iTransactionResultListener.onFailure(Exception("Oops!! Something went wrong"))
            }
        }
    }
}