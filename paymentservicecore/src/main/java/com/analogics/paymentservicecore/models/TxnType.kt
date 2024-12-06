package com.analogics.paymentservicecore.models

enum class TxnType {
    PURCHASE,
    REFUND,
    PREAUTH,
    AUTHCAP,
    VOID,
    TXNLIST
}

fun String.toEmvTransType() : String
{
    return when(this)
    {
        TxnType.REFUND.toString() -> "09"
        TxnType.VOID.toString() -> "20"
        else -> "00"
    }
}
