package com.eazypaytech.posafrica.rootUtils.miscellaneous

import android.content.Context
import com.eazypaytech.paymentservicecore.listeners.responseListener.IPrinterServiceResponseListener
import com.eazypaytech.paymentservicecore.model.emv.PrinterServiceResult
import com.eazypaytech.posafrica.rootModel.ObjRootAppPaymentDetails
import com.eazypaytech.posafrica.rootUiScreens.dialogs.CustomDialogBuilder
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.PrinterServiceRepository

object PrinterUtils {

    fun printReceipt(context: Context, objRootAppPaymentDetails: ObjRootAppPaymentDetails, isCustomer : Boolean = true)
    {
        PrinterServiceRepository().init(context, object : IPrinterServiceResponseListener {
              override fun onPrinterServiceResponse(response: Any) {
                  when (response) {
                      is PrinterServiceResult.Result ->{
                          when(response.status)
                          {
                              PrinterServiceResult.Status.PRINTING-> CustomDialogBuilder.composePrintingDialog()
                              else->CustomDialogBuilder.hideProgress()
                          }
                      }
                  }
              }
          })
            .addText("Hello World!!")
            .addText("Hello World!!")
            .addText("Hello World!!")
            .addText("Hello World!!")
            .addText("Hello World!!")
            .addText("Hello World!!")
            .addText("Hello World!!")
            .feedLine(5)
            .print()
    }
}