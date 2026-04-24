package com.eazypaytech.pos.core.utils.miscellaneous

import android.content.Context
import java.io.BufferedReader

/**
 * Reads a text file from the app's assets folder and returns its content as a String.
 *
 * This utility is used to load static files bundled in the APK assets directory,
 * such as configuration files, templates, or sample data.
 *
 * If the file cannot be read (missing file, I/O error, etc.), the function safely
 * returns null instead of throwing an exception.
 *
 * @param context Android context used to access the assets directory
 * @param fileName Name of the asset file to read (including extension)
 * @return File content as String, or null if reading fails
 */
fun readAsset(context: Context, fileName: String): String? {
    var content : String?=null
    try {
        content = context
            .assets
            .open(fileName)
            .bufferedReader().use(BufferedReader::readText)
    }catch (e : Exception) {
        //AppLogger.d(AppLogger.MODULE.APP_UI, e.message ?: "")
    }
    return content
}