package com.eazypaytech.paymentservicecore.constants

object AppConstants {
    /* Acquirers Supported */
    const val ACQUIRER_LYRA = "LYRA"

    /* Key constants */
    const val NAV_KEY_CUSTOM_TIP_AMOUNT = "customTipAmount"
    const val NAV_KEY_TAX_TYPE = "taxType"
    const val NAV_KEY_TIP_PERCENT_INDEX = "tipPercentIndex"

    /* Value constants */
    const val DEFAULT_UI_LANGUAGE_CODE = "en"
    const val DEFAULT_CURRENCY_CODE = "0356"    /* INR */
    const val DEFAULT_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss"
    const val DEFAULT_DATE_TIME_FORMAT1 = "yyyy-MM-dd HH:mm"
    const val UNIQUE_ID_DATE_TIME_FORMAT = "yyMMddHHmmss"
    const val SPLASH_SCREEN_TIMEOUT_MS = 3000L
    const val TRAINING_MODE_BLINK_DELAY_MS = 1000L
    const val MIN_DIALOG_SHOW_TIME_MS = 500L
    const val CARD_CHECK_TIMEOUT_S = 30L
    const val CARD_CHECK_RESTART_DELAY_MS = 1000L
    const val DEMO_MODE_PROMPTS_DELAY_MS = 3000L
    const val AUTO_PRINT_RECEIPT_DELAY_MS = 1000L

    /* UI */
    const val BUTTON_CLICK_EFFECT_MS = 50L

    /* Configuration Events */
    const val BUTTON_CLICK_EVENT_SET_LANGUAGE =         "EVT_SET_LANGUAGE"
    const val BUTTON_CLICK_EVENT_USER_MANAGEMENT =      "EVT_USER_MANAGEMENT"
    const val BUTTON_CLICK_EVENT_CONFIGURATION =        "EVT_CONFIGURATION"
    const val BUTTON_CLICK_EVENT_RE_ACTIVATE_DEVICE =   "EVT_REACTIVATE_DEVICE"
    const val BUTTON_CLICK_EVENT_LOGOUT =               "EVT_LOGOUT"

    /* Dashboard Events */
    const val BUTTON_CLICK_EVENT_PURCHASE =             "EVT_PURCHASE"
    const val BUTTON_CLICK_EVENT_REFUND =               "EVT_REFUND"
    const val BUTTON_CLICK_EVENT_PREAUTH =              "EVT_PREAUTH"
    const val BUTTON_CLICK_EVENT_AUTH_CAPTURE =         "EVT_AUTH_CAPTURE"
    const val BUTTON_CLICK_EVENT_VOID =                 "EVT_VOID"
    const val BUTTON_CLICK_EVENT_TRANSACTIONS =         "EVT_TRANSACTIONS"

    const val BATCH_ID_START_VAL = 1

    const val DEFAULT_DEMO_MODE = false
    const val DEFAULT_TIP_MODE = true
    const val DEFAULT_TAX_MODE = true
    const val DEFAULT_INVOICE_MODE = true

    const val DEFAULT_HEADER_1 = "Bank Name"
    const val DEFAULT_HEADER_2 = "Address Line 1"
    const val DEFAULT_HEADER_3 = "Address Line 2"
    const val DEFAULT_HEADER_4 = "Phone Number"

    const val DEFAULT_FOOTER_1 = ""
    const val DEFAULT_FOOTER_2 = "THANK YOU"
    const val DEFAULT_FOOTER_3 = "VISIT AGAIN!"
    const val DEFAULT_FOOTER_4 = ""

    const val DEFAULT_TIP_PERCENT_1 = 5
    const val DEFAULT_TIP_PERCENT_2 = 10
    const val DEFAULT_TIP_PERCENT_3 = 15

    const val DEFAULT_TAX_PERCENT_SGST = 0.00
    const val DEFAULT_TAX_PERCENT_VAT = 14.00

    const val MIN_LENGTH_PASSWORD = 6
    const val MAX_LENGTH_TID = 8
    const val MAX_LENGTH_MID = 15

    const val NAV_VAL_TAX_TYPE_SGST = "SGST"
    const val NAV_VAL_TAX_TYPE_VAT = "VAT"

    const val DEFAULT_EMV_CONFIG_FILE_PATH = "EmvConfig.json"
    const val DEFAULT_EMV_CAP_KEY_FILE_PATH = "EmvCAPKeys.json"
    const val EMV_CAP_KEY_ARRAY_FIELD_NAME = "CAPKeys"

    /* Acquirer Specific Constants */
    const val LYRA_MAX_INVOICE_LENGTH = 6
    const val LYRA_CUSTOMER_CARE = "(020) 123 456 7890"
}