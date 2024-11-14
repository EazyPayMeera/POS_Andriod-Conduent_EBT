package com.analogics.paymentservicecore.models


enum class TxnStatus {
    INITIATED,
    APPROVED,
    DECLINED,
    ERROR,
    REVERSED,
    VOIDED,
    REFUNDED
}
