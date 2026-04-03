package com.eazypaytech.hardwarecore.data.model

sealed class PrinterSdkResult(
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

    class Result(status: Status? = null) : PrinterSdkResult(status)
}