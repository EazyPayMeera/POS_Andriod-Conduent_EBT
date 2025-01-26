package com.eazypaytech.paymentservicecore.listeners.requestListener

import android.content.Context
import com.eazypaytech.paymentservicecore.listeners.responseListener.IPrinterServiceResponseListener

interface IPrinterServiceRequestListener {
    fun print(context: Context, iPrinterServiceResponseListener: IPrinterServiceResponseListener)
}