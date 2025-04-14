package com.eazypaytech.tpaymentcore.repository

import android.content.Context
import com.eazypaytech.tpaymentcore.listener.requestListener.IEmvSdkRequestListener
import com.eazypaytech.tpaymentcore.listener.responseListener.IEmvSdkResponseListener
import com.eazypaytech.tpaymentcore.model.emv.AidConfig
import com.eazypaytech.tpaymentcore.model.emv.CAPKey
import com.eazypaytech.tpaymentcore.model.emv.EmvSdkException
import com.eazypaytech.tpaymentcore.model.emv.TransConfig
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

    override fun getEmvTag(tag: String?): String? {
        return try {
            EmvWrapperRepository.getEmvTag(tag)
        }catch (exception: Exception)
        {
            exception.printStackTrace()
            null
        }
    }
}