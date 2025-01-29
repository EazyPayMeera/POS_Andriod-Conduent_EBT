package com.eazypaytech.paymentservicecore.listeners.requestListener

import android.content.Context
import android.service.notification.Condition
import com.eazypaytech.paymentservicecore.listeners.responseListener.IPrinterServiceResponseListener
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.PrinterServiceRepository
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.PrinterServiceRepository.PrintFormat

interface IPrinterServiceRequestListener {
    fun init(context: Context, iPrinterServiceResponseListener: IPrinterServiceResponseListener) : PrinterServiceRepository
    fun addText(col1: String?=null, col2: String?=null, col3: String?=null, format: PrintFormat?=null, condition: Boolean?=true): PrinterServiceRepository
    fun feedLine(lines : Int?=1, condition: Boolean?=true) : PrinterServiceRepository
    fun print(): PrinterServiceRepository
}