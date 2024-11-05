package com.analogics.networkservicecore.serviceutils

object NetworkConstants {
        const val BASEURL = "https://apis.sandbox.globalpay.com"
        const val PATH_ACCESS_TOKEN = "/ucp/accesstoken"


        const val KEY_X_GP_VERSION = "X-GP-Version"
        const val VAL_X_GP_VERSION = "2021-03-22"
        const val KEY_GRANT_TYPE = "grant_type"
        const val VAL_GRANT_TYPE_CREDENTIALS = "client_credentials"

        const val DIGEST_ALGORITHM = "SHA-512"

        /* Processing Code */
        const val ISO_FIELD_PROC_CODE = 3
        const val ISO_FIELD_PROC_CODE_LENGTH = 6
        const val PROC_CODE_RKL_PART_SN = 990380
        const val PROC_CODE_RKL_FULL_SN = 991380

        /* STAN */
        const val ISO_FIELD_STAN = 11
        const val ISO_FIELD_STAN_LENGTH = 6

        /* Time */
        const val ISO_FIELD_TIME = 12
        const val ISO_FIELD_TIME_LENGTH = 6

        /* Date */
        const val ISO_FIELD_DATE = 13
        const val ISO_FIELD_DATE_LENGTH = 4

        /* NII */
        const val ISO_FIELD_NII = 24
        const val ISO_FIELD_NII_LENGTH = 3

        /* TID */
        const val ISO_FIELD_TID = 41
        const val ISO_FIELD_TID_LENGTH = 8

        /* MID */
        const val ISO_FIELD_MID = 42
        const val ISO_FIELD_MID_LENGTH = 15

        /* TERM SR NO */
        const val ISO_FIELD_TERM_SR_NO = 60

        /* WORKING KEY */
        const val ISO_FIELD_WORKING_KEY = 62
}