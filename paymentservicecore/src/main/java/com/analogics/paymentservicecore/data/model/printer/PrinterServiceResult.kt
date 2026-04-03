package com.analogics.paymentservicecore.data.model.printer

sealed class PrinterServiceResult(
    var status: Any? = null
)
{
    enum class Status {
        NONE,
        INIT_SUCCESS,
        INIT_FAILURE,
        PRINT_SUCCESS,
        PRINT_FAILURE,
        PRINTING,
        OUT_OF_PAPER,
        BUSY,
        JAMMED,
        ERROR
    }

    class Result(status: Status? = null) : PrinterServiceResult(status)
}