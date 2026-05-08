package com.analogics.paymentservicecore.domain.repository.emvService

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import com.eazypaytech.paymentservicecore.constants.AppConstants
import com.eazypaytech.paymentservicecore.constants.EmvConstants
import com.analogics.paymentservicecore.data.listeners.requestListener.IEmvServiceRequestListener
import com.analogics.paymentservicecore.data.listeners.responseListener.IApiServiceResponseListener
import com.analogics.paymentservicecore.data.listeners.responseListener.IEmvServiceResponseListener
import com.analogics.paymentservicecore.data.model.PaymentServiceTxnDetails
import com.analogics.paymentservicecore.data.model.emv.AidConfig
import com.analogics.paymentservicecore.data.model.emv.CAPKey
import com.analogics.paymentservicecore.data.model.emv.CardBrand
import com.analogics.paymentservicecore.data.model.emv.CardCheckMode
import com.analogics.paymentservicecore.data.model.emv.CardEntryMode
import com.analogics.paymentservicecore.data.model.emv.EmvServiceResult.CardCheckResult
import com.analogics.paymentservicecore.data.model.emv.EmvServiceResult.CardCheckStatus
import com.analogics.paymentservicecore.data.model.emv.EmvServiceResult.DisplayMsgId
import com.analogics.paymentservicecore.data.model.emv.EmvServiceResult.InitResult
import com.analogics.paymentservicecore.data.model.emv.EmvServiceResult.InitStatus
import com.analogics.paymentservicecore.data.model.emv.EmvServiceResult.TransResult
import com.analogics.paymentservicecore.data.model.emv.EmvServiceResult.TransStatus
import com.analogics.paymentservicecore.data.model.emv.TermConfig
import com.analogics.paymentservicecore.data.model.emv.TransConfig
import com.analogics.paymentservicecore.data.model.error.ApiServiceError
import com.analogics.paymentservicecore.data.model.error.ApiServiceTimeout
import com.analogics.paymentservicecore.data.model.error.EmvServiceException
import com.analogics.paymentservicecore.data.model.toEmvTransType
import com.analogics.paymentservicecore.domain.repository.apiService.ApiServiceRepository
import com.analogics.paymentservicecore.utils.toDecimalFormat
import com.eazypaytech.hardwarecore.domain.listener.responseListener.IEmvSdkResponseListener
import com.eazypaytech.hardwarecore.data.model.EmvSdkException
import com.eazypaytech.hardwarecore.data.model.EmvSdkResult
import com.eazypaytech.hardwarecore.domain.repository.EmvSdkRequestRepository
import com.eazypaytech.hardwarecore.utils.TlvUtils
//import com.eazypaytech.paymentservicecore.listeners.requestListener.IEmvServiceRequestListener
//import com.eazypaytech.paymentservicecore.listeners.responseListener.IApiServiceResponseListener
//import com.eazypaytech.paymentservicecore.listeners.responseListener.IEmvServiceResponseListener
//import com.eazypaytech.paymentservicecore.model.PaymentServiceTxnDetails
//import com.eazypaytech.paymentservicecore.model.emv.AidConfig
//import com.eazypaytech.paymentservicecore.model.emv.CAPKey
//import com.eazypaytech.paymentservicecore.model.emv.CardBrand
//import com.eazypaytech.paymentservicecore.model.emv.CardCheckMode
//import com.eazypaytech.paymentservicecore.model.emv.CardEntryMode
//import com.eazypaytech.paymentservicecore.model.emv.EmvServiceResult.CardCheckResult
//import com.eazypaytech.paymentservicecore.model.emv.EmvServiceResult.CardCheckStatus
//import com.eazypaytech.paymentservicecore.model.emv.EmvServiceResult.DisplayMsgId
//import com.eazypaytech.paymentservicecore.model.emv.EmvServiceResult.InitResult
//import com.eazypaytech.paymentservicecore.model.emv.EmvServiceResult.InitStatus
//import com.eazypaytech.paymentservicecore.model.emv.EmvServiceResult.TransResult
//import com.eazypaytech.paymentservicecore.model.emv.EmvServiceResult.TransStatus
//import com.eazypaytech.paymentservicecore.model.emv.TermConfig
//import com.eazypaytech.paymentservicecore.model.emv.TransConfig
//import com.eazypaytech.paymentservicecore.model.error.ApiServiceError
//import com.eazypaytech.paymentservicecore.model.error.ApiServiceTimeout
//import com.eazypaytech.paymentservicecore.model.error.EmvServiceException
//import com.eazypaytech.paymentservicecore.models.toEmvTransType
//import com.eazypaytech.paymentservicecore.repository.apiService.ApiServiceRepository
//import com.eazypaytech.paymentservicecore.utils.toDecimalFormat
//import com.eazypaytech.tpaymentcore.listener.responseListener.IEmvSdkResponseListener
//import com.eazypaytech.tpaymentcore.model.emv.EmvSdkException
//import com.eazypaytech.tpaymentcore.model.emv.EmvSdkResult
//import com.eazypaytech.tpaymentcore.repository.EmvSdkRequestRepository
//
//import com.eazypaytech.tpaymentcore.utils.TlvUtils
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject

