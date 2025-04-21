package com.eazypaytech.tpaymentcore.repository

import android.content.Context
import android.graphics.Paint
import android.util.Log
import android.view.Gravity
import com.eazypaytech.paymentservicecore.constants.EmvConstants
import com.eazypaytech.tpaymentcore.listener.responseListener.IPrinterSdkResponseListener
import com.eazypaytech.tpaymentcore.model.emv.PrinterSdkResult
import com.eazypaytech.tpaymentcore.repository.PrinterSdkRequestRepository.Align
import com.eazypaytech.tpaymentcore.repository.PrinterSdkRequestRepository.FontSize
import com.eazypaytech.tpaymentcore.repository.PrinterSdkRequestRepository.Style
import com.morefun.yapi.device.printer.FeedUnit
import com.morefun.yapi.device.printer.FontFamily
import com.morefun.yapi.device.printer.OnPrintListener
import com.morefun.yapi.device.printer.Printer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class PrinterWrapperRepository @Inject constructor(var iPrinterSdkResponseListener: IPrinterSdkResponseListener) {
    val TAG: String = "PRINTER_WRAPPER"
    lateinit var context: Context
    private var printer: Printer? = null
    private val printerPixelWidth = 376

    fun init(context: Context): Int {
        var printerInit = CountDownLatch(1);
        var printerStatus: Int? = -1
        try {
            /* Initialize Printer First */
            this.context = context
            CoroutineScope(Dispatchers.IO).launch {
                printer = EmvWrapperRepository.getDeviceService(context)?.printer
                printerStatus = printer?.initPrinter()
                printerInit.countDown()
            }
        } catch (exception: Exception) {
            printerInit.countDown()
            Log.e(TAG, exception.message.toString())
        }

        printerInit.await(5000, TimeUnit.MILLISECONDS)

        return if (printerStatus == 0) 0
        else {
            notifyInitStatus(printerStatus)
            -1
        }
    }

    private fun notifyInitStatus(status: Int?) {
        iPrinterSdkResponseListener.onPrinterSdkResponse(
            PrinterSdkResult.Result(
                when (status) {
                    EmvConstants.MF_PRINTER_RET_SUCCESS -> PrinterSdkResult.Status.INIT_SUCCESS
                    EmvConstants.MF_PRINTER_RET_BUSY -> PrinterSdkResult.Status.BUSY
                    EmvConstants.MF_PRINTER_RET_OUT_OF_PAPER -> PrinterSdkResult.Status.OUT_OF_PAPER
                    else -> PrinterSdkResult.Status.INIT_FAILURE
                }
            )
        )
    }

    private fun notifyPrintStatus(status: Int?) {
        iPrinterSdkResponseListener.onPrinterSdkResponse(
            PrinterSdkResult.Result(
                when (status) {
                    EmvConstants.MF_PRINTER_RET_SUCCESS -> PrinterSdkResult.Status.PRINT_SUCCESS
                    EmvConstants.MF_PRINTER_RET_BUSY -> PrinterSdkResult.Status.BUSY
                    EmvConstants.MF_PRINTER_RET_OUT_OF_PAPER -> PrinterSdkResult.Status.OUT_OF_PAPER
                    else -> PrinterSdkResult.Status.PRINT_FAILURE
                }
            )
        )
    }

    private fun toYSDKFont(format: Int) : Int
    {
        /* Font Size */
        return if (format.and(FontSize.LARGE.value) == FontSize.LARGE.value)
            FontFamily.BIG
        else if (format.and(FontSize.SMALL.value) == FontSize.SMALL.value)
            FontFamily.SMALL
        else
            FontFamily.MIDDLE
    }

    private fun toYSDKAlign(format: Int) : Int {
        /* Font Size */
        return if (format.and(Align.RIGHT.value) == Align.RIGHT.value)
            Gravity.RIGHT
        else if (format.and(Align.CENTER.value) == Align.CENTER.value)
            Gravity.CENTER
        else
            Gravity.LEFT
    }

    fun addText(col1: String? = null, col2: String? = null, col3: String? = null, format: Int) {
        try {
            if (col2 != null && col3 != null) {
                printer?.appendPrnStr(
                    makeLine(
                        format,
                        makeItem(format,col1),
                        makeItem(format,col2),
                        makeItem(format,col3),
                    ), toYSDKFont(format), format.and(Style.BOLD.value) == Style.BOLD.value
                )
            }
            else if(col2!=null) {
                printer?.appendPrnStr(
                    makeLine(
                        format,
                        makeItem(format, col1),
                        makeItem(format, col2),
                    ), toYSDKFont(format), format.and(Style.BOLD.value) == Style.BOLD.value
                )
            }
            else if(col1!=null) {
                printer?.appendPrnStr(
                    makeLine(
                        format,
                        makeItem(format, col1)
                    ), toYSDKFont(format), format.and(Style.BOLD.value) == Style.BOLD.value
                )
            }
        } catch (exception: Exception) {
            Log.e(TAG, exception.message.toString())
        }
    }

    fun feedLine(lines: Int? = 1) {
        try {
            printer?.feedPaper(lines?:1, FeedUnit.LINE)
        } catch (exception: Exception) {
            Log.e(TAG, exception.message.toString())
        }
    }

    fun print() {
        try {
            /* Notify that printing has started */
            if(printer?.status==0) {
                iPrinterSdkResponseListener.onPrinterSdkResponse(
                    PrinterSdkResult.Result(
                        PrinterSdkResult.Status.PRINTING
                    )
                )
            }
            /* Start Printing */
            feedLine(3)  /* To adjust the paper out after receipt print */
            printer?.startPrint(object : OnPrintListener.Stub() {
                override fun onPrintResult(p0: Int) {
                    Log.e(TAG, "Printer Result : $p0")
                    when(p0) {
                        EmvConstants.MF_PRINTER_RET_SUCCESS -> {
                            iPrinterSdkResponseListener.onPrinterSdkResponse(
                                PrinterSdkResult.Result(
                                    PrinterSdkResult.Status.PRINT_SUCCESS
                                )
                            )
                        }
                        else-> notifyPrintStatus(p0)
                    }
                }
            })
        } catch (exception: Exception) {
            iPrinterSdkResponseListener.onPrinterSdkResponse(
                PrinterSdkResult.Result(
                    PrinterSdkResult.Status.PRINT_FAILURE
                )
            )
            Log.e(TAG, exception.message.toString())
        }
    }

    private fun makeItem(format: Int, item: String?): TextItem {
        var textItem = TextItem(item ?: "")

        /* Font Size */
        textItem.setFont(toYSDKFont(format))

        /* Alignment */
        textItem.setPaddingAlign(toYSDKAlign(format))

        return textItem
    }

    private fun makeLine(format: Int, vararg textItems: TextItem): String {
        var totalPX = 0f
        val resultText = StringBuilder()

        /* Adjust alignment */
        if(textItems.size == 3)
        {
            textItems[0].setPaddingAlign(Gravity.LEFT)
            textItems[1].setPaddingAlign(Gravity.CENTER)
            textItems[2].setPaddingAlign(Gravity.RIGHT)
        }
        else if(textItems.size == 2)
        {
            textItems[0].setPaddingAlign(Gravity.LEFT)
            textItems[1].setPaddingAlign(Gravity.RIGHT)
        }
        else
        {
            textItems[0].setPaddingAlign(toYSDKAlign(format))
        }

        for (textItem in textItems) {
            val text = textItem.getText()
            val textPX = measureTextPX(text, textItem.font)
            totalPX += textPX

            when (textItem.getAlign()) {
                Gravity.RIGHT -> {
                    val fillSpaceNum = when (textItem.font) {
                        FontFamily.BIG -> ((printerPixelWidth - totalPX) / 8).toInt()
                        FontFamily.SMALL -> ((printerPixelWidth - totalPX) / 4).toInt()
                        else -> ((printerPixelWidth - totalPX) / 6).toInt()
                    }
                    textItem.setFillSpaceNum(fillSpaceNum)
                }
                Gravity.CENTER -> {
                    val fillSpaceNum = when (textItem.font) {
                        FontFamily.BIG -> ((printerPixelWidth - totalPX) / 8).toInt()
                        FontFamily.SMALL -> ((printerPixelWidth - totalPX) / 4).toInt()
                        else -> ((printerPixelWidth - totalPX) / 6).toInt()
                    }
                    textItem.setFillSpaceNum(fillSpaceNum/2)
                }
                Gravity.FILL_HORIZONTAL -> {
                    if (textItem.getPxSize() > textPX) {
                        val fillSpaceNum = when (textItem.font) {
                            FontFamily.BIG -> ((textItem.getPxSize() - textPX) / 8).toInt()
                            FontFamily.SMALL -> ((textItem.getPxSize() - textPX) / 4).toInt()
                            else -> ((textItem.getPxSize() - textPX) / 6).toInt()
                        }
                        textItem.setFillSpaceNum(fillSpaceNum)
                    }
                }
            }
            resultText.append(textItem.getText())
        }

        return resultText.toString()
    }

    private fun measureTextPX(text: String, font: Int): Float {
        val paint = Paint()
        paint.textSize = when (font) {
            FontFamily.BIG -> 32f
            FontFamily.SMALL -> 16f
            else -> 24f
        }
        return paint.measureText(text)
    }

    class TextItem(var originalText: String) {
        var font: Int = FontFamily.MIDDLE
            private set
        private var paddingAlign: Int = Gravity.LEFT
        private var fillSpaceNum: Int = 0
        private var pxSize: Float = 0f

        fun setFont(font: Int): TextItem {
            this.font = font
            return this
        }

        fun setPaddingAlign(align: Int): TextItem {
            this.paddingAlign = align
            return this
        }

        fun setFillSpaceNum(num: Int): TextItem {
            this.fillSpaceNum = num
            return this
        }

        fun getText(): String {
            if (originalText.isEmpty()) return ""
            return when (paddingAlign) {
                Gravity.RIGHT, Gravity.CENTER -> addPadding(originalText, true, ' ', fillSpaceNum)
                Gravity.FILL_HORIZONTAL -> addPadding(originalText, false, ' ', fillSpaceNum)
                else -> originalText
            }
        }

        fun setText(text: String) {
            this.originalText = text
        }

        fun getAlign(): Int = paddingAlign

        fun getPxSize(): Float = pxSize

        fun setPxSize(pxSize: Float): TextItem {
            this.pxSize = pxSize
            return this
        }

        private fun addPadding(src: String, isLeft: Boolean, padding: Char, fixLen: Int): String {
            val paddingStr = padding.toString().repeat(fixLen)
            return if (isLeft) paddingStr + src else src + paddingStr
        }
    }
}

