package com.eazypaytech.securityframework.listener

import android.content.Context

interface ISharedPrefReqListener {
    fun getConfigVal(context: Context, key: String) : Any?
    fun setConfigVal(context: Context, key: String, value: Any?)
}