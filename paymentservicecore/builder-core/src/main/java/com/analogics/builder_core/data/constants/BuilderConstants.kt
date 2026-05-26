package com.analogics.builder_core.data.constants

object BuilderConstants {

    /* -------------------------------------------------------------------------
     * Shared Preferences
     * ------------------------------------------------------------------------- */
    const val SHARED_PREF_NAME = "BuilderSharedPref"
    const val SHARED_PREF_KEY_STAN = "stan"

    /* -------------------------------------------------------------------------
     * Default Values
     * ------------------------------------------------------------------------- */
    const val DEFAULT_ISO8583_CURRENCY_CODE = "840"
    const val DEFAULT_ACCOUNT_TYPE = "96"
    const val DEFAULT_AMOUNT_TYPE = "40"
    const val DEFAULT_AMOUNT_SIGN = "C"

    const val DEFAULT_ISO8583_DATE_TIME_FORMAT = "MMDDhhmmss"
    const val DEFAULT_ISO8583_TIME_FORMAT = "MMddHHmmss"

    const val MIN_STAN_VAL = 1
    const val MAX_STAN_VAL = 999999

    const val ISO_CONFIG_PATH = "assets/iso_config.xml"
    const val ISO_FIELD_CURRENCY_CODE_LEN = 3
    const val ISO_FIELD_STAN_MAX_VAL = 999999

    /* -------------------------------------------------------------------------
     * ISO Fields - General
     * ------------------------------------------------------------------------- */
    const val ISO_FIELD_PAN_NO = 2
    const val ISO_FIELD_PAN_NO_LENGTH = 19

    const val ISO_FIELD_PROCESSING_CODE = 3
    const val ISO_FIELD_PROCESSING_CODE_LENGTH = 6

    const val ISO_FIELD_AMOUNT = 4
    const val ISO_FIELD_AMOUNT_LENGTH = 12

    const val ISO_FIELD_TRANSMISSION_DATE = 7
    const val ISO_FIELD_TRANSMISSION_DATE_LENGTH = 10

    const val ISO_FIELD_STAN = 11
    const val ISO_FIELD_STAN_LENGTH = 6

    const val ISO_FIELD_LOC_TIME = 12
    const val ISO_FIELD_LOC_TIME_LENGTH = 6

    const val ISO_FIELD_LOC_DATE = 13
    const val ISO_FIELD_LOC_DATE_LENGTH = 4

    const val ISO_FIELD_EXPIRY_DATE = 14
    const val ISO_FIELD_SET_DATE = 15
    const val ISO_FIELD_CAP_DATE = 17

    const val ISO_FIELD_MERCHANT_TYPE = 18
    const val ISO_FIELD_MERCHANT_TYPE_LENGTH = 4

    const val ISO_FIELD_ENTRY_MODE = 22
    const val ISO_FIELD_ENTRY_MODE_LENGTH = 3

    const val ISO_FIELD_PAN_SEQ_NO = 23
    const val ISO_FIELD_PAN_SEQ_NO_LENGTH = 3

    const val ISO_FIELD_ACQUIRER_ID = 32
    const val ISO_FIELD_ACQUIRER_ID_LENGTH = 11

    const val ISO_FIELD_TRACK2_DATA = 35
    const val ISO_FIELD_TRACK2_DATA_LENGTH = 37

    const val ISO_FIELD_RRN = 37
    const val ISO_FIELD_RRN_LENGTH = 12

    const val ISO_FIELD_AUTH_ID = 38
    const val ISO_FIELD_AUTH_ID_LENGTH = 6

    const val ISO_FIELD_RESPONSE_CODE = 39
    const val ISO_FIELD_RESPONSE_CODE_LENGTH = 2

    const val ISO_FIELD_TERMINAL_ID = 41
    const val ISO_FIELD_TERMINAL_ID_LENGTH = 8

