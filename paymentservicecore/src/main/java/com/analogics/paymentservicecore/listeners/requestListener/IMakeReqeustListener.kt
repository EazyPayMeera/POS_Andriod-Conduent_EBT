package com.analogics.paymentservicecore.listeners.requestListener

import android.content.Context
import com.analogics.paymentservicecore.listeners.responseListener.IResultProviderListener
import com.analogics.paymentservicecore.listeners.rootListener.IOnRootAppListener


interface IMakeReqeustListener {
    suspend fun getUserListAPI(iResultProviderListener: IResultProviderListener)

     fun  initPayment(iOnRootAppListener: IOnRootAppListener, context: Context)
}