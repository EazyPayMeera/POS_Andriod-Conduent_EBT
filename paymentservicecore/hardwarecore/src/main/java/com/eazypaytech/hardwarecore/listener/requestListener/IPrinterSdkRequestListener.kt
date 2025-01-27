package com.eazypaytech.tpaymentcore.listener.requestListener

import android.content.Context
import com.eazypaytech.tpaymentcore.listener.responseListener.IPrinterSdkResponseListener
import com.eazypaytech.tpaymentcore.model.emv.AidConfig
import com.eazypaytech.tpaymentcore.model.emv.CAPKey
import com.eazypaytech.tpaymentcore.model.emv.TransConfig

interface IPrinterSdkRequestListener {
    var iPrinterSdkResponseListener : IPrinterSdkResponseListener

    fun init(context: Context) : Int
    fun print() : Int
}