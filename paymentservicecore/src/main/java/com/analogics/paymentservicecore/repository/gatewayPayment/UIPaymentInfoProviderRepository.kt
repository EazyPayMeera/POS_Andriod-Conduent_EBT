package com.analogics.paymentservicecore.repository.gatewayPayment

import android.content.Context
import com.analogics.networkservicecore.nComponent.ResultProvider
import com.analogics.networkservicecore.nComponent.ResultProviderString
import com.analogics.paymentservicecore.R
import com.analogics.paymentservicecore.listeners.responseListener.IResultProviderListener
import com.analogics.paymentservicecore.listeners.responseListener.ITransactionResultListener
import com.analogics.paymentservicecore.models.TransactionData
import com.analogics.tpaymentcore.handler.PaymentConfigurationHandler
import com.analogics.tpaymentcore.handler.PaymentConfigurationHandler.initPayment
import com.analogics.tpaymentcore.listener.IPaymentCoreHandlerListener
import java.lang.Exception

object UIPaymentInfoProviderRepository:IPaymentCoreHandlerListener {
    override fun onPMTRespHandler(uiData: String) {
    }

    fun initPayment(context: Context, iTransactionResultListener: ITransactionResultListener) {
        when (val transactionData = initPayment<TransactionData>(context)) {
            is ResultProvider.Success -> {
                iTransactionResultListener.onSuccess(transactionData.data)
            }
            is ResultProvider.Error -> {
                iTransactionResultListener.onFailure(transactionData.exception)
            }
            else -> {
                iTransactionResultListener.onFailure(Exception("Oops!! Something went wrong"))
            }
        }
    }
}