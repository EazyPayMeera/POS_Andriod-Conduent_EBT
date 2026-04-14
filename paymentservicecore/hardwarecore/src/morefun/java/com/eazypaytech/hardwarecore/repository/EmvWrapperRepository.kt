package com.eazypaytech.tpaymentcore.repository

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.os.RemoteException
import android.util.Log
import com.eazypaytech.paymentservicecore.constants.EmvConstants
import com.eazypaytech.tpaymentcore.constants.EncryptionConstants
import com.eazypaytech.tpaymentcore.listener.requestListener.IEmvWrapperRequestListener
import com.eazypaytech.tpaymentcore.listener.responseListener.IEmvSdkResponseListener
import com.eazypaytech.tpaymentcore.model.emv.AidConfig
import com.eazypaytech.tpaymentcore.model.emv.CAPKey
import com.eazypaytech.tpaymentcore.model.emv.CardCheckMode
import com.eazypaytech.tpaymentcore.model.emv.EmvSdkException
import com.eazypaytech.tpaymentcore.model.emv.EmvSdkResult
import com.eazypaytech.tpaymentcore.model.emv.EmvSdkResult.DisplayMsgId
import com.eazypaytech.tpaymentcore.model.emv.EmvSdkResult.InitResult
import com.eazypaytech.tpaymentcore.model.emv.EmvSdkResult.InitStatus
import com.eazypaytech.tpaymentcore.model.emv.EmvSdkResult.TransStatus
import com.eazypaytech.tpaymentcore.model.emv.TransConfig
import com.eazypaytech.tpaymentcore.utils.TlvUtils
import com.morefun.yapi.ServiceResult
import com.morefun.yapi.device.pinpad.CheckKeyEnum
import com.morefun.yapi.device.pinpad.DispTextMode
import com.morefun.yapi.device.pinpad.DukptCalcObj
import com.morefun.yapi.device.pinpad.DukptLoadObj
import com.morefun.yapi.device.pinpad.OnPinPadInputListener
import com.morefun.yapi.device.pinpad.PinAlgorithmMode
import com.morefun.yapi.device.pinpad.PinPadConstrants
import com.morefun.yapi.device.reader.icc.ICCSearchResult
import com.morefun.yapi.device.reader.mag.MagCardInfoEntity
import com.morefun.yapi.device.reader.mag.OnSearchMagCardListener
import com.morefun.yapi.emv.EmvAidPara
import com.morefun.yapi.emv.EmvCapk
import com.morefun.yapi.emv.EmvDataSource
import com.morefun.yapi.emv.EmvOnlineResult
import com.morefun.yapi.emv.EmvTermCfgConstrants
import com.morefun.yapi.emv.EmvTransDataConstrants
import com.morefun.yapi.emv.OnEmvProcessListener
import com.morefun.yapi.engine.DeviceInfoConstrants
import com.morefun.yapi.engine.DeviceServiceEngine
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Hashtable
import java.util.Locale
import javax.inject.Inject
import kotlin.collections.set
import kotlin.text.decodeToString
import kotlin.text.substring
import kotlin.text.toInt
import kotlin.text.uppercase
import kotlin.toString



