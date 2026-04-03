package com.eazypaytech.hardwarecore.domain.listener.requestListener

import android.content.Context
import com.eazypaytech.hardwarecore.domain.listener.responseListener.IEmvSdkResponseListener
import com.eazypaytech.hardwarecore.data.model.AidConfig
import com.eazypaytech.hardwarecore.data.model.CAPKey
import com.eazypaytech.hardwarecore.data.model.TransConfig

interface IEmvSdkRequestListener {
    var iEmvSdkResponseListener : IEmvSdkResponseListener

    fun initPaymentSDK(aidConfig : AidConfig?,capKeys: List<CAPKey>?)
    fun startPayment(context: Context, transConfig: TransConfig?)
    fun abortPayment()
    fun getEmvTag(tag : String?) : String?
    fun pinGeneration(pan: String?, amount: String, nResult: (pinBlock: ByteArray?) -> Unit)
    fun isCardExists(context: Context): Boolean
}