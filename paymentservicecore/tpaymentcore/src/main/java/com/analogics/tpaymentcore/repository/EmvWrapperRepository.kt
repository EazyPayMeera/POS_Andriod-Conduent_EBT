package com.analogics.tpaymentcore.repository

import android.content.Context
import android.device.SEManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.IInputActionListener
import android.text.TextUtils
import android.util.Log
import com.analogics.paymentservicecore.constants.EmvConstants
import com.analogics.tpaymentcore.constants.EncryptionConstants
import com.analogics.tpaymentcore.listener.requestListener.IEmvWrapperRequestListener
import com.analogics.tpaymentcore.listener.responseListener.IEmvSdkResponseListener
import com.analogics.tpaymentcore.model.emv.AidConfig
import com.analogics.tpaymentcore.model.emv.CAPKey
import com.analogics.tpaymentcore.model.emv.CardCheckMode
import com.analogics.tpaymentcore.model.emv.EmvSdkException
import com.analogics.tpaymentcore.model.emv.EmvSdkResult
import com.analogics.tpaymentcore.model.emv.EmvSdkResult.InitResult
import com.analogics.tpaymentcore.model.emv.EmvSdkResult.InitStatus
import com.analogics.tpaymentcore.model.emv.EmvSdkResult.TransStatus
import com.analogics.tpaymentcore.model.emv.TransConfig
import com.analogics.tpaymentcore.utils.TlvUtils
import com.urovo.i9000s.api.emv.ContantPara
import com.urovo.i9000s.api.emv.EmvListener
import com.urovo.i9000s.api.emv.EmvNfcKernelApi
import com.urovo.i9000s.api.emv.Funs
import com.urovo.sdk.pinpad.PinPadProviderImpl
import com.urovo.sdk.pinpad.listener.PinInputListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Hashtable
import java.util.Locale
import javax.inject.Inject
import kotlin.collections.contentToString
import kotlin.collections.set
import kotlin.text.decodeToString
import kotlin.text.substring
import kotlin.text.toInt
import kotlin.text.uppercase
import kotlin.toString

