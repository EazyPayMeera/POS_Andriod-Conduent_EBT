package com.eazypaytech.builder_core.constants

import android.util.Log

object BuilderConstants {

    const val SHARED_PREF_NAME = "BuilderSharedPref"
    const val SHARED_PREF_KEY_STAN = "stan"

    const val DEFAULT_ISO8583_CURRENCY_CODE = "840"
    const val DEFAULT_ACCOUNT_TYPE = "96"
    const val DEFAULT_AMOUNT_TYPE = "40"
    const val DEFAULT_AMOUNT_SIGN = "C"
    const val DEFAULT_ISO8583_NII = "0110"
    const val DEFAULT_ISO8583_DATE_TIME_FORMAT = "MMDDhhmmss"
    const val DEFAULT_ISO8583_DATE_FORMAT = "MMdd"
    const val DEFAULT_ISO8583_TIME_FORMAT = "MMddHHmmss"

    /* For Dummy Host Response */
    const val DUMMY_RANDOM_CHARSET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
    const val DUMMY_DATE_TIME_FORMAT_RRN = "MMddHHmmss"
    const val DUMMY_DATE_TIME_FORMAT_AUTH_CODE = "ddHHmm"

    const val ISO_CONFIG_PATH = "assets/iso_config.xml"

    /* MTI Values */
    const val MTI_AUTH_REQ = 0x0100
    const val MTI_SALE_REQ = 0x0200
    const val MTI_SALE_RES = 0x0210
    const val MTI_NETWORK_REQ = 0x0800
    const val MTI_VOID_REQ = 0x0220
    const val MTI_VOID_RES = 0x0230



    /* TPDU */
    val ISO_HEADER = byteArrayOf(0x60.toByte(),0x00.toByte(),0x11.toByte(),0x00.toByte(),0x00.toByte())


    /* Processing Code */
    const val ISO_FIELD_PROC_CODE = 3
    const val ISO_FIELD_PROC_CODE_LENGTH = 6
    const val PROC_CODE_SALE = 0
    const val PROC_CODE_RKL_PART_SN = 990380
    const val PROC_CODE_RKL_FULL_SN = 991380
    const val PROC_CODE_REFUND = 2
    const val PROC_CODE_VOID_SALE = 20000
    const val PROC_CODE_VOID_REFUND = 22
    const val PROC_CODE_VOID_PRE_AUTH = 32


    const val ISO_FIELD_STAN_MAX_VAL = 999999

    /* Time */
    const val ISO_FIELD_TIME = 12
    const val ISO_FIELD_TIME_LENGTH = 6

    /* Date */
    const val ISO_FIELD_DATE = 13
    const val ISO_FIELD_DATE_LENGTH = 4


    /* POS Entry Mode */
    const val ISO_FIELD_PAN_SEQ_NO = 23
    const val ISO_FIELD_PAN_SEQ_NO_LENGTH = 3

    /* NII */
    const val ISO_FIELD_NII = 24
    const val ISO_FIELD_NII_LENGTH = 4



    /* Auth Code */
    const val ISO_FIELD_AUTH_CODE = 38
    const val ISO_FIELD_AUTH_CODE_LENGTH = 6

    /* Response Code */
    const val ISO_FIELD_RESP_CODE = 39
    const val ISO_FIELD_RESP_CODE_LENGTH = 2

    /* TID */
    const val ISO_FIELD_TID = 41
    const val ISO_FIELD_TID_LENGTH = 8

    /* MID */
    const val ISO_FIELD_MID = 42
    const val ISO_FIELD_MID_LENGTH = 15

    /* ADDITIONAL DATA */
    const val ISO_FIELD_ADDL_DATA_KSN = 48
    const val ISO_FIELD_KSN_TAG = "4801"
    const val ISO_FIELD_KSN_LENGTH = 20
    const val ISO_FIELD_KSN_PAD_CHAR = 'F'

