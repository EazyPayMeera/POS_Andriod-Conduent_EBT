package com.eazypaytech.networkservicecore.serviceutils

object NetworkConstants {
        const val BASEURL = "https://posuat.services.conduent.com/"
        const val PATH_ACCESS_TOKEN = "/ucp/accesstoken"


        const val KEY_X_GP_VERSION = "X-GP-Version"
        const val VAL_X_GP_VERSION = "2021-03-22"
        const val KEY_GRANT_TYPE = "grant_type"
        const val VAL_GRANT_TYPE_CREDENTIALS = "client_credentials"

        const val DIGEST_ALGORITHM = "SHA-512"

        /* HOST Address */
        //const val HOST_ADDRESS = "posuat.services.conduent.com"
        //const val HOST_PORT = 54811
        var HOST_ADDRESS: String = ""
        var HOST_PORT: Int = 0

        fun updateHost(baseUrl: String?, port: Int?) {
                try {

                        if (baseUrl.isNullOrEmpty()) return

                        val url = java.net.URL(baseUrl)
                        var BASEURL: String = ""
                        BASEURL = baseUrl
                        HOST_ADDRESS = url.host
                        HOST_PORT = port ?: url.port

                } catch (e: Exception) {
                        e.printStackTrace()
                }
        }

}