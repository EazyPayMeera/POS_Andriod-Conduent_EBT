package com.eazypaytech.posafrica.rootUtils.genericComposeUI

import android.content.Context
import com.eazypaytech.paymentservicecore.listeners.requestListener.IPrinterServiceRequestListener
import com.eazypaytech.paymentservicecore.listeners.responseListener.IPrinterServiceResponseListener
import com.eazypaytech.paymentservicecore.model.emv.PrinterServiceResult.InitResult
import com.eazypaytech.paymentservicecore.model.emv.PrinterServiceResult.InitStatus
import com.eazypaytech.paymentservicecore.model.error.PrinterServiceException
import com.eazypaytech.tpaymentcore.listener.responseListener.IPrinterSdkResponseListener
import com.eazypaytech.tpaymentcore.model.emv.PrinterSdkException
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

    override fun print(
        context: Context,
        iPrinterServiceResponseListener: IPrinterServiceResponseListener
    ) {
        this.context = context
        this.iPrinterServiceResponseListener = iPrinterServiceResponseListener
        job?.cancel()
        job = CoroutineScope(Dispatchers.Default).launch {
            printerSdkRequestRepository.print(context)
        }
    }

    fun sdkToPrinterInitStatus(value: PrinterSdkResult.InitStatus) : InitStatus {
        return when (value) {
            PrinterSdkResult.InitStatus.SUCCESS -> InitStatus.SUCCESS
            else -> InitStatus.FAILURE
        }
    }

    fun sdkToPrinterService(response: Any) : Any
    {
        return when(response) {
            is PrinterSdkResult.InitResult -> {
                InitResult(
                    status = sdkToPrinterInitStatus(response.status as PrinterSdkResult.InitStatus)
                )
            }

            is PrinterSdkException ->{
                PrinterServiceException(errorMessage = response.errorMessage)
            }
            else -> {
                PrinterServiceException(errorMessage = "Unknown SDK Error")
            }
        }
    }

    override fun onPrinterSdkResponse(response: Any) {
        iPrinterServiceResponseListener?.onPrinterServiceResponse(sdkToPrinterService(response))
    }

    override fun onPrinterDisplayMessage(printerMsgId: PrinterSdkResult.PrinterMsgId) {
        iPrinterServiceResponseListener?.onPrinterServiceDisplayMessage(printerMsgId)
    }
}
