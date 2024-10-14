package com.analogics.paymentservicecore.repository.emvService

import android.content.Context
import com.analogics.paymentservicecore.listeners.requestListener.IEmvServiceRequestListener
import com.analogics.paymentservicecore.listeners.responseListener.IEmvServiceResponseListener
import com.analogics.paymentservicecore.model.error.EmvServiceError
import com.analogics.tpaymentcore.listener.responseListener.IEmvSdkResponseListener
import com.analogics.tpaymentcore.repository.EmvSdkRequestRepository
import kotlinx.coroutines.delay
import javax.inject.Inject

class EmvServiceRepository @Inject constructor() :
    IEmvServiceRequestListener,
    IEmvSdkResponseListener {
    private val emvSdkRequestRepository = EmvSdkRequestRepository(this)
    lateinit var iEmvServiceResponseListener: IEmvServiceResponseListener
    lateinit var context: Context


    override fun onEmvSdkSuccess(uiData: String) {
        /* Just for testing comparing with uiData value */
        if (uiData == "SUCCESS") {
            iEmvServiceResponseListener.onEmvSuccess(true)
        } else {
            iEmvServiceResponseListener.onEmvError(EmvServiceError("Error"))
        }
    }

    override fun onEmvSdkError(uiData: String) {
        /* Just for testing comparing with uiData value */
        iEmvServiceResponseListener.onDisplayProgress(false)
        if (uiData == "SUCCESS")
            iEmvServiceResponseListener.onEmvSuccess(true)
        else
            iEmvServiceResponseListener.onEmvError(EmvServiceError("Error"))
    }

    override fun onEmvSdkDisplayMessage(uiData: String?) {
        iEmvServiceResponseListener.onDisplayProgress(!uiData.isNullOrBlank(), message = uiData)
    }

    override fun initPaymentSDK(
        context: Context,
        iEmvServiceResponseListener: IEmvServiceResponseListener
    ) {
        this.iEmvServiceResponseListener = iEmvServiceResponseListener
        this.context = context
        iEmvServiceResponseListener.onDisplayProgress(false)
        emvSdkRequestRepository.initPaymentSDK(context)
    }

    override fun startPayment(
        context: Context,
        iEmvServiceResponseListener: IEmvServiceResponseListener
    ) {
        this.iEmvServiceResponseListener = iEmvServiceResponseListener
        iEmvServiceResponseListener.onDisplayProgress(false)
        emvSdkRequestRepository.startPayment(context)
    }

    override fun onEmvServiceResponse(response: Any) {
        iEmvServiceResponseListener.onDisplayProgress(false)
        when (response) {
            is EmvServiceError -> {
                iEmvServiceResponseListener.onEmvError(EmvServiceError(response.toString()))
            }
            else ->
            {
                iEmvServiceResponseListener.onEmvSuccess(response)
            }
        }
    }
}