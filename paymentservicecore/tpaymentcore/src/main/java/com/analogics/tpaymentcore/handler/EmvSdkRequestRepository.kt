package com.analogics.tpaymentcore.handler

import android.content.Context
import com.analogics.tpaymentcore.EMV.EmvWrapper
import com.analogics.tpaymentcore.listener.IEmvSdkRequestListener
import com.analogics.tpaymentcore.listener.IEmvSdkResponseListener
import javax.inject.Inject

class EmvSdkRequestRepository @Inject constructor() : IEmvSdkRequestListener {
    private var emvSdkResponseListener: IEmvSdkResponseListener? = null
    override fun initPaymentSDK(
        context: Context,
        iEmvSdkResponseListener: IEmvSdkResponseListener
    ) {
        this.emvSdkResponseListener = iEmvSdkResponseListener
        try {
            EmvWrapper.initialize(context);
            onEmvSdkResponse("SUCCESS")
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
            EmvWrapper.startPayment(context,iEmvSdkResponseListener);
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