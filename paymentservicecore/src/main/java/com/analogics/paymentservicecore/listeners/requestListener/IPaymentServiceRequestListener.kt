package com.analogics.paymentservicecore.listeners.requestListener

import android.content.Context
import com.analogics.builder_core.model.PaymentServiceTxnDetails
import com.analogics.paymentservicecore.listeners.rootListener.IOnRootAppPaymentListener
import com.analogics.paymentservicecore.models.PosConfig

interface IPaymentServiceRequestListener {
    fun initPaymentSDK(context: Context, iOnRootAppPaymentListener: IOnRootAppPaymentListener)
    fun startPayment(
        context: Context,
        iOnRootAppPaymentListener: IOnRootAppPaymentListener
    )

    fun getPosConfig(context: Context): PosConfig

    suspend fun apiServiceRefund(
        paymentServiceTxnDetails: PaymentServiceTxnDetails?,
        iOnRootAppPaymentListener: IOnRootAppPaymentListener
    )

    suspend fun apiServiceVoid(
        paymentServiceTxnDetails: PaymentServiceTxnDetails?,
        iOnRootAppPaymentListener: IOnRootAppPaymentListener
    )

    suspend fun apiServicePurchase(
        paymentServiceTxnDetails: PaymentServiceTxnDetails?,
        iOnRootAppPaymentListener: IOnRootAppPaymentListener
    )

    suspend fun apiServiceAuthCapture(
        paymentServiceTxnDetails: PaymentServiceTxnDetails?,
        iOnRootAppPaymentListener: IOnRootAppPaymentListener
    )

    suspend fun apiServiceReversal(
        paymentServiceTxnDetails: PaymentServiceTxnDetails?,
        iOnRootAppPaymentListener: IOnRootAppPaymentListener
    )

    suspend fun apiServiceLogin(
        paymentServiceTxnDetails: PaymentServiceTxnDetails?,
        iOnRootAppPaymentListener: IOnRootAppPaymentListener
    )

    suspend fun apiServiceAccessToken(
        paymentServiceTxnDetails: PaymentServiceTxnDetails?,
        iOnRootAppPaymentListener: IOnRootAppPaymentListener
    )

    suspend fun apiServiceBatch(
        paymentServiceTxnDetails: PaymentServiceTxnDetails?,
        iOnRootAppPaymentListener: IOnRootAppPaymentListener
    )

    fun onAPIServiceResponse(response: Any)
}