package com.analogics.paymentservicecore.constants

object EmvConstants {

    const val EMV_TAG_TRACK2 =                      "57"
    const val EMV_TAG_TRACK2_HEX =                  0x57
    const val EMV_TAG_PAN =                         "5A"
    const val EMV_TAG_PAN_HEX =                     0x5A

    /* End to End Encryption */
    const val EMV_KEY_ENC_TRACK =                   "ENC_TRACK"
    const val EMV_KEY_ENC_PAN =                     "ENC_PAN"
    const val EMV_KEY_ENC_KSN =                     "ENC_KSN"

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

    /* Custom SDK Tags */
    const val EMV_TAG_SUPPORT_RANDOM_TRANS =         "DF02"
    const val EMV_TAG_SUPPORT_EXCEP_FILE_CHECK =     "DF03"
    const val EMV_TAG_SUPPORT_SM =                   "DF04"
    const val EMV_TAG_SUPPORT_VELOCITY_CHECK =       "DF05"

    /* Host Response */
    const val EMV_TAG_RESP_CODE =                     "8A"
    const val EMV_TAG_VAL_APPROVED_ONLINE =                 "3030"
    const val EMV_TAG_VAL_DECLINED_ONLINE =                 "3035"
    const val EMV_TAG_VAL_UNABLE_TO_GO_ONLINE_APPROVE =     "5933"
    const val EMV_TAG_VAL_UNABLE_TO_GO_ONLINE_DECLINE =     "5A33"
}