    const val ISO_FIELD_MERCHANT_ID = 42
    const val ISO_FIELD_MERCHANT_ID_LENGTH = 15

    const val ISO_FIELD_MERCHANT_NAME = 43
    const val ISO_FIELD_MERCHANT_NAME_LENGTH = 40

    const val ISO_FIELD_MERCHANT_BANK = 48
    const val ISO_FIELD_MERCHANT_BANK_LENGTH = 16

    const val ISO_FIELD_CURRENCY_CODE = 49
    const val ISO_FIELD_CURRENCY_CODE_LENGTH = 3

    const val ISO_FIELD_PIN_BLOCK = 52
    const val ISO_FIELD_PIN_BLOCK_LENGTH = 16

    const val ISO_FIELD_ICC_DATA = 55

    const val ISO_FIELD_ADD_AMOUNT = 54

    const val ISO_FIELD_POS_CONDITION_CODE = 58
    const val ISO_FIELD_POS_CONDITION_CODE_LENGTH = 10

    const val ISO_FIELD_RESERVED_PRIVATE = 60

    const val ISO_FIELD_NET_MGMT_INFO_CODE = 70
    const val ISO_FIELD_NET_MGMT_INFO_CODE_LENGTH = 3

    const val ISO_FIELD_ORIGINAL_DATA = 90
    const val ISO_FIELD_ORIGINAL_DATA_LENGTH = 42

    const val ISO_FIELD_KEY_MGMT_DATA = 96

    const val ISO_FIELD_ADDITIONAL_DATA = 111
    const val ISO_FIELD_ADDITIONAL_DATA_LENGTH = 7

    const val ISO_FIELD_KEY_DATA = 125

    const val ISO_FIELD_ACQ_TRACE_DATA = 127
    const val ISO_FIELD_ACQ_TRACE_DATA_LENGTH = 20

    const val ISO_FIELD_RESPONSE_TEXT = 127

    /* -------------------------------------------------------------------------
     * ISO Formats & MTI
     * ------------------------------------------------------------------------- */
    const val ISO_DATE_FORMAT = "MMddHHmmss"

    const val ISO_TYPE_SIGN_ON = 0x0800
    const val ISO_TYPE_NTW_RES = 0x0810

    const val MTI_FINANCIAL_REQ = 0x0200
    const val MTI_REVERSAL_REQ = 0x0420

    /* -------------------------------------------------------------------------
     * Network Management
     * ------------------------------------------------------------------------- */
    const val SIGN_ON_REQUEST = 1
    const val SIGN_OFF_REQUEST = 2
    const val HANDSHAKE_REQUEST = 301
    const val KEY_CHANGE = 101
    const val KEY_CHANGE_REQUEST = 180

    const val ISO_FIELD_PROC_ID_LENGTH = 11

