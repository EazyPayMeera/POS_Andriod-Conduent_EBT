package com.eazypaytech.tpaymentcore.repository

import android.content.Context
import com.eazypaytech.tpaymentcore.listener.responseListener.IPrinterSdkResponseListener
import com.eazypaytech.tpaymentcore.model.emv.PrinterSdkResult
import com.urovo.sdk.print.PrinterProviderImpl
import javax.inject.Inject

class PrinterWrapperRepository @Inject constructor(var iPrinterSdkResponseListener: IPrinterSdkResponseListener)
{
    fun print(context: Context)
    {
        try {
            when (PrinterProviderImpl.getInstance(context).initPrint()) {
                0 -> iPrinterSdkResponseListener.onPrinterSdkResponse(
                    PrinterSdkResult.Result(
                        PrinterSdkResult.Status.INIT_SUCCESS
                    )
                )
                else ->
                    iPrinterSdkResponseListener.onPrinterSdkResponse(
                        PrinterSdkResult.Result(
                            PrinterSdkResult.Status.INIT_FAILURE
                        )
                    )
            }
        } catch (exception: Exception) {
            iPrinterSdkResponseListener.onPrinterSdkResponse(
                PrinterSdkResult.Result(
                    PrinterSdkResult.Status.INIT_FAILURE
                )
            )
        }
    }
}