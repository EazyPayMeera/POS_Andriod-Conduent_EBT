package com.analogics.builder_core.requestBuilder

data class SavedTxnData(
    val stan: String? = null,              // DE011
    val rrn: String? = null,               // DE037
    val amount: String? = null,            // DE004
    val ttlAmount: String? = null,
    val processingCode: String? = null,    // DE003
    val transmissionDateTime: String? = null, // DE007
    val localTime: String? = null,         // DE012
    val procId: String? = null,         // DE012
    val localDate: String? = null,         // DE013
    val terminalId: String? = null,        // DE041
    val merchantId: String? = null,        // DE042
    val merchantType: String? = null,
    val merchantName: String? = null,      // DE043
    val merchantBank: String? = null,      // DE048
    val currencyCode: String? = null,      // DE049
    val emvData: String? = null,      // DE049
    val pan: String? = null,               // DE002 (masked preferred)
    val track2Data: String? = null,        // DE035
    val entryMode: String? = null,         // DE022
    val posConditionCode: String? = null,  // DE025
    val acquirerId: String? = null,        // DE032
    val additionalData: String? = null,    // DE058
    val originalData: String? = null       // DE127 or for reversal reference
)