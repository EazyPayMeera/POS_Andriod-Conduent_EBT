package com.analogics.tpaymentcore.repository

import android.content.Context
import com.analogics.tpaymentcore.listener.requestListener.IEmvSdkRequestListener
import com.analogics.tpaymentcore.listener.responseListener.IEmvSdkResponseListener
import javax.inject.Inject
import kotlin.toString

class EmvSdkRequestRepository @Inject constructor(override var iEmvSdkResponseListener: IEmvSdkResponseListener) : IEmvSdkRequestListener {
    private var emvWrapper : EmvWrapperRepository = EmvWrapperRepository (iEmvSdkResponseListener)
    override fun initPaymentSDK(
        context: Context
    ) {
        try {
            emvWrapper.initializeSdk()
        } catch (exception: Exception) {
            onEmvSdkResponse(exception.message.toString())
        }

    }

    override fun startPayment(
        context: Context
    ) {
        try {
            EmvWrapperRepository.startPayment(context,iEmvSdkResponseListener);
        } catch (exception: Exception) {
            onEmvSdkResponse(exception.message.toString())
        }
    }

    override fun onEmvSdkResponse(response: Any) {
        when (response) {
            is String -> {
                iEmvSdkResponseListener?.onEmvSdkSuccess(response)
            }
            else -> {
                iEmvSdkResponseListener?.onEmvSdkError(response.toString())
            }
        }
    }
}