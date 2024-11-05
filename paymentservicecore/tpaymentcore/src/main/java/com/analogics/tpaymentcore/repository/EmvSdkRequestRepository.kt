package com.analogics.tpaymentcore.repository

import android.content.Context
import com.analogics.tpaymentcore.listener.requestListener.IEmvSdkRequestListener
import com.analogics.tpaymentcore.listener.responseListener.IEmvSdkResponseListener
import com.analogics.tpaymentcore.model.emv.AidConfig
import com.analogics.tpaymentcore.model.emv.CAPKey
import com.analogics.tpaymentcore.model.emv.EmvSdkException
import com.analogics.tpaymentcore.model.emv.TransConfig
import javax.inject.Inject
import kotlin.toString

class EmvSdkRequestRepository @Inject constructor(override var iEmvSdkResponseListener: IEmvSdkResponseListener) : IEmvSdkRequestListener {
    private var emvWrapper = EmvWrapperRepository(iEmvSdkResponseListener)
    override fun initPaymentSDK(
        aidConfig: AidConfig?,
        capKeys: List<CAPKey>?
    ) {
        try {
            emvWrapper.initializeSdk(aidConfig, capKeys)
        } catch (exception: Exception) {
            iEmvSdkResponseListener.onEmvSdkResponse(EmvSdkException(exception.message.toString()))
        }
    }

    override fun startPayment(
        context: Context,
        transConfig: TransConfig?
    ) {
        try {
            EmvWrapperRepository.startPayment(context, transConfig, iEmvSdkResponseListener);
        } catch (exception: Exception) {
            iEmvSdkResponseListener.onEmvSdkResponse(EmvSdkException(exception.message.toString()))
        }
    }

    override fun abortPayment() {
        try {
            EmvWrapperRepository.abortPayment()
        } catch (exception: Exception) {
            iEmvSdkResponseListener.onEmvSdkResponse(EmvSdkException(exception.message.toString()))
        }
    }

}