    /* Currency Code */
    const val ISO_FIELD_CURRENCY_CODE_TXN = 49
    const val ISO_FIELD_CURRENCY_CODE_LEN = 3

    /* Pin Block */


    /* ICC Related Data */
    const val ISO_FIELD_ICC_DATA = 55
    const val ISO_FIELD_ICC_DATA_MAX_LENGTH = 255

    /* TERM SR NO */
    const val ISO_FIELD_TERM_SR_NO = 60

    /* Batch Number */
    const val ISO_FIELD_PVT_USE_BATCH = 60
    const val ISO_FIELD_PVT_USE_BATCH_MAX_LENGTH = 999
    const val ISO_FIELD_PVT_USE_BATCH_LENGTH = 6
    const val ISO_FIELD_PVT_USE_BATCH_LENGTH_LENGTH = 2

    /* Invoice Number */
    const val ISO_FIELD_INVOICE_NUMBER = 62
    const val ISO_FIELD_INVOICE_NUMBER_LENGTH = 6

    /* WORKING KEY */
    const val ISO_FIELD_WORKING_KEY = 62
    const val ISO_FIELD_KCV_LENGTH = 6

    const val MIN_STAN_VAL = 1
    const val MAX_STAN_VAL = 999999



    /* ISO Response Codes */
    const val ISO_RESP_CODE_INVALID_VOUCHER_ID = "A1"        // Invalid Voucher ID
    const val ISO_RESP_CODE_INVALID_AUTH_NUMBER = "A2"       // Invalid Authorization Number
    const val ISO_RESP_CODE_APPROVED = "00"                  // Approved
    const val ISO_RESP_CODE_BAD_FNS_STATUS = "03"           // Bad FNS Status for Merchant
    const val ISO_RESP_CODE_INVALID_MERCHANT = "03"         // Invalid Merchant
    const val ISO_RESP_CODE_GENERAL_DENIAL = "05"           // General Denial
    const val ISO_RESP_CODE_INVALID_TRANSACTION = "06"      // Invalid Transaction
    const val ISO_RESP_CODE_INVALID_TXN_TYPE = "12"         // Invalid Transaction Type
    const val ISO_RESP_CODE_INVALID_AMOUNT = "13"           // Invalid Amount Field
    const val ISO_RESP_CODE_INVALID_CARD_NUMBER = "14"      // Invalid Card Number
    const val ISO_RESP_CODE_REENTER_TRANSACTION = "19"     // Re-enter Transaction
    const val ISO_RESP_CODE_UNACCEPTABLE_TXN_FEE = "23"    // Unacceptable Transaction Fee
    const val ISO_RESP_CODE_FORMAT_ERROR = "30"             // Format Error
    const val ISO_RESP_CODE_INVALID_ISO_PREFIX = "31"       // Card Has Invalid ISO Prefix
    const val ISO_RESP_CODE_FUNCTION_NOT_AVAILABLE = "40"   // Function Not Available
    const val ISO_RESP_CODE_LOST_CARD = "41"                // Lost card
    const val ISO_RESP_CODE_NO_ACCOUNT = "42"               // No account
    const val ISO_RESP_CODE_LOST_STOLEN_CARD = "43"         // Lost/Stolen Card
    const val ISO_RESP_CODE_INSUFFICIENT_FUNDS = "51"       // Insufficient Funds
    const val ISO_RESP_CODE_NO_ACCOUNT_ON_FILE = "52"       // No Account on File
    const val ISO_RESP_CODE_EXPIRED_CARD = "54"             // Expired Card
    const val ISO_RESP_CODE_INVALID_PIN = "55"              // Invalid PIN
    const val ISO_RESP_CODE_PIN_NOT_SELECTED = "55"         // PIN Not Selected
    const val ISO_RESP_CODE_CARD_NUMBER_NOT_FOUND = "56"    // Card Number Not Found
    const val ISO_RESP_CODE_TXN_NOT_PERMITTED_CARDHOLDER = "57" // Transaction Not Permitted to Cardholder
    const val ISO_RESP_CODE_INVALID_TXN_ALT = "58"          // Invalid Transaction
    const val ISO_RESP_CODE_FRAUD_RETURN_CARD = "59"       // Fraud (Return Card)
    const val ISO_RESP_CODE_RETURN_EXCEEDS_AUTH = "61"      // Return Exceeds Benefit Authorization
    const val ISO_RESP_CODE_RESTRICTED_CARD = "62"         // Restricted Card
    const val ISO_RESP_CODE_PIN_TRIES_EXCEEDED = "75"      // PIN Tries Exceeded
    const val ISO_RESP_CODE_INVALID_REVERSAL = "76"        // Invalid Reversal
    const val ISO_RESP_CODE_VOUCHER_EXPIRED = "80"         // Voucher Expired
    const val ISO_RESP_CODE_INVALID_SECURITY_CODE = "86"   // Invalid Security Code
    const val ISO_RESP_CODE_PROCESSOR_NOT_LOGGED_ON = "90" // Processor Not Logged On
    const val ISO_RESP_CODE_AUTHORIZER_NOT_AVAILABLE = "91" // Authorizer Not Available (time-out)
    const val ISO_RESP_CODE_TXN_DESTINATION_NOT_FOUND = "92" // Transaction Destination Cannot Be Found For Routing
    const val ISO_RESP_CODE_SYSTEM_MALFUNCTION = "96"       // System Malfunction
    const val ISO_RESP_CODE_PIN_NOT_SELECTED_ALT = "S5"    // PIN Not Selected
    const val ISO_RESP_CODE_PIN_ALREADY_SELECTED = "S6"    // PIN Already Selected
    const val ISO_RESP_CODE_UNMATCHED_VOUCHER_INFO = "S7" // Unmatched Voucher Information
    const val ISO_RESP_CODE_INVALID_HIP_AMOUNT = "TL"      // Invalid HIP Amount




