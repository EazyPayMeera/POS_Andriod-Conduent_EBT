package com.eazypaytech.paymentservicecore.models

enum class TxnType {
    PURCHASE,
    REFUND,
    PREAUTH,
    AUTHCAP,
    VOID,
    TXNLIST
}

fun TxnType.toEmvTransType() : String
{
    return when(this)
    {
        TxnType.REFUND -> "09"
        TxnType.VOID -> "20"
        else -> "00"
    }
}
