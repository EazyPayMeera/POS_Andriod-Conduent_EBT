package com.eazypaytech.paymentservicecore.listeners.responseListener

import com.eazypaytech.tpaymentcore.model.emv.PrinterSdkResult

interface IPrinterServiceResponseListener {
    fun onPrinterServiceResponse(response: Any)
}