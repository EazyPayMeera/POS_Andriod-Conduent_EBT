package com.eazypaytech.paymentservicecore.repository.emvService

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.eazypaytech.paymentservicecore.constants.AppConstants
import com.eazypaytech.paymentservicecore.constants.ConfigConstants
import com.eazypaytech.paymentservicecore.constants.EmvConstants
import com.eazypaytech.paymentservicecore.listeners.requestListener.IEmvServiceRequestListener
import com.eazypaytech.paymentservicecore.listeners.responseListener.IApiServiceResponseListener
import com.eazypaytech.paymentservicecore.listeners.responseListener.IEmvServiceResponseListener
import com.eazypaytech.paymentservicecore.model.PaymentServiceTxnDetails
import com.eazypaytech.paymentservicecore.model.emv.AidConfig
import com.eazypaytech.paymentservicecore.model.emv.CAPKey
import com.eazypaytech.paymentservicecore.model.emv.CardBrand
import com.eazypaytech.paymentservicecore.model.emv.CardCheckMode
import com.eazypaytech.paymentservicecore.model.emv.CardEntryMode
import com.eazypaytech.paymentservicecore.model.emv.EmvServiceResult.CardCheckResult
import com.eazypaytech.paymentservicecore.model.emv.EmvServiceResult.CardCheckStatus
import com.eazypaytech.paymentservicecore.model.emv.EmvServiceResult.DisplayMsgId
import com.eazypaytech.paymentservicecore.model.emv.EmvServiceResult.InitResult
import com.eazypaytech.paymentservicecore.model.emv.EmvServiceResult.InitStatus
import com.eazypaytech.paymentservicecore.model.emv.EmvServiceResult.TransResult
import com.eazypaytech.paymentservicecore.model.emv.EmvServiceResult.TransStatus
import com.eazypaytech.paymentservicecore.model.emv.TermConfig
import com.eazypaytech.paymentservicecore.model.emv.TransConfig
import com.eazypaytech.paymentservicecore.model.error.ApiServiceError
import com.eazypaytech.paymentservicecore.model.error.EmvServiceException
import com.eazypaytech.paymentservicecore.models.toEmvTransType
import com.eazypaytech.paymentservicecore.repository.apiService.ApiServiceRepository
import com.eazypaytech.paymentservicecore.utils.maskPAN
import com.eazypaytech.paymentservicecore.utils.toDecimalFormat
import com.eazypaytech.tpaymentcore.listener.responseListener.IEmvSdkResponseListener
import com.eazypaytech.tpaymentcore.model.emv.EmvSdkException
import com.eazypaytech.tpaymentcore.model.emv.EmvSdkResult
import com.eazypaytech.tpaymentcore.repository.EmvSdkRequestRepository
import com.eazypaytech.tpaymentcore.utils.TlvUtils
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

