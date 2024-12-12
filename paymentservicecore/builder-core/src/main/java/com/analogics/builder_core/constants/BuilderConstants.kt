package com.analogics.builder_core.constants

object BuilderConstants {
    const val SHARED_PREF_NAME = "BuilderSharedPref"
    const val SHARED_PREF_KEY_STAN = "stan"

    const val DEFAULT_ISO8583_CURRENCY_CODE = "356"
    const val DEFAULT_ISO8583_NII = "0110"
    const val DEFAULT_ISO8583_DATE_TIME_FORMAT = "yyMMddHHmmss"
    const val DEFAULT_ISO8583_DATE_FORMAT = "MMdd"
    const val DEFAULT_ISO8583_TIME_FORMAT = "HHmmss"

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
    const val MIT_VOID_REQ = 0x0220


    /* TPDU */
    val ISO_HEADER = byteArrayOf(0x60.toByte(),0x00.toByte(),0x11.toByte(),0x00.toByte(),0x00.toByte())
    const val ISO_HEADER_LENGTH = 5

    /* PAN */
    const val ISO_FIELD_PAN = 2

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

    /* AMOUNT */
    const val ISO_FIELD_AMOUNT = 4
    const val ISO_FIELD_AMOUNT_LENGTH = 12

    /* STAN */
    const val ISO_FIELD_STAN = 11
    const val ISO_FIELD_STAN_LENGTH = 6
    const val ISO_FIELD_STAN_MAX_VAL = 999999

    /* Time */
    const val ISO_FIELD_TIME = 12
    const val ISO_FIELD_TIME_LENGTH = 6

    /* Date */
    const val ISO_FIELD_DATE = 13
    const val ISO_FIELD_DATE_LENGTH = 4

    /* POS Entry Mode */
    const val ISO_FIELD_POS_ENTRY_MODE = 22
    const val ISO_FIELD_POS_ENTRY_MODE_LENGTH = 3

    /* POS Entry Mode */
    const val ISO_FIELD_PAN_SEQ_NO = 23
    const val ISO_FIELD_PAN_SEQ_NO_LENGTH = 3

    /* NII */
    const val ISO_FIELD_NII = 24
    const val ISO_FIELD_NII_LENGTH = 4

    /* POS Condition Code */
    const val ISO_FIELD_POS_CONDITION_CODE = 25
    const val ISO_FIELD_POS_CONDITION_CODE_LENGTH = 2

    /* Track 2 Data */
    const val ISO_FIELD_TRACK2_DATA = 35
    const val ISO_FIELD_TRACK2_DATA_MAX_LEN = 40

