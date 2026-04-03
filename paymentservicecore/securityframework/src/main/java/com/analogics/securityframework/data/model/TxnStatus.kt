package com.analogics.securityframework.data.model

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