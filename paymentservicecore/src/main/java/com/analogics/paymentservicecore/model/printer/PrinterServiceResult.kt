package com.eazypaytech.paymentservicecore.model.emv

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
        JAMMED,
        ERROR
    }

    class Result(status: Status? = null) : PrinterServiceResult(status)
}