    // for Conduent
    const val ISO_FIELD_PAN_NO = 2   // DE007
    const val ISO_FIELD_PAN_NO_LENGTH = 19

    const val ISO_FIELD_PROCESSING_CODE = 3
    const val ISO_FIELD_PROCESSING_CODE_LENGTH = 6

    const val ISO_FIELD_AMOUNT = 4
    const val ISO_FIELD_AMOUNT_LENGTH = 12

    const val ISO_FIELD_TRANSMISSION_DATE = 7   // DE007
    const val ISO_FIELD_TRANSMISSION_DATE_LENGTH = 10   // DE007

    const val ISO_FIELD_STAN = 11
    const val ISO_FIELD_STAN_LENGTH = 6

    const val ISO_FIELD_NETWORK_CODE = 41
    const val ISO_FIELD_NETWORK_CODE_LENGTH = 6

    const val ISO_FIELD_LOC_TIME = 12      // DE096
    const val ISO_FIELD_LOC_TIME_LENGTH = 6

    const val ISO_FIELD_LOC_DATE = 13      // DE096
    const val ISO_FIELD_LOC_DATE_LENGTH = 4

    const val ISO_FIELD_EXPIRY_DATE = 14   // DE007
    const val ISO_FIELD_SET_DATE = 15   // DE007
    const val ISO_FIELD_CAP_DATE = 17   // DE007

    const val ISO_FIELD_MERCHANT_TYPE = 18  // DE007
    const val ISO_FIELD_MERCHANT_TYPE_LENGTH = 4  // DE007

    const val ISO_FIELD_ENTRY_MODE = 22  // DE007
    const val ISO_FIELD_ENTRY_MODE_LENGTH = 3  // DE007


    const val ISO_FIELD_ACQUIRER_ID = 32
    const val ISO_FIELD_ACQUIRER_ID_LENGTH = 11

    const val ISO_FIELD_TRACK2_DATA = 35  // DE007
    const val ISO_FIELD_TRACK2_DATA_LENGTH = 37  // DE007

