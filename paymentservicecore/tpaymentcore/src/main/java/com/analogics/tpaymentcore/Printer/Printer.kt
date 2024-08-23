package com.analogics.tpaymentcore.Printer


import android.content.Context
import android.device.PrinterManager
import android.graphics.Paint
import android.os.Bundle
import com.urovo.file.logfile
import com.urovo.sdk.print.PrinterProviderImpl
import com.urovo.sdk.print.PrinterProviderImpl.TAG


class Printer private constructor() {

    companion object {
        @Volatile
        private var INSTANCE: Printer? = null

        fun getInstance(): Printer =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Printer().also { INSTANCE = it }
            }
    }


    private var mPrinter: PrinterProviderImpl? = null
    private var mPrinterMan:PrinterManager? = null


    private var listener: PrinterListener? = null

    fun setListener(listener: PrinterListener) {
        this.listener = listener
    }

    fun initialize() {
        // Initialization logic
    }

    fun initPrint(context: Context) {

        logfile.printLog("===initPrint")

        // Use the provided context instance instead of `Context` type
        if (this.mPrinter == null) {
            // Ensure PrinterProviderImpl.getInstance returns a PrinterManager or compatible type
            this.mPrinter = PrinterProviderImpl.getInstance(context)
        }
        mPrinter?.initPrint()

    }

    fun getPrinterStatus(): Int {
        logfile.printLog(TAG + "===getStatus")
        var ret = -1

        if (this.mPrinterMan == null) {
            // Initialize mPrinterMan if it's null
            this.mPrinterMan = PrinterManager() // Replace with actual initialization if needed
        }

        ret = mPrinterMan?.getStatus() ?: -1 // Safely call getStatus and provide a default value of -1

        // Return the status
        return ret

    }

    fun addText(text: String) {
        // Define default formatting options
        val defaultFont = 1
        val defaultAlign = 0
        val defaultFontName = ""
        val defaultFontBold = false
        val defaultNewline = true
        val defaultLineHeight = 5
        val defaultFontSize = 24

        // Create a Bundle with default values
        val format = Bundle().apply {
            putInt("font", defaultFont)
            putInt("align", defaultAlign)
            putString("fontName", defaultFontName)
            putBoolean("fontBold", defaultFontBold)
            putBoolean("newline", defaultNewline)
            putInt("lineHeight", defaultLineHeight)
            putInt("fontSize", defaultFontSize)
        }

        try {
            mPrinter?.addText(format, text)

            listener?.onPrintSuccess()
        } catch (e: Exception) {
            listener?.onPrintError("Error: ${e.message}")
        }
    }

    fun startPrinting()
    {
        mPrinter?.startPrint()
    }
}