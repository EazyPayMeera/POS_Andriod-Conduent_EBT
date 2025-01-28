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
    var iPrinterServiceResponseListener: IPrinterServiceResponseListener?=null
    lateinit var context : Context
    var job: Job?=null

    class LineFormat(private var format: Int?=0x00000000) {
        fun fontSize(size: FontSize): LineFormat {
            format = format?.or(size.value)
            return this
        }

        fun align(align: Align): LineFormat {
            format = format?.or(align.value)
            return this
        }

        fun lineSpacing(spacing: LineSpacing): LineFormat {
            format = format?.or(spacing.value)
            return this
        }

        fun style(style: Style): LineFormat {
            format = format?.or(style.value)
            return this
        }

        fun font(name: FontName): LineFormat {
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
        job = CoroutineScope(Dispatchers.Default).launch {
            printerSdkRequestRepository.init(context)
        }
        return this
    }

    override fun addText(text : String, format : LineFormat?) : PrinterServiceRepository {
        job = CoroutineScope(Dispatchers.Default).launch {
            printerSdkRequestRepository.addText(text,toSdkLineFormat(format?: LineFormat()))
        }
        return this
    }

    override fun print( ): PrinterServiceRepository {
        job?.cancel()
        job = CoroutineScope(Dispatchers.Default).launch {
            printerSdkRequestRepository.print()
        }
        return this
    }

    fun toSdkLineFormat(format: LineFormat) : PrinterSdkRequestRepository.LineFormat {
        return PrinterSdkRequestRepository.LineFormat(format.getVal())
    }

    fun sdkToPrinterStatus(value: PrinterSdkResult.Status) : Status {
        return when (value) {
            PrinterSdkResult.Status.INIT_SUCCESS -> Status.INIT_SUCCESS
            PrinterSdkResult.Status.INIT_FAILURE -> Status.INIT_FAILURE
            PrinterSdkResult.Status.PRINT_SUCCESS -> Status.PRINT_SUCCESS
            PrinterSdkResult.Status.PRINT_FAILURE -> Status.PRINT_FAILURE
            else -> Status.PRINT_FAILURE
        }
    }

    fun sdkToPrinterService(response: Any) : Any
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
