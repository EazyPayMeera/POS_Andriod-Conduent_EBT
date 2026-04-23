package com.eazypaytech.hardwarecore.domain.repository

import android.content.Context
import com.eazypaytech.hardwarecore.domain.listener.requestListener.IPrinterSdkRequestListener
import com.eazypaytech.hardwarecore.domain.listener.responseListener.IPrinterSdkResponseListener
import com.eazypaytech.hardwarecore.data.printer.PrinterSdkException
import com.eazypaytech.tpaymentcore.repository.PrinterWrapperRepository
import javax.inject.Inject
import kotlin.toString

class PrinterSdkRequestRepository @Inject constructor(override var iPrinterSdkResponseListener: IPrinterSdkResponseListener) : IPrinterSdkRequestListener {
    private var printerWrapper = PrinterWrapperRepository(iPrinterSdkResponseListener)

    /**
     * Font size bitmask flags used by the printer engine.
     * These values are designed to be combined using bitwise OR operations.
     */
    enum class FontSize(val value: Int) {
        EXTRA_SMALL(0x00000001),
        SMALL(0x00000002),
        MEDIUM(0x00000004),
        LARGE(0x00000008),
        EXTRA_LARGE(0x00000010);
    }

    /**
     * Text alignment options for printed content.
     */
    enum class Align(val value: Int) {
        LEFT(0x00000100),
        CENTER(0x00000200),
        RIGHT(0x00000400);
    }

    /**
     * Line spacing options for printed text.
     */
    enum class LineSpacing(val value: Int) {
        EXTRA_SMALL(0x00001000),
        SMALL(0x00002000),
        MEDIUM(0x00004000),
        LARGE(0x00008000),
        EXTRA_LARGE(0x00010000);
    }

    /**
     * Text style options such as bold and line break behavior.
     */
    enum class Style(val value: Int) {
        BOLD(0x00100000),
        NO_LINE_BREAK(0x00200000);
    }

    /**
     * Supported font families for printing.
     */
    enum class FontName(val value: Int) {
        SIMSUN(0x01000000);
    }

    /**
     * Initializes the printer SDK with required Android context.
     *
     * @param context Application or activity context
     * @return 0 if success, -1 if failure
     */
    override fun init(
        context: Context
    ) : Int {
        try {
            return printerWrapper.init(context)
        } catch (exception: Exception) {
            iPrinterSdkResponseListener.onPrinterSdkResponse(PrinterSdkException(exception.message.toString()))
        }
        return -1
    }

    /**
     * Adds a text row to the printer buffer.
     *
     * @param col1 First column text (can be null)
     * @param col2 Second column text (can be null)
     * @param col3 Third column text (can be null)
     * @param format Bitmask format combining FontSize, Align, Style, etc.
     */
    override fun addText(col1: String?, col2: String?, col3: String?, format: Int?)
    {
        try {
            printerWrapper.addText(col1, col2, col3, format?: 0x00000000)
        } catch (exception: Exception) {
            iPrinterSdkResponseListener.onPrinterSdkResponse(PrinterSdkException(exception.message.toString()))
        }
    }

    /**
     * Feeds blank lines (line spacing) to the printer output.
     *
     * @param lines Number of empty lines to feed
     */
    override fun feedLine(lines: Int?)
    {
        try {
            printerWrapper.feedLine(lines)
        } catch (exception: Exception) {
            iPrinterSdkResponseListener.onPrinterSdkResponse(PrinterSdkException(exception.message.toString()))
        }
    }

    /**
     * Executes the print command and flushes the print buffer.
     *
     * @return 0 if printing succeeds, -1 if failure occurs
     */
    override fun print(
    ) : Int {
        try {
            printerWrapper.print()
            return 0
        } catch (exception: Exception) {
            iPrinterSdkResponseListener.onPrinterSdkResponse(PrinterSdkException(exception.message.toString()))
        }
        return -1
    }
}