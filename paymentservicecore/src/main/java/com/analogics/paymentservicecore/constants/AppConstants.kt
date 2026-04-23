package com.eazypaytech.paymentservicecore.constants

object AppConstants {

    /* Acquirers Supported */
    const val ACQUIRER_CONDUENT = "CONDUENT"


    /* Value constants */
    const val DEFAULT_UI_LANGUAGE_CODE = "en"
    const val DEFAULT_CURRENCY_CODE = "0840"    /* INR */
    const val DEFAULT_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss"
    const val UNIQUE_ID_DATE_TIME_FORMAT = "yyMMddHHmmss"
    const val SPLASH_SCREEN_TIMEOUT_MS = 3000L
    const val TRAINING_MODE_BLINK_DELAY_MS = 1000L
    const val CARD_CHECK_TIMEOUT_S = 30L
    const val CARD_CHECK_RESTART_DELAY_MS = 1000L
    const val AUTO_PRINT_RECEIPT_DELAY_MS = 1000L

    /* UI */
    const val BUTTON_CLICK_EFFECT_MS = 50L

    /* Configuration Events */
    const val BUTTON_CLICK_EVENT_SET_LANGUAGE =         "EVT_SET_LANGUAGE"
    const val BUTTON_CLICK_EVENT_USER_MANAGEMENT =      "EVT_USER_MANAGEMENT"
    const val BUTTON_CLICK_EVENT_KEY_MAN =        "EVT_KEY_MAN"
    const val BUTTON_CLICK_EVENT_CONFIGURATION =        "EVT_CONFIGURATION"
    const val BUTTON_CLICK_EVENT_RE_ACTIVATE_DEVICE =   "EVT_REACTIVATE_DEVICE"
    const val BUTTON_CLICK_EVENT_LOGOUT =               "EVT_LOGOUT"

    /* Dashboard Events */
    const val BUTTON_CLICK_EVENT_FOOD_PURCHASE =    "EVT_FOOD_PURCHASE"
    const val BUTTON_CLICK_EVENT_FOODSTAMP_RETURN = "EVT_FOODSTAMP_RETURN"
    const val BUTTON_CLICK_EVENT_PURCHASE_CASHBACK = "EVT_PURCHASE_CASHBACK"
    const val BUTTON_CLICK_EVENT_CASH_WITHDRAW = "EVT_CASH_WITHDRAW"
    const val BUTTON_CLICK_EVENT_E_VOUCHER =        "EVT_E_VOUCHER"
    const val BUTTON_CLICK_EVENT_VOID_LAST =        "EVT_VOID_LAST"
    const val BUTTON_CLICK_EVENT_BALANCE_ENQUIRY =  "EVT_BALANCE_ENQUIRY"


    const val CARD_RETRY_COUNT = 2


    /* Receipt Constants */
    const val DEFAULT_HEADER_1 = "Bank Name"
    const val DEFAULT_HEADER_2 = "Address Line 1"
    const val DEFAULT_HEADER_3 = "Address Line 2"
    const val DEFAULT_HEADER_4 = "Phone Number"

    const val DEFAULT_FOOTER_1 = ""
    const val DEFAULT_FOOTER_2 = "THANK YOU"
    const val DEFAULT_FOOTER_3 = "VISIT AGAIN!"
    const val DEFAULT_FOOTER_4 = ""

    const val DEFAULT_RECEIPT_DATE_FORMAT = "yy/MM/dd"
    const val DEFAULT_RECEIPT_TIME_FORMAT = "HH:mm:ss"


    const val MIN_LENGTH_PASSWORD = 6
    const val MAX_LENGTH_PROC_ID = 11
    const val MAX_LENGTH_CARD_NO = 16
    const val MAX_LENGTH_AUTH_CODE = 6
    const val MAX_LENGTH_MID = 15
    const val MAX_LENGTH_TID = 8

    const val DEFAULT_EMV_CONFIG_FILE_PATH = "EmvConfig.json"
    const val DEFAULT_EMV_CAP_KEY_FILE_PATH = "EmvCAPKeys.json"
    const val EMV_CAP_KEY_ARRAY_FIELD_NAME = "CAPKeys"

    /* Acquirer Specific Constants */
    const val CONDUENT_CUSTOMER_CARE = "(020) 123 456 7890"
}