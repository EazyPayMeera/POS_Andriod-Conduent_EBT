package com.eazypaytech.tpaymentcore.listener.requestListener

import android.content.Context
import com.eazypaytech.tpaymentcore.listener.responseListener.IPrinterSdkResponseListener

interface IPrinterSdkRequestListener {
    var iPrinterSdkResponseListener : IPrinterSdkResponseListener

    fun init(context: Context) : Int
    fun addText(text : String, format : Int?=null)
    fun print() : Int
}