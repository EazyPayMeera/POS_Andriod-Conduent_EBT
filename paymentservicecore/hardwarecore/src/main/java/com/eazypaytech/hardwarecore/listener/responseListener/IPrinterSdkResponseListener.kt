package com.eazypaytech.tpaymentcore.listener.responseListener

import com.eazypaytech.tpaymentcore.model.emv.PrinterSdkResult

interface IPrinterSdkResponseListener {
      fun onPrinterSdkResponse(response: Any)
      fun onPrinterDisplayMessage(displayMsgId: PrinterSdkResult.PrinterMsgId)
}