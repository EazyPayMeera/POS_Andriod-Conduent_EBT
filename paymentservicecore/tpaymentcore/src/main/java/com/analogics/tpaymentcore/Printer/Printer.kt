package com.analogics.tpaymentcore.Printer


import android.content.Context
import android.device.PrinterManager
import android.os.Bundle
import android.util.Log
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

    // To Initialize the Printer
    fun initPrint(context: Context) {

        logfile.printLog("===initPrint in Printer kt")

        // Use the provided context instance instead of `Context` type
        if (this.mPrinter == null) {
            // Ensure PrinterProviderImpl.getInstance returns a PrinterManager or compatible type
            this.mPrinter = PrinterProviderImpl.getInstance(context)
        }
        mPrinter?.initPrint()

    }

    // Get Printer Status
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

    // To Add Image In Receipt
    fun addImage(format: Bundle,imageData: ByteArray)
    {
        mPrinter?.addImage(format,imageData)
    }


    // To add text
    fun addText(text: String,alignment: Int)
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
            putInt("align", alignment)
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

    // Add Text on Left
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

    // Add Text on Left and Right
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

    // Add Text On Left Center And Right Align
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

    // To add Barcode
    fun barCodePrinting(format: Bundle,barcode:String)
    {
        mPrinter?.addBarCode(format,barcode)
    }

    // Feed Line to Receipt
    fun feedLine(lines:Int)
    {
        mPrinter?.feedLine(lines)
    }

    // Add Qr Code in Receipt
    fun qrCodePrinting(format: Bundle,barcode:String)
    {
        mPrinter?.addQrCode(format,barcode)
    }

    // Start Print API
    fun startPrinting()
    {
        mPrinter?.startPrint()
    }

    fun printMultipleTextsAndStartPrinting(format: Bundle, barcode: String, texts: List<String>, alignments: List<Int>) {
        try {
            // Ensure that texts and alignment lists are of the same size
            if (texts.size != alignments.size) {
                throw IllegalArgumentException("Texts and alignment lists must be of the same size")
            }

            // Add each text to the printer with its corresponding alignment
            for (i in texts.indices) {
                val text = texts[i]
                val alignment = alignments[i]

                // Add the text to the printer with alignment
                addText(text, alignment)
            }

            // Print the QR code
            qrCodePrinting(format, barcode)

            // Feed 3 lines after printing
            feedLine(3)

            // Start the printing process
            startPrinting()

        } catch (e: Exception) {
            // Handle the exception
            Log.e(TAG, "Error in printMultipleTextsAndStartPrinting: ${e.message}")
        }
    }


}