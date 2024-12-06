package com.analogics.paymentservicecore.repository.emvService

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.analogics.paymentservicecore.constants.AppConstants
import com.analogics.paymentservicecore.constants.ConfigConstants
import com.analogics.paymentservicecore.constants.EmvConstants
import com.analogics.paymentservicecore.listeners.requestListener.IEmvServiceRequestListener
import com.analogics.paymentservicecore.listeners.responseListener.IApiServiceResponseListener
import com.analogics.paymentservicecore.listeners.responseListener.IEmvServiceResponseListener
import com.analogics.paymentservicecore.model.PaymentServiceTxnDetails
import com.analogics.paymentservicecore.model.emv.AidConfig
import com.analogics.paymentservicecore.model.emv.CAPKey
import com.analogics.paymentservicecore.model.emv.CardCheckMode
import com.analogics.paymentservicecore.model.emv.EmvServiceResult
import com.analogics.paymentservicecore.model.emv.EmvServiceResult.CardCheckResult
import com.analogics.paymentservicecore.model.emv.EmvServiceResult.CardCheckStatus
import com.analogics.paymentservicecore.model.emv.EmvServiceResult.DisplayMsgId
import com.analogics.paymentservicecore.model.emv.EmvServiceResult.InitResult
import com.analogics.paymentservicecore.model.emv.EmvServiceResult.InitStatus
import com.analogics.paymentservicecore.model.emv.EmvServiceResult.TransResult
import com.analogics.paymentservicecore.model.emv.EmvServiceResult.TransStatus
import com.analogics.paymentservicecore.model.emv.TermConfig
import com.analogics.paymentservicecore.model.emv.TransConfig
import com.analogics.paymentservicecore.model.error.ApiServiceError
import com.analogics.paymentservicecore.model.error.EmvServiceException
import com.analogics.paymentservicecore.models.toEmvTransType
import com.analogics.paymentservicecore.repository.apiService.ApiServiceRepository
import com.analogics.paymentservicecore.utils.PaymentServiceUtils
import com.analogics.paymentservicecore.utils.toDecimalFormat
import com.analogics.tpaymentcore.listener.responseListener.IEmvSdkResponseListener
import com.analogics.tpaymentcore.model.emv.EmvSdkException
import com.analogics.tpaymentcore.model.emv.EmvSdkResult
import com.analogics.tpaymentcore.repository.EmvSdkRequestRepository
import com.analogics.tpaymentcore.utils.TlvUtils
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

