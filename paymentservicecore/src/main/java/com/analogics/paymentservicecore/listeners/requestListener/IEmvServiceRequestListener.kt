package com.eazypaytech.paymentservicecore.listeners.requestListener

import android.content.Context
import com.eazypaytech.paymentservicecore.listeners.responseListener.IEmvServiceResponseListener
import com.eazypaytech.paymentservicecore.model.PaymentServiceTxnDetails
import com.eazypaytech.paymentservicecore.model.emv.AidConfig
import com.eazypaytech.paymentservicecore.model.emv.CAPKey
import com.eazypaytech.paymentservicecore.model.emv.TermConfig
import com.eazypaytech.paymentservicecore.model.emv.TransConfig
import java.io.File
import java.nio.file.Files

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
}