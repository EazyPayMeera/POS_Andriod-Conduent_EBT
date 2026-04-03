package com.eazypaytech.tpaymentcore.listener.requestListener

import android.content.Context
import com.eazypaytech.tpaymentcore.listener.responseListener.IPrinterSdkResponseListener

interface IPrinterSdkRequestListener {
    var iPrinterSdkResponseListener : IPrinterSdkResponseListener

    fun init(context: Context) : Int
    fun addText(col1 : String?=null, col2 : String?=null, col3 : String?=null, format : Int?=0x00000000)
    fun feedLine(lines : Int?=1)
    fun print() : Int
}