package com.eazypaytech.tpaymentcore.repository

import android.content.Context
import com.eazypaytech.tpaymentcore.listener.requestListener.IPrinterSdkRequestListener
import com.eazypaytech.tpaymentcore.listener.responseListener.IPrinterSdkResponseListener
import com.eazypaytech.tpaymentcore.model.emv.PrinterSdkException
import javax.inject.Inject
import kotlin.toString

class PrinterSdkRequestRepository @Inject constructor(override var iPrinterSdkResponseListener: IPrinterSdkResponseListener) : IPrinterSdkRequestListener {
    private var printerWrapper = PrinterWrapperRepository(iPrinterSdkResponseListener)

/*    data class LineFormat(val value: Int=0x00000000)
    {
        operator fun plus(increment: Any?): LineFormat {
            return when(increment) {
                is FontSize -> LineFormat(value or increment.value)
                is Align -> LineFormat(value or increment.value)
                is LineSpacing -> LineFormat(value or increment.value)
                is Style -> LineFormat(value or increment.value)
                is FontName -> LineFormat(value or increment.value)
                else -> LineFormat(value)
            }
        }
    }*/

    enum class FontSize(val value: Int) {
        EXTRA_SMALL(0x00000001),
        SMALL(0x00000002),
        MEDIUM(0x00000004),
        LARGE(0x00000008),
        EXTRA_LARGE(0x00000010);
    }

    enum class Align(val value: Int) {
        LEFT(0x00000100),
        CENTER(0x00000200),
        RIGHT(0x00000400);
    }

    enum class LineSpacing(val value: Int) {
        EXTRA_SMALL(0x00001000),
        SMALL(0x00002000),
        MEDIUM(0x00004000),
        LARGE(0x00008000),
        EXTRA_LARGE(0x00010000);
    }

    enum class Style(val value: Int) {
        BOLD(0x00100000),
        NO_LINE_BREAK(0x00200000);
    }

    enum class FontName(val value: Int) {
        SIMSUN(0x01000000);
    }

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

    override fun addText(col1: String?, col2: String?, col3: String?, format: Int?)
    {
        try {
            printerWrapper.addText(col1, col2, col3, format?: 0x00000000)
        } catch (exception: Exception) {
            iPrinterSdkResponseListener.onPrinterSdkResponse(PrinterSdkException(exception.message.toString()))
        }
    }

    override fun feedLine(lines: Int?)
    {
        try {
            printerWrapper.feedLine(lines)
        } catch (exception: Exception) {
            iPrinterSdkResponseListener.onPrinterSdkResponse(PrinterSdkException(exception.message.toString()))
        }
    }

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