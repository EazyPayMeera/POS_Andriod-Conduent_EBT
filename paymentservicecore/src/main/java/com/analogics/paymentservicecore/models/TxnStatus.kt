package com.eazypaytech.paymentservicecore.models


enum class TxnStatus {
    INITIATED,
    APPROVED,
    DECLINED,
    ERROR,
    TERMINATED,
    REVERSED,
    VOIDED,
    REFUNDED,
    CAPTURED
}
