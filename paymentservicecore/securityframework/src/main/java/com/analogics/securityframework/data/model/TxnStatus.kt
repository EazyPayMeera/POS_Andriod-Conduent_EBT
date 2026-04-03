package com.eazypaytech.securityframework.model

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