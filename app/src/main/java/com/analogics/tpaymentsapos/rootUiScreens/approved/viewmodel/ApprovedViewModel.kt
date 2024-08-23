package com.analogics.tpaymentsapos.rootUiScreens.approved.viewmodel

import android.content.Context
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.analogics.tpaymentcore.Printer.Printer
import com.analogics.tpaymentcore.Printer.PrinterListener

class ApprovedViewModel(context: Context): ViewModel(), PrinterListener {

    private val _printStatus = mutableStateOf("")
    val printStatus: MutableState<String> = _printStatus

    private val _errorMessage = mutableStateOf("")
    val errorMessage: MutableState<String> = _errorMessage

    private val printer = Printer.getInstance()


    fun initPrint(context: Context) {
        printer.initPrint(context) // Pass the context instance directly
    }

    fun addTextDetails(text: String) {
        printer.addText(text)
    }

    fun printReceipt(context: Context)
    {
        printer.startPrinting()
    }

 override fun onPrintSuccess() {
        _printStatus.value = "Print successful"
    }

    override fun onPrintError(error: String) {
        _errorMessage.value = "Print error: $error"
    }

    override fun onPrinterStatus(status: String) {
        _printStatus.value = status
    }


    fun GetStatus() {
        val status = printer.getPrinterStatus()
        _printStatus.value = when (status) {
            0 -> "Printer is OK"
            240 -> "Error: Status 240"
            243 -> "Error: Status 243"
            225 -> "Error: Status 225"
            247 -> "Error: Status 247"
            251 -> "Error: Status 251"
            242 -> "Error: Status 242"
            else -> "Unknown status: $status"
        }
    }
}
