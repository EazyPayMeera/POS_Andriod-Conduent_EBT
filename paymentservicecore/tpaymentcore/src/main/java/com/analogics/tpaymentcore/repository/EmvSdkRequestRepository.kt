package com.analogics.tpaymentcore.repository

import android.content.Context
import com.analogics.tpaymentcore.listener.requestListener.IEmvSdkRequestListener
import com.analogics.tpaymentcore.listener.responseListener.IEmvSdkResponseListener
import com.analogics.tpaymentcore.listener.responseListener.IEmvWrapperResponseListener
import javax.inject.Inject
import kotlin.toString

class EmvSdkRequestRepository @Inject constructor( private var emvWrapper : EmvWrapperRepository) :
    IEmvSdkRequestListener, IEmvWrapperResponseListener {
    private var emvSdkResponseListener: IEmvSdkResponseListener? = null

    override fun initPaymentSDK(
        context: Context,
        iEmvSdkResponseListener: IEmvSdkResponseListener
    ) {
        this.emvSdkResponseListener = iEmvSdkResponseListener
        try {
            emvWrapper.initializeSdk(this)
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

    override fun onEmvWrapperSuccess(uiData: String) {
        onEmvSdkResponse(uiData)
    }

    override fun onEmvWrapperError(uiData: String) {
        onEmvSdkResponse(uiData)
    }

    override fun onEmvWrapperDisplayMessage(uiData: String?) {
        onEmvSdkResponse(uiData?:"")
    }
}