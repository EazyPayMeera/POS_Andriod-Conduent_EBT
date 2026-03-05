package com.eazypaytech.paymentservicecore.model.error

data class ApiServiceError(var errorMessage:String="")
data class ApiServiceTimeout(val message: String = "Transaction Timed Out")
