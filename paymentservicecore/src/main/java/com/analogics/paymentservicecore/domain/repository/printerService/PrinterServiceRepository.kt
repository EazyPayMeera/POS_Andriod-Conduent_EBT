package com.analogics.paymentservicecore.domain.repository.printerService

import android.content.Context
import com.analogics.paymentservicecore.data.listeners.requestListener.IPrinterServiceRequestListener
import com.analogics.paymentservicecore.data.listeners.responseListener.IPrinterServiceResponseListener
import com.analogics.paymentservicecore.data.model.printer.PrinterServiceResult.Result
import com.analogics.paymentservicecore.data.model.printer.PrinterServiceResult.Status
import com.analogics.paymentservicecore.data.model.error.PrinterServiceException
import com.eazypaytech.hardwarecore.domain.listener.responseListener.IPrinterSdkResponseListener
import com.eazypaytech.hardwarecore.data.printer.PrinterSdkException
import com.eazypaytech.hardwarecore.data.model.PrinterSdkResult
import com.eazypaytech.hardwarecore.domain.repository.PrinterSdkRequestRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * PrinterServiceRepository
 *
 * Acts as a bridge between:
 * - Application Printer Service Layer
 * - Hardware SDK Printer Layer
 *
 * Responsibilities:
 * 1. Initialize printer SDK
 * 2. Build print job (text, formatting, line spacing)
 * 3. Trigger print execution
 * 4. Map SDK responses → application-level responses
 * 5. Handle printer status updates and errors
 */
class PrinterServiceRepository @Inject constructor() : IPrinterServiceRequestListener,
    IPrinterSdkResponseListener {
    private val printerSdkRequestRepository = PrinterSdkRequestRepository(this)
    private var iPrinterServiceResponseListener: IPrinterServiceResponseListener?=null
    private lateinit var context : Context
    private var job: Job?=null

    /**
     * PrintFormat builder
     *
     * Bitmask-based formatting system used to define:
     * - Font size
     * - Alignment
     * - Line spacing
     * - Style (bold, reverse, etc.)
     * - Font type
     *
     * Each option is OR-ed into a single integer flag.
     */
    class PrintFormat(private var format: Int?=0x00000000) {
        /**
         * Font size options for printing
         */
        fun fontSize(size: FontSize): PrintFormat {
            format = format?.or(size.value)
            return this
        }

        /**
         * Text alignment options
         */
        fun align(align: Align): PrintFormat {
            format = format?.or(align.value)
            return this
        }

        /**
         * Line spacing options for receipt formatting
         */
        fun lineSpacing(spacing: LineSpacing): PrintFormat {
            format = format?.or(spacing.value)
            return this
        }

        /**
         * Text styling options
         */
        fun style(style: Style): PrintFormat {
            format = format?.or(style.value)
            return this
        }

        /**
         * Font family selection
         */
        fun font(name: FontName): PrintFormat {
            format = format?.or(name.value)
            return this
        }

        fun getVal() : Int {
            return format?:0x00000000
        }

    }

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
        NO_LINE_BREAK(0x00200000),
        REVERSE(0x00400000);
    }

    enum class FontName(val value: Int) {
        SIMSUN(0x01000000);
    }

    /**
     * Initializes printer SDK
     *
     * @param context Android context
     * @param iPrinterServiceResponseListener callback for printer events
     */
    override fun init(
        context: Context,
        iPrinterServiceResponseListener: IPrinterServiceResponseListener
    ) : PrinterServiceRepository {
        this.context = context
        this.iPrinterServiceResponseListener = iPrinterServiceResponseListener
        printerSdkRequestRepository.init(context)
        return this
    }

    /**
     * Adds text line to print buffer
     *
     * Supports 3-column layout (receipt style printing)
     *
     * @param col1 Left column text
     * @param col2 Center column text
     * @param col3 Right column text
     * @param format Print formatting options (font, align, style, etc.)
     * @param condition If false → skip adding text
     */
    override fun addText(col1 : String?, col2: String?, col3 : String?, format : PrintFormat?, condition: Boolean?) : PrinterServiceRepository {
        condition?.takeIf { it == true }?.let {
            printerSdkRequestRepository.addText(col1, col2, col3, format?.getVal())
        }
        return this
    }

    /**
     * Adds blank line spacing to print buffer
     *
     * @param lines number of empty lines
     * @param condition if false → skip operation
     */
    override fun feedLine(lines : Int?, condition: Boolean?) : PrinterServiceRepository {
        condition?.takeIf { it == true }?.let {
            printerSdkRequestRepository.feedLine(lines)
        }
        return this
    }

    /**
     * Executes print job asynchronously
     *
     * Ensures:
     * - Only one print job runs at a time
     * - Previous job is cancelled before new execution
     */
    override fun print( ): PrinterServiceRepository {
        job?.cancel()
        job = CoroutineScope(Dispatchers.Default).launch {
            printerSdkRequestRepository.print()
            job?.join()
            job = null
        }
        return this
    }

    /**
     * Maps SDK printer status → application printer status
     */
    private fun sdkToPrinterStatus(value: PrinterSdkResult.Status) : Status {
        return when (value) {
            PrinterSdkResult.Status.INIT_SUCCESS -> Status.INIT_SUCCESS
            PrinterSdkResult.Status.INIT_FAILURE -> Status.INIT_FAILURE
            PrinterSdkResult.Status.PRINT_SUCCESS -> Status.PRINT_SUCCESS
            PrinterSdkResult.Status.PRINT_FAILURE -> Status.PRINT_FAILURE
            PrinterSdkResult.Status.PRINTING -> Status.PRINTING
            PrinterSdkResult.Status.OUT_OF_PAPER -> Status.OUT_OF_PAPER
            PrinterSdkResult.Status.BUSY -> Status.BUSY
            PrinterSdkResult.Status.JAMMED -> Status.JAMMED
            PrinterSdkResult.Status.ERROR -> Status.ERROR
            else -> Status.NONE
        }
    }

    /**
     * Converts SDK response → application-level printer response
     */
    private fun sdkToPrinterService(response: Any) : Any
    {
        return when(response) {
            is PrinterSdkResult.Result -> {
                Result(
                    status = sdkToPrinterStatus(response.status as PrinterSdkResult.Status)
                )
            }
            is PrinterSdkException ->{
                PrinterServiceException(errorMessage = response.errorMessage)
            }
            else -> {
                PrinterServiceException(errorMessage = "Unknown Printer Error")
            }
        }
    }

    /**
     * SDK callback entry point for printer events
     */
    override fun onPrinterSdkResponse(response: Any) {
        iPrinterServiceResponseListener?.onPrinterServiceResponse(sdkToPrinterService(response))
    }
}
