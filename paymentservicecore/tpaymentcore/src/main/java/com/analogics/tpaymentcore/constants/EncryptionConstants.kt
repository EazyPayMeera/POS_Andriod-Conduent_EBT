package com.analogics.tpaymentcore.constants

object EncryptionConstants {

    /* Key ID */
    const val KEY_INDEX_MAIN_KEY = 1

    /* Master Session Key Type */
    const val MS_KEY_TYPE_MAIN = 0
    const val MS_KEY_TYPE_MAC = 1
    const val MS_KEY_TYPE_PIN = 2
    const val MS_KEY_TYPE_TDK = 3
    const val MS_KEY_TYPE_ENC_DEC = 4

    /* DUKPT Key Type */
    const val DUKPT_KEY_TYPE_PIN = 1
    const val DUKPT_KEY_TYPE_MAC = 2
    const val DUKPT_KEY_TYPE_TRACK_DATA = 3

    /* DUKPT Key Set */
    const val DUKPT_KEY_SET_TDK = 1
    const val DUKPT_KEY_SET_EMV = 2
    const val DUKPT_KEY_SET_PIN = 3
    const val DUKPT_KEY_SET_MAC = 4

    /* KSN */
    const val DUKPT_KSN_MIN_LENGTH = 16
    const val DUKPT_KSN_MAX_LENGTH = 20

    /* DES Mode */
    const val DES_MODE_ENCRYPT = 0
    const val DES_MODE_DECRYPT = 1

    /* DES Algorithm */
    const val DES_ALG_ECB = 1
    const val DES_ALG_CBC = 2
    const val DES_ALG_SM4 = 3
    const val DES_ALG_AES_ECB = 7
    const val DES_ALG_AES_CBC = 8

}
