package com.eazypaytech.posafrica.rootUtils.genericComposeUI

import android.content.Context
import com.eazypaytech.paymentservicecore.listeners.requestListener.IPrinterServiceRequestListener
import com.eazypaytech.paymentservicecore.listeners.responseListener.IPrinterServiceResponseListener
import com.eazypaytech.paymentservicecore.model.emv.PrinterServiceResult.Result
import com.eazypaytech.paymentservicecore.model.emv.PrinterServiceResult.Status
import com.eazypaytech.paymentservicecore.model.error.PrinterServiceException
import com.eazypaytech.tpaymentcore.listener.responseListener.IPrinterSdkResponseListener
import com.eazypaytech.tpaymentcore.model.emv.PrinterSdkException
import com.eazypaytech.tpaymentcore.model.emv.PrinterSdkResult
import com.eazypaytech.tpaymentcore.repository.PrinterSdkRequestRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

class PrinterServiceRepository @Inject constructor() : IPrinterServiceRequestListener,
    IPrinterSdkResponseListener {
    private val printerSdkRequestRepository = PrinterSdkRequestRepository(this)
    private var iPrinterServiceResponseListener: IPrinterServiceResponseListener?=null
    private lateinit var context : Context
    private var job: Job?=null

    class PrintFormat(private var format: Int?=0x00000000) {
        fun fontSize(size: FontSize): PrintFormat {
            format = format?.or(size.value)
            return this
        }

        fun align(align: Align): PrintFormat {
            format = format?.or(align.value)
            return this
        }

        fun lineSpacing(spacing: LineSpacing): PrintFormat {
            format = format?.or(spacing.value)
            return this
        }

        fun style(style: Style): PrintFormat {
            format = format?.or(style.value)
            return this
        }

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
        NO_LINE_BREAK(0x00200000);
    }

    enum class FontName(val value: Int) {
        SIMSUN(0x01000000);
    }

    override fun init(
        context: Context,
        iPrinterServiceResponseListener: IPrinterServiceResponseListener
    ) : PrinterServiceRepository {
        this.context = context
        this.iPrinterServiceResponseListener = iPrinterServiceResponseListener
        printerSdkRequestRepository.init(context)
        return this
    }

    override fun addText(col1 : String?, col2: String?, col3 : String?, format : PrintFormat?, condition: Boolean?) : PrinterServiceRepository {
        condition?.takeIf { it == true }?.let {
            printerSdkRequestRepository.addText(col1, col2, col3, format?.getVal())
        }
        return this
    }

    override fun feedLine(lines : Int?, condition: Boolean?) : PrinterServiceRepository {
        condition?.takeIf { it == true }?.let {
            printerSdkRequestRepository.feedLine(lines)
        }
        return this
    }

    override fun print( ): PrinterServiceRepository {
        job?.cancel()
        job = CoroutineScope(Dispatchers.Default).launch {
            printerSdkRequestRepository.print()
            job?.join()
            job = null
        }
        return this
    }

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

    override fun onPrinterSdkResponse(response: Any) {
        iPrinterServiceResponseListener?.onPrinterServiceResponse(sdkToPrinterService(response))
    }
}
