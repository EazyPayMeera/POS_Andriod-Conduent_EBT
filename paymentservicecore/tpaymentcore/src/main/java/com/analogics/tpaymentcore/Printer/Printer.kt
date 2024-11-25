package com.analogics.tpaymentcore.Printer


import android.content.Context
import android.device.PrinterManager
import android.os.Bundle
import android.util.Log
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
        if (this.mPrinter == null) {
            // Ensure PrinterProviderImpl.getInstance returns a PrinterManager or compatible type
            this.mPrinter = PrinterProviderImpl.getInstance(context)
        }
        mPrinter?.initPrint()

    }

    // Get Printer Status
    fun getPrinterStatus(): Int {
        var ret = -1

        if (this.mPrinterMan == null) {
            // Initialize mPrinterMan if it's null
            this.mPrinterMan = PrinterManager() // Replace with actual initialization if needed
        }

        ret = mPrinterMan?.getStatus() ?: -1 // Safely call getStatus and provide a default value of -1
        return ret

    }

    // To Add Image In Receipt
    fun addImage(format: Bundle,imageData: ByteArray)
    {
        mPrinter?.addImage(format,imageData)
    }


    // To add text
    fun addText(text: String,alignment: Int,fontSize:Int)
    {
        val defaultFont = 1
        val defaultFontName = ""
        val defaultFontBold = true
        val defaultNewline = true
        val defaultLineHeight = 0
        val defaultFontSize = fontSize

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
            Log.e(TAG, "Failed to add text: '$text'. Error: ${e.message}", e)
        }
    }

    // Add Text on Left
    // Overloaded method with String only
    fun addTextOnlyLeft(text: String) {
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
    fun addTextLeft_Right(textLeft: String?, textRight: String?, fontSize: Int) {
        val defaultFormat = Bundle()
        defaultFormat.putInt("font", 1) // Default font type
        defaultFormat.putString("fontName", fontName_default) // Default font name
        defaultFormat.putBoolean("fontBold", false) // Default to non-bold font
        defaultFormat.putInt("lineHeight", 5) // Default line height
        defaultFormat.putInt("fontSize", fontSize)
        mPrinter?.addTextLeft_Right(defaultFormat, textLeft, textRight)
    }

    fun addRightLeftDetails(label: List<String>,description: List<String>,fontsize: List<Int>)
    {
        for (i in label.indices) {
            val leftText = label[i]
            val rightText = description[i]
            val fontSize = fontsize[i]
            addTextLeft_Right(leftText, rightText,fontSize)
        }
    }

    // Add Text On Left Center And Right Align
    fun addTextLeft_Center_Right(
        textLeft: String?, textCenter: String?, textRight: String?,
        fontSize: Int
    ) {
        val defaultFormat = Bundle()
        defaultFormat.putInt("font", 1) // Default font type
        defaultFormat.putString("fontName", fontName_default) // Default font name
        defaultFormat.putBoolean("fontBold", false) // Default font bold setting
        defaultFormat.putInt("lineHeight", 5) // Default line height
        defaultFormat.putInt("fontSize", fontSize)
        mPrinter?.addTextLeft_Center_Right(defaultFormat, textLeft, textCenter, textRight)

    }

    fun getStatusOfPrinter()
    {
        mPrinter?.status
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

    fun stopPrinting()
    {
        mPrinter?.close()
    }

    fun printMultipleTextsAndStartPrinting(format: Bundle, barcode: String, texts: List<String>,descriptions: List<String>, alignments: List<Int>,fontsize: List<Int>) {
        try {
            for (i in texts.indices) {
                val text = texts[i]
                val alignment = alignments[i]
                val description = descriptions[i]
                val fontsize = fontsize[i]
                if(alignment == -1) {
                    addTextLeft_Right(text,description,fontsize)
                }
                else
                {
                    addText(text, alignment,fontsize)
                }
            }
            qrCodePrinting(format, barcode)
            feedLine(1)
            startPrinting()

        } catch (e: Exception) {
            // Handle the exception
            Log.e(TAG, "Error in printMultipleTextsAndStartPrinting: ${e.message}")
        }
    }

    fun printLeftCenterRightPrinting(
        Trasaction: List<String>,
        Count: List<String>, Total: List<String>,
        fontsize: List<Int>
    ) {
        try {

            for (i in Trasaction.indices) {
                val trasaction = Trasaction[i]
                val count = Count[i]
                val total = Total[i]
                val FontSize = fontsize[i]
                if(FontSize == 32)
                {
                    addText(count,1,FontSize)
                }
                else
                {
                    addTextLeft_Center_Right(trasaction, count, total, FontSize)
                }
            }
            feedLine(3)
            startPrinting()

        } catch (e: Exception) {
            Log.e(TAG, "Error in printMultipleTextsAndStartPrinting: ${e.message}")
        }
    }


}