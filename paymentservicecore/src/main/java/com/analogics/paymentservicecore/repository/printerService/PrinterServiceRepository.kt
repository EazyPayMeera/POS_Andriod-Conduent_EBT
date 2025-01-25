package com.eazypaytech.posafrica.rootUtils.genericComposeUI

import android.content.Context
import com.eazypaytech.paymentservicecore.listeners.requestListener.IPrinterServiceRequestListener
import com.eazypaytech.paymentservicecore.listeners.responseListener.IPrinterServiceResponseListener
import com.eazypaytech.tpaymentcore.listener.responseListener.IPrinterSdkResponseListener
import com.eazypaytech.tpaymentcore.model.emv.PrinterSdkResult
import com.eazypaytech.tpaymentcore.repository.PrinterSdkRequestRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

class PrinterServiceRepository @Inject constructor() : IPrinterServiceRequestListener,
    IPrinterSdkResponseListener {
    private val printerSdkRequestRepository = PrinterSdkRequestRepository(this)
    var iPrinterServiceResponseListener: IPrinterServiceResponseListener?=null
    var context: Context?=null
    var job: Job?=null

    override fun initPrinter(
        context: Context,
        iPrinterServiceResponseListener: IPrinterServiceResponseListener
    ) {
        this.context = context
        this.iPrinterServiceResponseListener = iPrinterServiceResponseListener
        job?.cancel()
        job = CoroutineScope(Dispatchers.Default).launch {
            printerSdkRequestRepository.initPrinter(context)
        }
    }

    override fun onPrinterSdkResponse(response: Any) {
        iPrinterServiceResponseListener?.onPrinterServiceResponse(response)
    }

    override fun onPrinterDisplayMessage(printerMsgId: PrinterSdkResult.PrinterMsgId) {
        iPrinterServiceResponseListener?.onPrinterServiceDisplayMessage(printerMsgId)
    }
}
