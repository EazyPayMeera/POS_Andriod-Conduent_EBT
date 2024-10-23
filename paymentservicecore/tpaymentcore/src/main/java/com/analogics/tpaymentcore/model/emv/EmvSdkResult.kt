package com.analogics.tpaymentcore.model.emv

sealed class EmvSdkResult(
    var status: Any? = null,
    var displayMsgId : DisplayMsgId? = null,
    var emvTags : HashMap<String,String>? = null
)
{
    enum class TransStatus {
        APPROVED_ONLINE,
        DECLINED_ONLINE,
        APPROVED_OFFLINE,
        DECLINED_OFFLINE,
        CANCELED,
        TIMEOUT,
        TERMINATED,
        CARD_BLOCKED,
        APP_BLOCKED,
        NO_EMV_APPS,
        APP_SELECTION_FAILED,
        TRY_ANOTHER_INTERFACE,
        INVALID_ICC_CARD,
        RETRY,
        CARD_REMOVED,
        ISSUER_SCRIPT_UPDATE_SUCCESSFUL,
        ISSUER_SCRIPT_UPDATE_FAILED,
        ERROR
    }
    enum class InitStatus {
        SUCCESS,
        FAILURE
    }
    enum class CardCheckStatus {
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
    enum class DisplayMsgId {

        /* NFC Tip Messages */
        CARD_READ_OK,
        REMOVE_CARD,
        USE_CONTACT_IC_CARD,
        USE_MAG_STRIPE,
        INSERT_SWIPE_OR_TRY_ANOTHER_CARD,
        SEE_PHONE_AND_PRESENT_CARD_AGAIN,
        NEED_SIGNATURE,
        END_APPLICATION,
        DISPLAY_BALANCE,
        TAP_CARD_AGAIN,
        APP_BLOCKED,
        TERMINATED,

        /* Error Messages */
        ERR_CARD_READ,
        ERR_PROCESSING,
        ERR_LOAD_CALLBACK,
        ERR_ICS_PARAM_NOT_FOUND,
        ERR_KERNEL,
        ERR_PIN_LENGTH,
        ERR_MULTI_CARD,
        ERR_CHECK_CARD,
        ERR_AID_PARAM_NOT_FIND,
        ERR_CAPK_PARAM_NOT_FIND,
        ERR_GET_KERNEL_DATA_FAILED,
        ERR_QPBOC_APPLICATION,
        ERR_QPBOC_FDDA_FAILED,
        ERR_PURE_ELE_CASH_CARD_NOT_ALLOW_ONLINE_TRANS
    }

    class InitResult(status: InitStatus? = null, displayMsgId: DisplayMsgId? = null) : EmvSdkResult(status, displayMsgId)
    class CardCheckResult(status: CardCheckStatus? = null, displayMsgId: DisplayMsgId? = null) : EmvSdkResult(status, displayMsgId)
    class TransResult(status: TransStatus? = null, displayMsgId: DisplayMsgId? = null) : EmvSdkResult(status, displayMsgId)
}