class EmvServiceRepository @Inject constructor(@ApplicationContext context: Context, var apiServiceRepository: ApiServiceRepository) :
    IEmvServiceRequestListener,
    IEmvSdkResponseListener {
    private val emvSdkRequestRepository = EmvSdkRequestRepository(context,this)
    lateinit var iEmvServiceResponseListener: IEmvServiceResponseListener
    //lateinit var context: Context
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

            EmvSdkResult.DisplayMsgId.PROCESSING_ONLINE -> DisplayMsgId.PROCESSING_ONLINE
        }
    }

    fun emvCardCheckStatusToDisplayMsgId(status: CardCheckStatus) : DisplayMsgId
    {
        return when (status) {
            CardCheckStatus.NO_CARD_DETECTED -> DisplayMsgId.ERR_CARD_READ
            CardCheckStatus.CARD_INSERTED -> DisplayMsgId.CARD_INSERTED
            CardCheckStatus.CARD_TAPPED -> DisplayMsgId.CARD_TAPPED
            CardCheckStatus.CARD_SWIPED -> DisplayMsgId.CARD_SWIPED
            CardCheckStatus.NOT_ICC_CARD -> DisplayMsgId.INVALID_ICC_CARD
            CardCheckStatus.USE_ICC_CARD -> DisplayMsgId.USE_CONTACT_IC_CARD
            CardCheckStatus.BAD_SWIPE -> DisplayMsgId.ERR_CARD_READ
            CardCheckStatus.NEED_FALLBACK -> DisplayMsgId.USE_MAG_STRIPE
            CardCheckStatus.MULTIPLE_CARDS -> DisplayMsgId.ERR_MULTI_CARD
            CardCheckStatus.TIMEOUT -> DisplayMsgId.TIMEOUT
            CardCheckStatus.CANCEL -> DisplayMsgId.CANCELED
            CardCheckStatus.DEVICE_BUSY -> DisplayMsgId.ERR_PROCESSING

            else -> DisplayMsgId.NONE
        }
    }

    fun adjustTransStatusFromDisplayMsgId(status: TransStatus, displayMsgId: DisplayMsgId?) : TransStatus
    {
        var transStatus = when (status) {
            TransStatus.TERMINATED -> when(displayMsgId)
            {
                DisplayMsgId.APP_BLOCKED -> TransStatus.APP_BLOCKED
                DisplayMsgId.CARD_BLOCKED -> TransStatus.CARD_BLOCKED
                DisplayMsgId.NO_EMV_APPS -> TransStatus.NO_EMV_APPS
                DisplayMsgId.TRY_ANOTHER_INTERFACE-> TransStatus.TRY_ANOTHER_INTERFACE
                DisplayMsgId.TAP_CARD_AGAIN,
                DisplayMsgId.INSERT_SWIPE_OR_TRY_ANOTHER_CARD,
                DisplayMsgId.RETRY -> TransStatus.RETRY
                else -> status
            }
            else -> status
        }

        return transStatus
    }

    fun emvTransStatusToDisplayMsgId(status: TransStatus) : DisplayMsgId?
    {
        return when (status) {
            TransStatus.APPROVED_ONLINE -> DisplayMsgId.APPROVED_ONLINE
            TransStatus.DECLINED_ONLINE -> DisplayMsgId.DECLINED_ONLINE
            TransStatus.APPROVED_OFFLINE -> DisplayMsgId.APPROVED_OFFLINE
            TransStatus.DECLINED_OFFLINE -> DisplayMsgId.DECLINED_OFFLINE
            TransStatus.CANCELED -> DisplayMsgId.CANCELED
            TransStatus.TIMEOUT -> DisplayMsgId.TIMEOUT
            TransStatus.TERMINATED -> DisplayMsgId.TERMINATED
            TransStatus.CARD_BLOCKED -> DisplayMsgId.CARD_BLOCKED
            TransStatus.APP_BLOCKED -> DisplayMsgId.APP_BLOCKED
            TransStatus.NO_EMV_APPS -> DisplayMsgId.NO_EMV_APPS
            TransStatus.APP_SELECTION_FAILED -> DisplayMsgId.APP_SELECTION_FAILED
            TransStatus.TRY_ANOTHER_INTERFACE -> DisplayMsgId.TRY_ANOTHER_INTERFACE
            TransStatus.INVALID_ICC_CARD -> DisplayMsgId.INVALID_ICC_CARD
            TransStatus.RETRY -> DisplayMsgId.RETRY
            TransStatus.CARD_REMOVED -> DisplayMsgId.CARD_REMOVED
            TransStatus.ISSUER_SCRIPT_UPDATE_SUCCESSFUL -> DisplayMsgId.ISSUER_SCRIPT_UPDATE_SUCCESSFUL
            TransStatus.ISSUER_SCRIPT_UPDATE_FAILED -> DisplayMsgId.ISSUER_SCRIPT_UPDATE_FAILED
            else -> DisplayMsgId.NONE
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
                var status = sdkToEmvCardCheckStatus(response.status as EmvSdkResult.CardCheckStatus)
                CardCheckResult(
                    status = status,
                    displayMsgId = emvCardCheckStatusToDisplayMsgId(status)
                )
            }
            is EmvSdkResult.TransResult -> {
                var status = sdkToEmvTransStatus(response.status as EmvSdkResult.TransStatus)
                var displayMsgId : DisplayMsgId? = null
                    response.displayMsgId?.let {
                        displayMsgId = sdkToEmvDisplayMsgId(it)
                        status = adjustTransStatusFromDisplayMsgId(status,displayMsgId) /* For NFC needs adjustment */
                    }?:let {
                        displayMsgId = emvTransStatusToDisplayMsgId(status)
                    }

                TransResult(
                    status = status,
                    displayMsgId = displayMsgId
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
        if (response is EmvSdkResult.CardCheckResult) {
            paymentServiceTxnDetails.cardEntryMode = when(sdkToEmvCardCheckStatus(response.status as EmvSdkResult.CardCheckStatus)){
                CardCheckStatus.CARD_INSERTED -> CardEntryMode.CONTACT.toString()
                CardCheckStatus.CARD_TAPPED -> CardEntryMode.CONTACLESS.toString()
                CardCheckStatus.CARD_SWIPED -> CardEntryMode.MAGSTRIPE.toString()
                else -> null
            }
        }
        else if(response is EmvSdkResult.TransResult)
            abortPayment()
    }

    override fun onEmvSdkDisplayMessage(displayMsgId: EmvSdkResult.DisplayMsgId) {
        iEmvServiceResponseListener.onEmvServiceDisplayMessage(sdkToEmvDisplayMsgId(displayMsgId))
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onEmvSdkOnlineRequest(emvTags : HashMap<String,String>, onResponse : (HashMap<String,String>)->Unit) {
        try {
            CoroutineScope(Dispatchers.Default).launch {
                onEmvServiceRequestOnline(emvTags, onResponse)
            }
        }catch (e: Exception)
        {
            e.printStackTrace()
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

            var sdkAidConfig : com.eazypaytech.tpaymentcore.model.emv.AidConfig? = Gson().fromJson(aidConfig?:"",
                    com.eazypaytech.tpaymentcore.model.emv.AidConfig::class.java
                )

            var sdkCapKeys : List<com.eazypaytech.tpaymentcore.model.emv.CAPKey>? = Gson().fromJson(jsonCapKeys,
                    Array<com.eazypaytech.tpaymentcore.model.emv.CAPKey>::class.java
                ).toList()

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
            var sdkAidConfig : com.eazypaytech.tpaymentcore.model.emv.AidConfig? = Gson().fromJson(Gson().toJson(aidConfig),
                    com.eazypaytech.tpaymentcore.model.emv.AidConfig::class.java
                )
            var sdkCapKeys : List<com.eazypaytech.tpaymentcore.model.emv.CAPKey>? = Gson().fromJson(
                    Gson().toJson(capKeys),
                    Array<com.eazypaytech.tpaymentcore.model.emv.CAPKey>::class.java
                ).toList()

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
        //this.context = context
        iEmvServiceResponseListener.onEmvServiceDisplayMessage(DisplayMsgId.NONE)
        var transConfig = getTransConfig(paymentServiceTxnDetails)

        var sdkTransConfig : com.eazypaytech.tpaymentcore.model.emv.TransConfig? = Gson().fromJson(
                Gson().toJson(transConfig),
                com.eazypaytech.tpaymentcore.model.emv.TransConfig::class.java
            )

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
            forceOnlinePin = true,
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

        extractReceiptInfo(emvTags)
        prepareHostTlvData(emvTags)

        iEmvServiceResponseListener.onEmvServiceDisplayMessage(DisplayMsgId.PROCESSING_ONLINE)
        apiServiceRepository.apiServiceRequestOnlineAuth(
            paymentServiceTxnDetails, object : IApiServiceResponseListener {

                override fun onApiServiceSuccess(apiPaymentServiceTxnDetails: PaymentServiceTxnDetails) {
                    paymentServiceTxnDetails = apiPaymentServiceTxnDetails.copy(emvData = paymentServiceTxnDetails.emvData)
                    responseEmvTags =
                        TlvUtils(apiPaymentServiceTxnDetails.emvData).tlvMap
                    onResponse(responseEmvTags)
                }

                override fun onApiServiceError(apiServiceError: ApiServiceError) {
                    onResponse(responseEmvTags)
                }
            })
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun extractReceiptInfo(emvTags: HashMap<String, String>)
    {
        try {
            /* Card Brand. AID has a preference over PAN */
            emvSdkRequestRepository.getEmvTag(EmvConstants.EMV_TAG_AID_CARD)?.let {
                paymentServiceTxnDetails.cardBrand = getCardBrand(aid = it)
            }?:emvSdkRequestRepository.getEmvTag(EmvConstants.EMV_TAG_PAN)?.let {
                paymentServiceTxnDetails.cardBrand = getCardBrand(pan = it)
            }?: emvTags.containsKey(EmvConstants.EMV_TAG_PAN).takeIf{ it == true }?.let {
                paymentServiceTxnDetails.cardBrand = getCardBrand(pan = emvTags[EmvConstants.EMV_TAG_PAN])
            }

            /* Language Preference */
            emvSdkRequestRepository.getEmvTag(EmvConstants.EMV_TAG_LANG_PREF)?.let {
                paymentServiceTxnDetails.cardLanguagePref = it.hexToByteArray().decodeToString()
            }

            /* Issuer/Card Country Code */
            emvSdkRequestRepository.getEmvTag(EmvConstants.EMV_TAG_CARD_COUNTRY_CODE)?.let {
                paymentServiceTxnDetails.cardCountryCode = it.toInt().toString()    // To trim 0
            }
        }catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    private fun prepareHostTlvData(emvTags: HashMap<String, String>)
    {
        try {
            /* Masked PAN */
            if (emvTags.containsKey(EmvConstants.EMV_TAG_PAN)) {
                paymentServiceTxnDetails.cardMaskedPan = maskPAN(emvTags[EmvConstants.EMV_TAG_PAN].toString())
                emvTags.remove(EmvConstants.EMV_TAG_PAN)
            }

            /* Encrypted PAN */
            if (emvTags.containsKey(EmvConstants.EMV_TAG_ENC_PAN)) {
                paymentServiceTxnDetails.cardPan = emvTags[EmvConstants.EMV_TAG_ENC_PAN]
                emvTags.remove(EmvConstants.EMV_TAG_ENC_PAN)
            }

            /* Encrypted Track2 */
            if (emvTags.containsKey(EmvConstants.EMV_TAG_ENC_TRACK)) {
                paymentServiceTxnDetails.trackData = emvTags[EmvConstants.EMV_TAG_ENC_TRACK]
                emvTags.remove(EmvConstants.EMV_TAG_ENC_TRACK)
            }

            /* Remove Track2 */
            if (emvTags.containsKey(EmvConstants.EMV_TAG_TRACK2)) {
                emvTags.remove(EmvConstants.EMV_TAG_TRACK2)
            }

            /* KSN */
            if (emvTags.containsKey(EmvConstants.EMV_TAG_ENC_KSN)) {
                paymentServiceTxnDetails.ksn = emvTags[EmvConstants.EMV_TAG_ENC_KSN]
                emvTags.remove(EmvConstants.EMV_TAG_ENC_KSN)
            }

            /* PinBlock */
            if (emvTags.containsKey(EmvConstants.EMV_TAG_ENC_PIN_BLOCK)) {
                paymentServiceTxnDetails.pinBlock = emvTags[EmvConstants.EMV_TAG_ENC_PIN_BLOCK]
                emvTags.remove(EmvConstants.EMV_TAG_ENC_PIN_BLOCK)
            }

            /* TLV string for Host Communication */
            paymentServiceTxnDetails.emvData = TlvUtils(emvTags).toTlvString()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getCardBrand(aid : String?=null, pan : String?=null) : CardBrand?
    {
        var cardBrand : CardBrand? = CardBrand.UNKNOWN
        try {

            aid?.takeIf { it.length>=10 }?.let {
                return when(it.substring(0,10))
                {
                    "A000000003" -> CardBrand.VISA
                    "A000000004" -> CardBrand.MASTERCARD
                    "A000000524" -> CardBrand.RUPAY
                    "A000000025" -> CardBrand.AMEX
                    "A000000065" -> CardBrand.JCB
                    "A000000152" -> CardBrand.DISCOVER
                    else -> CardBrand.UNKNOWN
                }
            }?: pan?.takeIf { it.length>=6 }?.let {
                return when(it.substring(0,6).toInt())
                {
                    in 400000..499999 -> CardBrand.VISA

                    // Mastercard ranges: 51-55 and 2221-2720
                    in 510000..559999, in 222100..272099 -> CardBrand.MASTERCARD

                    // American Express: 34, 37
                    in 340000..349999, in 370000..379999 -> CardBrand.AMEX

                    // Discover: 6011, 644-649, 65
                    in 601100..601199, in 644000..649999, in 650000..659999 -> CardBrand.DISCOVER

                    // Diners Club: 36, 38, 39
                    in 360000..369999, in 380000..399999 -> CardBrand.DINERS

                    // JCB: 3528-3589
                    in 352800..358999 -> CardBrand.JCB

                    // UnionPay: 62
                    in 620000..629999 -> CardBrand.UPI

                    // Maestro: 50, 56-69
                    in 500000..509999, in 560000..699999 -> CardBrand.MASTERCARD

                    else -> CardBrand.UNKNOWN
                }
            }
        }catch (e: Exception){
            e.printStackTrace()
        }
        return cardBrand
    }
}