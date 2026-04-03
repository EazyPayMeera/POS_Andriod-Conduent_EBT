package com.eazypaytech.tpaymentcore.repository

import android.content.Context
import android.graphics.Paint
import android.util.Log
import android.view.Gravity
import com.eazypaytech.paymentservicecore.constants.EmvConstants
import com.eazypaytech.hardwarecore.domain.listener.responseListener.IPrinterSdkResponseListener
import com.eazypaytech.hardwarecore.data.model.PrinterSdkResult
import com.eazypaytech.hardwarecore.domain.repository.PrinterSdkRequestRepository.Align
import com.eazypaytech.hardwarecore.domain.repository.PrinterSdkRequestRepository.FontSize
import com.eazypaytech.hardwarecore.domain.repository.PrinterSdkRequestRepository.Style
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
    private val printerPixelWidth = 384

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

    private fun toYSDKFont(format: Int): Int {
        /* Font Size */
        return if (format.and(FontSize.LARGE.value) == FontSize.LARGE.value)
            FontFamily.BIG
        else if (format.and(FontSize.SMALL.value) == FontSize.SMALL.value)
            FontFamily.SMALL
        else
            FontFamily.MIDDLE
    }

    private fun toYSDKAlign(format: Int): Int {
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
                        format = format,
                        columnPercents = listOf(0.4f, 0.2f, 0.4f),
                        maxLinesPerColumn = 2,
                        addEllipsis = true,
                        makeItem(format, col1),
                        makeItem(format, col2),
                        makeItem(format, col3),
                    ), toYSDKFont(format), format.and(Style.BOLD.value) == Style.BOLD.value
                )
            } else if (col2 != null) {
                printer?.appendPrnStr(
                    makeLine(
                        format,
                        columnPercents = listOf(0.45f, 0.55f),
                        maxLinesPerColumn = 2,
                        addEllipsis = true,
                        makeItem(format, col1),
                        makeItem(format, col2),
                    ), toYSDKFont(format), format.and(Style.BOLD.value) == Style.BOLD.value
                )
            } else if (col1 != null) {
                printer?.appendPrnStr(
                    makeLine(
                        format,
                        columnPercents = listOf(1.0f),
                        maxLinesPerColumn = 2,
                        addEllipsis = true,
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
            printer?.feedPaper(lines ?: 1, FeedUnit.LINE)
        } catch (exception: Exception) {
            Log.e(TAG, exception.message.toString())
        }
    }

    fun print() {
        try {
            /* Notify that printing has started */
            if (printer?.status == 0) {
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
                    when (p0) {
                        EmvConstants.MF_PRINTER_RET_SUCCESS -> {
                            iPrinterSdkResponseListener.onPrinterSdkResponse(
                                PrinterSdkResult.Result(
                                    PrinterSdkResult.Status.PRINT_SUCCESS
                                )
                            )
                        }

                        else -> notifyPrintStatus(p0)
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

    private fun makeLine(
        format: Int,
        columnPercents: List<Float>,
        maxLinesPerColumn: Int = 2,
        addEllipsis: Boolean = true,
        vararg textItems: TextItem
    ): String {

        val columnWidthsPX = columnPercents.map { it * printerPixelWidth }
        val wrappedColumns = textItems.mapIndexed { index, item ->
            wrapText(
                item.originalText,
                item.font,
                columnWidthsPX[index],
                maxLinesPerColumn,
                addEllipsis
            )
        }

        if (textItems.size == 3) {
            textItems[0].setPaddingAlign(Gravity.LEFT)
            textItems[1].setPaddingAlign(Gravity.CENTER)
            textItems[2].setPaddingAlign(Gravity.RIGHT)
        } else if (textItems.size == 2) {
            textItems[0].setPaddingAlign(Gravity.LEFT)
            textItems[1].setPaddingAlign(Gravity.RIGHT)
        } else {
            textItems[0].setPaddingAlign(toYSDKAlign(format))
        }

        val maxLines = wrappedColumns.maxOfOrNull { it.size } ?: 0
        val resultLines = mutableListOf<String>()

        for (lineIndex in 0 until maxLines) {
            val lineBuilder = StringBuilder()

            for (i in textItems.indices) {
                val item = textItems[i]
                val colWidthPX = columnWidthsPX[i]
                val text = wrappedColumns[i].getOrNull(lineIndex) ?: ""
                val textPX = measureTextPX(text, item.font)

                val spaceLeft = colWidthPX - textPX
                val spaceChars = when (item.font) {
                    FontFamily.BIG -> (spaceLeft / 8).toInt()
                    FontFamily.SMALL -> (spaceLeft / 4).toInt()
                    else -> (spaceLeft / 6).toInt()
                }.coerceAtLeast(0)

                val alignedText = when (item.getAlign()) {
                    Gravity.CENTER -> item.copy(originalText = text).addBothSidePadding(spaceChars)
                    Gravity.RIGHT -> item.copy(originalText = text).addLeftPadding(spaceChars)
                    else -> item.copy(originalText = text).addRightPadding(spaceChars)
                }

                // Ensure column stays in place by padding to full column width
                val fixedColumnChars = getSpaceWidth(item.font).toInt()
                val paddedFixedWidth = alignedText.padEnd(fixedColumnChars, ' ')
                lineBuilder.append(paddedFixedWidth)
            }

            resultLines.add(lineBuilder.toString())
        }

        return resultLines.joinToString("\n")
    }

    private fun getSpaceWidth(font: Int): Float {
        val paint = Paint()
        paint.textSize = when (font) {
            FontFamily.BIG -> 32f
            FontFamily.SMALL -> 16f
            else -> 24f
        }
        return paint.measureText(" ")  // Measure width of space character dynamically
    }

    private fun wrapText(
        text: String,
        font: Int,
        maxWidth: Float,
        maxLines: Int? = null,
        addEllipsis: Boolean = false
    ): List<String> {
        val paint = Paint().apply {
            textSize = when (font) {
                FontFamily.BIG -> 32f
                FontFamily.SMALL -> 16f
                else -> 24f
            }
        }

        val lines = mutableListOf<String>()
        var currentLine = ""

        fun finalizeLine() {
            if (currentLine.isNotEmpty()) {
                lines.add(currentLine)
                currentLine = ""
            }
        }

        val words = text.split(" ")
        for (word in words) {
            val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
            if (paint.measureText(testLine) <= maxWidth) {
                currentLine = testLine
            } else {
                if (paint.measureText(word) > maxWidth) {
                    // Break word by characters
                    finalizeLine()
                    var part = ""
                    for (char in word) {
                        val tryPart = part + char
                        if (paint.measureText(tryPart) > maxWidth) {
                            lines.add(part)
                            part = "$char"
                            if (maxLines != null && lines.size >= maxLines) break
                        } else {
                            part = tryPart
                        }
                    }
                    if (part.isNotEmpty() && (maxLines == null || lines.size < maxLines)) {
                        lines.add(part)
                    }
                } else {
                    finalizeLine()
                    currentLine = word
                }

                if (maxLines != null && lines.size >= maxLines) break
            }
        }

        finalizeLine()

        if (maxLines != null && lines.size > maxLines) {
            val truncated = lines.take(maxLines).toMutableList()
            if (addEllipsis && truncated.isNotEmpty()) {
                val last = truncated.last()
                val shortLast = last.dropLast(3).takeIf { it.length >= 3 } ?: last
                truncated[truncated.lastIndex] = "$shortLast..."
            }
            return truncated
        }

        return lines
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

        fun setFont(font: Int): TextItem {
            this.font = font
            return this
        }

        fun setPaddingAlign(align: Int): TextItem {
            this.paddingAlign = align
            return this
        }

        fun getAlign(): Int = paddingAlign

        // Padding helpers
        fun addLeftPadding(numSpaces: Int): String {
            return " ".repeat(numSpaces) + originalText
        }

        fun addRightPadding(numSpaces: Int): String {
            return originalText + " ".repeat(numSpaces)
        }

        fun addBothSidePadding(totalSpaces: Int): String {
            val half = totalSpaces / 2
            val left = " ".repeat(half)
            val right = " ".repeat(totalSpaces - half)
            return "$left$originalText$right"
        }

        fun copy(originalText: String = this.originalText): TextItem {
            return TextItem(originalText).setFont(font).setPaddingAlign(paddingAlign)
        }

    }
}

