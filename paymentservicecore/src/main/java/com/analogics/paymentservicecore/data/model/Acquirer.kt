package com.analogics.paymentservicecore.data.model

import com.eazypaytech.paymentservicecore.constants.AppConstants

enum class Acquirer(val acquirerName: String) {
    LYRA(AppConstants.ACQUIRER_LYRA),
    NONE("")
}