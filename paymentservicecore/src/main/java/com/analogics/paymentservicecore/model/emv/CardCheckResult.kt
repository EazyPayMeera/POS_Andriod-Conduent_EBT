package com.analogics.paymentservicecore.model.emv

enum class CardCheckResult {
    NO_CARD_DETECTED,
    CARD_INSERTED,
    CARD_TAPPED,
    CARD_SWIPED,
    NOT_ICC_CARD,
    USE_ICC_CARD,
    BAD_SWIPE,
    NEED_FALLBACK,
    MULTIPLE_CARDS,
    TIMEOUT,
    CANCEL,
    DEVICE_BUSY
}