package com.analogics.paymentservicecore.models

import com.analogics.paymentservicecore.constants.AppConstants

enum class Acquirer(val acquirerName: String) {
    LYRA(AppConstants.ACQUIRER_LYRA),
    NONE("")
}