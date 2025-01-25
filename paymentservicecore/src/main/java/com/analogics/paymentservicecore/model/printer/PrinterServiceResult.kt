package com.eazypaytech.paymentservicecore.model.emv

sealed class PrinterServiceResult(
    var status: Any? = null,
    var printerMsgId : PrinterMsgId? = null
)
{
    enum class PrinterStatus {
        NONE,
        SUCCESS,
        FAILURE,
        OUT_OF_PAPER,
        JAMMED,
        ERROR
    }
    enum class InitStatus {
        SUCCESS,
        FAILURE
    }

    enum class PrinterMsgId {
        NONE, /* Clear Display */
        PRINTING
    }

    class InitResult(status: InitStatus? = null, printerMsgId: PrinterMsgId? = null) : PrinterServiceResult(status, printerMsgId)
    class PrinterResult(status: PrinterStatus? = null, printerMsgId: PrinterMsgId? = null) : PrinterServiceResult(status, printerMsgId)
}