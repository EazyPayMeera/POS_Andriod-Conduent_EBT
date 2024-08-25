package com.analogics.tpaymentcore.Printer


import android.content.Context
import android.device.PrinterManager
import android.os.Bundle
import com.urovo.file.logfile
import com.urovo.sdk.print.PrinterProviderImpl
import com.urovo.sdk.print.PrinterProviderImpl.TAG
import com.urovo.sdk.print.PrinterProviderImpl.fontName_default


class Printer constructor() {

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

    fun addImage(format: Bundle,imageData: ByteArray)
    {
        mPrinter?.addImage(format,imageData)
    }



    fun addText(text: String)
    {
        // Define default formatting options
        val defaultFont = 1
        val defaultAlign = 0
        val defaultFontName = ""
        val defaultFontBold = true
        val defaultNewline = true
        val defaultLineHeight = 0
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


        } catch (e: Exception) {

        }
    }

    // Overloaded method with String only
    fun addTextOnlyLeft(text: String) {

        // Create a Bundle with default values
        val format = Bundle().apply {
            putInt("font", 1)
            putString("fontName", "Arial")
            putBoolean("fontBold", false)
            putInt("lineHeight", 5)
            putInt("fontSize", 24)
        }

        mPrinter?.addTextOnlyLeft(format,text)
    }

    fun addTextLeft_Right(textLeft: String?, textRight: String?) {
        // Create a default Bundle with predefined formatting
        val defaultFormat = Bundle()
        defaultFormat.putInt("font", 1) // Default font type
        defaultFormat.putString("fontName", fontName_default) // Default font name
        defaultFormat.putBoolean("fontBold", false) // Default to non-bold font
        defaultFormat.putInt("lineHeight", 5) // Default line height


        // Call the existing method with the default formatting Bundle
        mPrinter?.addTextLeft_Right(defaultFormat, textLeft, textRight)
    }

    fun addTextLeft_Center_Right(textLeft: String?, textCenter: String?, textRight: String?) {
        // Call the original method with default formatting
        val defaultFormat = Bundle()
        defaultFormat.putInt("font", 1) // Default font type
        defaultFormat.putString("fontName", fontName_default) // Default font name
        defaultFormat.putBoolean("fontBold", false) // Default font bold setting
        defaultFormat.putInt("lineHeight", 5) // Default line height


        // Invoke the original method with default settings
        mPrinter?.addTextLeft_Center_Right(defaultFormat, textLeft, textCenter, textRight)
    }

    fun barCodePrinting(format: Bundle,barcode:String)
    {
        mPrinter?.addBarCode(format,barcode)
    }

    fun feedLine(lines:Int)
    {
        mPrinter?.feedLine(lines)
    }

    fun qrCodePrinting(format: Bundle,barcode:String)
    {
        mPrinter?.addQrCode(format,barcode)
    }

    fun startPrinting()
    {
        mPrinter?.startPrint()
    }

    fun printMultipleTextsAndStartPrinting(texts: List<String>) {

        try {
            // Add each text to the printer
            for (text in texts) {
                addText(text)
            }

        } catch (e: Exception) {
            // Handle the exception (e.g., log it or show an error message)
            logfile.printLog("Error in printMultipleTextsAndStartPrinting: ${e.message}")
        }
    }

}