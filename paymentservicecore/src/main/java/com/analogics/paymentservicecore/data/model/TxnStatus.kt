package com.analogics.paymentservicecore.data.model


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
data class EBTBalance(
    val snap: Double,
    val cash: Double
)