class EmvServiceRepository @Inject constructor(var apiServiceRepository: ApiServiceRepository) :
    IEmvServiceRequestListener,
    IEmvSdkResponseListener {
    private val emvSdkRequestRepository = EmvSdkRequestRepository(this)
    lateinit var iEmvServiceResponseListener: IEmvServiceResponseListener
    lateinit var context: Context
    lateinit var paymentServiceTxnDetails : PaymentServiceTxnDetails

    fun sdkToEmvInitStatus(value: EmvSdkResult.InitStatus) : InitStatus {
        return when (value) {
            EmvSdkResult.InitStatus.SUCCESS -> InitStatus.SUCCESS
            else -> InitStatus.FAILURE
        }
    }

    fun sdkToEmvCardCheckStatus(value: EmvSdkResult.CardCheckStatus) : CardCheckStatus {
        return when (value) {
            EmvSdkResult.CardCheckStatus.NO_CARD_DETECTED -> CardCheckStatus.NO_CARD_DETECTED
            EmvSdkResult.CardCheckStatus.CARD_INSERTED -> CardCheckStatus.CARD_INSERTED
            EmvSdkResult.CardCheckStatus.CARD_TAPPED -> CardCheckStatus.CARD_TAPPED
            EmvSdkResult.CardCheckStatus.CARD_SWIPED -> CardCheckStatus.CARD_SWIPED
            EmvSdkResult.CardCheckStatus.NOT_ICC_CARD -> CardCheckStatus.NOT_ICC_CARD
            EmvSdkResult.CardCheckStatus.USE_ICC_CARD -> CardCheckStatus.USE_ICC_CARD
            EmvSdkResult.CardCheckStatus.BAD_SWIPE -> CardCheckStatus.BAD_SWIPE
            EmvSdkResult.CardCheckStatus.NEED_FALLBACK -> CardCheckStatus.NEED_FALLBACK
            EmvSdkResult.CardCheckStatus.MULTIPLE_CARDS -> CardCheckStatus.MULTIPLE_CARDS
            EmvSdkResult.CardCheckStatus.TIMEOUT -> CardCheckStatus.TIMEOUT
            EmvSdkResult.CardCheckStatus.CANCEL -> CardCheckStatus.CANCEL
            EmvSdkResult.CardCheckStatus.DEVICE_BUSY -> CardCheckStatus.DEVICE_BUSY
            else -> CardCheckStatus.ERROR
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
            EmvSdkResult.TransStatus.INITIATED -> TransStatus.INITIATED

            else -> TransStatus.ERROR
        }
    }

    fun sdkToEmvDisplayMsgId(value: EmvSdkResult.DisplayMsgId) : DisplayMsgId
    {
        return when (value) {
            EmvSdkResult.DisplayMsgId.NONE -> DisplayMsgId.NONE
            EmvSdkResult.DisplayMsgId.CARD_INSERTED -> DisplayMsgId.CARD_INSERTED
            EmvSdkResult.DisplayMsgId.CARD_SWIPED -> DisplayMsgId.CARD_SWIPED
            EmvSdkResult.DisplayMsgId.CARD_TAPPED -> DisplayMsgId.CARD_TAPPED
            EmvSdkResult.DisplayMsgId.CARD_READ_OK -> DisplayMsgId.CARD_READ_OK
            EmvSdkResult.DisplayMsgId.REMOVE_CARD -> DisplayMsgId.REMOVE_CARD
            EmvSdkResult.DisplayMsgId.USE_CONTACT_IC_CARD -> DisplayMsgId.USE_CONTACT_IC_CARD
            EmvSdkResult.DisplayMsgId.USE_MAG_STRIPE -> DisplayMsgId.USE_MAG_STRIPE
            EmvSdkResult.DisplayMsgId.INSERT_SWIPE_OR_TRY_ANOTHER_CARD -> DisplayMsgId.INSERT_SWIPE_OR_TRY_ANOTHER_CARD
            EmvSdkResult.DisplayMsgId.SEE_PHONE_AND_PRESENT_CARD_AGAIN -> DisplayMsgId.SEE_PHONE_AND_PRESENT_CARD_AGAIN
            EmvSdkResult.DisplayMsgId.NEED_SIGNATURE -> DisplayMsgId.NEED_SIGNATURE
            EmvSdkResult.DisplayMsgId.END_APPLICATION -> DisplayMsgId.END_APPLICATION
            EmvSdkResult.DisplayMsgId.DISPLAY_BALANCE -> DisplayMsgId.DISPLAY_BALANCE
            EmvSdkResult.DisplayMsgId.TAP_CARD_AGAIN -> DisplayMsgId.TAP_CARD_AGAIN
            EmvSdkResult.DisplayMsgId.APP_BLOCKED -> DisplayMsgId.APP_BLOCKED
            EmvSdkResult.DisplayMsgId.TERMINATED -> DisplayMsgId.TERMINATED

            EmvSdkResult.DisplayMsgId.ERR_CARD_READ -> DisplayMsgId.ERR_CARD_READ
            EmvSdkResult.DisplayMsgId.ERR_PROCESSING -> DisplayMsgId.ERR_PROCESSING
            EmvSdkResult.DisplayMsgId.ERR_LOAD_CALLBACK -> DisplayMsgId.ERR_LOAD_CALLBACK
            EmvSdkResult.DisplayMsgId.ERR_ICS_PARAM_NOT_FOUND -> DisplayMsgId.ERR_ICS_PARAM_NOT_FOUND
            EmvSdkResult.DisplayMsgId.ERR_KERNEL -> DisplayMsgId.ERR_KERNEL
            EmvSdkResult.DisplayMsgId.ERR_PIN_LENGTH -> DisplayMsgId.ERR_PIN_LENGTH
            EmvSdkResult.DisplayMsgId.ERR_MULTI_CARD -> DisplayMsgId.ERR_MULTI_CARD
            EmvSdkResult.DisplayMsgId.ERR_CHECK_CARD -> DisplayMsgId.ERR_CHECK_CARD
            EmvSdkResult.DisplayMsgId.ERR_AID_PARAM_NOT_FIND -> DisplayMsgId.ERR_AID_PARAM_NOT_FIND
            EmvSdkResult.DisplayMsgId.ERR_CAPK_PARAM_NOT_FIND -> DisplayMsgId.ERR_CAPK_PARAM_NOT_FIND
            EmvSdkResult.DisplayMsgId.ERR_GET_KERNEL_DATA_FAILED -> DisplayMsgId.ERR_GET_KERNEL_DATA_FAILED
            EmvSdkResult.DisplayMsgId.ERR_QPBOC_APPLICATION -> DisplayMsgId.ERR_QPBOC_APPLICATION
            EmvSdkResult.DisplayMsgId.ERR_QPBOC_FDDA_FAILED -> DisplayMsgId.ERR_QPBOC_FDDA_FAILED
            EmvSdkResult.DisplayMsgId.ERR_PURE_ELE_CASH_CARD_NOT_ALLOW_ONLINE_TRANS -> DisplayMsgId.ERR_PURE_ELE_CASH_CARD_NOT_ALLOW_ONLINE_TRANS
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
            is EmvSdkResult.CardCheckResult -> {
                CardCheckResult(
                    status = sdkToEmvCardCheckStatus(response.status as EmvSdkResult.CardCheckStatus)
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
        iEmvServiceResponseListener.onEmvServiceResponse(sdkToEmvService(response))
    }

    override fun onEmvSdkDisplayMessage(displayMsgId: EmvSdkResult.DisplayMsgId) {
        iEmvServiceResponseListener.onEmvServiceDisplayMessage(sdkToEmvDisplayMsgId(displayMsgId))
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onEmvSdkOnlineRequest(emvTags : HashMap<String,String>, onResponse : (HashMap<String,String>)->Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            onEmvServiceRequestOnline(emvTags, onResponse)
        }
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
                ConfigConstants.CONFIG_VAL_DEVICE_TYPE_UROVO,ConfigConstants.CONFIG_VAL_DEVICE_TYPE_TIANYU -> Gson().fromJson(aidConfig?:"",
                    com.analogics.tpaymentcore.model.emv.AidConfig::class.java
                )
                else -> {null}
            }

            var sdkCapKeys : List<com.analogics.tpaymentcore.model.emv.CAPKey>? = when(android.os.Build.MANUFACTURER.uppercase()) {
                ConfigConstants.CONFIG_VAL_DEVICE_TYPE_UROVO,ConfigConstants.CONFIG_VAL_DEVICE_TYPE_TIANYU -> Gson().fromJson(jsonCapKeys,
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
                ConfigConstants.CONFIG_VAL_DEVICE_TYPE_UROVO,ConfigConstants.CONFIG_VAL_DEVICE_TYPE_TIANYU -> Gson().fromJson(Gson().toJson(aidConfig),
                    com.analogics.tpaymentcore.model.emv.AidConfig::class.java
                )
                else -> {null}
            }
            var sdkCapKeys : List<com.analogics.tpaymentcore.model.emv.CAPKey>? = when(android.os.Build.MANUFACTURER.uppercase()) {
                ConfigConstants.CONFIG_VAL_DEVICE_TYPE_UROVO,ConfigConstants.CONFIG_VAL_DEVICE_TYPE_TIANYU -> Gson().fromJson(
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
        paymentServiceTxnDetails: PaymentServiceTxnDetails?,
        iEmvServiceResponseListener: IEmvServiceResponseListener
    ) {
        this.paymentServiceTxnDetails = paymentServiceTxnDetails?: PaymentServiceTxnDetails()
        this.iEmvServiceResponseListener = iEmvServiceResponseListener
        iEmvServiceResponseListener.onEmvServiceDisplayMessage(DisplayMsgId.NONE)
        var transConfig = getTransConfig(paymentServiceTxnDetails)

        var sdkTransConfig : com.analogics.tpaymentcore.model.emv.TransConfig? = when(android.os.Build.MANUFACTURER.uppercase()) {
            ConfigConstants.CONFIG_VAL_DEVICE_TYPE_UROVO,ConfigConstants.CONFIG_VAL_DEVICE_TYPE_TIANYU -> Gson().fromJson(
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

    private fun getTransConfig(paymentServiceTxnDetails: PaymentServiceTxnDetails?) : TransConfig
    {
        return TransConfig(
            amount = (paymentServiceTxnDetails?.ttlAmount?.toDoubleOrNull()?:0.00).toDecimalFormat(),
            cashbackAmount = (paymentServiceTxnDetails?.cashback?.toDoubleOrNull()?:0.00).toDecimalFormat(),
            currencyCode = paymentServiceTxnDetails?.txnCurrencyCode?: AppConstants.DEFAULT_CURRENCY_CODE,
            transactionType = paymentServiceTxnDetails?.txnType?.toEmvTransType(),
            cardCheckMode = CardCheckMode.SWIPE_OR_INSERT_OR_TAP,
            cardCheckTimeout = AppConstants.CARD_CHECK_TIMEOUT_S.toString(),
            supportDRL = false
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun onEmvServiceRequestOnline(
        emvTags: HashMap<String, String>,
        onResponse: (HashMap<String, String>) -> Unit
    ) {

        var responseEmvTags =
            hashMapOf(EmvConstants.EMV_TAG_RESP_CODE to EmvConstants.EMV_TAG_VAL_UNABLE_TO_GO_ONLINE_DECLINE)  // Unable to go online, Decline

        val tlvData = TlvUtils(emvTags)
        if (tlvData.tlvMap.containsKey(EmvConstants.EMV_TAG_ENC_TRACK)) {
            paymentServiceTxnDetails.trackData =
                tlvData.tlvMap[EmvConstants.EMV_TAG_ENC_TRACK]
            tlvData.tlvMap.remove(EmvConstants.EMV_TAG_ENC_TRACK)
        }
        if (tlvData.tlvMap.containsKey(EmvConstants.EMV_TAG_ENC_KSN)) {
            paymentServiceTxnDetails.ksn =
                tlvData.tlvMap[EmvConstants.EMV_TAG_ENC_KSN]
            tlvData.tlvMap.remove(EmvConstants.EMV_TAG_ENC_KSN)
        }
        if (tlvData.tlvMap.containsKey(EmvConstants.EMV_TAG_ENC_PIN_BLOCK)) {
            paymentServiceTxnDetails.pinBlock =
                tlvData.tlvMap[EmvConstants.EMV_TAG_ENC_PIN_BLOCK]
            tlvData.tlvMap.remove(EmvConstants.EMV_TAG_ENC_PIN_BLOCK)
        }
        if (tlvData.tlvMap.containsKey(EmvConstants.EMV_TAG_PAN)) {
            tlvData.tlvMap.remove(EmvConstants.EMV_TAG_PAN)
        }

        paymentServiceTxnDetails.emvData = tlvData.toTlvString()

        iEmvServiceResponseListener.onEmvServiceDisplayMessage(DisplayMsgId.PROCESSING_ONLINE)
        apiServiceRepository.apiServiceRequestOnlineAuth(
            paymentServiceTxnDetails, object : IApiServiceResponseListener {

                override fun onApiServiceSuccess(apiPaymentServiceTxnDetails: PaymentServiceTxnDetails) {
                    paymentServiceTxnDetails = apiPaymentServiceTxnDetails
                    responseEmvTags =
                        TlvUtils(paymentServiceTxnDetails.emvData).tlvMap
                    onResponse(responseEmvTags)
                }

                override fun onApiServiceError(apiServiceError: ApiServiceError) {
                    onResponse(responseEmvTags)
                }
            })
    }
}