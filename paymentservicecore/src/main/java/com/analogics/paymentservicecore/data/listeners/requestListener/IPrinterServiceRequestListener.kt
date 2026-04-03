package com.analogics.paymentservicecore.data.listeners.requestListener

import android.content.Context
import com.analogics.paymentservicecore.data.listeners.responseListener.IPrinterServiceResponseListener
import com.analogics.paymentservicecore.domain.repository.printerService.PrinterServiceRepository
import com.analogics.paymentservicecore.domain.repository.printerService.PrinterServiceRepository.PrintFormat

interface IPrinterServiceRequestListener {
    fun init(context: Context, iPrinterServiceResponseListener: IPrinterServiceResponseListener) : PrinterServiceRepository
    fun addText(col1: String?=null, col2: String?=null, col3: String?=null, format: PrintFormat?=null, condition: Boolean?=true): PrinterServiceRepository
    fun feedLine(lines : Int?=1, condition: Boolean?=true) : PrinterServiceRepository
    fun print(): PrinterServiceRepository
}