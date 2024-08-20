package com.analogics.paymentservicecore.listeners.requestListener

import android.content.Context
import com.analogics.paymentservicecore.listeners.responseListener.IResultProviderListener


interface IMakeRequestListener {
    suspend fun getUserListAPI(iResultProviderListener: IResultProviderListener)
    suspend fun initPaymentSDK(context: Context, iResultProviderListener: IResultProviderListener)
}