    const val ISO_FIELD_RRN = 37  // DE007
    const val ISO_FIELD_RRN_LENGTH = 12

    const val ISO_FIELD_AUTH_ID = 38  // DE007
    const val ISO_FIELD_AUTH_ID_LENGTH = 6  // DE007
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

    const val ISO_FIELD_ADD_AMOUNT = 54
    const val ISO_FIELD_POS_CONDITION_CODE = 58
    const val ISO_FIELD_POS_CONDITION_CODE_LENGTH = 10

    const val ISO_FIELD_RESERVED_PRIVATE = 60     // DE060
    const val ISO_FIELD_SETTLEMENT_CODE = 66
    const val ISO_FIELD_NET_MGMT_INFO_CODE = 70
    const val ISO_FIELD_NET_MGMT_INFO_CODE_LENGTH = 3
    const val ISO_FIELD_CREDITS_NUMBER = 74
    const val ISO_FIELD_CREDITS_REV_NUMBER = 75
    const val ISO_FIELD_DEBITS_NUMBER = 76
    const val ISO_FIELD_DEBITS_REV_NUMBER = 77
    const val ISO_FIELD_INQUIRIES_NUMBER = 80
    const val ISO_FIELD_AUTH_NUMBER = 81
    const val ISO_FIELD_CREDITS_AMOUNT = 86
    const val ISO_FIELD_CREDITS_REV_AMOUNT = 87
    const val ISO_FIELD_DEBITS_AMOUNT = 88
    const val ISO_FIELD_DEBITS_REV_AMOUNT = 89

    const val ISO_FIELD_ORIGINAL_DATA = 90
    const val ISO_FIELD_ORIGINAL_DATA_LENGTH = 42

    const val ISO_FIELD_KEY_MGMT_DATA = 96
    const val ISO_FIELD_NET_SETTLEMENT = 97
    const val ISO_FIELD_SETTLEMENT_INST_ID = 99

    const val ISO_FIELD_ADDITIONAL_DATA = 111
    const val ISO_FIELD_ADDITIONAL_DATA_LENGTH = 7

    const val ISO_FIELD_KEY_DATA = 125

    const val ISO_FIELD_ACQ_TRACE_DATA = 127
    const val ISO_FIELD_ACQ_TRACE_DATA_LENGTH = 20

    const val ISO_FIELD_RESPONSE_TEXT = 127


    const val ISO_DATE_FORMAT = "MMddHHmmss"
    const val ISO_TYPE_SIGN_ON: Int = 0x0800



    const val MTI_FINANCIAL_REQ = 0x0200
    const val MTI_REVERSAL_REQ = 0x0420




    // Network Management Request
    const val SIGN_ON_REQUEST = 1
    const val SIGN_OFF_REQUEST = 2
    const val HANDSHAKE_REQUEST = 301
    const val KEY_CHANGE = 101
    const val KEY_CHANGE_REQUEST = 180


    const val ISO_FIELD_PROC_ID_LENGTH = 11

