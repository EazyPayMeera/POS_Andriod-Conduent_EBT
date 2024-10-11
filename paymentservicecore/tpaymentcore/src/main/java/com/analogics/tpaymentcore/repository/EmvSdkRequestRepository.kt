package com.analogics.tpaymentcore.repository

import android.content.Context
import com.analogics.tpaymentcore.listener.requestListener.IEmvSdkRequestListener
import com.analogics.tpaymentcore.listener.responseListener.IEmvSdkResponseListener
import javax.inject.Inject
import kotlin.toString

class EmvSdkRequestRepository @Inject constructor() : IEmvSdkRequestListener {
    private var emvSdkResponseListener: IEmvSdkResponseListener? = null
    private var emvWrapper : EmvWrapperRepository = EmvWrapperRepository { onEmvSdkResponse(it) }
    override fun initPaymentSDK(
        context: Context,
        iEmvSdkResponseListener: IEmvSdkResponseListener
    ) {
        this.emvSdkResponseListener = iEmvSdkResponseListener
        try {
            emvWrapper.initializeSdk()
        } catch (exception: Exception) {
            onEmvSdkResponse(exception.message.toString())
        }

    }

    override fun startPayment(
        context: Context,
        iEmvSdkResponseListener: IEmvSdkResponseListener
    ) {
        this.emvSdkResponseListener = iEmvSdkResponseListener
        try {
            EmvWrapperRepository.startPayment(context,iEmvSdkResponseListener);
        } catch (exception: Exception) {
            onEmvSdkResponse(exception.message.toString())
        }
    }

    override fun onEmvSdkResponse(response: Any) {
        when (response) {
            is String -> {
                emvSdkResponseListener?.onEmvSdkSuccess(response)
            }
            else -> {
                emvSdkResponseListener?.onEmvSdkError(response.toString())
            }
        }
    }
}