package com.analogics.paymentservicecore.listeners.requestListener

import android.content.Context
import com.analogics.builder_core.model.PaymentServiceTxnDetails
import com.analogics.paymentservicecore.listeners.rootListener.IApiServiceResponseListener
import com.analogics.paymentservicecore.models.PosConfig

interface IApiServiceRequestListener {
    fun initPaymentSDK(context: Context, iApiServiceResponseListener: IApiServiceResponseListener)
    fun startPayment(
        context: Context,
        iApiServiceResponseListener: IApiServiceResponseListener
    )

    fun getPosConfig(context: Context): PosConfig

    suspend fun apiServiceRefund(
        paymentServiceTxnDetails: PaymentServiceTxnDetails?,
        iApiServiceResponseListener: IApiServiceResponseListener
    )

    suspend fun apiServiceVoid(
        paymentServiceTxnDetails: PaymentServiceTxnDetails?,
        iApiServiceResponseListener: IApiServiceResponseListener
    )

    suspend fun apiServicePurchase(
        paymentServiceTxnDetails: PaymentServiceTxnDetails?,
        iApiServiceResponseListener: IApiServiceResponseListener
    )

    suspend fun apiServiceAuthCapture(
        paymentServiceTxnDetails: PaymentServiceTxnDetails?,
        iApiServiceResponseListener: IApiServiceResponseListener
    )

    suspend fun apiServiceReversal(
        paymentServiceTxnDetails: PaymentServiceTxnDetails?,
        iApiServiceResponseListener: IApiServiceResponseListener
    )

    suspend fun apiServiceLogin(
        paymentServiceTxnDetails: PaymentServiceTxnDetails?,
        iApiServiceResponseListener: IApiServiceResponseListener
    )

    suspend fun apiServiceAccessToken(
        paymentServiceTxnDetails: PaymentServiceTxnDetails?,
        iApiServiceResponseListener: IApiServiceResponseListener
    )

    suspend fun apiServiceBatch(
        paymentServiceTxnDetails: PaymentServiceTxnDetails?,
        iApiServiceResponseListener: IApiServiceResponseListener
    )

    fun onApiServiceResponse(response: Any)
}