class EmvServiceRepository @Inject constructor(@ApplicationContext context: Context, var apiServiceRepository: ApiServiceRepository) :
    IEmvServiceRequestListener,
    IEmvSdkResponseListener {
    private val emvSdkRequestRepository = EmvSdkRequestRepository(context,this)
    lateinit var iEmvServiceResponseListener: IEmvServiceResponseListener
    lateinit var paymentServiceTxnDetails : PaymentServiceTxnDetails

    /**
     * Maps SDK InitStatus → App InitStatus
     */
    fun sdkToEmvInitStatus(value: EmvSdkResult.InitStatus) : InitStatus {
        return when (value) {
            EmvSdkResult.InitStatus.SUCCESS -> InitStatus.SUCCESS
            else -> InitStatus.FAILURE
        }
    }

    /**
     * Maps SDK CardCheckStatus → App CardCheckStatus
     *
     * Used during card detection flow (insert / tap / swipe / errors)
     */
    fun sdkToEmvCardCheckStatus(value: EmvSdkResult.CardCheckStatus) : CardCheckStatus {
        return when (value) {
            EmvSdkResult.CardCheckStatus.NO_CARD_DETECTED -> CardCheckStatus.NO_CARD_DETECTED
            EmvSdkResult.CardCheckStatus.CARD_INSERTED -> CardCheckStatus.CARD_INSERTED
            EmvSdkResult.CardCheckStatus.CHIP_CARD_SWIPED -> CardCheckStatus.CHIP_CARD_SWIPED
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

    /**
     * Maps SDK TransStatus → App TransStatus
     *
     * Represents final transaction outcome (online/offline, approvals, failures)
     */
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

    /**
     * Maps SDK DisplayMsgId → App DisplayMsgId
     *
     * Used for UI messaging during EMV flow (insert card, error, processing, etc.)
     */
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
            EmvSdkResult.DisplayMsgId.INSERT_OR_TAP_CARD -> DisplayMsgId.CHIP_CARD_SWIPED
            EmvSdkResult.DisplayMsgId.PROCESSING_ONLINE -> DisplayMsgId.PROCESSING_ONLINE
        }
    }

    /**
     * Converts CardCheckStatus → DisplayMsgId
     *
     * Used to directly show UI messages during card detection phase
     */
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

    /**
     * Adjusts TransStatus based on DisplayMsgId context.
     *
     * Used mainly for TERMINATED flows where UI message
     * defines the actual reason of termination.
     */
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

    /**
     * Maps TransStatus → DisplayMsgId (for UI rendering)
     */
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

    /**
     * Central SDK → App response mapper.
     *
     * Converts raw SDK result objects into unified app-level response models:
     * - InitResult
     * - CardCheckResult
     * - TransResult
     * - Exceptions
     *
     * Acts as a single translation layer between SDK and business logic.
     */
    fun sdkToEmvService(response: Any) : Any
    {
        return when(response) {
            is EmvSdkResult.InitResult -> {
                InitResult(
                    status = sdkToEmvInitStatus(response.status as EmvSdkResult.InitStatus)
                )
            }
            is EmvSdkResult.CardCheckResult -> {
                Log.d("EMV_CARD_CHECK", Gson().toJson(response))
                var status = sdkToEmvCardCheckStatus(response.status as EmvSdkResult.CardCheckStatus)
                CardCheckResult(
                    status = status,
                    displayMsgId = emvCardCheckStatusToDisplayMsgId(status)
                )
            }
            is EmvSdkResult.TransResult -> {
                //Log.d("EMV_CARD_CHECK", Gson().toJson(response))
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
                    displayMsgId = displayMsgId,
                    paymentServiceTxnDetails = paymentServiceTxnDetails
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

    /**
     * Handles all EMV SDK response events (generic dispatcher).
     *
     * Responsibilities:
     * 1. Converts SDK response → app-level response using mapping layer
     * 2. Updates transaction metadata (card entry mode)
     * 3. Aborts payment after transaction completion event
     *
     * Flow:
     * SDK → onEmvSdkResponse → mapped response → UI/business layer
     */
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

    /**
     * Handles EMV Online Authorization request.
     *
     * Triggered when EMV kernel requires host authorization.
     *
     * Flow:
     * EMV Kernel → Online Request → Host API → Response → EMV continuation
     */
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

    /**
     * Initializes EMV Payment SDK with configuration payloads.
     *
     * Responsibilities:
     * 1. Parse JSON-based AID and CAP key configuration
     * 2. Convert them into SDK-compatible models
     * 3. Apply terminal overrides (merchant, timeout, etc.)
     * 4. Initialize SDK repository
     *
     * Inputs:
     * - termConfig: Terminal-level overrides
     * - aidConfig: AID configuration JSON
     * - capKeys: CAP keys JSON array string
     */
    override fun initPaymentSDK(
        termConfig: TermConfig?,
        aidConfig: String?,
        capKeys: String?,
        iEmvServiceResponseListener: IEmvServiceResponseListener
    ) {
        try {
            this.iEmvServiceResponseListener = iEmvServiceResponseListener
            val jsonCapKeys = JSONObject(capKeys?:"").getJSONArray(AppConstants.EMV_CAP_KEY_ARRAY_FIELD_NAME).toString()

            var sdkAidConfig : com.eazypaytech.hardwarecore.data.model.AidConfig? = Gson().fromJson(aidConfig?:"",
                    com.eazypaytech.hardwarecore.data.model.AidConfig::class.java
                )

            var sdkCapKeys : List<com.eazypaytech.hardwarecore.data.model.CAPKey>? = Gson().fromJson(jsonCapKeys,
                    Array<com.eazypaytech.hardwarecore.data.model.CAPKey>::class.java
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

    /**
     * Initializes the EMV Payment SDK with terminal, AID, and CAP key configurations.
     *
     * Responsibilities:
     * 1. Converts external models (aidConfig, capKeys) into SDK-compatible models
     * 2. Overrides terminal-specific configuration parameters (if provided)
     * 3. Initializes the EMV SDK via repository
     *
     * Notes:
     * - Uses Gson-based mapping between app layer and SDK layer models
     * - Terminal config overrides take precedence over AID config defaults
     *
     * @param termConfig Terminal-specific configuration overrides (merchant, terminal ID, etc.)
     * @param aidConfig EMV AID configuration for card processing rules
     * @param capKeys List of CAP keys used for EMV cryptographic operations
     * @param iEmvServiceResponseListener Callback listener for SDK events/responses
     */
    override fun initPaymentSDK(
        termConfig: TermConfig?,
        aidConfig: AidConfig?,
        capKeys: List<CAPKey>,
        iEmvServiceResponseListener: IEmvServiceResponseListener
    ) {
        try {
            this.iEmvServiceResponseListener = iEmvServiceResponseListener
            var sdkAidConfig : com.eazypaytech.hardwarecore.data.model.AidConfig? = Gson().fromJson(Gson().toJson(aidConfig),
                    com.eazypaytech.hardwarecore.data.model.AidConfig::class.java
                )
            var sdkCapKeys : List<com.eazypaytech.hardwarecore.data.model.CAPKey>? = Gson().fromJson(
                    Gson().toJson(capKeys),
                    Array<com.eazypaytech.hardwarecore.data.model.CAPKey>::class.java
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

    /**
     * Starts an EMV payment transaction.
     *
     * Responsibilities:
     * 1. Prepares transaction configuration (amount, currency, type, card modes)
     * 2. Converts app-level transaction model → SDK-level model
     * 3. Triggers EMV SDK payment flow
     * 4. Displays initial UI state (NONE)
     *
     * @param context Android context required by EMV SDK
     * @param paymentServiceTxnDetails Transaction details (amount, type, card flags, etc.)
     * @param iEmvServiceResponseListener Callback listener for EMV events
     */
    override fun startPayment(
        context: Context,
        paymentServiceTxnDetails: PaymentServiceTxnDetails?,
        iEmvServiceResponseListener: IEmvServiceResponseListener
    ) {
        this.paymentServiceTxnDetails = paymentServiceTxnDetails?: PaymentServiceTxnDetails()
        this.iEmvServiceResponseListener = iEmvServiceResponseListener

        iEmvServiceResponseListener.onEmvServiceDisplayMessage(DisplayMsgId.NONE)
        var transConfig = getTransConfig(paymentServiceTxnDetails)

        var sdkTransConfig : com.eazypaytech.hardwarecore.data.model.TransConfig? = Gson().fromJson(
                Gson().toJson(transConfig),
                com.eazypaytech.hardwarecore.data.model.TransConfig::class.java
            )
        emvSdkRequestRepository.startPayment(context, sdkTransConfig)
    }


    /**
     * Generates PIN block using PAN and transaction amount.
     *
     * Typically used for:
     * - Online PIN entry flow
     * - Host PIN encryption process
     *
     * @param pan Primary Account Number (can be null for some card flows)
     * @param amount Transaction amount used in PIN block generation
     * @param nResult Callback returning encrypted PIN block
     */
    override fun pinGeneration(
        pan: String?,
        amount: String,
        nResult: (pinBlock: ByteArray?) -> Unit
    ) {


        emvSdkRequestRepository.pinGeneration(pan, amount, nResult)
    }


    /**
     * Checks whether a card is currently inserted/tapped/detected.
     *
     * @return true if card is present in reader, false otherwise
     */
    override fun isCardExists(context: Context): Boolean {
        return emvSdkRequestRepository.isCardExists(context)
    }

    /**
     * Aborts the current EMV transaction flow immediately.
     *
     * Used when:
     * - User cancels transaction
     * - Timeout handling
     * - UI/navigation interruption
     */
    override fun abortPayment() {
        emvSdkRequestRepository.abortPayment()
    }

    /**
     * Resolves card interaction mode based on terminal capabilities.
     *
     * Examples:
     * - EMV + NFC → Insert / Tap / Swipe
     * - EMV only → Insert / Swipe
     * - NFC only → Tap / Swipe
     *
     * Default fallback: Swipe only
     */
    private fun resolveCardCheckMode(isEMVEnable: Boolean?, isTapEnable: Boolean?, isFallback: Boolean?): CardCheckMode {
        val emv = isEMVEnable == true
        val tap = isTapEnable == true
        val fallBack = isFallback == true

        val result = when {
            fallBack -> CardCheckMode.SWIPE
            emv && tap -> CardCheckMode.SWIPE_OR_INSERT_OR_TAP
            emv && !tap -> CardCheckMode.SWIPE_OR_INSERT
            !emv && tap -> CardCheckMode.SWIPE_OR_TAP
            else -> CardCheckMode.SWIPE
        }

        Log.d("CardCheckMode", "========> resolveCardCheckMode = $result")
        return result
    }

    /**
     * Builds EMV transaction configuration object.
     *
     * Includes:
     * - Amount & cashback formatting
     * - Currency handling
     * - Transaction type mapping
     * - Card interaction mode resolution
     * - Default EMV constraints (PIN, DRL, timeout)
     */
    private fun getTransConfig(paymentServiceTxnDetails: PaymentServiceTxnDetails?) : TransConfig
    {
        val isEMVEnable = paymentServiceTxnDetails?.isEMVEnable
        val isTapEnable = paymentServiceTxnDetails?.isTapEnable
        val isFallback = paymentServiceTxnDetails?.isFallback
        val timeout = AppConstants.CARD_CHECK_TIMEOUT_S.toString()
        return TransConfig(
            amount = (paymentServiceTxnDetails?.txnAmount?.toDoubleOrNull()?:0.00).toDecimalFormat(),
            cashbackAmount = (paymentServiceTxnDetails?.cashback?.toDoubleOrNull()?:0.00).toDecimalFormat(),
            currencyCode = paymentServiceTxnDetails?.currencyCode?: AppConstants.DEFAULT_CURRENCY_CODE,
            transactionType = paymentServiceTxnDetails?.txnType?.toEmvTransType(),
            cardCheckMode = resolveCardCheckMode(isEMVEnable, isTapEnable, isFallback),
            cardCheckTimeout = timeout,
            forceOnlinePin = true,
            supportDRL = false,
            isFallback = paymentServiceTxnDetails?.isFallback

        )
    }

    /**
     * Handles EMV "Go Online" request flow.
     *
     * This function is responsible for:
     * 1. Extracting EMV receipt and transaction info from tags
     * 2. Preparing TLV data for host communication
     * 3. Initiating online authorization request
     * 4. Handling success, timeout, and failure scenarios
     * 5. Performing reversal attempts (up to 3 retries) if needed
     * 6. Returning final EMV response tags via callback
     *
     * Flow Summary:
     * EMV Tags → Extract Info → Build TLV → Online Auth →
     *     ├── Success → return response TLV
     *     ├── Error → attempt reversal (max 3 tries)
     *     └── Timeout → attempt reversal (max 3 tries)
     *
     * @param emvTags Raw EMV tag map received from terminal/card
     * @param onResponse Callback returning final EMV response tags to EMV kernel
     */
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
                    CoroutineScope(Dispatchers.IO).launch {

                        var attempt = 0
                        var isSuccess = false

                        while (attempt < 3 && !isSuccess) {
                            attempt++
                            val result = CompletableDeferred<Boolean>()
                            apiServiceRepository.apiServiceReversal(
                                paymentServiceTxnDetails,
                                object : IApiServiceResponseListener {
                                    override fun onApiServiceSuccess(response: PaymentServiceTxnDetails) {
                                        paymentServiceTxnDetails =
                                            response.copy(emvData = paymentServiceTxnDetails.emvData)
                                        val responseTags =
                                            TlvUtils(response.emvData).tlvMap
                                        isSuccess = true
                                        result.complete(true)
                                        onResponse(responseTags)
                                    }
                                    override fun onApiServiceError(error: ApiServiceError) {
                                        result.complete(false)
                                    }

                                    override fun onApiServiceTimeout(apiServiceTimeout: ApiServiceTimeout) {
                                        result.complete(false)
                                    }
                                }
                            )

                            val success = result.await()

                            if (success) break
                        }

                        if (!isSuccess) {
                            val declineTags = hashMapOf(
                                EmvConstants.EMV_TAG_RESP_CODE to
                                        EmvConstants.EMV_TAG_VAL_UNABLE_TO_GO_ONLINE_DECLINE
                            )

                            onResponse(declineTags)
                        }
                    }
                }

                override fun onApiServiceTimeout(apiServiceTimeout: ApiServiceTimeout) {
                    CoroutineScope(Dispatchers.IO).launch {

                        var attempt = 0
                        var isSuccess = false

                        while (attempt < 3 && !isSuccess) {
                            attempt++

                            Log.d("EMV", "Reversal Attempt: $attempt")

                            val result = CompletableDeferred<Boolean>()

                            apiServiceRepository.apiServiceReversal(
                                paymentServiceTxnDetails,
                                object : IApiServiceResponseListener {

                                    override fun onApiServiceSuccess(response: PaymentServiceTxnDetails) {
                                        Log.d("EMV","Reversal Success")

                                        paymentServiceTxnDetails =
                                            response.copy(emvData = paymentServiceTxnDetails.emvData)

                                        val responseTags =
                                            TlvUtils(response.emvData).tlvMap

                                        isSuccess = true
                                        result.complete(true)

                                        onResponse(responseTags)
                                    }

                                    override fun onApiServiceError(error: ApiServiceError) {
                                        result.complete(false)
                                    }

                                    override fun onApiServiceTimeout(apiServiceTimeout: ApiServiceTimeout) {
                                        result.complete(false)
                                    }
                                }
                            )

                            val success = result.await()

                            if (success) break
                        }

                        if (!isSuccess) {
                            val declineTags = hashMapOf(
                                EmvConstants.EMV_TAG_RESP_CODE to
                                        EmvConstants.EMV_TAG_VAL_UNABLE_TO_GO_ONLINE_DECLINE
                            )

                            onResponse(declineTags)
                        }
                    }
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

    /**
     * Prepares TLV data required for host communication.
     * Extracts sensitive fields separately and builds TLV for remaining tags.
     */
    private fun prepareHostTlvData(emvTags: HashMap<String, String>)
    {
        try {
            //Log.d("DEBUG_EMV_TAGS", "Input EMV Tags: $emvTags")
            /* Masked PAN */
            if (emvTags.containsKey(EmvConstants.EMV_TAG_PAN)) {
                paymentServiceTxnDetails.cardMaskedPan = emvTags[EmvConstants.EMV_TAG_PAN].toString()
                emvTags.remove(EmvConstants.EMV_TAG_PAN)
            }


            /* Encrypted PAN */
            if (emvTags.containsKey(EmvConstants.EMV_TAG_ENC_PAN)) {
                paymentServiceTxnDetails.cardPan = emvTags[EmvConstants.EMV_TAG_ENC_PAN]
                emvTags.remove(EmvConstants.EMV_TAG_ENC_PAN)
            }

            /* Encrypted Track2 */
            if (emvTags.containsKey(EmvConstants.EMV_TAG_TRACK2)) {
                paymentServiceTxnDetails.trackData = emvTags[EmvConstants.EMV_TAG_TRACK2]
                emvTags.remove(EmvConstants.EMV_TAG_TRACK2)
            }
            //Log.d("DEBUG_TRACK2", "Plain Track2 : ${paymentServiceTxnDetails.trackData}")
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

    /**
     * Determines card brand using:
     * 1. AID (preferred, EMV standard)
     * 2. PAN (fallback BIN range)
     */
    fun getCardBrand(aid : String?=null, pan : String?=null) : CardBrand?
    {
        var cardBrand : CardBrand? = CardBrand.UNKNOWN
        try {

            aid?.takeIf { it.length>=10 }?.let {
                return when(it.substring(0,10))
                {
                    "A000000003" -> CardBrand.VISA
                    "A000000004" -> CardBrand.EBT
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
                    in 510000..559999, in 222100..272099 -> CardBrand.EBT

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
                    in 500000..509999, in 560000..699999 -> CardBrand.EBT

                    else -> CardBrand.UNKNOWN
                }
            }
        }catch (e: Exception){
            e.printStackTrace()
        }
        return cardBrand
    }

}