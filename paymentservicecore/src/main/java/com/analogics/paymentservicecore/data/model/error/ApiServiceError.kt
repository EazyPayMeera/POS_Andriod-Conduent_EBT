package com.analogics.paymentservicecore.data.model.error

data class ApiServiceError(var errorMessage:String="")
data class ApiServiceTimeout(val message: String = "Transaction Timed Out")
