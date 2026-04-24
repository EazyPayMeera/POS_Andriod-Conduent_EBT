package com.analogics.paymentservicecore.data.model

/**
 * Represents all supported transaction types in the system.
 *
 * These map business-level transactions to EMV/acquirer transaction types.
 */
enum class TxnType {
    PURCHASE_CASHBACK,
    CASH_PURCHASE,
    CASH_WITHDRAWAL,
    BALANCE_ENQUIRY_CASH,
    BALANCE_ENQUIRY_SNAP,
    VOUCHER_CLEAR,
    VOUCHER_RETURN,
    VOID_LAST,
    FOOD_PURCHASE,
    FOODSTAMP_RETURN,
    E_VOUCHER
}


/**
 * Maps internal transaction types to EMV transaction codes.
 *
 * ⚠ IMPORTANT:
 * These values are typically ISO8583 / EMV processing codes
 * and must match host/acquirer specifications exactly.
 */
fun TxnType.toEmvTransType(): String {  // Change as per Conduent
    return when (this) {
        TxnType.CASH_PURCHASE -> "00"          // EMV purchase
        TxnType.CASH_WITHDRAWAL -> "00"
        TxnType.PURCHASE_CASHBACK -> "09"     // EMV purchase with cashback
        TxnType.FOOD_PURCHASE -> "00"         // EMV food stamp purchase
        TxnType.BALANCE_ENQUIRY_CASH -> "31"       // EMV balance inquiry
        TxnType.BALANCE_ENQUIRY_SNAP -> "31"
        TxnType.VOUCHER_CLEAR -> "20"         // EMV food stamp return
        TxnType.VOUCHER_RETURN -> "20"        // EMV food stamp return
        TxnType.VOID_LAST -> "20"             // EMV void
        TxnType.FOODSTAMP_RETURN -> "20"      // EMV food stamp return
        TxnType.E_VOUCHER -> "60"             // EMV eVoucher
    }
}

