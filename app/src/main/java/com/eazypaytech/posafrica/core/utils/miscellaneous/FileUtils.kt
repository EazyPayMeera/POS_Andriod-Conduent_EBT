package com.eazypaytech.posafrica.core.utils.miscellaneous

import android.content.Context
import com.eazypaytech.paymentservicecore.logger.AppLogger
import java.io.BufferedReader

fun readAsset(context: Context, fileName: String): String? {
    var content : String?=null
    try {
        content = context
            .assets
            .open(fileName)
            .bufferedReader().use(BufferedReader::readText)
    }catch (e : Exception) {
        AppLogger.d(AppLogger.MODULE.APP_UI, e.message ?: "")
    }
    return content
}