package com.analogics.tpaymentcore.Printer

interface PrinterListener {
    fun onPrintSuccess()
    fun onPrintError(error: String)
    fun onPrinterStatus(status: String)
}