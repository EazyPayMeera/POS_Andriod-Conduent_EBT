package com.analogics.paymentservicecore.models

enum class TransactionStatus { APPROVED, DECLINED, ERROR, PROCESSING }

data class TransactionData(var status : TransactionStatus = TransactionStatus.ERROR)
