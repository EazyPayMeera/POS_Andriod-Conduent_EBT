package com.eazypaytech.paymentservicecore.constants

object EmvConstants {

    const val EMV_TAG_AID_CARD =                    "4F"

    const val EMV_TAG_TRACK2 =                      "57"
    const val EMV_TAG_TRACK2_HEX =                  0x57
    const val EMV_TAG_PAN =                         "5A"
    const val EMV_TAG_PAN_HEX =                     0x5A

    /* End to End Encryption */
    const val EMV_TAG_ENC_TRACK =                   "FF01"
    const val EMV_TAG_ENC_KSN =                     "FF02"
    const val EMV_TAG_ENC_PAN =                     "FF03"
    const val EMV_TAG_ENC_PIN_BLOCK =               "FF04"

    const val EMV_TAG_LANG_PREF =                   "5F2D"
    const val EMV_TAG_CARD_COUNTRY_CODE =           "5F28"
    const val EMV_TAG_TRANS_CURRENCY_EXPONENT =     "5F36"

    const val EMV_TAG_MERCH_CATEGORY_CODE =         "9F15"
    const val EMV_TAG_MERCH_ID =                    "9F16"
    const val EMV_TAG_TERM_COUNTRY_CODE =           "9F1A"
    const val EMV_TAG_TERM_ID =                     "9F1C"
    const val EMV_TAG_IFD_SERIAL_NO =               "9F1E"
    const val EMV_TAG_TERM_CAP =                    "9F33"
    const val EMV_TAG_TERM_TYPE =                   "9F35"
    const val EMV_TAG_ADDL_TERM_CAP =               "9F40"
    const val EMV_TAG_MERCH_NAME_LOC =              "9F4E"

    /* Default Values */
    const val EMV_DEFAULT_CTLS_RDR_LIMIT =          "999999999999"
    const val EMV_HASH_ALG_SHA1 =                   "01"

    /* Custom SDK Tags */
    const val EMV_TAG_SUPPORT_RANDOM_TRANS =         "DF02"
    const val EMV_TAG_SUPPORT_EXCEP_FILE_CHECK =     "DF03"
    const val EMV_TAG_SUPPORT_SM =                   "DF04"
    const val EMV_TAG_SUPPORT_VELOCITY_CHECK =       "DF05"

    /* MoreFun Specific values */
    const val MF_SERVICE_PACKAGE =                      "com.morefun.ysdk"
    const val MF_SERVICE_ACTION =                       "com.morefun.ysdk.service"
    const val UROVO_SDK_KEY_EMV_DATA =                  "EMVDATA"
    const val UROVO_SDK_KEY_MSR_DATA =                  "StripInfo"
    const val UROVO_SDK_KEY_MSR_TRACK1 =                "D1"
    const val UROVO_SDK_KEY_MSR_TRACK2 =                "D2"
    const val UROVO_SDK_EMV_LOG_DISABLE   =             0
    const val UROVO_SDK_EMV_LOG_ENABLE   =              1

    const val UROVO_SDK_PRINTER_KEY_FONT_SIZE   =           "fontSize"
    const val UROVO_SDK_PRINTER_KEY_FONT_NAME   =           "fontName"
    const val UROVO_SDK_PRINTER_KEY_ALIGN   =               "align"
    const val UROVO_SDK_PRINTER_KEY_BOLD_FONT   =           "fontBold"
    const val UROVO_SDK_PRINTER_KEY_NEW_LINE   =            "newline"
    const val UROVO_SDK_PRINTER_KEY_LINE_HEIGHT   =         "lineHeight"

    const val UROVO_SDK_PRINTER_VAL_FONT_SIZE_EXTRA_SMALL = 12
    const val UROVO_SDK_PRINTER_VAL_FONT_SIZE_SMALL =       16
    const val UROVO_SDK_PRINTER_VAL_FONT_SIZE_MEDIUM =      24
    const val UROVO_SDK_PRINTER_VAL_FONT_SIZE_LARGE =       32
    const val UROVO_SDK_PRINTER_VAL_FONT_SIZE_EXTRA_LARGE = 40

    const val UROVO_SDK_PRINTER_VAL_FONT_NAME_SIMSUN =      "simsun"

    const val UROVO_SDK_PRINTER_VAL_ALIGN_LEFT =            0
    const val UROVO_SDK_PRINTER_VAL_ALIGN_CENTER =          1
    const val UROVO_SDK_PRINTER_VAL_ALIGN_RIGHT =           2

    const val UROVO_SDK_PRINTER_VAL_LINE_HEIGHT_EXTRA_SMALL =   1
    const val UROVO_SDK_PRINTER_VAL_LINE_HEIGHT_SMALL =         2
    const val UROVO_SDK_PRINTER_VAL_LINE_HEIGHT_MEDIUM =        3
    const val UROVO_SDK_PRINTER_VAL_LINE_HEIGHT_LARGE =         4
    const val UROVO_SDK_PRINTER_VAL_LINE_HEIGHT_EXTRA_LARGE =   5

    const val UROVO_SDK_PRINTER_RET_SUCCESS =               0x00
    const val UROVO_SDK_PRINTER_RET_OUT_OF_PAPER =          0xF0
    const val UROVO_SDK_PRINTER_RET_OVERHEAT =              0xF3
    const val UROVO_SDK_PRINTER_RET_LOW_POWER =             0xE1
    const val UROVO_SDK_PRINTER_RET_BUSY =                  0xF7
    const val UROVO_SDK_PRINTER_RET_ERROR =                 0xFB
    const val UROVO_SDK_PRINTER_RET_HARDWARE_ERROR =        0xF2

    /* Host Response */
    const val EMV_TAG_RESP_CODE =                           "8A"
    const val EMV_TAG_VAL_APPROVED_ONLINE =                 "3030"
    const val EMV_TAG_VAL_DECLINED_ONLINE =                 "3035"
    const val EMV_TAG_VAL_UNABLE_TO_GO_ONLINE_APPROVE =     "5933"
    const val EMV_TAG_VAL_UNABLE_TO_GO_ONLINE_DECLINE =     "5A33"
}