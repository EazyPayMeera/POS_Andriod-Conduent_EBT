package com.eazypaytech.tpaymentcore.listener.requestListener

import android.content.Context
import com.eazypaytech.tpaymentcore.listener.responseListener.IEmvSdkResponseListener
import com.eazypaytech.tpaymentcore.model.emv.AidConfig
import com.eazypaytech.tpaymentcore.model.emv.CAPKey
import com.eazypaytech.tpaymentcore.model.emv.TransConfig

interface IEmvSdkRequestListener {
    var iEmvSdkResponseListener : IEmvSdkResponseListener

    fun initPaymentSDK(aidConfig : AidConfig?,capKeys: List<CAPKey>?)
    fun startPayment(context: Context, transConfig: TransConfig?)
    fun abortPayment()
    fun getEmvTag(tag : String?) : String?
}