    fun getIsoResponseMessage(code: String): String {
        return when (code) {
            ISO_RESP_CODE_INVALID_VOUCHER_ID -> "INVALID VOUCHER ID"
            ISO_RESP_CODE_INVALID_AUTH_NUMBER -> "INVALID AUTHORIZATION NUMBER"
            ISO_RESP_CODE_APPROVED -> "APPROVED"
            ISO_RESP_CODE_BAD_FNS_STATUS -> "BAD FNS STATUS FOR MERCHANT"
            ISO_RESP_CODE_INVALID_MERCHANT -> "INVALID MERCHANT"
            ISO_RESP_CODE_GENERAL_DENIAL -> "GENERAL DENIAL"
            ISO_RESP_CODE_INVALID_TRANSACTION -> "INVALID TRANSACTION"
            ISO_RESP_CODE_INVALID_TXN_TYPE -> "INVALID TRANSACTION TYPE"
            ISO_RESP_CODE_INVALID_AMOUNT -> "INVALID AMOUNT"
            ISO_RESP_CODE_INVALID_CARD_NUMBER -> "INVALID CARD NUMBER"
            ISO_RESP_CODE_REENTER_TRANSACTION -> "RE-ENTER TRANSACTION"
            ISO_RESP_CODE_UNACCEPTABLE_TXN_FEE -> "UNACCEPTABLE TRANSACTION FEE"
            ISO_RESP_CODE_FORMAT_ERROR -> "FORMAT ERROR"
            ISO_RESP_CODE_INVALID_ISO_PREFIX -> "CARD HAS INVALID ISO PREFIX"
            ISO_RESP_CODE_FUNCTION_NOT_AVAILABLE -> "FUNCTION NOT AVAILABLE"
            ISO_RESP_CODE_LOST_CARD -> "LOST CARD"
            ISO_RESP_CODE_NO_ACCOUNT -> "NO ACCOUNT"
            ISO_RESP_CODE_LOST_STOLEN_CARD -> "LOST/STOLEN CARD"
            ISO_RESP_CODE_INSUFFICIENT_FUNDS -> "INSUFFICIENT FUNDS"
            ISO_RESP_CODE_NO_ACCOUNT_ON_FILE -> "NO ACCOUNT ON FILE"
            ISO_RESP_CODE_EXPIRED_CARD -> "EXPIRED CARD"
            ISO_RESP_CODE_INVALID_PIN -> "INVALID PIN"
            ISO_RESP_CODE_CARD_NUMBER_NOT_FOUND -> "CARD NUMBER NOT FOUND"
            ISO_RESP_CODE_TXN_NOT_PERMITTED_CARDHOLDER -> "TRANSACTION NOT PERMITTED (CARDHOLDER)"
            ISO_RESP_CODE_INVALID_TXN_ALT -> "INVALID TRANSACTION"
            ISO_RESP_CODE_FRAUD_RETURN_CARD -> "FRAUD (RETURN CARD)"
            ISO_RESP_CODE_RETURN_EXCEEDS_AUTH -> "RETURN EXCEEDS BENEFIT AUTHORIZATION"
            ISO_RESP_CODE_RESTRICTED_CARD -> "RESTRICTED CARD"
            ISO_RESP_CODE_PIN_TRIES_EXCEEDED -> "PIN TRIES EXCEEDED"
            ISO_RESP_CODE_INVALID_REVERSAL -> "INVALID REVERSAL"
            ISO_RESP_CODE_VOUCHER_EXPIRED -> "VOUCHER EXPIRED"
            ISO_RESP_CODE_INVALID_SECURITY_CODE -> "INVALID SECURITY CODE"
            ISO_RESP_CODE_PROCESSOR_NOT_LOGGED_ON -> "PROCESSOR NOT LOGGED ON"
            ISO_RESP_CODE_AUTHORIZER_NOT_AVAILABLE -> "AUTHORIZER NOT AVAILABLE (TIME-OUT)"
            ISO_RESP_CODE_TXN_DESTINATION_NOT_FOUND -> "TRANSACTION DESTINATION NOT FOUND"
            ISO_RESP_CODE_SYSTEM_MALFUNCTION -> "SYSTEM MALFUNCTION"
            ISO_RESP_CODE_PIN_NOT_SELECTED_ALT -> "PIN NOT SELECTED"
            ISO_RESP_CODE_PIN_ALREADY_SELECTED -> "PIN ALREADY SELECTED"
            ISO_RESP_CODE_UNMATCHED_VOUCHER_INFO -> "UNMATCHED VOUCHER INFORMATION"
            ISO_RESP_CODE_INVALID_HIP_AMOUNT -> "INVALID HIP AMOUNT"
            else -> "UNKNOWN RESPONSE CODE ($code)"
        }
    }

}