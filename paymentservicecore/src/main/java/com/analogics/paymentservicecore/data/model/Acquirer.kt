package com.eazypaytech.paymentservicecore.models

import com.eazypaytech.paymentservicecore.constants.AppConstants

enum class Acquirer(val acquirerName: String) {
    LYRA(AppConstants.ACQUIRER_LYRA),
    NONE("")
}