    /* -------------------------------------------------------------------------
     * ISO Response Codes
     * ------------------------------------------------------------------------- */
    const val ISO_RESP_CODE_APPROVED = "00"
    const val ISO_RESP_CODE_INVALID_MERCHANT_FNS = "03"
    const val ISO_RESP_CODE_DO_NOT_HONOR = "05"
    const val ISO_RESP_CODE_INVALID_TXN = "06"
    const val ISO_RESP_CODE_INVALID_TXN_TYPE = "12"
    const val ISO_RESP_CODE_INVALID_AMOUNT = "13"
    const val ISO_RESP_CODE_DECLINED_INVALID_CARD = "14"
    const val ISO_RESP_CODE_DECLINED_REENTER_TXN = "19"
    const val ISO_RESP_CODE_UNACCEPTABLE_TXN_FEE = "23"
    const val ISO_RESP_CODE_FORMAT_ERROR = "30"
    const val ISO_RESP_CODE_INVALID_ISO_PREFIX = "31"
    const val ISO_RESP_CODE_DECLINED_UNSUPPORTED_FUNCTION = "40"
    const val ISO_RESP_CODE_PICKUP_LOST_CARD = "41"
    const val ISO_RESP_CODE_DECLINED_NO_UNIVERSAL_ACCOUNT = "42"
    const val ISO_RESP_CODE_PICKUP_STOLEN_CARD = "43"
    const val ISO_RESP_CODE_DECLINED_INSUFFICIENT_FUNDS = "51"
    const val ISO_RESP_CODE_NO_ACCOUNT_ON_FILE = "52"
    const val ISO_RESP_CODE_EXPIRED_CARD_CHECK = "54"
    const val ISO_RESP_CODE_INCORRECT_PIN = "55"
    const val ISO_RESP_CODE_CARD_NUMBER_NOT_FOUND = "56"
    const val ISO_RESP_CODE_DECLINED_NOT_PERMITTED_TO_CARDHOLDER = "57"
    const val ISO_RESP_CODE_DECLINED_NOT_PERMITTED_TO_TERMINAL = "58"
    const val ISO_RESP_CODE_DECLINED_SUSPECTED_FRAUD_ALT = "59"
    const val ISO_RESP_CODE_EXCEEDS_WITHDRAWAL_LIMIT = "61"
    const val ISO_RESP_CODE_RESTRICTED_CARD_CAPTURE = "62"
    const val ISO_RESP_CODE_EXCEEDED_PIN_TRIES = "75"
    const val ISO_RESP_CODE_PVT_ERROR_TO_ACCOUNT = "76"
    const val ISO_RESP_CODE_VOUCHER_EXPIRED = "80"
    const val ISO_RESP_CODE_PVT_ERROR_PIN_VALIDATION = "86"
    const val ISO_RESP_CODE_CUT_OFF_IN_PROCESS = "90"
    const val ISO_RESP_CODE_TRY_AFTER_5MIN_AUTH_SYS_INOPERATIVE = "91"
    const val ISO_RESP_CODE_TRY_AFTER_5MIN_UNABLE_ROUTE = "92"
    const val ISO_RESP_CODE_SYSTEM_ERROR = "96"

    /* Custom / Extended Response Codes */
    const val ISO_RESP_CODE_INVALID_VOUCHER_ID = "A1"
    const val ISO_RESP_CODE_INVALID_AUTH_NUMBER = "A2"
    const val ISO_RESP_CODE_PIN_NOT_SELECTED = "S5"
    const val ISO_RESP_CODE_PIN_ALREADY_SELECTED = "S6"
    const val ISO_RESP_CODE_UNMATCHED_VOUCHER_INFO = "S7"
    const val ISO_RESP_CODE_INVALID_HIP_AMOUNT = "TL"

