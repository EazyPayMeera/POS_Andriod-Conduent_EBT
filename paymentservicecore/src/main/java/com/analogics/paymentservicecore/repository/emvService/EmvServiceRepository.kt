package com.analogics.paymentservicecore.repository.emvService

import android.content.Context
import com.analogics.paymentservicecore.constants.AppConstants
import com.analogics.paymentservicecore.constants.ConfigConstants
import com.analogics.paymentservicecore.listeners.requestListener.IEmvServiceRequestListener
import com.analogics.paymentservicecore.listeners.responseListener.IEmvServiceResponseListener
import com.analogics.paymentservicecore.model.emv.AidConfig
import com.analogics.paymentservicecore.model.emv.CAPKey
import com.analogics.paymentservicecore.model.emv.EmvServiceResult
import com.analogics.paymentservicecore.model.emv.EmvServiceResult.InitResult
import com.analogics.paymentservicecore.model.emv.EmvServiceResult.TransResult
import com.analogics.paymentservicecore.model.emv.EmvServiceResult.TransStatus
import com.analogics.paymentservicecore.model.emv.TermConfig
import com.analogics.paymentservicecore.model.emv.TransConfig
import com.analogics.paymentservicecore.model.error.EmvServiceException
import com.analogics.tpaymentcore.listener.responseListener.IEmvSdkResponseListener
import com.analogics.tpaymentcore.model.emv.EmvSdkException
import com.analogics.tpaymentcore.model.emv.EmvSdkResult
import com.analogics.tpaymentcore.repository.EmvSdkRequestRepository
import com.google.gson.Gson
import org.json.JSONObject
import javax.inject.Inject

