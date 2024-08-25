package com.analogics.tpaymentcore.handler
import android.content.Context
import com.analogics.tpaymentcore.Printer.Printer
import com.analogics.tpaymentcore.listener.IPrinterHandlerListener
import com.analogics.tpaymentcore.listener.PrinterListener

object PrinterHandler : PrinterListener {

    override fun initPrinter(
        context: Context,
        IPrinterHandlerListener: IPrinterHandlerListener
    ) {
        try {
            // Call the initPrint function on the Printer instance
            Printer.getInstance().initPrint(context)

            // Notify success
            IPrinterHandlerListener.onPrinterRespHandler("SUCCESS")
        } catch (exception: Exception) {
            // Handle exceptions and notify failure
            IPrinterHandlerListener.onPrinterRespHandler("FAILURE")
        }
    }
}