    /* -------------------------------------------------------------------------
     * Response Message Mapper
     * ------------------------------------------------------------------------- */
    fun getIsoResponseMessage(code: String): String {
        return when (code) {
            ISO_RESP_CODE_APPROVED -> "APPROVED"
            ISO_RESP_CODE_DO_NOT_HONOR -> "GENERAL DENIAL"
            ISO_RESP_CODE_INVALID_TXN -> "INVALID TRANSACTION"
            ISO_RESP_CODE_INVALID_TXN_TYPE -> "INVALID TRANSACTION TYPE"
            ISO_RESP_CODE_INVALID_AMOUNT -> "INVALID AMOUNT FIELD"
            ISO_RESP_CODE_DECLINED_INVALID_CARD -> "INVALID CARD NUMBER"
            ISO_RESP_CODE_DECLINED_REENTER_TXN -> "RE-ENTER TRANSACTION"
            ISO_RESP_CODE_UNACCEPTABLE_TXN_FEE -> "UNACCEPTABLE TRANSACTION FEE"
            ISO_RESP_CODE_FORMAT_ERROR -> "FORMAT ERROR"
            ISO_RESP_CODE_INVALID_ISO_PREFIX -> "CARD HAS INVALID ISO PREFIX"
            ISO_RESP_CODE_DECLINED_UNSUPPORTED_FUNCTION -> "FUNCTION NOT AVAILABLE"
            ISO_RESP_CODE_PICKUP_LOST_CARD -> "LOST CARD"
            ISO_RESP_CODE_DECLINED_NO_UNIVERSAL_ACCOUNT -> "NO ACCOUNT"
            ISO_RESP_CODE_PICKUP_STOLEN_CARD -> "LOST/STOLEN CARD"
            ISO_RESP_CODE_DECLINED_INSUFFICIENT_FUNDS -> "INSUFFICIENT FUNDS"
            ISO_RESP_CODE_NO_ACCOUNT_ON_FILE -> "NO ACCOUNT ON FILE"
            ISO_RESP_CODE_EXPIRED_CARD_CHECK -> "EXPIRED CARD"
            ISO_RESP_CODE_INCORRECT_PIN -> "INVALID PIN / PIN NOT SELECTED"
            ISO_RESP_CODE_CARD_NUMBER_NOT_FOUND -> "CARD NUMBER NOT FOUND"
            ISO_RESP_CODE_DECLINED_NOT_PERMITTED_TO_CARDHOLDER -> "TRANSACTION NOT PERMITTED TO CARDHOLDER"
            ISO_RESP_CODE_DECLINED_NOT_PERMITTED_TO_TERMINAL -> "INVALID TRANSACTION"
            ISO_RESP_CODE_DECLINED_SUSPECTED_FRAUD_ALT -> "FRAUD (RETURN CARD)"
            ISO_RESP_CODE_EXCEEDS_WITHDRAWAL_LIMIT -> "RETURN EXCEEDS BENEFIT AUTHORIZATION"
            ISO_RESP_CODE_RESTRICTED_CARD_CAPTURE -> "RESTRICTED CARD"
            ISO_RESP_CODE_EXCEEDED_PIN_TRIES -> "PIN TRIES EXCEEDED"
            ISO_RESP_CODE_PVT_ERROR_TO_ACCOUNT -> "KEY SYNCHRONIZATION ERROR"
            ISO_RESP_CODE_VOUCHER_EXPIRED -> "VOUCHER EXPIRED"
            ISO_RESP_CODE_PVT_ERROR_PIN_VALIDATION -> "INVALID SECURITY CODE"
            ISO_RESP_CODE_CUT_OFF_IN_PROCESS -> "PROCESSOR NOT LOGGED ON"
            ISO_RESP_CODE_TRY_AFTER_5MIN_AUTH_SYS_INOPERATIVE -> "AUTHORIZER NOT AVAILABLE (TIME-OUT)"
            ISO_RESP_CODE_TRY_AFTER_5MIN_UNABLE_ROUTE -> "TRANSACTION DESTINATION CANNOT BE FOUND FOR ROUTING"
            ISO_RESP_CODE_SYSTEM_ERROR -> "SYSTEM MALFUNCTION"
            ISO_RESP_CODE_INVALID_MERCHANT_FNS -> "INVALID MERCHANT"
            ISO_RESP_CODE_INVALID_VOUCHER_ID -> "INVALID VOUCHER ID"
            ISO_RESP_CODE_INVALID_AUTH_NUMBER -> "INVALID AUTHORIZATION NUMBER"
            ISO_RESP_CODE_PIN_NOT_SELECTED -> "PIN NOT SELECTED"
            ISO_RESP_CODE_PIN_ALREADY_SELECTED -> "PIN ALREADY SELECTED"
            ISO_RESP_CODE_UNMATCHED_VOUCHER_INFO -> "UNMATCHED VOUCHER INFORMATION"
            ISO_RESP_CODE_INVALID_HIP_AMOUNT -> "INVALID HIP AMOUNT"

            else -> "UNKNOWN RESPONSE CODE"
        }
    }
}