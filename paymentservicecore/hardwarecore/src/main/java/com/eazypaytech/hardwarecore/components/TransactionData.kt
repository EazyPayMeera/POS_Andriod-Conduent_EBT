package com.eazypaytech.tpaymentcore.components

enum class TransactionStatus { APPROVED, DECLINED, ERROR, PROCESSING }

data class TransactionData(var status : TransactionStatus = TransactionStatus.ERROR)