class EmvServiceRepository @Inject constructor() :
    IEmvServiceRequestListener,
    IEmvSdkResponseListener {
    private val emvSdkRequestRepository = EmvSdkRequestRepository(this)
    lateinit var iEmvServiceResponseListener: IEmvServiceResponseListener
    lateinit var context: Context

    fun sdkToEmvInitStatus(value: EmvSdkResult.InitStatus) :EmvServiceResult.InitStatus {
        return when (value) {
            EmvSdkResult.InitStatus.SUCCESS -> EmvServiceResult.InitStatus.SUCCESS
            else -> EmvServiceResult.InitStatus.FAILURE
        }
    }

    fun sdkToEmvTransStatus(value: EmvSdkResult.TransStatus) :TransStatus {
        return when (value) {
            EmvSdkResult.TransStatus.APPROVED_ONLINE -> TransStatus.APPROVED_ONLINE
            EmvSdkResult.TransStatus.DECLINED_ONLINE -> TransStatus.DECLINED_ONLINE
            EmvSdkResult.TransStatus.APPROVED_OFFLINE -> TransStatus.APPROVED_OFFLINE
            EmvSdkResult.TransStatus.DECLINED_OFFLINE -> TransStatus.DECLINED_OFFLINE
            EmvSdkResult.TransStatus.CANCELED -> TransStatus.CANCELED
            EmvSdkResult.TransStatus.TIMEOUT -> TransStatus.TIMEOUT
            EmvSdkResult.TransStatus.TERMINATED -> TransStatus.TERMINATED
            EmvSdkResult.TransStatus.CARD_BLOCKED -> TransStatus.CARD_BLOCKED
            EmvSdkResult.TransStatus.APP_BLOCKED -> TransStatus.APP_BLOCKED
            EmvSdkResult.TransStatus.NO_EMV_APPS -> TransStatus.NO_EMV_APPS
            EmvSdkResult.TransStatus.APP_SELECTION_FAILED -> TransStatus.APP_SELECTION_FAILED
            EmvSdkResult.TransStatus.TRY_ANOTHER_INTERFACE -> TransStatus.TRY_ANOTHER_INTERFACE
            EmvSdkResult.TransStatus.INVALID_ICC_CARD -> TransStatus.INVALID_ICC_CARD
            EmvSdkResult.TransStatus.RETRY -> TransStatus.RETRY
            EmvSdkResult.TransStatus.CARD_REMOVED -> TransStatus.CARD_REMOVED
            EmvSdkResult.TransStatus.ISSUER_SCRIPT_UPDATE_SUCCESSFUL -> TransStatus.ISSUER_SCRIPT_UPDATE_SUCCESSFUL
            EmvSdkResult.TransStatus.ISSUER_SCRIPT_UPDATE_FAILED -> TransStatus.ISSUER_SCRIPT_UPDATE_FAILED

            else -> TransStatus.ERROR
        }
    }

    fun sdkToEmvService(response: Any) : Any
    {
        return when(response) {
            is EmvSdkResult.InitResult -> {
                InitResult(
                    status = sdkToEmvInitStatus(response.status as EmvSdkResult.InitStatus)
                )
            }
            is EmvSdkResult.TransResult -> {
                TransResult(
                    status = sdkToEmvTransStatus(response.status as EmvSdkResult.TransStatus)
                )
            }
            is EmvSdkException ->{
                EmvServiceException(errorMessage = response.errorMessage)
            }
            else -> {
                EmvServiceException(errorMessage = "Unknown SDK Error")
            }
        }
    }

    override fun onEmvSdkResponse(response: Any) {
        iEmvServiceResponseListener.onEmvServiceDisplayProgress(false)
        iEmvServiceResponseListener.onEmvServiceResponse(sdkToEmvService(response))
    }

    override fun onEmvSdkDisplayMessage(uiData: String?) {
        iEmvServiceResponseListener.onEmvServiceDisplayProgress(!uiData.isNullOrBlank(), message = uiData)
    }

    override fun initPaymentSDK(
        termConfig: TermConfig?,
        aidConfig: String?,
        capKeys: String?,
        iEmvServiceResponseListener: IEmvServiceResponseListener
    ) {
        try {
            this.iEmvServiceResponseListener = iEmvServiceResponseListener
            val jsonCapKeys = JSONObject(capKeys?:"").getJSONArray(AppConstants.EMV_CAP_KEY_ARRAY_FIELD_NAME).toString()

            var sdkAidConfig : com.analogics.tpaymentcore.model.emv.AidConfig? = when(android.os.Build.MANUFACTURER.uppercase()) {
                ConfigConstants.CONFIG_VAL_DEVICE_TYPE_UROVO -> Gson().fromJson(aidConfig?:"",
                    com.analogics.tpaymentcore.model.emv.AidConfig::class.java
                )
                else -> {null}
            }

            var sdkCapKeys : List<com.analogics.tpaymentcore.model.emv.CAPKey>? = when(android.os.Build.MANUFACTURER.uppercase()) {
                ConfigConstants.CONFIG_VAL_DEVICE_TYPE_UROVO -> Gson().fromJson(jsonCapKeys,
                    Array<com.analogics.tpaymentcore.model.emv.CAPKey>::class.java
                ).toList()
                else -> {null}
            }

            /* Override Terminal Specific Parameters */
            termConfig?.let {
                it.terminalIdentifier?.let { sdkAidConfig?.terminalIdentifier = it }
                it.merchantIdentifier?.let { sdkAidConfig?.merchantIdentifier = it }
                it.merchantCategoryCode?.let { sdkAidConfig?.merchantCategoryCode = it }
                it.merchantNameLocation?.let { sdkAidConfig?.merchantNameLocation = it }
                it.ifdSerialNumber?.let { sdkAidConfig?.ifdSerialNumber = it }
                it.cardCheckTimeout?.let { sdkAidConfig?.cardCheckTimeout = it }
                it.enableBeeper?.let { sdkAidConfig?.enableBeeper = it }
            }

            emvSdkRequestRepository.initPaymentSDK(sdkAidConfig,sdkCapKeys)
        }catch (e : Exception)
        {
            iEmvServiceResponseListener.onEmvServiceResponse(EmvServiceException(e.message.toString()))
        }
    }

    override fun initPaymentSDK(
        termConfig: TermConfig?,
        aidConfig: AidConfig?,
        capKeys: List<CAPKey>,
        iEmvServiceResponseListener: IEmvServiceResponseListener
    ) {
        try {
            this.iEmvServiceResponseListener = iEmvServiceResponseListener
            var sdkAidConfig : com.analogics.tpaymentcore.model.emv.AidConfig? = when(android.os.Build.MANUFACTURER.uppercase()) {
                ConfigConstants.CONFIG_VAL_DEVICE_TYPE_UROVO -> Gson().fromJson(Gson().toJson(aidConfig),
                    com.analogics.tpaymentcore.model.emv.AidConfig::class.java
                )
                else -> {null}
            }
            var sdkCapKeys : List<com.analogics.tpaymentcore.model.emv.CAPKey>? = when(android.os.Build.MANUFACTURER.uppercase()) {
                ConfigConstants.CONFIG_VAL_DEVICE_TYPE_UROVO -> Gson().fromJson(
                    Gson().toJson(capKeys),
                    Array<com.analogics.tpaymentcore.model.emv.CAPKey>::class.java
                ).toList()
                else -> {null}
            }

            /* Override Terminal Specific Parameters */
            termConfig?.let {
                it.terminalIdentifier?.let { sdkAidConfig?.terminalIdentifier = it }
                it.merchantIdentifier?.let { sdkAidConfig?.merchantIdentifier = it }
                it.merchantCategoryCode?.let { sdkAidConfig?.merchantCategoryCode = it }
                it.merchantNameLocation?.let { sdkAidConfig?.merchantNameLocation = it }
                it.ifdSerialNumber?.let { sdkAidConfig?.ifdSerialNumber = it }
                it.cardCheckTimeout?.let { sdkAidConfig?.cardCheckTimeout = it }
                it.enableBeeper?.let { sdkAidConfig?.enableBeeper = it }
            }

            emvSdkRequestRepository.initPaymentSDK(sdkAidConfig,sdkCapKeys)
        }catch (e : Exception)
        {
            iEmvServiceResponseListener.onEmvServiceResponse(EmvServiceException(e.message.toString()))
        }
    }

    override fun startPayment(
        context: Context,
        transConfig: TransConfig?,
        iEmvServiceResponseListener: IEmvServiceResponseListener
    ) {
        this.iEmvServiceResponseListener = iEmvServiceResponseListener
        iEmvServiceResponseListener.onEmvServiceDisplayProgress(false)

        var sdkTransConfig : com.analogics.tpaymentcore.model.emv.TransConfig? = when(android.os.Build.MANUFACTURER.uppercase()) {
            ConfigConstants.CONFIG_VAL_DEVICE_TYPE_UROVO -> Gson().fromJson(
                Gson().toJson(transConfig),
                com.analogics.tpaymentcore.model.emv.TransConfig::class.java
            )
            else -> {null}
        }
        emvSdkRequestRepository.startPayment(context, sdkTransConfig)
    }

    override fun abortPayment() {
        emvSdkRequestRepository.abortPayment()
    }
}