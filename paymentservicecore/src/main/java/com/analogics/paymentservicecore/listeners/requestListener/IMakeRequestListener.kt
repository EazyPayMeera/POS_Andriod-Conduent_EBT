package com.analogics.paymentservicecore.listeners.requestListener

import com.analogics.paymentservicecore.listeners.responseListener.IResultProviderListener


interface IMakeRequestListener {
    suspend fun getUserListAPI(iResultProviderListener: IResultProviderListener)
}