class EmvWrapperRepository @Inject constructor(
    @ApplicationContext context: Context,
    override var iEmvSdkResponseListener: IEmvSdkResponseListener
) :
    IEmvWrapperRequestListener {
    private var TAG = "MOREFUN"
    private var isMagSupported = false
    val arqcTLVTags: Array<String> = arrayOf(
        "9F26",  // Application Cryptogram
        "9F27",  // Cryptogram Information Data
        "9F10",  // Issuer Application Data
        "9F34",  // CVM Results
        "9F33",  // Terminal Capabilities
        "9F37",  // Unpredictable Number
        "9F36",  // ATC
        "95",    // TVR
        "9A",    // Transaction Date
        "9C",    // Transaction Type
        "9F02",  // Amount Authorized
        "9F03",  // Amount Other ← ADD THIS
        "5F2A",  // Transaction Currency Code
        "82",    // AIP
        "84",    // Dedicated File Name
        "9F1A",  // Terminal Country Code
        "9F35",  // Terminal Type  keep if your acquirer needs it
    )

    enum class CheckCardResult(value : Int) {
        CARD_INSERTED(1),
        CARD_TAPPED(7),
        CARD_SWIPED(9)
    }

    init {
        CoroutineScope(Dispatchers.Default).launch {
            bindService(context)
        }
    }

    fun getEmvDataSafe( tagList: Array<String>, bundle: Bundle?): ByteArray? {
        val outBuffer = ByteArray(2048)
        return try {
            val resultCode = deviceService?.emvHandler?.readEmvData(
                tagList,
                outBuffer,
                bundle
            ) ?: return null

            Log.d("EMV_READ", "Result code = $resultCode")
            Log.d("EMV_READ", "Tags requested = ${tagList.joinToString()}")

            if (resultCode != 0) {
                Log.e("EMV_READ", "EMV read failed with code = $resultCode")
                return null
            }

            val cleanData = outBuffer.takeWhile { it.toInt() != 0 }.toByteArray()
            Log.d("EMV_READ", "Output size = ${cleanData.size}")
            cleanData

        } catch (e: Exception) {
            Log.e("EMV_READ", "Failed to read EMV data", e)
            null
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun collectTlvData(tags: Array<String>): String? {

        for (i in tags.indices) {
            tags[i] = tags[i].uppercase(Locale.getDefault())
        }

        try {
            val buffer = ByteArray(3096)
            val bundle = Bundle()
            bundle.putInt(DukptCalcObj.Param.DUKPT_KEY_INDEX, EncryptionConstants.KEY_INDEX_DATA_KEY.ordinal)

            val bytesRead = deviceService?.emvHandler?.readEmvData(tags, buffer, bundle)?:0

            return if (bytesRead > 0) {
                val hexData = buffer.slice(0..bytesRead-1).toByteArray().toHexString().uppercase()
                Log.d("TLV", "🔡 Hex Data Extracted: $hexData")
                hexData
            } else {
                Log.w("TLV", "⚠️ No data read from EMV")
                null
            }
        } catch (e: Exception) {
            Log.e("TLV", "❗ Unexpected exception occurred", e)
        }
        return null
    }

    @OptIn(ExperimentalStdlibApi::class)
    fun initTermConfig(aidConfig: AidConfig?): Boolean {
        var result = true

        try {
            var bundle = Bundle()
            aidConfig?.merchantIdentifier?.let {
                bundle.putByteArray(EmvTermCfgConstrants.MERID_ANS_9F16,it.toByteArray())
            }
            aidConfig?.terminalIdentifier?.let {
                bundle.putByteArray(EmvTermCfgConstrants.TERMID,it.toByteArray())
            }
            aidConfig?.merchantNameLocation?.let {
                bundle.putByteArray(EmvTermCfgConstrants.MERNAMELOC,it.toByteArray())
            }
            aidConfig?.merchantCategoryCode?.let {
                bundle.putByteArray(EmvTermCfgConstrants.MERCATRE_CODE_N_9F15,it.padStart(4,'0').hexToByteArray())
            }
            aidConfig?.ifdSerialNumber?.let {
                bundle.putByteArray(EmvTermCfgConstrants.IFD_AN_9F1E,it.toByteArray())
            }
            aidConfig?.terminalCapabilities?.let {
                bundle.putByteArray(EmvTermCfgConstrants.TERMCAP,it.hexToByteArray())
            }
            aidConfig?.terminalCountryCode?.let {
                bundle.putByteArray(EmvTermCfgConstrants.COUNTRYCODE,it.padStart(4,'0').hexToByteArray())
            }
            aidConfig?.currencyCode?.let {
                bundle.putByteArray(EmvTermCfgConstrants.CURRENCYCODE, it.padStart(4,'0').hexToByteArray())
            }
            aidConfig?.terminalType?.let {
                bundle.putByte(EmvTermCfgConstrants.TERMTYPE,it.hexToByte())
            }

            result = deviceService?.emvHandler?.initTermConfig(bundle) == 0
        } catch (exception: Exception) {
            result = false
            Log.e("EMV_APP", exception.message.toString())
        }
        return result
    }

    override fun initializeSdk(aidConfig: AidConfig?, capKeys: List<CAPKey>?) {
        CoroutineScope(Dispatchers.Default).launch {
            serviceConnected.await()
            var result = true
            try {
                aidConfig?.let {
                    result = result && initAidConfig(it) && initTermConfig(it)
                }
                capKeys?.let { result = result && initCAPKeys(it) }

                when (result) {
                    true ->
                        iEmvSdkResponseListener.onEmvSdkResponse(InitResult(InitStatus.SUCCESS))//DF02---random trans select enable  DF03--Except file check enable DF04--Support SM DF05-- Velocity Check enable
                    else ->
                        iEmvSdkResponseListener.onEmvSdkResponse(InitResult(InitStatus.FAILURE))//DF02---random trans select enable  DF03--Except file check enable DF04--Support SM DF05-- Velocity Check enable
                }
            } catch (e: Exception) {
                e.printStackTrace()
                iEmvSdkResponseListener.onEmvSdkResponse(EmvSdkException(e.message.toString()))
            }
        }
    }

    val magCardListener = object : OnSearchMagCardListener.Stub() {

        override fun onSearchResult(
            p0: Int,
            p1: MagCardInfoEntity?
        ) {
//            Log.d(TAG, "Card Swiped......: $p0")
//            Log.d(TAG, "Card No..........: ${p1?.cardNo}")
//            Log.d(TAG, "Card Holder Name.: ${p1?.cardholderName}")
//            Log.d(TAG, "Expiry Date......: ${p1?.expDate}")
//            Log.d(TAG, "Service Code.....: ${p1?.serviceCode}")
//            Log.d(TAG, "Track2 Raw.......: ${p1?.tk2}")

            p0.takeIf {
                it == ServiceResult.Success &&
                        p1?.tk2ValidResult == ServiceResult.Success
            }?.let {
                checkCardResult = CheckCardResult.CARD_SWIPED

                iEmvSdkResponseListener?.onEmvSdkResponse(
                    EmvSdkResult.CardCheckResult(
                        status = EmvSdkResult.CardCheckStatus.CARD_SWIPED
                    )
                )

                val trackData = p1?.tk2
                    ?.uppercase()
                    ?.replace('=', 'D')
                    ?.trimEnd('F') ?: ""

                val pan = p1?.cardNo ?: ""
                inputOnlinePin(pan) { pinBlock ->
                    val msrTlv = TlvUtils()
                    msrTlv.addTagValHex(
                        EmvConstants.EMV_TAG_TRACK2,
                        trackData,
                        0,
                        trackData.length
                    )

                    val finalTlv = msrTlv.toTlvString()
                    encryptThenRequestOnline(finalTlv)
                }

            } ?: let {
                iEmvSdkResponseListener?.onEmvSdkResponse(
                    EmvSdkResult.CardCheckResult(
                        status = EmvSdkResult.CardCheckStatus.NO_CARD_DETECTED
                    )
                )
                deviceService?.magCardReader?.searchCard(this, 30, Bundle())
                deviceService?.magCardReader?.setIsCheckLrc(true)
            }
        }
    }

    val emvListener = object : OnEmvProcessListener.Stub() {
        override fun onSelApp(
            p0: List<String?>?,
            p1: Boolean
        ) {
            Log.d(TAG, "Service connected")
        }

        override fun onConfirmCardNo(p0: String?) {
            Log.d(TAG, "onConfirmCardNo ${p0}")
            deviceService?.emvHandler?.onSetConfirmCardNoResponse(true)
        }

        override fun onCardHolderInputPin(
            p0: Boolean,
            p1: Int
        ) {
            Log.d(TAG, "onCardHolderInputPin ${p0} ${p1}")
            var pan = readPan()?:""
             if (p0) {
             inputOnlinePin(
                 pan,
                 ) { pinBlock ->
                 try {
                     deviceService?.emvHandler?.onSetCardHolderInputPin(pinBlock)
                 } catch (e: RemoteException) {
                     e.printStackTrace()
                 }
             }
         } else {
             inputOfflinePin(pan) { pinBlock ->
                 try {
                     Companion.pinBlock
                     deviceService?.emvHandler?.onSetCardHolderInputPin(pinBlock)
                 } catch (e: RemoteException) {
                     e.printStackTrace()
                 }
             }
         }
        }

        override fun onPinPress(p0: Byte) {
            Log.d(TAG, "onPinPress ${p0}")
        }

        override fun onCertVerify(p0: String?, p1: String?) {
            Log.d(TAG, "onCertVerify ${p0} ${p1}")
        }

        override fun onOnlineProc(p0: Bundle?) {
            Log.d(TAG, "onOnlineProc ${p0}")
            encryptThenRequestOnline(collectTlvData(arqcTLVTags))
        }

        override fun onContactlessOnlinePlaceCardMode(p0: Int) {
            Log.d(TAG, "onContactlessOnlinePlaceCardMode ${p0}")
        }

        override fun onFinish(p0: Int, p1: Bundle?) {
            Log.d(TAG, "onFinish ${p0} ${p1}")
            p1?.let { bundle ->
                for (key in bundle.keySet()) {
                    val value = bundle.get(key)
                    Log.d(TAG, "Bundle key: $key, value: $value")
                }
            }

            iEmvSdkResponseListener?.onEmvSdkResponse(
                EmvSdkResult.TransResult(
                    ysdkToEmvTransResult(
                        p0
                    )
                )
            )
        }

        override fun onSetAIDParameter(p0: String?) {
            Log.d(TAG, "onSetAIDParameter ${p0}")
        }

        override fun onSetCAPubkey(
            p0: String?,
            p1: Int,
            p2: Int
        ) {
            Log.d(TAG, "onSetCAPubkey ${p0} ${p1} ${p2}")
        }

        override fun onTRiskManage(p0: String?, p1: String?) {
            Log.d(TAG, "onTRiskManage ${p0} ${p1}")
        }

        override fun onSelectLanguage(p0: String?) {
            Log.d(TAG, "onSelectLanguage ${p0}")
        }

        override fun onSelectAccountType(p0: List<String?>?) {
            Log.d(TAG, "onSelectAccountType ${p0}")
        }

        override fun onIssuerVoiceReference(p0: String?) {
            Log.d(TAG, "onIssuerVoiceReference ${p0}")
        }

        override fun onDisplayOfflinePin(p0: Int) {
            Log.d(TAG, "onDisplayOfflinePin ${p0}")
        }

        override fun inputAmount(p0: Int) {
            Log.d(TAG, "inputAmount ${p0}")
        }

        override fun onGetCardResult(
            p0: Int,
            p1: Bundle?
        ) {
            Log.d(TAG, "onGetCardResult ${p0} ${p1}")
            p1?.let { bundle ->
                for (key in bundle.keySet()) {
                    val value = bundle.get(key)
                    Log.d(TAG, "Bundle key: $key, value: $value")
                }
            }

            when(p0) {
                ServiceResult.Success -> {
                    // 7: TAP card 1:DIP card
                    var type = p1?.getInt(ICCSearchResult.CARDOTHER) ?: 0
                    iEmvSdkResponseListener?.onEmvSdkResponse(
                        EmvSdkResult.CardCheckResult(
                            status = ysdkToCheckCardStatus(
                                type
                            )
                        )
                    )
                }
                ServiceResult.TimeOut ->{
                    iEmvSdkResponseListener?.onEmvSdkResponse(
                        EmvSdkResult.CardCheckResult(
                            status = EmvSdkResult.CardCheckStatus.TIMEOUT
                        )
                    )
                }
                else -> {
                    iEmvSdkResponseListener?.onEmvSdkResponse(
                        EmvSdkResult.CardCheckResult(
                            status = EmvSdkResult.CardCheckStatus.NO_CARD_DETECTED
                        )
                    )
                }
            }
        }

        override fun onDisplayMessage() {
            Log.d(TAG, "onDisplayMessage")
        }

        override fun onUpdateServiceAmount(p0: String?) {
            Log.d(TAG, "Service connected")
        }

        override fun onCheckServiceBlackList(
            p0: String?,
            p1: String?
        ) {
            Log.d(TAG, "onCheckServiceBlackList ${p0} ${p1}")
        }

        override fun onGetServiceDirectory(p0: ByteArray?) {
            Log.d(TAG, "onGetServiceDirectory ${p0}")
        }

        override fun onRupayCallback(
            p0: Int,
            p1: Bundle?
        ) {
            Log.d(TAG, "onRupayCallback ${p0} ${p1}")
        }
    }



    @OptIn(ExperimentalStdlibApi::class)
    private fun inputOnlinePin(
        pan: String?,
//        amount: String,
        onResult: (pinBlock: ByteArray?) -> Unit
    ) {
        //val panBlock = requireNotNull(pan) { "PAN cannot be null" }.toByteArray()
        //Log.d("PIN_DEBUG", "PAN (masked): $pan")
        val panBlock = requireNotNull(pan) { "PAN cannot be null" }.toByteArray()
        val bundle = Bundle().apply {
            putBoolean(PinPadConstrants.COMMON_IS_RANDOM, false)
            if (getDeviceModel().contains("MF960") ||
                getDeviceModel().contains("H9PRO")
            ) {
                putBoolean(PinPadConstrants.COMMON_IS_PHYSICAL_KEYBOARD, true)
            }
            putString(
                PinPadConstrants.TITLE_HEAD_CONTENT,
                "Please input the online pin"
            )
        }
        loginDevice()
        try {

            deviceService?.pinPad?.apply {
                setTimeOut(30)
                setSupportPinLen(intArrayOf(4, 6))
                inputOnlinePin(bundle, panBlock,  EncryptionConstants.KEY_INDEX_MAIN_KEY.ordinal, PinAlgorithmMode.ISO9564FMT1,
                    object : OnPinPadInputListener.Stub() {
                        override fun onInputResult(
                            ret: Int,
                            pinBlock: ByteArray?,
                            ksn: String?
                        ) {
                            Companion.pinBlock = pinBlock?.toHexString()
                            Companion.ksn = ksn
                            Log.d("PIN", "ret=$ret")
                            Log.d("PIN", "pinBlock=${pinBlock?.toHexString() ?: "NULL"}")
                            Log.d("PIN", "ksn=$ksn")

                            if (pinBlock == null) {
                                Log.e("PIN", "❌ PIN block is NULL — PIN entry failed!")
                            } else {
                                Log.d("PIN", "✅ PIN block received successfully")
                            }
                            onResult(pinBlock)
                        }

                        override fun onSendKey(keyCode: Byte) {
                            if (keyCode == ServiceResult.PinPad_Input_Cancel.toByte()) {
                                onResult(null)
                            }
                        }
                    }
                )
            }
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    fun inputManualPin(
        pan: String?,
        amount: String,
        onResult: (pinBlock: ByteArray?) -> Unit
    ) {
        //Log.d("PIN_DEBUG", "PAN (masked): $pan")
        val panBlock = requireNotNull(pan) { "PAN cannot be null" }.toByteArray()
        val bundle = Bundle().apply {
            putBoolean(PinPadConstrants.COMMON_IS_RANDOM, false)
            if (getDeviceModel().contains("MF960") ||
                getDeviceModel().contains("H9PRO")
            ) {
                putBoolean(PinPadConstrants.COMMON_IS_PHYSICAL_KEYBOARD, true)
            }
            putString(
                PinPadConstrants.TITLE_HEAD_CONTENT,
                "Please input the online pin \nAmount: $amount"
            )
        }
        loginDevice()
        try {

            deviceService?.pinPad?.apply {
                setTimeOut(30)
                setSupportPinLen(intArrayOf(4, 6))
                inputOnlinePin(bundle, panBlock,  EncryptionConstants.KEY_INDEX_MAIN_KEY.ordinal, PinAlgorithmMode.ISO9564FMT1,
                    object : OnPinPadInputListener.Stub() {
                        override fun onInputResult(
                            ret: Int,
                            pinBlock: ByteArray?,
                            ksn: String?
                        ) {
                            Companion.pinBlock = pinBlock?.toHexString()
                            Companion.ksn = ksn

                            val builder = StringBuilder().apply {
                                append("ON INPUT RESULT: $ret\n")
                                append("PIN BLOCK: ${pinBlock?.toHexString()}\n")
                                append("KSN: $ksn")
                            }

                            Log.d("InputOnlinePin", builder.toString())
                            onResult(pinBlock)
                        }

                        override fun onSendKey(keyCode: Byte) {
                            if (keyCode == ServiceResult.PinPad_Input_Cancel.toByte()) {
                                onResult(null)
                            }
                        }
                    }
                )
            }
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun inputOfflinePin(pan: String?, onResult: (pinBlock: ByteArray?) -> Unit) {

        val bundle = Bundle().apply {
            putBoolean(PinPadConstrants.COMMON_IS_RANDOM, false)
            putString(PinPadConstrants.TITLE_HEAD_CONTENT, "Please Enter PIN")
        }

        try {
            val minLength = 0
            val maxLength = 6
            deviceService?.pinPad?.apply {
                setSupportPinLen(intArrayOf(minLength, maxLength))
                inputText(bundle, object : OnPinPadInputListener.Stub() {
                    override fun onInputResult(
                        ret: Int,
                        pinBlock: ByteArray?,
                        ksn: String?
                    ) {
                        val builder = StringBuilder().apply {
                            append("INPUT PIN RESULT: $ret\n")
                            append("PIN BLOCK: ${pinBlock?.toHexString()}\n")
                            append("KSN: $ksn")
                        }
                        Log.d("PIN", builder.toString())

                        onResult(pinBlock)
                    }

                    override fun onSendKey(keyCode: Byte) {
                        if (keyCode == ServiceResult.PinPad_Input_Cancel.toByte()) {
                            onResult(null)
                        }
                    }
                }, DispTextMode.PASSWORD)
            }
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
    }

    suspend fun isCardExists(context: Context?): Boolean {
        return try {

            val service = getDeviceService(context)

            if (service == null) {
                Log.w(TAG, "deviceService is null — service not connected")
                return false
            }

            val reader = service.getIccCardReader(1)

            if (reader == null) {
                Log.w(TAG, "getIccCardReader(1) returned null")
                return false
            }

            val exists = reader.isCardExists()
            Log.d(TAG, "isCardExists: $exists")
            exists

        } catch (e: RemoteException) {
            Log.e(TAG, "RemoteException in isCardExists()", e)
            false
        }
    }

    fun getCurrentTime(format: String?): String {
        val df = SimpleDateFormat(format)
        val curDate = Date(System.currentTimeMillis())
        return df.format(curDate)
    }

    fun loginDevice() {
        val bundle = Bundle() // reserved, keep empty

        try {
            val result = deviceService?.login(bundle, "00000000")

            when (result) {
                0 -> Log.d("LOGIN", "✅ Login success (EMV ready)")
                1 -> Log.e("LOGIN", "❌ Login failed")
                2 -> Log.d("LOGIN", "⚠️ Login success (NO EMV file)")
            }
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
    }

    fun getDeviceModel(): String {
        return try {
            val devInfo = deviceService?.getDevInfo()
            devInfo?.getString(DeviceInfoConstrants.COMMOM_MODEL_EX) ?: ""
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    fun startPayment(
        context: Context,
        transConfig: TransConfig?,
        isTap: Boolean?,
        isChip: Boolean?,
        iEmvSdkResponseListener: IEmvSdkResponseListener

    ) {
        resetTransData()
        /*thread = Thread {*/
        try {
            this.iEmvSdkResponseListener = iEmvSdkResponseListener
            Companion.iEmvSdkResponseListener = iEmvSdkResponseListener
            val data = Hashtable<String, Any>()
            val date: String = getCurrentTime("yyMMddHHmmss")
            val bundle = Bundle()
            bundle.putString(EmvTransDataConstrants.TRANSDATE, date.substring(0, 6))
            bundle.putString(EmvTransDataConstrants.TRANSTIME, date.substring(6, 12))

            transConfig?.let {
                it.amount?.let { bundle.putString(EmvTransDataConstrants.TRANSAMT, it); _sAmount = it ;_amount = it.toLongOrNull() ?: 0L }
                it.cashbackAmount?.let {
                    bundle.putString(EmvTransDataConstrants.CASHBACKAMT, it); _cashbackAmount = it.toLongOrNull() ?: 0L
                }
                it.currencyCode?.let { data["currencyCode"] = it.takeLast(3) }
                it.transactionType?.let {
                    bundle.putByte(EmvTransDataConstrants.B9C, it.take(2).toByte())
                }    //00-goods 01-cash 09-cashback 20-refund
                (it.cardCheckMode ?: CardCheckMode.SWIPE_OR_INSERT_OR_TAP).let {
                    //data["checkCardMode"] = it.sdkValue

                    bundle.putBoolean(EmvTransDataConstrants.EMV_TRANS_ENABLE_CONTACT, it in listOf(CardCheckMode.INSERT, CardCheckMode.SWIPE_OR_INSERT, CardCheckMode.INSERT_OR_TAP, CardCheckMode.SWIPE_OR_INSERT_OR_TAP))
                    bundle.putBoolean(EmvTransDataConstrants.EMV_TRANS_ENABLE_CONTACTLESS, it in listOf(CardCheckMode.TAP, CardCheckMode.INSERT_OR_TAP, CardCheckMode.SWIPE_OR_INSERT_OR_TAP, CardCheckMode.SWIPE_OR_TAP))
                    isMagSupported = it in listOf(CardCheckMode.SWIPE, CardCheckMode.SWIPE_OR_INSERT, CardCheckMode.SWIPE_OR_INSERT_OR_TAP, CardCheckMode.SWIPE_OR_TAP)
                    bundle.putBoolean(EmvTransDataConstrants.SUPPORT_MAG_CARD, isMagSupported)
                }
                it.cardCheckTimeout?.let { bundle.putInt(EmvTransDataConstrants.CHECK_CARD_TIME_OUT, it.toInt()) }
                //it.enableBeeper?.let { data["enableBeeper"] = it }
                it.supportFallback?.let {
                    if (it == true)
                        bundle.putBoolean(EmvTransDataConstrants.CONTACT_SERVICE_SWITCH, true)
                    else
                        bundle.putBoolean(EmvTransDataConstrants.CONTACT_SERVICE_SWITCH, false)
                }
                //it.supportDRL?.let { data["supportDRL"] = it }
                it.forceOnline?.let {
                    if (it == true)
                        bundle.putBoolean(EmvTransDataConstrants.ISQPBOCFORCEONLINE, true)
                    else
                        bundle.putBoolean(EmvTransDataConstrants.ISQPBOCFORCEONLINE, false)

                }
                it.forceOnlinePin?.let {
                    if (it == true)
                        bundle.putBoolean(EmvTransDataConstrants.FORCE_ONLINE_CALL_PIN, true)
                    else
                        bundle.putBoolean(EmvTransDataConstrants.FORCE_ONLINE_CALL_PIN, false)

                }
            }

            initEncryption()
            job = CoroutineScope(Dispatchers.Default).launch {
                getDeviceService(context)?.let {
                    it.emvHandler?.emvTrans(bundle, emvListener)
                    if(isMagSupported) {
                        it.magCardReader?.searchCard(magCardListener,
                            transConfig?.cardCheckTimeout?.toInt()
                                ?: EmvConstants.EMV_DEFAULT_CARD_READ_TIMEOUT,
                            Bundle()
                        )
                        it.magCardReader?.setIsCheckLrc(true)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            this.iEmvSdkResponseListener?.onEmvSdkResponse(EmvSdkException(errorMessage = e.message.toString()))
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    fun printAidInfo() {
        for (it in deviceService?.emvHandler?.getAidParaList() ?: listOf<EmvAidPara>()) {
            Log.d(TAG, "------------------ AID PARAM ------------------")
            Log.d(
                TAG,
                "AID: ${it.aid.copyOf(it.aiD_Length).toHexString()}"
            ) // AID trimmed to actual length
            Log.d(TAG, "AID Length: ${it.aiD_Length}")
            Log.d(TAG, "AppSelIndicator: ${it.appSelIndicator}")
            Log.d(TAG, "TerminalPriority: ${it.terminalPriority}")
            Log.d(TAG, "MaxTargetDomestic: ${it.maxTargetDomestic}")
            Log.d(TAG, "TargetPercentageDomestic: ${it.targetPercentageDomestic}")
            Log.d(TAG, "TFL_Domestic: ${it.tfL_Domestic.toHexString()}")
            Log.d(TAG, "ThresholdValueDomestic: ${it.thresholdValueDomestic.toHexString()}")
            Log.d(TAG, "MaxTargetPercentageInt: ${it.maxTargetPercentageInt}")
            Log.d(TAG, "TargetPercentageInt: ${it.targetPercentageInt}")
            Log.d(TAG, "TFL_International: ${it.tfL_International.toHexString()}")
            Log.d(TAG, "ThresholdValueInt: ${it.thresholdValueInt.toHexString()}")
            Log.d(TAG, "TermAppVer: ${it.termAppVer.toHexString()}")
            Log.d(TAG, "MerCateCode: ${it.merCateCode.toHexString()}")
            Log.d(TAG, "TransCateCode: ${it.transCateCode}")
            Log.d(TAG, "TrnCurrencyCode: ${it.trnCurrencyCode.toHexString()}")
            Log.d(TAG, "TermCountryCode: ${it.termCountryCode.toHexString()}")
            Log.d(TAG, "TAC_Default: ${it.taC_Default.toHexString()}")
            Log.d(TAG, "TAC_Denial: ${it.taC_Denial.toHexString()}")
            Log.d(TAG, "TAC_Online: ${it.taC_Online.toHexString()}")
            Log.d(TAG, "DDOL: ${it.ddol.copyOf(it.ddoL_Length.toInt()).toHexString()}")
            Log.d(TAG, "DDOL Length: ${it.ddoL_Length}")
            Log.d(TAG, "TDOL: ${it.tdol.copyOf(it.tdoL_Length).toHexString()}")
            Log.d(TAG, "TDOL Length: ${it.tdoL_Length}")
            Log.d(TAG, "TrnCurrencyExp: ${it.trnCurrencyExp}")
            Log.d(TAG, "EC_TFL: ${it.eC_TFL.toHexString()}")
            Log.d(TAG, "TermType: ${it.termType}")
            Log.d(TAG, "TermCap: ${it.termCap.toHexString()}")
            Log.d(TAG, "AddTermCap: ${it.addTermCap.toHexString()}")
            Log.d(TAG, "RFOfflineLimit: ${it.rfOfflineLimit.toHexString()}")
            Log.d(TAG, "RFTransLimit: ${it.rfTransLimit.toHexString()}")
            Log.d(TAG, "RFCVMLimit: ${it.rfcvmLimit.toHexString()}")
            Log.d(TAG, "TransProp: ${it.transProp.toHexString()}")
            Log.d(TAG, "RiskManagement(9F1D): ${it.riskManagement9F1D.toHexString()}")
            Log.d(TAG, "StatusCheck: ${it.statusCheck}")
            //Log.d(TAG, "OnlinePinCap (DF18): ${it.cOnlinePinCap_b_DF18}")
            Log.d(TAG, "AcquirerID: ${it.acquirerID.toHexString()}")
            Log.d(TAG, "----------------------------------------------")
        }

    }

    @OptIn(ExperimentalStdlibApi::class)
    fun initAidConfig(aidConfig: AidConfig?): Boolean {
        var result = false
        /* Clear Aid Config first */
        deviceService?.emvHandler?.clearAIDParam()

        aidConfig?.let {
            result = addContactAid(it) && addContactlessAid(it)
            //printAidInfo()
        }
        return result
    }

    @OptIn(ExperimentalStdlibApi::class)
    fun initCAPKeys(capKeys: List<CAPKey>?): Boolean {
        var result = true
        capKeys?.let {
            /* Clear CAP Keys first */
            deviceService?.emvHandler?.clearCAPKParam()

            /* Add CAP Keys */
            var capkList = listOf<EmvCapk>()
            for (key in it) {
                val capk = EmvCapk()
                key.rid?.let {
                    var hex = it.hexToByteArray()
                    hex.copyInto(capk.rid, 0, 0, minOf(hex.size, capk.rid.size))
                }
                key.index?.let {
                    capk.cA_PKIndex = it.hexToByte()
                }
                key.exponent?.let {
                    var hex = it.hexToByteArray()
                    var length = minOf(hex.size, capk.capkExponent.size)
                    capk.lengthOfCAPKExponent = length
                    hex.copyInto(capk.capkExponent, capk.capkExponent.size-length, 0, length)
                }
                key.modulus?.let {
                    var hex = it.hexToByteArray()
                    var length = minOf(hex.size, capk.capkModulus.size)
                    capk.lengthOfCAPKModulus = length
                    hex.copyInto(capk.capkModulus, 0, 0, length)
                }
                key.checksum?.let {
                    var hex = it.hexToByteArray()
                    hex.copyInto(capk.checksumHash, 0, 0, minOf(hex.size, capk.checksumHash.size))
                }

                /* Hard coded constants for algorithm SHA1 */
                capk.cA_PKAlgoIndicator = EmvConstants.EMV_HASH_ALG_SHA1.hexToByte()
                capk.cA_HashAlgoIndicator = EmvConstants.EMV_HASH_ALG_SHA1.hexToByte()

                capkList = capkList.plus(capk)
            }
            result = deviceService?.emvHandler?.setCAPKList(capkList) == 0
        }
        return result
    }

    @OptIn(ExperimentalStdlibApi::class)
    fun addContactAid(config: AidConfig): Boolean {
        var result = true
        try {
            /* Add Contact Configuration */
            var aidParaList = listOf<EmvAidPara>()
            for (aid in config.contact?.aidList ?: emptyList()) {
                val aidPara = EmvAidPara()
                (aid.aid ?: config.contact?.aid ?: config.aid)?.let {
                    var hex = it.hexToByteArray()
                    aidPara.aiD_Length = minOf(hex.size, aidPara.aid.size)
                    hex.copyInto(aidPara.aid, 0, 0, aidPara.aiD_Length)
                }
                (aid.appVersion ?: config.contact?.appVersion
                ?: config.appVersion)?.let {
                    var hex = it.padEnd(aidPara.termAppVer.size * 2, '0').hexToByteArray()
                    hex.copyInto(aidPara.termAppVer, 0, 0, minOf(hex.size, aidPara.termAppVer.size))
                }
                (aid.terminalFloorLimit ?: config.contact?.terminalFloorLimit
                ?: config.terminalFloorLimit)?.let {
                    var hex = it.padStart(12, '0').hexToByteArray()
                    var binaryVal = bcdToBinaryArray(hex)
                    binaryVal.copyInto(
                        aidPara.tfL_Domestic,
                        0,
                        0,
                        minOf(binaryVal.size, aidPara.tfL_Domestic.size)
                    )
                    binaryVal.copyInto(
                        aidPara.tfL_International,
                        0,
                        0,
                        minOf(binaryVal.size, aidPara.tfL_International.size)
                    )
                    hex.copyInto(aidPara.eC_TFL, 0, 0, minOf(hex.size, aidPara.eC_TFL.size))
                }
                (aid.terminalFloorLimitCheck ?: config.contact?.terminalFloorLimitCheck
                ?: config.terminalFloorLimitCheck)?.let {
                }
                (aid.tacDefault ?: config.contact?.tacDefault
                ?: config.tacDefault)?.let {
                    var hex = it.padEnd(aidPara.taC_Default.size * 2, '0').hexToByteArray()
                    hex.copyInto(
                        aidPara.taC_Default,
                        0,
                        0,
                        minOf(hex.size, aidPara.taC_Default.size)
                    )
                }
                (aid.tacDenial ?: config.contact?.tacDenial
                ?: config.tacDenial)?.let {
                    var hex = it.padEnd(aidPara.taC_Denial.size * 2, '0').hexToByteArray()
                    hex.copyInto(aidPara.taC_Denial, 0, 0, minOf(hex.size, aidPara.taC_Denial.size))
                }
                (aid.tacOnline ?: config.contact?.tacOnline
                ?: config.tacOnline)?.let {
                    var hex = it.padEnd(aidPara.taC_Online.size * 2, '0').hexToByteArray()
                    hex.copyInto(aidPara.taC_Online, 0, 0, minOf(hex.size, aidPara.taC_Online.size))
                }
                (aid.defaultDDOL ?: config.contact?.defaultDDOL
                ?: config.defaultDDOL)?.let {
                    var hex = it.hexToByteArray()
                    aidPara.ddoL_Length = minOf(hex.size, aidPara.ddol.size).toByte()
                    hex.copyInto(aidPara.ddol, 0, 0, aidPara.ddoL_Length.toInt())
                }
                (aid.defaultTDOL ?: config.contact?.defaultTDOL
                ?: config.defaultTDOL)?.let {
                    var hex = it.hexToByteArray()
                    aidPara.tdoL_Length = minOf(hex.size, aidPara.tdol.size)
                    hex.copyInto(aidPara.tdol, 0, 0, aidPara.tdoL_Length)
                }
                (aid.acquirerId ?: config.contact?.acquirerId
                ?: config.acquirerId)?.let {
                    var hex = it.padStart(aidPara.acquirerID.size * 2, '0').hexToByteArray()
                    hex.copyInto(aidPara.acquirerID, 0, 0, minOf(hex.size, aidPara.acquirerID.size))
                }
                (aid.threshold ?: config.contact?.threshold
                ?: config.threshold)?.let {
                    var hex =
                        it.padStart(aidPara.thresholdValueDomestic.size * 2, '0').hexToByteArray()
                    var binaryVal = bcdToBinaryArray(hex)
                    binaryVal.copyInto(
                        aidPara.thresholdValueDomestic,
                        0,
                        0,
                        minOf(binaryVal.size, aidPara.thresholdValueDomestic.size)
                    )
                    binaryVal.copyInto(
                        aidPara.thresholdValueInt,
                        0,
                        0,
                        minOf(binaryVal.size, aidPara.thresholdValueInt.size)
                    )
                }
                (aid.targetPercentage ?: config.contact?.targetPercentage
                ?: config.targetPercentage)?.let {
                    aidPara.targetPercentageDomestic = it.hexToByte()
                    aidPara.targetPercentageInt = it.hexToByte()
                }
                (aid.maxTargetPercentage ?: config.contact?.maxTargetPercentage
                ?: config.maxTargetPercentage)?.let {
                    aidPara.maxTargetDomestic = it.hexToByte()
                    aidPara.maxTargetPercentageInt = it.hexToByte()
                }
                (aid.appSelIndicator ?: config.contact?.appSelIndicator
                ?: config.appSelIndicator)?.let {
                    aidPara.appSelIndicator = it.hexToByte()
                }
                (aid.terminalAppPriority ?: config.contact?.terminalAppPriority
                ?: config.terminalAppPriority)?.let {
                    aidPara.terminalPriority = it.hexToByte()
                }
                (aid.terminalCapabilities ?: config.contact?.terminalCapabilities
                ?: config.terminalCapabilities)?.let {
                    var hex = it.padEnd(aidPara.termCap.size * 2, '0').hexToByteArray()
                    hex.copyInto(aidPara.termCap, 0, 0, minOf(hex.size, aidPara.termCap.size))
                }
                (aid.addlTerminalCapabilities ?: config.contact?.addlTerminalCapabilities
                ?: config.addlTerminalCapabilities)?.let {
                    var hex = it.padEnd(aidPara.addTermCap.size * 2, '0').hexToByteArray()
                    hex.copyInto(aidPara.addTermCap, 0, 0, minOf(hex.size, aidPara.addTermCap.size))
                }
                (aid.terminalCountryCode ?: config.contact?.terminalCountryCode
                ?: config.terminalCountryCode)?.let {
                    var hex = it.padStart(aidPara.termCountryCode.size * 2, '0').hexToByteArray()
                    hex.copyInto(
                        aidPara.termCountryCode,
                        0,
                        0,
                        minOf(hex.size, aidPara.termCountryCode.size)
                    )
                }

                (aid.rdrCVMRequiredLimit ?: config.contact?.rdrCVMRequiredLimit
                ?: config.rdrCVMRequiredLimit)?.let {
                    var hex = it.padStart(aidPara.rfcvmLimit.size * 2, '0').hexToByteArray()
                    hex.copyInto(aidPara.rfcvmLimit, 0, 0, minOf(hex.size, aidPara.rfcvmLimit.size))
                }
                (aid.rdrCtlsFloorLimit ?: config.contact?.rdrCtlsFloorLimit
                ?: config.rdrCtlsFloorLimit)?.let {
                    var hex = it.padStart(aidPara.rfOfflineLimit.size * 2, '0').hexToByteArray()
                    hex.copyInto(
                        aidPara.rfOfflineLimit,
                        0,
                        0,
                        minOf(hex.size, aidPara.rfOfflineLimit.size)
                    )
                }
                (aid.rdrCtlsTransLimit ?: config.contact?.rdrCtlsTransLimit
                ?: config.rdrCtlsTransLimit ?: EmvConstants.EMV_DEFAULT_CTLS_RDR_LIMIT).let {
                    var hex = it.padStart(aidPara.rfTransLimit.size * 2, '0').hexToByteArray()
                    hex.copyInto(
                        aidPara.rfTransLimit,
                        0,
                        0,
                        minOf(hex.size, aidPara.rfTransLimit.size)
                    )
                }
                (aid.merchantCategoryCode ?: config.contact?.merchantCategoryCode
                ?: config.merchantCategoryCode)?.let {
                    var hex = it.padStart(aidPara.merCateCode.size * 2, '0').hexToByteArray()
                    hex.copyInto(
                        aidPara.merCateCode,
                        0,
                        0,
                        minOf(hex.size, aidPara.merCateCode.size)
                    )
                }

                (aid.currencyCode ?: config.contact?.currencyCode
                ?: config.currencyCode)?.let {
                    var hex = it.padStart(aidPara.trnCurrencyCode.size * 2, '0').hexToByteArray()
                    hex.copyInto(
                        aidPara.trnCurrencyCode,
                        0,
                        0,
                        minOf(hex.size, aidPara.trnCurrencyCode.size)
                    )
                }
                (aid.currencyExponent ?: config.contact?.currencyExponent
                ?: config.currencyExponent)?.let {
                    aidPara.trnCurrencyExp = it.hexToByte()
                }
                (aid.terminalType ?: config.contact?.terminalType
                ?: config.terminalType)?.let {
                    aidPara.termType = it.hexToByte()
                }
                (aid.riskManagementData ?: config.contact?.riskManagementData
                ?: config.riskManagementData)?.let {
                    var hex = it.padEnd(aidPara.riskManagement9F1D.size * 2, '0').hexToByteArray()
                    hex.copyInto(
                        aidPara.riskManagement9F1D,
                        0,
                        0,
                        minOf(hex.size, aidPara.riskManagement9F1D.size)
                    )
                }
                (aid.statusCheckSupported ?: config.contact?.statusCheckSupported
                ?: config.statusCheckSupported)?.let {
                    aidPara.statusCheck = it.hexToByte()
                }

                aidParaList = aidParaList.plus(aidPara)
            }

            result = result && deviceService?.emvHandler?.setAidParaList(aidParaList) == 0
        } catch (exception: Exception) {
            result = false
            exception.printStackTrace()
        }

        return result
    }

    @OptIn(ExperimentalStdlibApi::class)
    fun addContactlessAid(config: AidConfig): Boolean {
        var result = true
        try {
            /* Add Contact Configuration */
            var aidParaList = deviceService?.emvHandler?.aidParaList    // Read the existing list added for contact
            for (aid in config.contactless?.aidList ?: emptyList()) {
                val aidPara = EmvAidPara()
                (aid.aid ?: config.contactless?.aid ?: config.aid)?.let {
                    var hex = it.hexToByteArray()
                    aidPara.aiD_Length = minOf(hex.size, aidPara.aid.size)
                    hex.copyInto(aidPara.aid, 0, 0, aidPara.aiD_Length)
                }
                (aid.appVersion ?: config.contactless?.appVersion
                ?: config.appVersion)?.let {
                    var hex = it.padEnd(aidPara.termAppVer.size * 2, '0').hexToByteArray()
                    hex.copyInto(aidPara.termAppVer, 0, 0, minOf(hex.size, aidPara.termAppVer.size))
                }
                (aid.terminalFloorLimit ?: config.contactless?.terminalFloorLimit
                ?: config.terminalFloorLimit)?.let {
                    var hex = it.padStart(12, '0').hexToByteArray()
                    var binaryVal = bcdToBinaryArray(hex)
                    binaryVal.copyInto(
                        aidPara.tfL_Domestic,
                        0,
                        0,
                        minOf(binaryVal.size, aidPara.tfL_Domestic.size)
                    )
                    binaryVal.copyInto(
                        aidPara.tfL_International,
                        0,
                        0,
                        minOf(binaryVal.size, aidPara.tfL_International.size)
                    )
                    hex.copyInto(aidPara.eC_TFL, 0, 0, minOf(hex.size, aidPara.eC_TFL.size))
                }
                (aid.terminalFloorLimitCheck ?: config.contactless?.terminalFloorLimitCheck
                ?: config.terminalFloorLimitCheck)?.let {
                }
                (aid.tacDefault ?: config.contactless?.tacDefault
                ?: config.tacDefault)?.let {
                    var hex = it.padEnd(aidPara.taC_Default.size * 2, '0').hexToByteArray()
                    hex.copyInto(
                        aidPara.taC_Default,
                        0,
                        0,
                        minOf(hex.size, aidPara.taC_Default.size)
                    )
                }
                (aid.tacDenial ?: config.contactless?.tacDenial
                ?: config.tacDenial)?.let {
                    var hex = it.padEnd(aidPara.taC_Denial.size * 2, '0').hexToByteArray()
                    hex.copyInto(aidPara.taC_Denial, 0, 0, minOf(hex.size, aidPara.taC_Denial.size))
                }
                (aid.tacOnline ?: config.contactless?.tacOnline
                ?: config.tacOnline)?.let {
                    var hex = it.padEnd(aidPara.taC_Online.size * 2, '0').hexToByteArray()
                    hex.copyInto(aidPara.taC_Online, 0, 0, minOf(hex.size, aidPara.taC_Online.size))
                }
                (aid.defaultDDOL ?: config.contactless?.defaultDDOL
                ?: config.defaultDDOL)?.let {
                    var hex = it.hexToByteArray()
                    aidPara.ddoL_Length = minOf(hex.size, aidPara.ddol.size).toByte()
                    hex.copyInto(aidPara.ddol, 0, 0, aidPara.ddoL_Length.toInt())
                }
                (aid.defaultTDOL ?: config.contactless?.defaultTDOL
                ?: config.defaultTDOL)?.let {
                    var hex = it.hexToByteArray()
                    aidPara.tdoL_Length = minOf(hex.size, aidPara.tdol.size)
                    hex.copyInto(aidPara.tdol, 0, 0, aidPara.tdoL_Length)
                }
                (aid.acquirerId ?: config.contactless?.acquirerId
                ?: config.acquirerId)?.let {
                    var hex = it.padStart(aidPara.acquirerID.size * 2, '0').hexToByteArray()
                    hex.copyInto(aidPara.acquirerID, 0, 0, minOf(hex.size, aidPara.acquirerID.size))
                }
                (aid.threshold ?: config.contactless?.threshold
                ?: config.threshold)?.let {
                    var hex =
                        it.padStart(aidPara.thresholdValueDomestic.size * 2, '0').hexToByteArray()
                    var binaryVal = bcdToBinaryArray(hex)
                    binaryVal.copyInto(
                        aidPara.thresholdValueDomestic,
                        0,
                        0,
                        minOf(binaryVal.size, aidPara.thresholdValueDomestic.size)
                    )
                    binaryVal.copyInto(
                        aidPara.thresholdValueInt,
                        0,
                        0,
                        minOf(binaryVal.size, aidPara.thresholdValueInt.size)
                    )
                }
                (aid.targetPercentage ?: config.contactless?.targetPercentage
                ?: config.targetPercentage)?.let {
                    aidPara.targetPercentageDomestic = it.hexToByte()
                    aidPara.targetPercentageInt = it.hexToByte()
                }
                (aid.maxTargetPercentage ?: config.contactless?.maxTargetPercentage
                ?: config.maxTargetPercentage)?.let {
                    aidPara.maxTargetDomestic = it.hexToByte()
                    aidPara.maxTargetPercentageInt = it.hexToByte()
                }
                (aid.appSelIndicator ?: config.contactless?.appSelIndicator
                ?: config.appSelIndicator)?.let {
                    aidPara.appSelIndicator = it.hexToByte()
                }
                (aid.terminalAppPriority ?: config.contactless?.terminalAppPriority
                ?: config.terminalAppPriority)?.let {
                    aidPara.terminalPriority = it.hexToByte()
                }
                (aid.terminalCapabilities ?: config.contactless?.terminalCapabilities
                ?: config.terminalCapabilities)?.let {
                    var hex = it.padEnd(aidPara.termCap.size * 2, '0').hexToByteArray()
                    hex.copyInto(aidPara.termCap, 0, 0, minOf(hex.size, aidPara.termCap.size))
                }
                (aid.addlTerminalCapabilities ?: config.contactless?.addlTerminalCapabilities
                ?: config.addlTerminalCapabilities)?.let {
                    var hex = it.padEnd(aidPara.addTermCap.size * 2, '0').hexToByteArray()
                    hex.copyInto(aidPara.addTermCap, 0, 0, minOf(hex.size, aidPara.addTermCap.size))
                }
                (aid.terminalCountryCode ?: config.contactless?.terminalCountryCode
                ?: config.terminalCountryCode)?.let {
                    var hex = it.padStart(aidPara.termCountryCode.size * 2, '0').hexToByteArray()
                    hex.copyInto(
                        aidPara.termCountryCode,
                        0,
                        0,
                        minOf(hex.size, aidPara.termCountryCode.size)
                    )
                }
                (aid.rdrCVMRequiredLimit ?: config.contactless?.rdrCVMRequiredLimit
                ?: config.rdrCVMRequiredLimit)?.let {
                    var hex = it.padStart(aidPara.rfcvmLimit.size * 2, '0').hexToByteArray()
                    hex.copyInto(aidPara.rfcvmLimit, 0, 0, minOf(hex.size, aidPara.rfcvmLimit.size))
                }
                (aid.rdrCtlsFloorLimit ?: config.contactless?.rdrCtlsFloorLimit
                ?: config.rdrCtlsFloorLimit)?.let {
                    var hex = it.padStart(aidPara.rfOfflineLimit.size * 2, '0').hexToByteArray()
                    hex.copyInto(
                        aidPara.rfOfflineLimit,
                        0,
                        0,
                        minOf(hex.size, aidPara.rfOfflineLimit.size)
                    )
                }
                (aid.rdrCtlsTransLimit ?: config.contactless?.rdrCtlsTransLimit
                ?: config.rdrCtlsTransLimit ?: EmvConstants.EMV_DEFAULT_CTLS_RDR_LIMIT).let {
                    var hex = it.padStart(aidPara.rfTransLimit.size * 2, '0').hexToByteArray()
                    hex.copyInto(
                        aidPara.rfTransLimit,
                        0,
                        0,
                        minOf(hex.size, aidPara.rfTransLimit.size)
                    )
                }
                (aid.merchantCategoryCode ?: config.contactless?.merchantCategoryCode
                ?: config.merchantCategoryCode)?.let {
                    var hex = it.padStart(aidPara.merCateCode.size * 2, '0').hexToByteArray()
                    hex.copyInto(
                        aidPara.merCateCode,
                        0,
                        0,
                        minOf(hex.size, aidPara.merCateCode.size)
                    )
                }

                (aid.currencyCode ?: config.contactless?.currencyCode
                ?: config.currencyCode)?.let {
                    var hex = it.padStart(aidPara.trnCurrencyCode.size * 2, '0').hexToByteArray()
                    hex.copyInto(
                        aidPara.trnCurrencyCode,
                        0,
                        0,
                        minOf(hex.size, aidPara.trnCurrencyCode.size)
                    )
                }
                (aid.currencyExponent ?: config.contactless?.currencyExponent
                ?: config.currencyExponent)?.let {
                    aidPara.trnCurrencyExp = it.hexToByte()
                }
                (aid.terminalType ?: config.contactless?.terminalType
                ?: config.terminalType)?.let {
                    aidPara.termType = it.hexToByte()
                }
                (aid.riskManagementData ?: config.contactless?.riskManagementData
                ?: config.riskManagementData)?.let {
                    var hex = it.padEnd(aidPara.riskManagement9F1D.size * 2, '0').hexToByteArray()
                    hex.copyInto(
                        aidPara.riskManagement9F1D,
                        0,
                        0,
                        minOf(hex.size, aidPara.riskManagement9F1D.size)
                    )
                }
                (aid.statusCheckSupported ?: config.contactless?.statusCheckSupported
                ?: config.statusCheckSupported)?.let {
                    aidPara.statusCheck = it.hexToByte()
                }
                (aid.ttq ?: config.contactless?.ttq
                ?: config.ttq)?.let {
                    var hex = it.padEnd(aidPara.transProp.size * 2, '0').hexToByteArray()
                    hex.copyInto(
                        aidPara.transProp,
                        0,
                        0,
                        minOf(hex.size, aidPara.transProp.size)
                    )
                }

                aidParaList = aidParaList?.plus(aidPara)
            }

            result = result && deviceService?.emvHandler?.setAidParaList(aidParaList) == 0
        } catch (exception: Exception) {
            result = false
            exception.printStackTrace()
        }

        return result
    }

    private fun readPan(): String? {
        val pan = getPbocData(EmvConstants.EMV_TAG_PAN)
        if (pan.isNullOrEmpty()) {
            return getPanFromTrack2()
        }
        return if (pan.endsWith("F")) {
            pan.substring(0, pan.length - 1)
        } else {
            pan
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun getPbocData(tagName: String, isHex: Boolean?=true): String? {
        return try {
            val bundle = Bundle().apply {
                putInt(DukptCalcObj.Param.DUKPT_KEY_INDEX, EncryptionConstants.KEY_INDEX_DATA_KEY.ordinal)
            }

            (deviceService?.emvHandler?.getTlvs(tagName.hexToByteArray(), EmvDataSource.FROMKERNEL, bundle) ?:
            deviceService?.emvHandler?.getTlvs(tagName.hexToByteArray(), EmvDataSource.FROMCARD, bundle))?.let {
                if (it.isNotEmpty()) {
                    if (isHex==true) it.toHexString().uppercase() else it.decodeToString()
                } else {
                    null
                }
            } ?:let {
                null
            }
        } catch (e: RemoteException) {
            e.printStackTrace()
            null
        }
    }

    private fun readTrack2(): String? {
        val track2 = getPbocData(EmvDataSource.GET_TRACK2_TAG_6B)
        return if (!track2.isNullOrEmpty() && track2.endsWith("F")) {
            track2.substring(0, track2.length - 1)
        } else {
            track2
        }
    }

    private fun getPanFromTrack2(): String? {
        val track2 = readTrack2()
        if (track2 != null) {
            for (i in track2.indices) {
                if (track2[i] == '=' || track2[i] == 'D') {
                    val endIndex = minOf(i, 19)
                    return track2.substring(0, endIndex)
                }
            }
        }
        return null
    }

    companion object  : ServiceConnection{
        private var iEmvSdkResponseListener: IEmvSdkResponseListener? = null
        private var job: Job? = null
        private var _amount: Long = 0L
        private var _sAmount: String = ""
        private var _cashbackAmount: Long = 0L
        private var pinBlock: String? = null
        private var ksn: String? = null
        private var nfcTlv: String? = null
        private var nfcDisplayMsgId: DisplayMsgId? = null
        private var checkCardResult: CheckCardResult? = null

        private var deviceService: DeviceServiceEngine? = null
        private var serviceConnected = CompletableDeferred<Boolean>()
        private var TAG = "MOREFUN"

        private suspend fun bindService(context: Context?=null, recreate : Boolean? = false) {
            try {
                //Log.d(TAG, "Binding Service with context $context")
                deviceService?.emvHandler?.endPBOC()
                deviceService?.magCardReader?.stopSearch()
                deviceService?.takeIf { recreate == true }?.let {
                    Log.d(TAG, "Unbinding earlier service")
                    context?.unbindService(this)
                    deviceService?.logout()
                    deviceService = null
                    serviceConnected = CompletableDeferred<Boolean>()
                }

                deviceService ?: let {
                    val intent = Intent(EmvConstants.MF_SERVICE_ACTION).apply {
                        setPackage(EmvConstants.MF_SERVICE_PACKAGE)
                    }
                    Log.d(TAG, "Binding new service")
                    context?.bindService(intent, this, Context.BIND_AUTO_CREATE)?.let {
                        if (it == true) {
                            Log.d(TAG, "Service binding successful")
                        } else {
                            Log.d(TAG, "Service binding failed")
                        }
                    }
                }
            } catch (exception: Exception) {
                exception.printStackTrace()
            }
        }

        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            deviceService = DeviceServiceEngine.Stub.asInterface(binder)

            var bundle = Bundle()
            Log.d(TAG, "Service connected")
            if (deviceService?.login(bundle, EmvConstants.MF_BUSINESS_ID_CLEAR_TRACK) == 0) {
                serviceConnected.complete(true)
                Log.d(TAG, "Login Successful")
            } else {
                serviceConnected.complete(false)
                Log.d(TAG, "Login Failure")
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            deviceService = null
            serviceConnected.complete(false)
            Log.d(TAG, "Service Disconnected")
        }

        fun resetTransData() {
            /* Interrupt existing thread if any */
            job?.cancel()
            job = null

            /* Reset transaction parameters */
            _amount = 0L
            _cashbackAmount = 0L
            pinBlock = null
            ksn = null
            nfcTlv = null
            nfcDisplayMsgId = null
            checkCardResult = null
        }

        suspend fun getDeviceService(context: Context?=null) : DeviceServiceEngine?
        {
            bindService(context)
            if(serviceConnected.isActive)
                serviceConnected.await()
            return deviceService
        }

        @OptIn(ExperimentalStdlibApi::class)
        suspend fun injectTMK(tmk: String, kcv: String, context: Context?=null): Boolean {

            Log.d("HARDWARE_UTILS", "injectTMK() called")
            Log.d("HARDWARE_UTILS", "TMK: $tmk")
            Log.d("HARDWARE_UTILS", "KCV: $kcv")
            Log.d("HARDWARE_UTILS", "TMK Length: ${tmk.length}")

            try {
                val tmkByteArray = tmk.hexToByteArray()
                Log.d("HARDWARE_UTILS", "Converted TMK to ByteArray: ${tmkByteArray.joinToString()}")

                var result = getDeviceService(context)?.pinPad?.loadPlainDesKey(
                    EncryptionConstants.KEY_INDEX_MAIN_KEY.ordinal,
                    tmkByteArray,
                    tmkByteArray.size
                )

                Log.d("HARDWARE_UTILS", "loadPlainMKey() result: $result")

                val isSuccess = result == 0
                Log.d("HARDWARE_UTILS", "TMK Injection Success: $isSuccess")

                var checkKey = deviceService?.pinPad?.getKeyKcv(CheckKeyEnum.DES_MASTER_KEY,EncryptionConstants.KEY_INDEX_MAIN_KEY.ordinal)

                Log.d("HARDWARE_UTILS", "Key KCV: ${checkKey?.kcv}")

                val kcvMatches = kcv==checkKey?.kcv

                Log.d("HARDWARE_UTILS", "KCV Matches : $kcvMatches")

                return isSuccess && kcvMatches
            } catch (exception: Exception) {
                Log.e("HARDWARE_UTILS", "Exception during TMK injection: ${exception.message}")
                exception.printStackTrace()
                return false
            }
        }

        @OptIn(ExperimentalStdlibApi::class)
        suspend fun injectTMKKey(tmk: String, kcv: String, context: Context? = null): Boolean {

            try {

                Log.d("HARDWARE_UTILS", "Injecting TMK")
                Log.d("HARDWARE_UTILS", "TMK: $tmk")

                val pinPad = getDeviceService(context)?.pinPad ?: return false
                val tmkBytes = tmk.hexToByteArray()

                if (tmkBytes.size != 16) {
                    Log.e("HARDWARE_UTILS", "Invalid TMK length. Expected 24 bytes.")
                    return false
                }

                val result = pinPad.loadPlainMKey(
                    EncryptionConstants.KEY_INDEX_MAIN_KEY.ordinal,
                    tmkBytes,
                    tmkBytes.size,
                    false
                )

                Log.d("HARDWARE_UTILS", "loadPlainMKey result: $result")

                if (result != 0) return false

                val deviceKcv = pinPad.getKeyKcv(
                    CheckKeyEnum.DES_MASTER_KEY,
                    EncryptionConstants.KEY_INDEX_MAIN_KEY.ordinal
                )?.kcv?.uppercase()

                val expectedKcv = kcv.uppercase()

                Log.d("HARDWARE_UTILS", "Expected KCV: $expectedKcv")
                Log.d("HARDWARE_UTILS", "Device KCV: $deviceKcv")

                return deviceKcv == expectedKcv

            } catch (e: Exception) {
                Log.e("HARDWARE_UTILS", "TMK injection exception", e)
                return false
            }
        }

        suspend fun loadAndVerifyWorkingPinKey(
            workingKeyHex: String,
            context: Context?=null,
            keyIndex: Int = 1,

        ): Boolean {
            try {
                Log.d("KEY_DEBUG", "Starting loadAndVerifyWorkingPinKey()")
                Log.d("KEY_DEBUG", "Input Working Key Hex: $workingKeyHex")
                Log.d("KEY_DEBUG", "Key Index: $keyIndex")

                // Convert hex to byte array
                val keyBytes = workingKeyHex.chunked(2)
                    .map { it.toInt(16).toByte() }
                    .toByteArray()
                Log.d("KEY_DEBUG", "Key Bytes: ${keyBytes.joinToString("") { "%02X".format(it) }}")

                // Load the key
                val loadResult = getDeviceService(context)?.pinPad?.loadWKey(1, keyIndex, keyBytes, 2)
                Log.d("KEY_DEBUG", "loadWKey result code: $loadResult")
                if (loadResult != 0) {
                    Log.e("KEY_DEBUG", "Failed to load working key. loadWKey returned $loadResult")
                    return false
                } else {
                    Log.d("KEY_DEBUG", "Working key loaded successfully.")
                }

                // Calculate KCV
                val kcv = getDeviceService(context)?.pinPad?.calcWKeyKCV(1, keyIndex)
                if (kcv == null) {
                    Log.e("KEY_DEBUG", "calcWKeyKCV returned null")
                    return false
                }
                Log.d(
                    "KEY_DEBUG",
                    "Calculated KCV: ${kcv.joinToString("") { "%02X".format(it) }}"
                )


                val result = kcv.isNotEmpty()
                Log.d("KEY_DEBUG", "Key injection result: $result")
                return result

            } catch (e: Exception) {
                Log.e("KEY_DEBUG", "Exception in loadAndVerifyWorkingPinKey: ${e.message}")
                return false
            }
        }

        @OptIn(ExperimentalStdlibApi::class)
        suspend fun injectWorkingKey(pinKey: String, context: Context? = null): Boolean {

            return try {

                Log.d("KEY_INJECT", "Starting Working Key Injection")
                Log.d("KEY_INJECT", "PIN Key (HEX): $pinKey")
                Log.d("KEY_INJECT", "PIN Key Length: ${pinKey.length}")

                val keyBytes = pinKey.hexToByteArray()

                Log.d("KEY_INJECT", "Converted keyBytes size: ${keyBytes.size}")

                val deviceService = getDeviceService(context)

                if (deviceService == null) {
                    Log.e("KEY_INJECT", "Device service is NULL")
                    return false
                }

                Log.d("KEY_INJECT", "Device service connected successfully")

                val result = deviceService.pinPad.loadWKey(
                    EncryptionConstants.KEY_INDEX_MAIN_KEY.ordinal,
                    0, // PIN KEY TYPE
                    keyBytes,
                    keyBytes.size
                )
                if (result != 0) return false
                val kcvObj = deviceService.pinPad.getKeyKcv(
                    CheckKeyEnum.DES_PIN_KEY,
                    EncryptionConstants.MS_KEY_TYPE_PIN
                )

                if (kcvObj?.kcv.isNullOrEmpty()) {
                    Log.e("KEY_DEBUG", "getKeyKcv returned null or empty")
                    return false
                }


                val kcvHex = kcvObj.kcv.uppercase()
                Log.d("KEY_DEBUG", "Calculated KCV: $kcvHex")


                return kcvHex.isNotEmpty()

            } catch (e: Exception) {
                Log.e("KEY_INJECT", "Exception during key injection: ${e.message}", e)
                false
            }
        }


        suspend fun injectDukptPinKey(ipek: String, ksn: String, context: Context?=null): Boolean {
            Log.d("HARDWARE_UTILS", "injectDukptPinKey() called")
            Log.d("HARDWARE_UTILS", "IPEK: $ipek")
            Log.d("HARDWARE_UTILS", "KSN: $ksn")

            return try {
                val dukptLoadObj = DukptLoadObj(
                    ipek,
                    ksn,
                    DukptLoadObj.DukptKeyTypeEnum.DUKPT_IPEK_PLAINTEXT,
                    EncryptionConstants.KEY_LOAD_INDEX_PIN_KEY
                )

                Log.d("HARDWARE_UTILS", "DukptLoadObj created: $dukptLoadObj")

                val result = getDeviceService(context)?.pinPad?.dukptLoad(dukptLoadObj)
                Log.d("HARDWARE_UTILS", "dukptLoad() result: $result")

                val isSuccess = result == 0
                Log.d("HARDWARE_UTILS", "DUKPT PIN Key Injection Success: $isSuccess")

                isSuccess
            } catch (e: Exception) {
                Log.e("HARDWARE_UTILS", "Exception during DUKPT key injection: ${e.message}")
                e.printStackTrace()
                false
            }
        }

        @OptIn(ExperimentalStdlibApi::class)
        fun getEmvTag(tag: String?): String? {
            return try {
                val bundle = Bundle().apply {
                    putInt(
                        DukptCalcObj.Param.DUKPT_KEY_INDEX,
                        EncryptionConstants.KEY_INDEX_DATA_KEY.ordinal
                    )
                }

                (deviceService?.emvHandler?.getTlvs(
                    tag?.hexToByteArray(),
                    EmvDataSource.FROMKERNEL,
                    bundle
                ) ?: deviceService?.emvHandler?.getTlvs(
                    tag?.hexToByteArray(),
                    EmvDataSource.FROMCARD,
                    bundle
                ))?.let {
                    if (it.isNotEmpty()) {
                        it.toHexString().uppercase()
                    } else {
                        null
                    }
                } ?: let {
                    null
                }
            } catch (e: RemoteException) {
                e.printStackTrace()
                null
            }
        }

        fun abortPayment() {
            job?.cancel()
            job = null
            deviceService?.emvHandler?.endPBOC()
            deviceService?.magCardReader?.stopSearch()

        }

        @OptIn(ExperimentalStdlibApi::class)
        fun encryptThenRequestOnline(p0: String? = null, p1: String? = null) {
            try {
                var tlvMap = TlvUtils(p0).tlvMap.apply {
                    for (tlv in getEncryptedData(TlvUtils(p0).tlvMap))
                        put(tlv.key, tlv.value)
                }
                iEmvSdkResponseListener?.onEmvSdkOnlineRequest(tlvMap) {
                    var tlvTags = TlvUtils(it)
                    var hasOnlineResp =
                        tlvTags.tlvMap.containsKey(EmvConstants.EMV_TAG_RESP_CODE)
                    var hasIssuerAuth =
                        tlvTags.tlvMap.containsKey(EmvConstants.EMV_TAG_ISSUER_AUTH_DATA)
                    var hasAuthCode =
                        tlvTags.tlvMap.containsKey(EmvConstants.EMV_TAG_AUTH_CODE)
                    var isApprovedOnline = hasOnlineResp
                            && tlvTags.tlvMap.containsKey(EmvConstants.EMV_TAG_RESP_CODE)
                            && tlvTags.tlvMap[EmvConstants.EMV_TAG_RESP_CODE] == EmvConstants.EMV_TAG_VAL_APPROVED_ONLINE
                    var isDeclineOnline = hasOnlineResp && !isApprovedOnline

                    when (checkCardResult) {
                        CheckCardResult.CARD_SWIPED -> {
                            /* Magstripe Conditions */
                            if (isApprovedOnline == true) {
                                iEmvSdkResponseListener?.onEmvSdkResponse(
                                    EmvSdkResult.TransResult(
                                        TransStatus.APPROVED_ONLINE
                                    )
                                )
                            } else if (isDeclineOnline) {
                                iEmvSdkResponseListener?.onEmvSdkResponse(
                                    EmvSdkResult.TransResult(
                                        TransStatus.DECLINED_ONLINE
                                    )
                                )
                            } else {
                                iEmvSdkResponseListener?.onEmvSdkResponse(
                                    EmvSdkResult.TransResult(
                                        TransStatus.ERROR
                                    )
                                )
                            }
                        }

                        else ->
                        {
                            try {
                                val onlineResp = Bundle().apply {
                                    if(hasOnlineResp == true) {
                                        putString(
                                            EmvOnlineResult.REJCODE,
                                            tlvTags.tlvMap[EmvConstants.EMV_TAG_RESP_CODE]?.hexToByteArray()?.decodeToString()
                                        )
                                        putByteArray(
                                            EmvOnlineResult.RECVARPC_DATA,
                                            tlvTags.toTlvString(listOf(
                                                EmvConstants.EMV_TAG_RESP_CODE,
                                                EmvConstants.EMV_TAG_ISSUER_AUTH_DATA,
                                                EmvConstants.EMV_TAG_AUTH_CODE,
                                                EmvConstants.EMV_TAG_SCRIPT_71,
                                                EmvConstants.EMV_TAG_SCRIPT_72,
                                                )
                                            ).hexToByteArray()
                                        )
                                    }
                                    if(hasAuthCode == true)
                                        putString(EmvOnlineResult.AUTHCODE, tlvTags.tlvMap[EmvConstants.EMV_TAG_AUTH_CODE])
                                }
                                deviceService?.emvHandler?.onSetOnlineProcResponse(
                                    if(isApprovedOnline || isDeclineOnline)
                                        ServiceResult.Success else ServiceResult.Fail,
                                    onlineResp
                                )
                            } catch (e: Exception) {
                                Log.e("OnlineProc", "❌ Exception in callback", e)
                            }

                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun initEncryption() {
            /* Pin & Data key is same for lyra. Increase only one */
            deviceService?.pinPad?.increaseKSN(EncryptionConstants.KEY_INDEX_DATA_KEY.ordinal,true)
            pinBlock = null
            ksn = null
        }

        @OptIn(ExperimentalStdlibApi::class)
        fun getEncryptedData(tlvMap: HashMap<String, String>): HashMap<String, String> {
            val hashMap = HashMap<String, String>()

            // 1️⃣ Get EMV Track2
            var trackData = (getEmvTag(EmvConstants.EMV_TAG_TRACK2) ?: "").uppercase()
            if (trackData.isEmpty() && tlvMap.containsKey(EmvConstants.EMV_TAG_TRACK2)) {
                trackData = tlvMap[EmvConstants.EMV_TAG_TRACK2] ?: ""
            }
//            val expiryDate = getEmvTag(EmvConstants.EMV_TAG_CARD_EXPIRY_DATE)
//            val terminalType = getEmvTag(EmvConstants.EMV_TAG_TERM_TYPE)
//            val terminalCap = getEmvTag(EmvConstants.EMV_TAG_TERM_CAP)
//            Log.d("EMV_TAGS", "Terminal Type: $terminalType | Terminal Cap: $terminalCap")
//            Log.d("ENCRYPTION", "📅 EMV Expiry Date (raw): $expiryDate")
            if (trackData.isEmpty()) {
                Log.w("ENCRYPTION", "⚠️ Track2 not available")
                return hashMap
            }

            // 2️⃣ Convert EMV Track2 → ISO format
            val plainTrack2Full = trackData.replace('D', '=').removeSuffix("F")
            val plainPan = plainTrack2Full.substringBefore('=')
            val afterEqual = plainTrack2Full.substringAfter('=').take(10) // Only first 10 digits after '='

            // ✅ Track2 in Conduent-ready format
            val track2ForHost = "$plainPan=$afterEqual"

            // Store plain values
            hashMap[EmvConstants.EMV_TAG_TRACK2] = track2ForHost
            hashMap[EmvConstants.EMV_TAG_PAN] = plainPan
//            Log.d("ENCRYPTION", "✅ Plain Track2 (Conduent): $track2ForHost")
//            Log.d("ENCRYPTION", "✅ Plain PAN: $plainPan")

            // 3️⃣ Prepare PAN for encryption (pad to multiple of 8 for DES)
            var panForEncryption = plainPan
            if (panForEncryption.length % 8 != 0) {
                panForEncryption = panForEncryption.padEnd(
                    panForEncryption.length + (8 - panForEncryption.length % 8),
                    '0'
                )
            }

            // 4️⃣ Encrypt PAN using DUKPT
            try {
                val panCalcObj = DukptCalcObj(
                    EncryptionConstants.KEY_INDEX_DATA_KEY,
                    DukptCalcObj.DukptTypeEnum.DUKPT_DES_KEY_PIN,
                    DukptCalcObj.DukptOperEnum.DUKPT_ENCRYPT,
                    DukptCalcObj.DukptAlgEnum.DUKPT_ALG_ECB,
                    panForEncryption.toByteArray().toHexString()
                )

                deviceService?.pinPad?.dukptCalcDes(panCalcObj)?.let {
                    val encryptedPan = it.getString(DukptCalcObj.DUKPT_DATA)?.uppercase()
                    val ksn = it.getString(DukptCalcObj.DUKPT_KSN)?.uppercase()

                    if (!encryptedPan.isNullOrEmpty()) {
                        hashMap[EmvConstants.EMV_TAG_ENC_PAN] = encryptedPan
                        //Log.d("ENCRYPTION", "✅ Encrypted PAN: $encryptedPan")
                    }

                    if (!ksn.isNullOrEmpty()) {
                        hashMap[EmvConstants.EMV_TAG_ENC_KSN] = ksn
                        //Log.d("ENCRYPTION", "🔑 KSN: $ksn")
                    }
                }
            } catch (e: RemoteException) {
                Log.e("ENCRYPTION", "❌ Exception encrypting PAN", e)
            }

            // 5️⃣ Set PIN block if available
            pinBlock?.let {
                hashMap[EmvConstants.EMV_TAG_ENC_PIN_BLOCK] = it
            }

            return hashMap
        }

        fun ysdkToEmvTransResult(result : Int): TransStatus? {
            return when (result) {
                EmvConstants.MF_EMV_RET_SUCCESS -> TransStatus.APPROVED_ONLINE
                EmvConstants.MF_EMV_RET_DECLINED -> TransStatus.DECLINED_ONLINE
                EmvConstants.MF_EMV_RET_CANCELLED -> TransStatus.CANCELED
                EmvConstants.MF_EMV_RET_TERMINATED -> TransStatus.TERMINATED
                EmvConstants.MF_EMV_RET_APP_BLOCKED -> TransStatus.APP_BLOCKED
                EmvConstants.MF_EMV_RET_FALLBACK -> TransStatus.TRY_ANOTHER_INTERFACE
                EmvConstants.MF_EMV_RET_OTHER_ERROR -> TransStatus.ERROR
                else -> TransStatus.ERROR
            }
        }

        fun ysdkToCheckCardStatus(checkCardResult : Int): EmvSdkResult.CardCheckStatus? {
            return when (checkCardResult) {
                    1 -> EmvSdkResult.CardCheckStatus.CARD_INSERTED
                    7 -> EmvSdkResult.CardCheckStatus.CARD_TAPPED
//                ContantPara.CheckCardResult.MSR -> EmvSdkResult.CardCheckStatus.CARD_SWIPED
//                ContantPara.CheckCardResult.NOT_ICC -> EmvSdkResult.CardCheckStatus.NOT_ICC_CARD
//                ContantPara.CheckCardResult.USE_ICC_CARD -> EmvSdkResult.CardCheckStatus.USE_ICC_CARD
//                ContantPara.CheckCardResult.BAD_SWIPE -> EmvSdkResult.CardCheckStatus.BAD_SWIPE
//                ContantPara.CheckCardResult.NEED_FALLBACK -> EmvSdkResult.CardCheckStatus.NEED_FALLBACK
//                ContantPara.CheckCardResult.MULT_CARD -> EmvSdkResult.CardCheckStatus.MULTIPLE_CARDS
//                ContantPara.CheckCardResult.TIMEOUT -> EmvSdkResult.CardCheckStatus.TIMEOUT
//                ContantPara.CheckCardResult.CANCEL -> EmvSdkResult.CardCheckStatus.CANCEL
//                ContantPara.CheckCardResult.DEVICE_BUSY -> EmvSdkResult.CardCheckStatus.DEVICE_BUSY
//                ContantPara.CheckCardResult.NO_CARD -> EmvSdkResult.CardCheckStatus.NO_CARD_DETECTED
                else -> EmvSdkResult.CardCheckStatus.ERROR

            }
        }
    }




    private fun bcdToBinaryArray(bcdArray: ByteArray): ByteArray {
        val decimalStr = buildString {
            for (bcd in bcdArray) {
                val high = (bcd.toInt() and 0xF0) ushr 4
                val low = bcd.toInt() and 0x0F
                if (high > 9 || low > 9) {
                    throw IllegalArgumentException(
                        "Invalid BCD byte: 0x${
                            bcd.toString(16).padStart(2, '0')
                        }"
                    )
                }
                append(high)
                append(low)
            }
        }

        // Convert the decimal string to a BigInteger, then to a byte array
        val bigInt = decimalStr.toBigInteger()
        val fullByteArray = bigInt.toByteArray()

        // Ensure it's unsigned: remove leading 0x00 if present for positive values
        return if (fullByteArray[0] == 0x00.toByte()) {
            fullByteArray.drop(1).toByteArray()
        } else {
            fullByteArray
        }
    }
}