class EmvWrapperRepository @Inject constructor(override var iEmvSdkResponseListener: IEmvSdkResponseListener) :
    IEmvWrapperRequestListener {

        fun overrideTerminalConfig(aidConfig: AidConfig?) : Boolean
        {
            var result = false
            try {
                var termTlvParams = TlvUtils()
                    .addTagValAscii(EmvConstants.EMV_TAG_MERCH_ID, aidConfig?.merchantIdentifier,15,15)
                    .addTagValAscii(EmvConstants.EMV_TAG_TERM_ID, aidConfig?.terminalIdentifier,8,8)
                    .addTagValAscii(EmvConstants.EMV_TAG_MERCH_NAME_LOC, aidConfig?.merchantNameLocation,0,40)
                    .addTagValHex(EmvConstants.EMV_TAG_MERCH_CATEGORY_CODE, aidConfig?.merchantCategoryCode,2,2)
                    .addTagValHex(EmvConstants.EMV_TAG_IFD_SERIAL_NO, aidConfig?.ifdSerialNumber,8,8)
                    .addTagValHex(EmvConstants.EMV_TAG_TERM_CAP, aidConfig?.terminalCapabilities,3,3)
                    .addTagValHex(EmvConstants.EMV_TAG_ADDL_TERM_CAP, aidConfig?.addlTerminalCapabilities,5,5)
                    .addTagValHex(EmvConstants.EMV_TAG_TERM_COUNTRY_CODE, aidConfig?.terminalCountryCode,2,2)
                    .addTagValHex(EmvConstants.EMV_TAG_TERM_TYPE, aidConfig?.terminalType,1,1)
                    .addTagValHex(EmvConstants.EMV_TAG_TRANS_CURRENCY_EXPONENT, aidConfig?.currencyExponent,1,1)
                    .addTagValBoolean(EmvConstants.EMV_TAG_SUPPORT_RANDOM_TRANS, aidConfig?.enableRandomTrans)
                    .addTagValBoolean(EmvConstants.EMV_TAG_SUPPORT_EXCEP_FILE_CHECK, aidConfig?.supportExceptionFile)
                    .addTagValBoolean(EmvConstants.EMV_TAG_SUPPORT_SM, aidConfig?.supportSM)
                    .addTagValBoolean(EmvConstants.EMV_TAG_SUPPORT_VELOCITY_CHECK, aidConfig?.supportVelocityCheck)
                    .toTlvString()

                result = EmvNfcKernelApi.getInstance().updateTerminalParamters(
                    ContantPara.CardSlot.UNKNOWN,
                    termTlvParams
                )

                EmvNfcKernelApi.getInstance().LogOutEnable(if(aidConfig?.enableEmvLogs == true) EmvConstants.UROVO_SDK_EMV_LOG_ENABLE else EmvConstants.UROVO_SDK_EMV_LOG_DISABLE)

                Log.d("EMV_APP", "Term Config Override: $termTlvParams")
            }catch (exception : Exception)
            {
                Log.e("EMV_APP", exception.message.toString())
            }
            return result
        }

    override fun initializeSdk(aidConfig : AidConfig?, capKeys: List<CAPKey>?) {
        Thread {
            var result = true
            try {
                aidConfig?.let { result = result && initAidConfig(it) && overrideTerminalConfig(it) }
                capKeys?.let { result = result && initCAPKeys(it) }

                when(result) {
                    true->
                        iEmvSdkResponseListener.onEmvSdkResponse(InitResult(InitStatus.SUCCESS))//DF02---random trans select enable  DF03--Except file check enable DF04--Support SM DF05-- Valocity Check enable
                    else ->
                        iEmvSdkResponseListener.onEmvSdkResponse(InitResult(InitStatus.FAILURE))//DF02---random trans select enable  DF03--Except file check enable DF04--Support SM DF05-- Valocity Check enable
                }
            } catch (e: Exception) {
                e.printStackTrace()
                iEmvSdkResponseListener.onEmvSdkResponse(EmvSdkException(e.message.toString()))
            }
        }.start()
    }

    companion object : EmvListener, PinInputListener {
        var iEmvSdkResponseListener: IEmvSdkResponseListener? = null
        var job : Job? = null
        var _amount : Long = 0L
        var _cashbackAmount : Long = 0L
        var pinBlock : String? = null
        var ksn : String? = null
        var nfcTlv : String? = null

        fun resetTransData()
        {
            /* Interrupt existing thread if any */
            job?.cancel()
            job = null

            /* Reset transaction parameters */
            _amount = 0L
            _cashbackAmount = 0L
            pinBlock = null
            ksn = null
            nfcTlv = null
        }

        fun startPayment(context: Context, transConfig: TransConfig?, iEmvSdkResponseListener: IEmvSdkResponseListener) {
            resetTransData()
            /*thread = Thread {*/
                try {
                    this.iEmvSdkResponseListener = iEmvSdkResponseListener
                    val data = Hashtable<String, Any>()
                    transConfig?.let {
                        it.amount?.let { data["amount"] = it; _amount = it.toLongOrNull()?:0L }
                        it.cashbackAmount?.let { data["cashbackAmount"] = it; _cashbackAmount = it.toLongOrNull()?:0L }
                        it.currencyCode?.let { data["currencyCode"] = it.takeLast(3) }
                        it.transactionType?.let { data["transactionType"] = it.takeLast(2) }    //00-goods 01-cash 09-cashback 20-refund
                        (it.cardCheckMode?: CardCheckMode.SWIPE_OR_INSERT_OR_TAP).let { data["checkCardMode"] = it.sdkValue }
                        it.cardCheckTimeout?.let { data["checkCardTimeout"] = it }
                        it.enableBeeper?.let { data["enableBeeper"] = it }
                        it.supportFallback?.let { if(it == true) data["FallbackSwitch"] = "1" else data["FallbackSwitch"] = "0"}
                        it.supportDRL?.let { data["supportDRL"] = it }
                        data["emvOption"] = if(it.forceOnline == true) ContantPara.EmvOption.START_WITH_FORCE_ONLINE else ContantPara.EmvOption.START // START_WITH_FORCE_ONLINE
                        data["isEnterAmtAfterReadRecord"] = false
                    }
                    initEncryption()
                    EmvNfcKernelApi.getInstance().setContext(context)
                    EmvNfcKernelApi.getInstance().setListener(this)

                    job = CoroutineScope(Dispatchers.Default).launch {
                        EmvNfcKernelApi.getInstance().startKernel(data)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    this.iEmvSdkResponseListener?.onEmvSdkResponse(EmvSdkException(errorMessage = e.message.toString()))
                }
/*
            }
            thread?.start()
*/
        }

        @OptIn(ExperimentalStdlibApi::class)
        fun getEmvTag(tag : String?) : String?
        {
            var tagVal: String? = null
            try {
                tag?.let {
                    EmvNfcKernelApi.getInstance().getValByTag(tag.hexToInt())?.let {
                        tagVal = it
                    }
                }
            }catch (e: Exception)
            {
                e.printStackTrace()
            }
            return tagVal
        }

        fun abortPayment()
        {
            job?.cancel()
            job = null
            EmvNfcKernelApi.getInstance().abortKernel()
        }

        fun encryptThenRequestOnline(p0: String?, p1: String?=null)
        {
            try {
                var tlvMap = TlvUtils(p0).tlvMap.apply {
                    for (tlv in getEncryptedData())
                        put(tlv.key, tlv.value)
                }
                iEmvSdkResponseListener?.onEmvSdkOnlineRequest(tlvMap) {
                    var tlvTags = TlvUtils(it)
                    var hasOnlineResp = tlvTags.tlvMap.containsKey(EmvConstants.EMV_TAG_RESP_CODE)
                    EmvNfcKernelApi.getInstance()
                        .sendOnlineProcessResult(hasOnlineResp, tlvTags.toTlvString())
                }
            }catch (e: Exception)
            {
                e.printStackTrace()
            }
        }

        fun initEncryption()
        {
            var ksnBytes = ByteArray(EncryptionConstants.DUKPT_KSN_MAX_LENGTH/2)
            PinPadProviderImpl.getInstance().DukptGetKsn(EncryptionConstants.DUKPT_KEY_SET_TDK,ksnBytes)
            PinPadProviderImpl.getInstance().DukptGetKsn(EncryptionConstants.DUKPT_KEY_SET_EMV,ksnBytes)
            PinPadProviderImpl.getInstance().DukptGetKsn(EncryptionConstants.DUKPT_KEY_SET_PIN,ksnBytes)
            PinPadProviderImpl.getInstance().DukptGetKsn(EncryptionConstants.DUKPT_KEY_SET_MAC,ksnBytes)
            pinBlock = null
        }

        @OptIn(ExperimentalStdlibApi::class)
        fun getEncryptedData() : HashMap<String,String>
        {
            var hashMap = HashMap<String,String>()
            var trackData = EmvNfcKernelApi.getInstance().getValByTag(EmvConstants.EMV_TAG_TRACK2_HEX).replace('D','=').removeSuffix("F")
            var cardPan = trackData.substringBefore('=')

            /* Clear PAN is OK to send to service layer. Service layer will filter it */
            hashMap[EmvConstants.EMV_TAG_PAN] = cardPan

            trackData.takeIf {
                (it.length % 8) !=0 }?.let {
                    trackData = it.padStart(it.length + (8-it.length%8),'0')
                }

            cardPan.takeIf {
                (it.length % 8) !=0 }?.let {
                cardPan = it.padStart(it.length + (8-it.length%8),'0')
            }

            var trackDataBytes = trackData.toByteArray()
            var cardPanBytes = cardPan.toByteArray()
            var encryptedBytes = ByteArray(trackDataBytes.size)
            var encryptedLen = IntArray(1)
            var ksnBytes = ByteArray(EncryptionConstants.DUKPT_KSN_MAX_LENGTH/2)
            var ksnLen = IntArray(1)
            var ivBytes = ByteArray(EncryptionConstants.TDES_IV_LENGTH)

            /* Encrypt Track2 */
            if(PinPadProviderImpl.getInstance().DukptEncryptDataIV(EncryptionConstants.DUKPT_KEY_TYPE_TRACK_DATA,
                    EncryptionConstants.DUKPT_KEY_SET_PIN, EncryptionConstants.DUKPT_MODE_ENCRYPT_ECB,
                    ivBytes,ivBytes.size,
                    trackDataBytes ,trackDataBytes.size,
                    encryptedBytes, encryptedLen,
                    ksnBytes, ksnLen
                ) == 0)
            {
                hashMap[EmvConstants.EMV_TAG_ENC_TRACK] = encryptedBytes.toHexString().uppercase()
                hashMap[EmvConstants.EMV_TAG_ENC_KSN] = ksnBytes.slice(0 until ksnLen[0]).toByteArray().toHexString().uppercase()
                Log.d("ENCRYPTION", "INPUT TRACK DATA (ASCII)    : "+trackDataBytes.decodeToString())
                Log.d("ENCRYPTION", "ENCRYPTED TRACK DATA (LYRA) : "+encryptedBytes.toHexString().uppercase())
                Log.d("ENCRYPTION", "KSN TRACK DATA (LYRA)       : "+ksnBytes.slice(0 until ksnLen[0]).toByteArray().toHexString().uppercase())
            }

            /* Encrypt PAN */
            if(PinPadProviderImpl.getInstance().DukptEncryptDataIV(EncryptionConstants.DUKPT_KEY_TYPE_TRACK_DATA,
                    EncryptionConstants.DUKPT_KEY_SET_PIN, EncryptionConstants.DUKPT_MODE_ENCRYPT_ECB,
                    ivBytes,ivBytes.size,
                    cardPanBytes ,cardPanBytes.size,
                    encryptedBytes, encryptedLen,
                    ksnBytes, ksnLen
                ) == 0)
            {
                hashMap[EmvConstants.EMV_TAG_ENC_PAN] = encryptedBytes.sliceArray(0 until encryptedLen[0]).toHexString().uppercase()
                Log.d("ENCRYPTION", "INPUT PAN (ASCII)    : "+cardPanBytes.decodeToString())
                Log.d("ENCRYPTION", "ENCRYPTED PAN (LYRA) : "+encryptedBytes.sliceArray(0 until encryptedLen[0]).toHexString().uppercase())
                Log.d("ENCRYPTION", "KSN PAN (LYRA)       : "+ksnBytes.slice(0 until ksnLen[0]).toByteArray().toHexString().uppercase())
            }

            /* Set Pin Block */
            pinBlock?.let {
                hashMap[EmvConstants.EMV_TAG_ENC_PIN_BLOCK] = it
            }

            return hashMap
        }

        override fun onRequestSetAmount() {
            Log.d("EMV_APP", "Request Amount:$_amount")
            EmvNfcKernelApi.getInstance().setAmountEx(_amount, _cashbackAmount)
        }

        override fun onReturnCheckCardResult(
            p0: ContantPara.CheckCardResult?,
            p1: Hashtable<String, String>?
        ) {
            Log.d("EMV_APP", "Check Card Result:" + p0?.toString())
            Log.d("EMV_APP", "Check Card List:" + p1?.toString())

            iEmvSdkResponseListener?.onEmvSdkResponse(EmvSdkResult.CardCheckResult(status = urovoToCheckCardStatus(p0)))
        }

        override fun onRequestSelectApplication(p0: ArrayList<String>?) {
            Log.d("EMV_APP", "Select Applications:" + p0.toString())
        }

        override fun onRequestPinEntry(p0: ContantPara.PinEntrySource?) {
            Log.d("EMV_APP", "Online PIN Prompt:" + p0.toString())
            if (p0 == ContantPara.PinEntrySource.KEYPAD) {
                emv_proc_onlinePin(true)
                Log.i("EMV_APP", "MainActivity  emv_proc_onlinePin over")
            }
        }

        override fun onRequestOfflinePinEntry(p0: ContantPara.PinEntrySource?, p1: Int) {
            Log.d("EMV_APP", "Offline PIN Prompt:" + p0.toString())
        }

        override fun onRequestConfirmCardno() {
            EmvNfcKernelApi.getInstance().sendConfirmCardnoResult(true)
        }

        override fun onRequestFinalConfirm() {
            EmvNfcKernelApi.getInstance().sendFinalConfirmResult(true)
        }

        override fun onRequestOnlineProcess(p0: String?, p1: String?) {
            Log.d("EMV_APP", "Process Online:" + p0?.toString() + "\n" + p1?.toString())
            encryptThenRequestOnline(p0,p1)
        }

        override fun onReturnBatchData(p0: String?) {
            Log.d("EMV_APP", "Batch Data:" + p0.toString())
        }

        override fun onReturnTransactionResult(p0: ContantPara.TransactionResult?) {
            Log.d("EMV_APP", "Transaction Result:" + p0.toString())
            Log.d("EMV_APP", "TLV Data:" + EmvNfcKernelApi.getInstance().GetField55ForSAMA())
            iEmvSdkResponseListener?.onEmvSdkResponse(EmvSdkResult.TransResult(urovoToEmvTransResult(p0)))
        }

        override fun onRequestDisplayText(p0: ContantPara.DisplayText?) {
            Log.d("EMV_APP", "***** DISPLAY *****\n" + p0.toString() + "*******************")
        }

        override fun onRequestOfflinePINVerify(
            p0: ContantPara.PinEntrySource?,
            p1: Int,
            p2: Bundle?
        ) {
            Log.d("EMV_APP", "Offline PIN Verify:" + p0.toString())
            if (p0 == ContantPara.PinEntrySource.KEYPAD) { //use in os 8.0 or above
                //pinEntryType 0-offline plain pin ,1-offline encrypt pin
                val pinTryTimes: Int = EmvNfcKernelApi.getInstance().getOfflinePinTryTimes()
                p2?.putInt("PinTryTimes", pinTryTimes)
                p2?.putBoolean("isFirstTime", true)
                if (pinTryTimes == 1) proc_offlinePin(p1, true, p2!!)
                else {
                    proc_offlinePin(p1, false, p2!!)
                }
            }
        }

        override fun onReturnIssuerScriptResult(p0: ContantPara.IssuerScriptResult?, p1: String?) {
            Log.d("EMV_APP", "Issuer Script Result:" + p0.toString())
        }

        override fun onNFCrequestTipsConfirm(p0: ContantPara.NfcTipMessageID?, p1: String?) {
            Log.d("EMV_APP", "NFC Request Tip:" + p0.toString())
        }

        override fun onReturnNfcCardData(p0: Hashtable<String, String>?) {
            try {
                p0?.containsKey(EmvConstants.UROVO_SDK_KEY_EMV_DATA)?.takeIf { it == true }?.let {
                    nfcTlv = TlvUtils(p0[EmvConstants.UROVO_SDK_KEY_EMV_DATA]).toTlvString()
                }
                Log.d("EMV_APP", "NFC Card Data:" + p0?.toString())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        override fun onNFCrequestOnline() {
            try {
                Log.d("EMV_APP", "NFC Process Online:" + nfcTlv)
                encryptThenRequestOnline(nfcTlv)

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        override fun onNFCrequestImportPin(p0: Int, p1: Int, p2: String?) {
            Log.d("EMV_APP", "NFC Import PIN:" + p2.toString())
            EmvNfcKernelApi.getInstance().sendPinEntry()
        }

        override fun onNFCTransResult(p0: ContantPara.NfcTransResult?) {
            Log.d("EMV_APP", "NFC Result :" + p0?.toString())
            iEmvSdkResponseListener?.onEmvSdkResponse(EmvSdkResult.TransResult(urovoToEmvTransResult(p0)))
        }

        override fun onNFCErrorInfor(p0: ContantPara.NfcErrMessageID?, p1: String?) {
            Log.d("EMV_APP", "NFC Trans Error:" + p0?.toString())
        }

        fun urovoToEmvTransResult(transactionResult : ContantPara.TransactionResult?) : TransStatus?
        {
           return when (transactionResult) {
                ContantPara.TransactionResult.ONLINE_APPROVAL ->  TransStatus.APPROVED_ONLINE
                ContantPara.TransactionResult.OFFLINE_APPROVAL -> TransStatus.APPROVED_OFFLINE
                ContantPara.TransactionResult.ONLINE_DECLINED -> TransStatus.DECLINED_ONLINE
                ContantPara.TransactionResult.OFFLINE_DECLINED -> TransStatus.DECLINED_OFFLINE
                ContantPara.TransactionResult.CANCELED -> TransStatus.CANCELED
                ContantPara.TransactionResult.CANCELED_OR_TIMEOUT -> TransStatus.CANCELED
                ContantPara.TransactionResult.TERMINATED -> TransStatus.TERMINATED
                ContantPara.TransactionResult.CARD_BLOCKED_APP_FAIL -> TransStatus.CARD_BLOCKED
                ContantPara.TransactionResult.APPLICATION_BLOCKED_APP_FAIL -> TransStatus.APP_BLOCKED
                ContantPara.TransactionResult.NO_EMV_APPS -> TransStatus.NO_EMV_APPS
                ContantPara.TransactionResult.SELECT_APP_FAIL -> TransStatus.APP_SELECTION_FAILED
                ContantPara.TransactionResult.INVALID_ICC_DATA -> TransStatus.INVALID_ICC_CARD
                ContantPara.TransactionResult.ICC_CARD_REMOVED -> TransStatus.CARD_REMOVED

                else -> TransStatus.ERROR
            }
        }

        fun urovoToEmvTransResult(transactionResult : ContantPara.NfcTransResult?) : TransStatus?
        {
            return when (transactionResult) {
                ContantPara.NfcTransResult.ONLINE_APPROVAL ->  TransStatus.APPROVED_ONLINE
                ContantPara.NfcTransResult.OFFLINE_APPROVAL -> TransStatus.APPROVED_OFFLINE
                ContantPara.NfcTransResult.DECLINE_ONLINE -> TransStatus.DECLINED_ONLINE
                ContantPara.NfcTransResult.DECLINE_OFFLINE -> TransStatus.DECLINED_OFFLINE
                ContantPara.NfcTransResult.CARD_REMOVED -> TransStatus.CARD_REMOVED
                ContantPara.NfcTransResult.TERMINATE -> TransStatus.TERMINATED
                ContantPara.NfcTransResult.RETRY -> TransStatus.RETRY
                ContantPara.NfcTransResult.OTHER_INTERFACES -> TransStatus.TRY_ANOTHER_INTERFACE

                else -> TransStatus.ERROR
            }
        }

        fun urovoToCheckCardStatus(checkCardResult : ContantPara.CheckCardResult?) : EmvSdkResult.CardCheckStatus?
        {
            return when (checkCardResult) {
                ContantPara.CheckCardResult.INSERTED_CARD -> EmvSdkResult.CardCheckStatus.CARD_INSERTED
                ContantPara.CheckCardResult.TAP_CARD_DETECTED -> EmvSdkResult.CardCheckStatus.CARD_TAPPED
                ContantPara.CheckCardResult.MSR -> EmvSdkResult.CardCheckStatus.CARD_SWIPED
                ContantPara.CheckCardResult.NOT_ICC -> EmvSdkResult.CardCheckStatus.NOT_ICC_CARD
                ContantPara.CheckCardResult.USE_ICC_CARD -> EmvSdkResult.CardCheckStatus.USE_ICC_CARD
                ContantPara.CheckCardResult.BAD_SWIPE -> EmvSdkResult.CardCheckStatus.BAD_SWIPE
                ContantPara.CheckCardResult.NEED_FALLBACK -> EmvSdkResult.CardCheckStatus.NEED_FALLBACK
                ContantPara.CheckCardResult.MULT_CARD -> EmvSdkResult.CardCheckStatus.MULTIPLE_CARDS
                ContantPara.CheckCardResult.TIMEOUT -> EmvSdkResult.CardCheckStatus.TIMEOUT
                ContantPara.CheckCardResult.CANCEL -> EmvSdkResult.CardCheckStatus.CANCEL
                ContantPara.CheckCardResult.DEVICE_BUSY -> EmvSdkResult.CardCheckStatus.DEVICE_BUSY
                ContantPara.CheckCardResult.NO_CARD -> EmvSdkResult.CardCheckStatus.NO_CARD_DETECTED
                else -> EmvSdkResult.CardCheckStatus.ERROR

            }
        }

        fun addContactAid(config: AidConfig) : Boolean {
            var result = true
            try {
                /* Add Contact Configuration */
                for (aid in config.contact?.aidList ?: emptyList()) {
                    val aidData = Hashtable<String, String>()
                    aidData["CardType"] = "IcCard"
                    (aid.aid ?: config.contact?.aid ?: config.aid)?.let {  aidData["aid"] = it }
                    (aid.appVersion ?: config.contact?.appVersion ?: config.appVersion)?.let { aidData["appVersion"] = it }
                    (aid.terminalFloorLimit ?: config.contact?.terminalFloorLimit
                                ?: config.terminalFloorLimit)?.let { aidData["terminalFloorLimit"] = it }
                    (aid.terminalFloorLimitCheck ?: config.contact?.terminalFloorLimitCheck
                                ?: config.terminalFloorLimitCheck)?.let { aidData["terminalFloorLimitCheck"] = it }
                    (aid.tacDefault ?: config.contact?.tacDefault ?: config.tacDefault)?.let { aidData["contactTACDefault"] = it }
                    (aid.tacDenial ?: config.contact?.tacDenial ?: config.tacDenial)?.let { aidData["contactTACDenial"] = it }
                    (aid.tacOnline ?: config.contact?.tacOnline ?: config.tacOnline)?.let { aidData["contactTACOnline"] = it }
                    (aid.defaultDDOL ?: config.contact?.defaultDDOL ?: config.defaultDDOL)?.let { aidData["defaultDDOL"] = it }
                    (aid.defaultTDOL ?: config.contact?.defaultTDOL ?: config.defaultTDOL)?.let{ aidData["defaultTDOL"] = it }
                    (aid.acquirerId ?: config.contact?.acquirerId ?: config.acquirerId)?.let{ aidData["AcquirerIdentifier"] = it }
                    (aid.threshold ?: config.contact?.threshold ?: config.threshold)?.let{ aidData["ThresholdValue"] = it }
                    (aid.targetPercentage ?: config.contact?.targetPercentage ?: config.targetPercentage)?.let{ aidData["TargetPercentage"] = it }
                    (aid.maxTargetPercentage ?: config.contact?.maxTargetPercentage?: config.maxTargetPercentage)?.let{ aidData["MaxTargetPercentage"] = it }
                    (aid.appSelIndicator ?: config.contact?.appSelIndicator ?: config.appSelIndicator)?.let{ aidData["AppSelIndicator"] = it }
                    (aid.terminalAppPriority ?: config.contact?.terminalAppPriority?: config.terminalAppPriority)?.let{ aidData["TerminalAppPriority"] = it }
                    (aid.terminalCapabilities ?: config.contact?.terminalCapabilities ?: config.terminalCapabilities)?.let{ aidData["TerminalCapabilities"] = it }
                    (aid.terminalCountryCode ?: config.contact?.terminalCountryCode ?: config.terminalCountryCode)?.let{ aidData["terminalCountryCode"] = it }
                    (aid.rdrCVMRequiredLimit ?: config.contact?.rdrCVMRequiredLimit ?: config.rdrCVMRequiredLimit)?.let{ aidData["contactlessCVMRequiredLimit"] = it }
                    (aid.rdrCtlsFloorLimit ?: config.contact?.rdrCtlsFloorLimit ?: config.rdrCtlsFloorLimit)?.let{ aidData["contactlessFloorLimit"] = it }
                    (aid.rdrCtlsTransLimit ?: config.contact?.rdrCtlsTransLimit ?: config.rdrCtlsTransLimit)?.let{ aidData["contactlessTransactionLimit"] = it }

                    result = result && EmvNfcKernelApi.getInstance()
                        .updateAID(ContantPara.Operation.ADD, aidData) //master
                    aidData.clear()
                }
            }catch (exception : Exception)
            {
                result = false
                exception.printStackTrace()
            }

            return result
        }

        @OptIn(ExperimentalStdlibApi::class)
        fun addContactlessAid(config: AidConfig) : Boolean {
            var result = true
            try {
                /* Add Contactless Configuration */
                for (aid in config.contactless?.aidList ?: emptyList()) {
                    val aidData = Hashtable<String, String>()

                    (aid.aid ?: config.contactless?.aid ?: config.aid)?.let{ aidData["ApplicationIdentifier"] = it }
                    getSdkCardType(aidData["ApplicationIdentifier"]?:"").let{ aidData["CardType"] = it }
                    (aid.transactionType ?: config.contactless?.transactionType ?: config.transactionType)?.let{ aidData["TransactionType"] = it }
                    (aid.acquirerId ?: config.contactless?.acquirerId ?: config.acquirerId)?.let{ aidData["AcquirerIdentifier"] = it }
                    (aid.addlTerminalCapabilities ?: config.contactless?.addlTerminalCapabilities ?: config.addlTerminalCapabilities)?.let{ aidData["AdditionalTerminalCapabilities"] = it }
                    (aid.appVersion ?: config.contactless?.appVersion ?: config.appVersion)?.let{ aidData["ApplicationVersionNumber"] = it }
                    (aid.cardDataInputCapability ?: config.contactless?.cardDataInputCapability ?: config.cardDataInputCapability)?.let{ aidData["CardDataInputCapability"] = it }
                    (aid.kernelConfiguration ?: config.contactless?.kernelConfiguration ?: config.kernelConfiguration)?.let{ aidData["KernelConfiguration"] = it }
                    (aid.cvmCapabilityCVMRequired ?: config.contactless?.cvmCapabilityCVMRequired ?: config.cvmCapabilityCVMRequired)?.let{ aidData["CVMCapabilityPerCVMRequired"] = it }
                    (aid.cvmCapabilityNoCVMRequired ?: config.contactless?.cvmCapabilityNoCVMRequired ?: config.cvmCapabilityNoCVMRequired)?.let{ aidData["CVMCapabilityNoCVMRequired"] = it }
                    (aid.magCVMCapabilityCVMRequired ?: config.contactless?.magCVMCapabilityCVMRequired ?: config.magCVMCapabilityCVMRequired)?.let{ aidData["MagStripeCVMCapabilityCVMRequired"] = it }
                    (aid.magCVMCapabilityNoCVMRequired ?: config.contactless?.magCVMCapabilityNoCVMRequired ?: config.magCVMCapabilityNoCVMRequired)?.let{ aidData["MagStripeCVMCapabilityPerNoCVMRequired"] = it }
                    (aid.securityCapability ?: config.contactless?.securityCapability ?: config.securityCapability)?.let{ aidData["SecurityCapability"] = it }
                    (aid.ifdSerialNumber ?: config.contactless?.ifdSerialNumber ?: config.ifdSerialNumber)?.let{ aidData["IFDsn"] = it.toByteArray().toHexString() }
                    (aid.merchantCategoryCode ?: config.contactless?.merchantCategoryCode ?: config.merchantCategoryCode)?.let{ aidData["MerchantCategoryCode"] = it }
                    (aid.merchantIdentifier ?: config.contactless?.merchantIdentifier ?: config.merchantIdentifier)?.let{ aidData["MerchantIdentifier"] = it.toByteArray().toHexString() }
                    (aid.merchantNameLocation ?: config.contactless?.merchantNameLocation ?: config.merchantNameLocation)?.let{ aidData["MerchantNameAndLocation"] = it.toByteArray().toHexString() }
                    (aid.defaultUDOL ?: config.contactless?.defaultUDOL ?: config.defaultUDOL)?.let{ aidData["DefaultUDOL"] = it }
                    (aid.terminalFloorLimit ?: config.contactless?.terminalFloorLimit ?: config.terminalFloorLimit)?.let{ aidData["FloorLimit"] = it }
                    (aid.rdrCtlsFloorLimit ?: config.contactless?.rdrCtlsFloorLimit ?: config.rdrCtlsFloorLimit)?.let{ aidData["ReaderContactlessFloorLimit"] = it }
                    (aid.rdrCtlsTransLimitNoODCVM ?: config.contactless?.rdrCtlsTransLimitNoODCVM ?: config.rdrCtlsTransLimitNoODCVM)?.let{ aidData["NoOnDeviceCVM"] = it }
                    (aid.rdrCtlsTransLimitODCVM ?: config.contactless?.rdrCtlsTransLimitODCVM ?: config.rdrCtlsTransLimitODCVM)?.let{ aidData["OnDeviceCVM"] = it }
                    (aid.rdrCVMRequiredLimit ?: config.contactless?.rdrCVMRequiredLimit ?: config.rdrCVMRequiredLimit)?.let{ aidData["ReaderCVMRequiredLimit"] = it }
                    (aid.tacDefault ?: config.contactless?.tacDefault ?: config.tacDefault)?.let{ aidData["TerminalActionCodesDefault"] = it }
                    (aid.tacDenial ?: config.contactless?.tacDenial ?: config.tacDenial)?.let{ aidData["TerminalActionCodesDenial"] = it }
                    (aid.tacOnline ?: config.contactless?.tacOnline ?: config.tacOnline)?.let{ aidData["TerminalActionCodesOnLine"] = it }
                    (aid.riskManagementData ?: config.contactless?.riskManagementData ?: config.riskManagementData)?.let{ aidData["TerminalRiskManagement"] = it }
                    (aid.terminalCountryCode ?: config.contactless?.terminalCountryCode ?: config.terminalCountryCode)?.let{ aidData["TerminalCountryCode"] = it }
                    (aid.terminalType ?: config.contactless?.terminalType ?: config.terminalType)?.let{ aidData["TerminalType"] = it }
                    (aid.dsvnTerm ?: config.contactless?.dsvnTerm ?: config.dsvnTerm)?.let{ aidData["DSVNTerm"] = it }
                    (aid.appSelIndicator ?: config.contactless?.appSelIndicator ?: config.appSelIndicator)?.let{ aidData["AppSelIndicator"] = it }
                    (aid.defaultDDOL ?: config.contactless?.defaultDDOL ?: config.defaultDDOL)?.let{ aidData["DefaultDDOL"] = it }
                    (aid.defaultTDOL ?: config.contactless?.defaultTDOL ?: config.defaultTDOL)?.let{ aidData["DefaultTDOL"] = it }

                    /* Visa Specific */
                    (aid.ttq ?: config.contactless?.ttq ?: config.ttq)?.let{ aidData["TerminalTransactionQualifiers"] = it }
                    (aid.rdrCVMRequiredLimit ?: config.contactless?.rdrCVMRequiredLimit ?: config.rdrCVMRequiredLimit)?.let{ aidData["CvmRequiredLimit"] = it }
                    (aid.rdrCtlsTransLimit ?: config.contactless?.rdrCtlsTransLimit ?: config.rdrCtlsTransLimit)?.let{ aidData["TransactionLimit"] = it }
                    (aid.disableProcRestrictions ?: config.contactless?.disableProcRestrictions ?: config.disableProcRestrictions)?.let{ aidData["ProRestrictionDisable"] = it }
                    (aid.limitSwitch ?: config.contactless?.limitSwitch ?: config.limitSwitch)?.let{ aidData["LimitSwitch"] = it }
                    (aid.programID ?: config.contactless?.programID ?: config.programID)?.let{ aidData["ProgramID"] = it }
                    (aid.terminalCapabilities ?: config.contactless?.terminalCapabilities ?: config.terminalCapabilities)?.let{ aidData["TerminalCapabilities"] = it }

                    /* Amex Specific */
                    (aid.ctlsRdrCapabilities ?: config.contactless?.ctlsRdrCapabilities ?: config.ctlsRdrCapabilities)?.let{ aidData["ContactlessReaderCapabilities"] = it }

                    /* Rupay Specific */
                    (aid.addlTerminalCapabilitiesExtension ?: config.contactless?.addlTerminalCapabilitiesExtension ?: config.addlTerminalCapabilitiesExtension)?.let{ aidData["AdditionalTerminalCapabilitiesExtension"] = it }
                    (aid.serviceDataFormat ?: config.contactless?.serviceDataFormat ?: config.serviceDataFormat)?.let{ aidData["ServiceFormatData"] = it }
                    (aid.threshold ?: config.contactless?.threshold ?: config.threshold)?.let{ aidData["ThresholdValue"] = it }
                    (aid.targetPercentage ?: config.contactless?.targetPercentage ?: config.targetPercentage)?.let{ aidData["TargetPercentage"] = it }
                    (aid.maxTargetPercentage ?: config.contactless?.maxTargetPercentage ?: config.maxTargetPercentage)?.let{ aidData["MaxTargetPercentage"] = it }

                    /* Entry Point Specific */
                    (aid.zeroAmountAllowed ?: config.contactless?.zeroAmountAllowed ?: config.zeroAmountAllowed)?.let{ aidData["ZeroAmountCheckFlag"] = it }
                    (aid.zeroAmountOfflineAllowed ?: config.contactless?.zeroAmountOfflineAllowed ?: config.zeroAmountOfflineAllowed)?.let{ aidData["ZeroAmountAllowedOfflineFlag"] = it }
                    (aid.statusCheckSupported ?: config.contactless?.statusCheckSupported ?: config.statusCheckSupported)?.let{ aidData["StatusCheckFlag"] = it }

                    result = result && EmvNfcKernelApi.getInstance()
                        .updateAID(ContantPara.Operation.ADD, aidData) //master
                    aidData.clear()
                }
            }
            catch (exception : Exception)
            {
                result = false
                exception.printStackTrace()
            }

            return result
        }

        fun initAidConfig(aidConfig: AidConfig?): Boolean {
            var result = false
            /* Clear Aid Config first */
            EmvNfcKernelApi.getInstance().updateAID(ContantPara.Operation.CLEAR, null)
            aidConfig?.let {
                result = addContactAid(it) && addContactlessAid(it)
            }
            return result
        }

        fun addCapKey(key : CAPKey) : Boolean
        {
            var result = true
            try {
                val capKey: Hashtable<String?, String?> = Hashtable<String?, String?>()
                capKey["Rid"] = key.rid
                capKey["Index"] = key.index
                capKey["Exponent"] = key.exponent
                capKey["Modulus"] = key.modulus
                capKey["Checksum"] = key.checksum
                result = result && EmvNfcKernelApi.getInstance()
                    .updateCAPK(ContantPara.Operation.ADD, capKey)
                capKey.clear()
            }catch (exception : Exception)
            {
                result = false
                exception.printStackTrace()
            }
            return result
        }

        fun initCAPKeys(capKeys: List<CAPKey>?): Boolean {
            var result = true
            capKeys?.let {
                /* Clear CAP Keys first */
                EmvNfcKernelApi.getInstance().updateCAPK(ContantPara.Operation.CLEAR, null)
                /* Add CAP Keys */
                for (key in it) {
                    result = result && addCapKey(key)
                }
            }
            return result
        }

        fun getSdkCardType(aid : String) : String
        {
            return when(aid.substring(0,10))
            {
                "A000000003" -> ContantPara.NfcCardType.VisaCard.name
                "A000000004" -> ContantPara.NfcCardType.MasterCard.name
                "A000000524" -> ContantPara.NfcCardType.RupayCard.name
                "A000000025" -> ContantPara.NfcCardType.AmexCard.name
                "A000000065" -> ContantPara.NfcCardType.JcbCard.name
                "A000000152" -> ContantPara.NfcCardType.DiscoverCard.name
                else -> ContantPara.NfcCardType.ErrorType.name
            }
        }

        fun emv_proc_onlinePin(isDUKPT: Boolean) {
            Log.i("applog", "emv_proc_onlinePin")

            val param: Bundle = Bundle()
            pinBlock = null /* Clear the pinblock variable */

            if (isDUKPT) param.putInt("PINKeyNo", EncryptionConstants.DUKPT_KEY_SET_PIN)
            else param.putInt("PINKeyNo", 10)
            val cardno: String = "4761730000000011"

            Log.i("applog", "emv_proc_onlinePin cardno $cardno")
            param.putString("cardNo", cardno)
            param.putBoolean("sound", false)
            param.putInt("soundVolume", 1)
            param.putBoolean("onlinePin", true)
            param.putBoolean("FullScreen", true)
            param.putLong("timeOutMS", 300000)
            param.putString("supportPinLen", "0,4,5,6,7,8,9,10,11,12") // "4,4");   //
            param.putString("title", "Security PINPAD")
            param.putString(
                "message", "Please Enter your PIN"
            ) // use your real amount

            param.putBoolean("randomKeyboard", true)

            if (TextUtils.equals("I5000", Build.MODEL.uppercase(Locale.getDefault()))) {
                val transparentWhite = Color.TRANSPARENT

                val backgroundColor = intArrayOf(
                    transparentWhite,
                    transparentWhite,
                    transparentWhite,
                    transparentWhite,
                    transparentWhite,
                    transparentWhite,
                    transparentWhite,
                    transparentWhite,
                    transparentWhite,
                    transparentWhite,
                    transparentWhite,
                    transparentWhite,
                    transparentWhite,
                    transparentWhite,
                    transparentWhite,
                    Color.BLUE
                )
                val textColor = intArrayOf(
                    Color.YELLOW, Color.YELLOW, Color.YELLOW, Color.YELLOW,
                    Color.YELLOW, Color.YELLOW, Color.YELLOW,
                    Color.YELLOW, Color.YELLOW, Color.YELLOW,
                    Color.YELLOW, Color.YELLOW, Color.YELLOW, Color.YELLOW
                )
                param.putIntArray("backgroundColor", backgroundColor)
                param.putIntArray("textColor", textColor)

                val leftMargin = shortArrayOf(0, 0, 50, 0, 0, 0, 0)
                val rightMargin = shortArrayOf(0, 0, 50, 0, 0, 0, 0)
                val topMargin = shortArrayOf(0, 0, 10, 0, 0, 0, 0)
                val bottomMargin = shortArrayOf(0, 0, 10, 0, 0, 0, 0)
                param.putShortArray("leftMargin", leftMargin)
                param.putShortArray("rightMargin", rightMargin)
                param.putShortArray("topMargin", topMargin)
                param.putShortArray("bottomMargin", bottomMargin)
            }

            Log.i("applog", "getPinBlockEx ")

            if (isDUKPT) {
                PinPadProviderImpl.getInstance().GetDukptPinBlock(param, this)
            }
            else
                PinPadProviderImpl.getInstance().getPinBlockEx(param, this)
        }

        fun proc_offlinePin(pinEntryType: Int, isLastPinTry: Boolean, bundle: Bundle): Int {
            var iret = 0

            // TODO Auto-generated method stub
            val emvBundle = bundle


            Log.d(
                "applog",
                "proc_offlinePin pinEntryType = $pinEntryType isLastPinTry=$isLastPinTry"
            )

            val paramVar = Bundle()
            paramVar.putInt("inputType", 3) //Offline PlainPin
            paramVar.putInt("CardSlot", 0)

            paramVar.putBoolean("sound", false)
            paramVar.putInt("soundVolume", 1)
            paramVar.putBoolean("onlinePin", false)
            paramVar.putBoolean("FullScreen", true)
            paramVar.putLong("timeOutMS", 30000)
            paramVar.putString("supportPinLen", "0,4,5,6,7,8,9,10,11,12")
            paramVar.putString("title", "Security Keyboard")
            paramVar.putBoolean("randomKeyboard", true)
            val pinTryTimes = bundle.getInt("PinTryTimes")
            val isFirst = bundle.getBoolean("isFirstTime", false)
            Log.d("applog", "PinTryTimes:$pinTryTimes")
            if (isLastPinTry) {
                if (isFirst) paramVar.putString("message", "Please input PIN \nLast PIN Try")
                else paramVar.putString("message", "Please input PIN \nWrong PIN \nLast Pin Try")
            } else {
                if (isFirst) paramVar.putString("message", "Please input PIN \n")
                else {
                    paramVar.putString(
                        "message",
                        "Please input PIN \nWrong PIN \nPin Try Times:$pinTryTimes"
                    )
                }
            }


            if (pinEntryType == 1) {
                paramVar.putInt("inputType", 4) //Offline CipherPin

                val pub = emvBundle.getByteArray("pub")
                val publen = emvBundle.getIntArray("publen")
                val exp = emvBundle.getByteArray("exp")
                val explen = emvBundle.getIntArray("explen")

                Log.d("applog", "ModuleLen = " + publen!![0] + ": " + Funs.bytesToHexString(pub))
                Log.d("applog", "ExponentLen = " + explen!![0] + ": " + Funs.bytesToHexString(exp))


                val ModuleLen = publen!![0]
                val ExponentLen = explen!![0]
                val Module = ByteArray(ModuleLen)
                val Exponent = ByteArray(ExponentLen)

                if (ModuleLen == 0 || ExponentLen == 0) {
                    EmvNfcKernelApi.getInstance().sendOfflinePINVerifyResult(-198)
                    return 0
                }

                System.arraycopy(pub, 0, Module, 0, ModuleLen)
                System.arraycopy(exp, 0, Exponent, 0, ExponentLen)

                paramVar.putInt("ModuleLen", ModuleLen) //Modulus length
                paramVar.putString("Module", Funs.bytesToHexString(Module)) //Module
                paramVar.putInt("ExponentLen", ExponentLen) //Exponent length
                paramVar.putString("Exponent", Funs.bytesToHexString(Exponent)) //Exponent
            }


            Log.d("applog", "proc_offlinePin getPinBlockEx start")

            /*
        paramVar.putInt("PinTryMode", 1);
        paramVar.putString("ErrorMessage", "Incorrect PIN, # More Retries");
        paramVar.putString("ErrorMessageLast", "Incorrect PIN, Last Chance");
        */
            val se = SEManager()
            iret = se.getPinBlockEx(paramVar, object : IInputActionListener.Stub() {
                override fun onInputChanged(type: Int, result: Int, bundle: Bundle) {
                    val resultBundle = bundle
                    try {
                        //    7101~7115 The number of remaining PIN tries(7101 PIN BLOCKED   7102 the last one chance  7103 two chances ....)
                        //		7006 PIN length error
                        //		7010 防穷举出错
                        //		7016 Wrong PIN
                        //		7071 The return code is wrong
                        //		7072 IC command failed
                        //		7073 Card data error
                        //		7074 PIN BLOCKED
                        //		7075 Encryption error
                        //
                        //The offline PIN verification result is sent to the kernel
                        //   use api EmvApi.sendOfflinePINVerifyResult();
                        //		    (-198)     //Return code error
                        //		    (-202)     //IC command failed
                        //		    (-192)     //PIN BLOCKED
                        //          (-199)     //user cancel or Pinpad timeout
                        //		    (1)        //bypass
                        //		    (0)        //success

                        Log.i(
                            "applog",
                            "proc_offlinePin：getPinBlockEx===onInputChanged：type=$type，result=$result"
                        )

                        if (type == 2) { // entering PIN
                        } else if (type == 0) //bypass
                        {
                            if (result == 0) {
                                Log.d("applog", "proc_offlinePin bypass")
                                EmvNfcKernelApi.getInstance().sendOfflinePINVerifyResult(1) //bypass
                            } else {
                                EmvNfcKernelApi.getInstance().sendOfflinePINVerifyResult(-198) //return code error
                            }
                        } else if (type == 3) //Offline plaintext
                        {
                            Log.d("applog", "proc_offlinePin Plaintext offline")
                            if (result == 0) {
                                EmvNfcKernelApi.getInstance().sendOfflinePINVerifyResult(0) //Offline plaintext verify successfully
                            } else { //Incorrect PIN, try again
                                val arg1Str = result.toString() + ""
                                if (arg1Str.length >= 4 && "71" == arg1Str.subSequence(0, 2)) {
                                    if ("7101" == arg1Str) {
                                        EmvNfcKernelApi.getInstance().sendOfflinePINVerifyResult(-192) //PIN BLOCKED
                                    } else {
                                        if ("7102" == arg1Str) {
                                            emvBundle.putBoolean("isFirstTime", false)
                                            emvBundle.putInt("PinTryTimes", 1)
                                            proc_offlinePin(
                                                pinEntryType,
                                                true,
                                                emvBundle
                                            ) //try again the last pin try
                                        } else {
                                            emvBundle.putBoolean("isFirstTime", false)
                                            emvBundle.putInt(
                                                "PinTryTimes",
                                                (arg1Str.substring(2, 4).toInt() - 1)
                                            )
                                            proc_offlinePin(
                                                pinEntryType,
                                                false,
                                                emvBundle
                                            ) //try again
                                        }
                                    }
                                } else if ("7074" == arg1Str) {
                                    EmvNfcKernelApi.getInstance().sendOfflinePINVerifyResult(-192) //PIN BLOCKED
                                } else if ("7072" == arg1Str || "7073" == arg1Str) {
                                    EmvNfcKernelApi.getInstance().sendOfflinePINVerifyResult(-202) //IC command failed
                                } else {
                                    EmvNfcKernelApi.getInstance().sendOfflinePINVerifyResult(-198) //Return code error
                                }
                            }
                        } else if (type == 4) //Offline encryption PIN
                        {
                            Log.d("applog", "proc_offlinePin Offline encryption")
                            if (result == 0) {
                                EmvNfcKernelApi.getInstance().sendOfflinePINVerifyResult(0) //Offline encryption PIN verify successfully
                            } else {
                                val arg1Str = result.toString() + ""
                                if (arg1Str.length >= 4 && "71" == arg1Str.subSequence(0, 2)) {
                                    if ("7101" == arg1Str) {
                                        EmvNfcKernelApi.getInstance().sendOfflinePINVerifyResult(-192) //PIN BLOCKED
                                    } else {
                                        Log.d(
                                            "applog",
                                            "proc_offlinePin Offline encryption entry pin again"
                                        )
                                        if ("7102" == arg1Str) {
                                            emvBundle.putBoolean("isFirstTime", false)
                                            emvBundle.putInt("PinTryTimes", 1)
                                            proc_offlinePin(
                                                pinEntryType,
                                                true,
                                                emvBundle
                                            ) //try again the last pin try
                                        } else {
                                            emvBundle.putBoolean("isFirstTime", false)
                                            emvBundle.putInt(
                                                "PinTryTimes",
                                                (arg1Str.substring(2, 4).toInt() - 1)
                                            )
                                            proc_offlinePin(
                                                pinEntryType,
                                                false,
                                                emvBundle
                                            ) //try again
                                        }
                                    }
                                } else if ("7074" == arg1Str) {
                                    EmvNfcKernelApi.getInstance().sendOfflinePINVerifyResult(-192) //PIN BLOCKED
                                } else if ("7072" == arg1Str || "7073" == arg1Str) {
                                    EmvNfcKernelApi.getInstance().sendOfflinePINVerifyResult(-202) //IC command failed(card removed)
                                } else {
                                    EmvNfcKernelApi.getInstance().sendOfflinePINVerifyResult(-198) //Return code error
                                }
                            }
                        } else if (type == 0x10) // click Cancel button
                        {
                            EmvNfcKernelApi.getInstance().sendOfflinePINVerifyResult(-199) //cancel
                        } else if (type == 0x11) // pinpad timed out
                        {
                            EmvNfcKernelApi.getInstance().sendOfflinePINVerifyResult(-199) //timeout
                        } else {
                            EmvNfcKernelApi.getInstance().sendOfflinePINVerifyResult(-198) //Return code error
                        }
                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                        Log.d("applog", "proc_offlinePin exception")
                    }
                }
            })
            if (iret == -3 || iret == -4) EmvNfcKernelApi.getInstance().sendOfflinePINVerifyResult(-198)
            return iret
        }

        fun promptPin(
            pinpadBundle: Bundle?,
            isOnlinePin: Boolean,
            keyIndex: Int,
            plainKey: String?,
            randomLocation: Boolean
        ) {
            var pinpadBundle = pinpadBundle
            if (pinpadBundle == null || pinpadBundle.isEmpty) {
                pinpadBundle = Bundle()
                if (!isOnlinePin) {
                    pinpadBundle.putInt("inputType", 3) //Offline PlainPin
                    pinpadBundle.putInt("CardSlot", 0)
                }
                pinpadBundle.putString("cardNo", "1122334455667788")
                pinpadBundle.putBoolean("sound", false)
                pinpadBundle.putBoolean("bypass", false)
                pinpadBundle.putInt("soundVolume", 1)
                pinpadBundle.putString("supportPinLen", "0,4,5,6,7,8,9,10,11,12")
                pinpadBundle.putBoolean("onlinePin", isOnlinePin)
                pinpadBundle.putInt("PINKeyNo", keyIndex)
                pinpadBundle.putLong("timeOutMS", (30 * 1000).toLong())
                pinpadBundle.putBoolean("randomKeyboard", true)

                pinpadBundle.putBoolean("FullScreen", true)
                pinpadBundle.putInt("customKeyboardDialog", 5)

                pinpadBundle.putString("title", "Security Keyboard")
                pinpadBundle.putString("message", "Enter Your Pin")
            }
            try {
                PinPadProviderImpl.getInstance()
                    .getPinBlockEx(pinpadBundle, this)
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }

        override fun onInput(p0: Int, p1: Int) {
            Log.d("EMV_LOG", "On Input: $p0, $p1")
        }

        override fun onConfirm(p0: ByteArray?, p1: Boolean) {
            if (p1) {
                EmvNfcKernelApi.getInstance().bypassPinEntry() //bypass
            } else {
                Log.d("EMV_APP", "PinBlock:" + p0.contentToString())
                    EmvNfcKernelApi.getInstance().sendPinEntry()
            }
        }

        @OptIn(ExperimentalStdlibApi::class)
        override fun onConfirm_dukpt(p0: ByteArray?, p1: ByteArray?) {
            //iEmvSdkResponseListener?.onEmvSdkDisplayMessage("Processing")
            if (p0 == null) {
                EmvNfcKernelApi.getInstance().bypassPinEntry() //bypass
            } else {
                Log.d("EMV_APP", "PinBlock:" + p0.decodeToString())
                Log.d("EMV_APP", "KSN     :" + p1?.toHexString())
                pinBlock = p0.decodeToString()
                ksn = p1?.decodeToString()
                EmvNfcKernelApi.getInstance().sendPinEntry()
            }
        }

        override fun onCancel() {
            EmvNfcKernelApi.getInstance().cancelPinEntry()
        }

        override fun onTimeOut() {
            EmvNfcKernelApi.getInstance().cancelPinEntry()
        }

        override fun onError(p0: Int) {
            EmvNfcKernelApi.getInstance().cancelPinEntry()
        }
    }
}