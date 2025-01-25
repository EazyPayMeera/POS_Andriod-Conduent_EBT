package com.eazypaytech.tpaymentcore.repository

import android.content.Context
import com.eazypaytech.tpaymentcore.listener.requestListener.IPrinterSdkRequestListener
import com.eazypaytech.tpaymentcore.listener.responseListener.IPrinterSdkResponseListener
import com.eazypaytech.tpaymentcore.model.emv.PrinterSdkException
import javax.inject.Inject
import kotlin.toString

class PrinterSdkRequestRepository @Inject constructor(override var iPrinterSdkResponseListener: IPrinterSdkResponseListener) : IPrinterSdkRequestListener {
    private var printerWrapper = PrinterWrapperRepository(iPrinterSdkResponseListener)
    override fun initPrinter(
    context: Context
    ) {
        try {
            printerWrapper.initPrinter(context)
        } catch (exception: Exception) {
            iPrinterSdkResponseListener.onPrinterSdkResponse(PrinterSdkException(exception.message.toString()))
        }
    }
}