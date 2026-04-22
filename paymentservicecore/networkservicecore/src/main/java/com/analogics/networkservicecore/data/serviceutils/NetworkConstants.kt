package com.analogics.networkservicecore.data.serviceutils

object NetworkConstants {
        const val BASEURL = "https://posuat.services.conduent.com/"
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