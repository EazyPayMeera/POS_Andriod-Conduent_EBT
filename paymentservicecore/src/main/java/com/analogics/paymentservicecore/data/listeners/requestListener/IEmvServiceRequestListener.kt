package com.analogics.paymentservicecore.data.listeners.requestListener

import android.content.Context
import com.analogics.paymentservicecore.data.listeners.responseListener.IEmvServiceResponseListener
import com.analogics.paymentservicecore.data.model.PaymentServiceTxnDetails
import com.analogics.paymentservicecore.data.model.emv.AidConfig
import com.analogics.paymentservicecore.data.model.emv.CAPKey
import com.analogics.paymentservicecore.data.model.emv.TermConfig

interface IEmvServiceRequestListener {
    fun initPaymentSDK(termConfig: TermConfig?=null, aidConfig: String?=null, capKeys: String?=null, iEmvServiceResponseListener: IEmvServiceResponseListener)
    fun initPaymentSDK(termConfig: TermConfig?=null, aidConfig: AidConfig?=null, capKeys: List<CAPKey>, iEmvServiceResponseListener: IEmvServiceResponseListener)
    fun startPayment(
        context: Context,
        paymentServiceTxnDetails : PaymentServiceTxnDetails?=null,
        iEmvServiceResponseListener: IEmvServiceResponseListener
    )
    fun pinGeneration(pan: String?, amount: String, nResult: (pinBlock: ByteArray?) -> Unit)
    fun abortPayment()
    fun isCardExists(context: Context):Boolean
}