    /* RRN */
    const val ISO_FIELD_RRN = 37
    const val ISO_FIELD_RRN_LENGTH = 12

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
    const val ISO_FIELD_PIN_BLOCK = 52
    const val ISO_FIELD_PIN_BLOCK_LENGTH = 8

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
    const val ISO_RESP_CODE_APPROVED = "00" // APPROVED
    const val ISO_RESP_CODE_CALL_ISSUER = "01" // CALL ISSUER - Refer to card issuer
    const val ISO_RESP_CODE_CALL_ISSUER_SPECIAL = "02" // CALL ISSUER - Refer to card issuer, special condition
    const val ISO_RESP_CODE_INVALID_MERCHANT = "03" // INVALID MERCHANT - CONTACT LYRA # 03
    const val ISO_RESP_CODE_DECLINED_PICKUP_CARD = "04" // DECLINED PICK UP CARD - Capture card
    const val ISO_RESP_CODE_DO_NOT_HONOR = "05" // DO NOT HONOR TRANS DECLINED
    const val ISO_RESP_CODE_ERROR_MERCHANT = "06" // ERROR/MERCHANT ERROR - CONTACT LYRA # 06
    const val ISO_RESP_CODE_PICKUP_SPECIAL = "07" // PICK UP # 07 - Pickup card, special condition (other than lost/stolen card)
    const val ISO_RESP_CODE_APPROVED_VERIFY_ID = "08" // APPROVED VERIFY ID & SIGNATURE - Honor with ID
    const val ISO_RESP_CODE_APPROVED_PARTIAL = "10" // APPROVED IN PART - Partial Approval
    const val ISO_RESP_CODE_INVALID_TXN = "12" // INVALID TXN - Invalid transaction
    const val ISO_RESP_CODE_INVALID_AMOUNT = "13" // INVALID AMOUNT - Invalid amount
    const val ISO_RESP_CODE_DECLINED_INVALID_CARD = "14" // DECLINED # 14 - Invalid card number
    const val ISO_RESP_CODE_DECLINED_INVALID_ISSUER = "15" // DECLINED # 15 - Invalid issuer
    const val ISO_RESP_CODE_DECLINED_CUSTOMER_CANCEL = "17" // DECLINED # 17 - Customer cancellation
    const val ISO_RESP_CODE_DECLINED_REENTER_TXN = "19" // DECLINED # 19 - Re-enter transaction
    const val ISO_RESP_CODE_INVALID_RESPONSE = "20" // INVALID RESPONSE - Invalid response
    const val ISO_RESP_CODE_RETRY_NO_ACTION = "21" // RETRY # 21 - No action taken
    const val ISO_RESP_CODE_RETRY_SUSPECTED_MALFUNCTION = "22" // RETRY # 22 - Suspected malfunction
    const val ISO_RESP_CODE_RETRY_UNABLE_LOCATE_RECORD = "25" // RETRY # 25 - Unable to locate record
    const val ISO_RESP_CODE_RETRY_FILE_UPDATE_FIELD_ERROR = "27" // RETRY # 27 - File Update field edit error
    const val ISO_RESP_CODE_RETRY_RECORD_ALREADY_EXISTS = "28" // RETRY # 28 - Record already exists in the file
    const val ISO_RESP_CODE_RETRY_FILE_UPDATE_NOT_SUCCESSFUL = "29" // RETRY # 29 - File Update not successful
    const val ISO_RESP_CODE_FORMAT_ERROR = "30" // FORMAT ERROR - PACKET / FORMAT ERROR (Mandatory fields absent or invalid characters)
    const val ISO_RESP_CODE_RETRY_UNSUPPORTED_BANK = "31" // RETRY # 31 - Bank not supported by switch
    const val ISO_RESP_CODE_RETRY_PARTIAL_REVERSAL = "32" // RETRY # 32 - Partial Reversal
    const val ISO_RESP_CODE_EXPIRED_CARD = "33" // EXPIRED CARD # 33 - Expired card, capture
    const val ISO_RESP_CODE_DECLINED_SUSPECTED_FRAUD = "34" // DECLINED # 34 - Suspected fraud, capture
    const val ISO_RESP_CODE_RESTRICTED_CARD = "36" // RESTRICTED CARD - Restricted card, capture
    const val ISO_RESP_CODE_EXCESS_PIN_TRIES = "38" // EXCESS PIN TRIES - Allowable PIN tries exceeded, capture
    const val ISO_RESP_CODE_DECLINED_NO_CREDIT_ACCOUNT = "39" // DECLINED # 39 - No credit account
    const val ISO_RESP_CODE_DECLINED_UNSUPPORTED_FUNCTION = "40" // DECLINED # 40 - Requested function not supported
    const val ISO_RESP_CODE_PICKUP_LOST_CARD = "41" // PICK UP # 41 - Lost card, capture
    const val ISO_RESP_CODE_DECLINED_NO_UNIVERSAL_ACCOUNT = "42" // DECLINED # 42 - No universal account
    const val ISO_RESP_CODE_PICKUP_STOLEN_CARD = "43" // PICK UP # 43 - Stolen card, capture
    const val ISO_RESP_CODE_DECLINED_INSUFFICIENT_FUNDS = "51" // DECLINED # 51 - Insufficient funds/over credit limit
    const val ISO_RESP_CODE_NO_CHECKING_ACCOUNT = "52" // NO CHK ACCT - No checking account
    const val ISO_RESP_CODE_NO_SAVINGS_ACCOUNT = "53" // NO SAVINGS ACCT - No savings account
    const val ISO_RESP_CODE_EXPIRED_CARD_CHECK = "54" // EXPIRED CARD - Expired card (if Expiry Date check is enabled for BIN range)
    const val ISO_RESP_CODE_INCORRECT_PIN = "55" // INCORRECT PIN - Invalid PIN
    const val ISO_RESP_CODE_DECLINED_NOT_PERMITTED_TO_CARDHOLDER = "57" // DECLINED # 57 - Transaction not permitted to Cardholder
    const val ISO_RESP_CODE_DECLINED_NOT_PERMITTED_TO_TERMINAL = "58" // DECLINED # 58 - Transaction not permitted to terminal
    const val ISO_RESP_CODE_DECLINED_SUSPECTED_FRAUD_ALT = "59" // DECLINED # 59 - Suspected fraud
    const val ISO_RESP_CODE_CONTACT_ACQUIRER_DECLINE = "60" // DECLINED # 60 - Card acceptor contact acquirer, decline
    const val ISO_RESP_CODE_EXCEEDS_WITHDRAWAL_LIMIT = "61" // EXCEEDS AMT LMT - Exceeds withdrawal amount limit
    const val ISO_RESP_CODE_RESTRICTED_CARD_CAPTURE = "62" // RESTRICTED CARD - Restricted card
    const val ISO_RESP_CODE_SECURITY_VIOLATION = "63" // SECURITY ERR # 63 - Security violation
    const val ISO_RESP_CODE_RETRY_AML_REQUIREMENT = "64" // RETRY # 64 - Transaction does not fulfill AML requirement (Stand-In Processing)
    const val ISO_RESP_CODE_EXCEEDS_WITHDRAWAL_FREQUENCY = "65" // EXCEEDS COUNT - Exceeds withdrawal frequency limit
    const val ISO_RESP_CODE_DECLINED_CONTACT_ACQUIRER = "66" // DECLINE # 66 - Card acceptor calls acquirer
    const val ISO_RESP_CODE_PICKUP_HARD_CAPTURE = "67" // PICK UP # 67 - Hard capture (requires card pickup at ATM)
    const val ISO_RESP_CODE_RETRY_ACQUIRER_TIMEOUT = "68" // RETRY # 68 - Acquirer time-out
    const val ISO_RESP_CODE_RETRY_MOBILE_RECORD_NOT_FOUND = "69" // RETRY # 69 - Mobile number record not found/mis-match
    const val ISO_RESP_CODE_RETRY_CONTACT_CARD_ISSUER = "70" // RETRY # 70 - Contact Card Issuer
    const val ISO_RESP_CODE_RETRY_DEEMED_ACCEPTANCE = "71" // RETRY # 71 - Deemed Acceptance / PIN Not Changed
    const val ISO_RESP_CODE_RETRY_ISSUER_RISK_DECLINE = "74" // RETRY # 74 - Transactions declined by Issuer based on Risk Score
    const val ISO_RESP_CODE_EXCEEDED_PIN_TRIES = "75" // EX-PIN TRIES # 75 - Allowable number of PIN tries exceeded
    const val ISO_RESP_CODE_PVT_ERROR_TO_ACCOUNT = "76" // PVT ERROR # 76 - MC: Invalid/nonexistent "To Account"; VISA: No match on Retrieval Reference number
    const val ISO_RESP_CODE_PVT_ERROR_FROM_ACCOUNT = "77" // PVT ERROR # 77 - MC: Invalid/nonexistent "From Account"; VISA: Inconsistent repeat/reversal data
    const val ISO_RESP_CODE_PVT_ERROR_GENERAL_ACCOUNT = "78" // PVT ERROR # 78 - MC: Invalid/nonexistent account; VISA: "Blocked, first used"
    const val ISO_RESP_CODE_TRANSACTION_REVERSED = "79" // TRANSACTION REVERSED - Transaction already reversed
    const val ISO_RESP_CODE_DUPLICATE_BATCH = "80" // BAD BATCH NO # 80 - Duplicate Batch
    const val ISO_RESP_CODE_PVT_ERROR_DOMESTIC_DEBIT = "81" // PVT ERROR # 81 - MC: Domestic Debit not allowed; VISA: PIN cryptographic error (Stand-In Processing)
    const val ISO_RESP_CODE_TIMEOUT_AT_ISSUER = "82" // TIMEOUT AT ISSUER - Timeout at issuer
    const val ISO_RESP_CODE_HSM_KEY_ERROR = "83" // HSM KEY ERROR - Key Error. Initiate Key Injection
    const val ISO_RESP_CODE_DECLINED_INVALID_AUTH_LIFE_CYCLE = "84" // DECLINED # 84 - Invalid Authorization Life Cycle
    const val ISO_RESP_CODE_PVT_ERROR_ZERO_AMOUNT = "85" // PVT ERROR # 85 - Not declined, valid for all zero amount transactions
    const val ISO_RESP_CODE_PVT_ERROR_PIN_VALIDATION = "86" // PVT ERROR # 86 - PIN Validation not possible
    const val ISO_RESP_CODE_PVT_ERROR_NO_CASH_BACK = "87" // PVT ERROR # 87 - Purchase Amount Only, No Cash Back Allowed
    const val ISO_RESP_CODE_CRYPTOGRAPHIC_FAILURE = "88" // CRYPTOGRAPHIC FAILURE - Cryptographic failure
    const val ISO_RESP_CODE_PVT_ERROR_UNACCEPTABLE_PIN = "89" // PVT ERROR # 89 - Unacceptable PIN - "Transaction Declined" Retry
    const val ISO_RESP_CODE_CUT_OFF_IN_PROCESS = "90" // CUT OFF IN PROCESS - Cut-off is in process
    const val ISO_RESP_CODE_TRY_AFTER_5MIN_AUTH_SYS_INOPERATIVE = "91" // TRY AFTER 5MIN # 91 - Authorization System or issuer system inoperative
    const val ISO_RESP_CODE_TRY_AFTER_5MIN_UNABLE_ROUTE = "92" // TRY AFTER 5MIN # 92 - Unable to route transaction
    const val ISO_RESP_CODE_DECLINED_VIOLATION_OF_LAW = "93" // DECLINED # 93 - Transaction cannot be completed; violation of law
    const val ISO_RESP_CODE_PVT_ERROR_DUPLICATE_TRANS = "94" // PVT ERROR # 94 - Duplicate transmission
    const val ISO_RESP_CODE_TOTALS_MISMATCH = "95" // TOTALS MISMATCH - Totals Mismatch (Only for Settlements); Reconcile error
    const val ISO_RESP_CODE_SYSTEM_ERROR = "96" // SYSTEM ERROR - System malfunction
    const val ISO_RESP_CODE_CVV2_FAILURE = "N7" // CVV2 FAILURE - Decline for CVV2 failure
    const val ISO_RESP_CODE_OFFLINE_APPROVED_1 = "Y1" // OFFLINE APPROVED 1 - EMV transaction offline approved by terminal
    const val ISO_RESP_CODE_OFFLINE_APPROVED_3 = "Y3" // OFFLINE APPROVED 3 - EMV transaction unable to go online; offline approved by terminal
    const val ISO_RESP_CODE_OFFLINE_DECLINED_1 = "Z1" // OFFLINE DECLINED 1 - EMV transaction offline declined at terminal
    const val ISO_RESP_CODE_OFFLINE_DECLINED_3 = "Z3" // OFFLINE DECLINED 3 - EMV transaction unable to go online; offline declined at terminal
    const val ISO_RESP_CODE_AAC_GENERATED_BY_CHIP = "E1" // AAC Generated by the Chip card
    const val ISO_RESP_CODE_REVERSAL_RESPONSE_CODE = "E2" // Reversal Response code - Terminal does not receive final application cryptogram from the chip

}