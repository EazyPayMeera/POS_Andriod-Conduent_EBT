package com.eazypaytech.posafrica.rootUtils.genericComposeUI

import android.content.Context
import com.eazypaytech.paymentservicecore.listeners.requestListener.IPrinterServiceRequestListener
import com.eazypaytech.paymentservicecore.listeners.responseListener.IPrinterServiceResponseListener
import com.eazypaytech.paymentservicecore.model.emv.PrinterServiceResult.Result
import com.eazypaytech.paymentservicecore.model.emv.PrinterServiceResult.Status
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

    fun sdkToPrinterStatus(value: PrinterSdkResult.Status) : Status {
        return when (value) {
            PrinterSdkResult.Status.INIT_SUCCESS -> Status.INIT_SUCCESS
            PrinterSdkResult.Status.INIT_FAILURE -> Status.INIT_FAILURE
            PrinterSdkResult.Status.PRINT_SUCCESS -> Status.PRINT_SUCCESS
            PrinterSdkResult.Status.PRINT_FAILURE -> Status.PRINT_FAILURE
            else -> Status.PRINT_FAILURE
        }
    }

    fun sdkToPrinterService(response: Any) : Any
    {
        return when(response) {
            is PrinterSdkResult.Result -> {
                Result(
                    status = sdkToPrinterStatus(response.status as PrinterSdkResult.Status)
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
}
