package com.eazypaytech.paymentservicecore.listeners.requestListener

import com.eazypaytech.paymentservicecore.listeners.responseListener.IApiServiceResponseListener
import com.eazypaytech.paymentservicecore.model.PaymentServiceTxnDetails
import com.eazypaytech.paymentservicecore.models.PosConfig

interface IApiServiceRequestListener {

    fun getPosConfig(): PosConfig

    suspend fun apiServiceRequestOnlineAuth(
        paymentServiceTxnDetails: PaymentServiceTxnDetails?,
        iApiServiceResponseListener: IApiServiceResponseListener
    )

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

    suspend fun apiServicePreAuth(
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

    suspend fun signOnRequest(
        paymentServiceTxnDetails: PaymentServiceTxnDetails?,
        iApiServiceResponseListener: IApiServiceResponseListener
    )

    suspend fun keyExchange(
        paymentServiceTxnDetails: PaymentServiceTxnDetails?,
        iApiServiceResponseListener: IApiServiceResponseListener
    )

    suspend fun keyChange(
        paymentServiceTxnDetails: PaymentServiceTxnDetails?,
        iApiServiceResponseListener: IApiServiceResponseListener
    )

    fun onApiServiceResponse(response: Any)
}