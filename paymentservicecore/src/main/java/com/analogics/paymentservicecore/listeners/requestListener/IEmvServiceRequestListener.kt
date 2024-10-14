package com.analogics.paymentservicecore.listeners.requestListener

import android.content.Context
import com.analogics.paymentservicecore.listeners.responseListener.IEmvServiceResponseListener
import com.analogics.paymentservicecore.model.emv.CAPKey
import java.io.File
import java.nio.file.Files

interface IEmvServiceRequestListener {
    fun initPaymentSDK(capKeys: String?=null, iEmvServiceResponseListener: IEmvServiceResponseListener)
    fun initPaymentSDK(capKeys: List<CAPKey>, iEmvServiceResponseListener: IEmvServiceResponseListener)
    fun startPayment(
        context: Context,
        iEmvServiceResponseListener: IEmvServiceResponseListener
    )
}