package com.analogics.paymentservicecore.listeners.requestListener

import com.analogics.paymentservicecore.listeners.responseListener.IResultProviderListener


interface IMakeReqeustListener {
    suspend fun getUserListAPI(iResultProviderListener: IResultProviderListener)
}