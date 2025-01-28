package com.eazypaytech.tpaymentcore.listener.requestListener

import android.content.Context
import com.eazypaytech.tpaymentcore.listener.responseListener.IPrinterSdkResponseListener
import com.eazypaytech.tpaymentcore.repository.PrinterSdkRequestRepository.LineFormat

interface IPrinterSdkRequestListener {
    var iPrinterSdkResponseListener : IPrinterSdkResponseListener

    fun init(context: Context) : Int
    fun addText(text : String, format : LineFormat?=null)
    fun print() : Int
}