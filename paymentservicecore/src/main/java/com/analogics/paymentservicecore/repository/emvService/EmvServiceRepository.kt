package com.analogics.paymentservicecore.repository.emvService

import android.content.Context
import com.analogics.paymentservicecore.BuildConfig
import com.analogics.paymentservicecore.constants.AppConstants
import com.analogics.paymentservicecore.constants.ConfigConstants
import com.analogics.paymentservicecore.listeners.requestListener.IEmvServiceRequestListener
import com.analogics.paymentservicecore.listeners.responseListener.IEmvServiceResponseListener
import com.analogics.paymentservicecore.model.emv.CAPKey
import com.analogics.paymentservicecore.model.error.EmvServiceError
import com.analogics.tpaymentcore.listener.responseListener.IEmvSdkResponseListener
import com.analogics.tpaymentcore.repository.EmvSdkRequestRepository
import com.google.gson.Gson
import com.google.gson.JsonElement
import org.json.JSONObject
import java.io.File
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
        capKeys: String?,
        iEmvServiceResponseListener: IEmvServiceResponseListener
    ) {
        try {
            this.iEmvServiceResponseListener = iEmvServiceResponseListener
            val jsonObject = JSONObject(capKeys?:"").getJSONArray(AppConstants.EMV_CAP_KEY_ARRAY_FIELD_NAME).toString()

            var sdkCapKeys : List<com.analogics.tpaymentcore.model.emv.CAPKey>? = when(android.os.Build.MANUFACTURER.uppercase()) {
                ConfigConstants.CONFIG_VAL_DEVICE_TYPE_UROVO -> Gson().fromJson(jsonObject,
                    Array<com.analogics.tpaymentcore.model.emv.CAPKey>::class.java
                ).toList()
                else -> {null}
            }
            emvSdkRequestRepository.initPaymentSDK(sdkCapKeys)
        }catch (e : Exception)
        {
            iEmvServiceResponseListener.onEmvError(EmvServiceError(e.message.toString()))
        }
    }

    override fun initPaymentSDK(
        capKeys: List<CAPKey>,
        iEmvServiceResponseListener: IEmvServiceResponseListener
    ) {
        try {
            this.iEmvServiceResponseListener = iEmvServiceResponseListener
            var sdkCapKeys : List<com.analogics.tpaymentcore.model.emv.CAPKey>? = when(android.os.Build.DEVICE.uppercase()) {
                ConfigConstants.CONFIG_VAL_DEVICE_TYPE_UROVO -> Gson().fromJson(
                    Gson().toJson(capKeys),
                    Array<com.analogics.tpaymentcore.model.emv.CAPKey>::class.java
                ).toList()
                else -> {null}
            }
            emvSdkRequestRepository.initPaymentSDK(sdkCapKeys)
        }catch (e : Exception)
        {
            iEmvServiceResponseListener.onEmvError(EmvServiceError(e.message.toString()))
        }
    }

    override fun startPayment(
        context: Context,
        iEmvServiceResponseListener: IEmvServiceResponseListener
    ) {
        this.iEmvServiceResponseListener = iEmvServiceResponseListener
        iEmvServiceResponseListener.onDisplayProgress(false)
        emvSdkRequestRepository.startPayment(context)
    }

}