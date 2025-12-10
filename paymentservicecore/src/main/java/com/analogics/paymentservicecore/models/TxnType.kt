package com.eazypaytech.paymentservicecore.models

enum class TxnType {
    PURCHASE_CASHBACK,
    CASH_PURCHASE,
    BALANCE_ENQUIRY,
    VOUCHER_CLEAR,
    VOUCHER_RETURN,
    VOID_LAST,
    FOOD_PURCHASE,
    FOODSTAMP_RETURN,
    E_VOUCHER
}


fun TxnType.toEmvTransType(): String {  // DO TO Need to change as per Conduent
    return when (this) {
        TxnType.PURCHASE_CASHBACK -> "00"
        TxnType.CASH_PURCHASE -> "43"
        TxnType.BALANCE_ENQUIRY -> "31"
        TxnType.VOUCHER_CLEAR -> "40"
        TxnType.VOUCHER_RETURN -> "41"
        TxnType.VOID_LAST -> "20"
        TxnType.FOOD_PURCHASE -> "50"
        TxnType.FOODSTAMP_RETURN -> "51"
        TxnType.E_VOUCHER -> "60"
    }
}

