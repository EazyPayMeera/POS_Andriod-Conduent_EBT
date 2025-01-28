package com.eazypaytech.paymentservicecore.listeners.requestListener

import android.content.Context
import com.eazypaytech.paymentservicecore.listeners.responseListener.IPrinterServiceResponseListener
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.PrinterServiceRepository
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.PrinterServiceRepository.LineFormat

interface IPrinterServiceRequestListener {
    fun init(context: Context, iPrinterServiceResponseListener: IPrinterServiceResponseListener) : PrinterServiceRepository
    fun addText(text: String, format: LineFormat?=null): PrinterServiceRepository
    fun print(): PrinterServiceRepository
}