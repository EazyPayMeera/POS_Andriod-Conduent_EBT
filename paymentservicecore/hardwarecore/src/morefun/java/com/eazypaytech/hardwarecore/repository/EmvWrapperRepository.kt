package com.eazypaytech.tpaymentcore.repository

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.device.SEManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.IInputActionListener
import android.os.RemoteException
import android.text.TextUtils
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
import com.morefun.yapi.emv.EmvAidPara
import com.morefun.yapi.emv.EmvCapk
import com.morefun.yapi.emv.EmvDataSource
import com.morefun.yapi.emv.EmvOnlineResult
import com.morefun.yapi.emv.EmvTermCfgConstrants
import com.morefun.yapi.emv.EmvTransDataConstrants
import com.morefun.yapi.emv.OnEmvProcessListener
import com.morefun.yapi.engine.DeviceInfoConstrants
import com.morefun.yapi.engine.DeviceServiceEngine
import com.urovo.i9000s.api.emv.ContantPara
import com.urovo.i9000s.api.emv.EmvNfcKernelApi
import com.urovo.i9000s.api.emv.Funs
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
import kotlin.collections.contentToString
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
    val arqcTLVTags: Array<String> = arrayOf(
        "9F26",
        "9F27",
        "9F10",
        "9F37",
        "9F36",
        "95",
        "9A",
        "9C",
        "9F02",
        "5F2A",
        "82",
        "9F1A",
        "9F33",
        "9F34",
        "9F35",
        "9F1E",
        "84",
        "9F09",
        "9F63",
        "50",
        "9F12",
        "5F34"
    )

    init {
        CoroutineScope(Dispatchers.Default).launch {
            bindService(context)
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
                 amount = _sAmount) { pinBlock ->
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
            }/*else {
                iEmvSdkResponseListener?.onEmvSdkResponse(
                    EmvSdkResult.CardCheckResult(
                        status = EmvSdkResult.CardCheckStatus.ERROR
                    )
                )
            }*/

                /*            iEmvSdkResponseListener?.onEmvSdkResponse(
                                EmvSdkResult.CardCheckResult(
                                    status = urovoToCheckCardStatus(
                                        p0
                                    )
                                )
                            )

                            *//* Process MSR from here only *//*
            p0.takeIf { it == ContantPara.CheckCardResult.MSR && p1?.containsKey(EmvConstants.UROVO_SDK_KEY_MSR_DATA) == true }
                ?.let {
                    var msrTlv = TlvUtils(p1?.get(EmvConstants.UROVO_SDK_KEY_MSR_DATA) ?: "")
                    var nfcTlv = TlvUtils()
                    checkCardResult = ContantPara.CheckCardResult.MSR

                    msrTlv.tlvMap.containsKey(EmvConstants.UROVO_SDK_KEY_MSR_TRACK2)
                        .takeIf { it == true }?.let {
                            msrTlv.tlvMap[EmvConstants.UROVO_SDK_KEY_MSR_TRACK2]?.let {
                                var trackData =
                                    it.hexToByteArray().decodeToString().replace('=', 'D')
                                nfcTlv.addTagValHex(
                                    EmvConstants.EMV_TAG_TRACK2,
                                    trackData,
                                    0,
                                    trackData.length
                                )
                            }
                        }
                    encryptThenRequestOnline(nfcTlv.toTlvString())
                }
        }*/
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
        amount: String,
        onResult: (pinBlock: ByteArray?) -> Unit
    ) {
        val panBlock = pan?.toByteArray() ?: ByteArray(16)
        val bundle = Bundle().apply {
            putBoolean(PinPadConstrants.COMMON_IS_RANDOM, true)
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

        try {
            deviceService?.pinPad?.apply {
                setTimeOut(30)
                setSupportPinLen(intArrayOf(0, 6))
                inputOnlinePin(bundle, panBlock, EncryptionConstants.KEY_INDEX_PIN_KEY.ordinal, PinAlgorithmMode.ISO9564FMT1,
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
                                append("PIN BLOCK: ${Funs.bytesToHexString(pinBlock)}\n")
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

    private fun inputOfflinePin(pan: String?, onResult: (pinBlock: ByteArray?) -> Unit) {
        //Log.d("PIN", "INPUT OFFLINE PIN: $pan")

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
                            append("PIN BLOCK: ${Funs.bytesToHexString(pinBlock)}\n")
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

    fun <T> createArrayList(vararg elements: T): java.util.ArrayList<T> {
        val list = java.util.ArrayList<T>()
        for (element in elements) {
            list.add(element)
        }
        return list
    }

    fun getCurrentTime(format: String?): String {
        val df = SimpleDateFormat(format)
        val curDate = Date(System.currentTimeMillis())
        return df.format(curDate)
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

    fun getTransBundle(amount: String?): Bundle {
        val bundle = Bundle()

        val date: String = getCurrentTime("yyMMddHHmmss")

        bundle.putBoolean(EmvTransDataConstrants.FORCE_ONLINE_CALL_PIN, true)
        bundle.putBoolean(EmvTransDataConstrants.EMV_TRANS_ENABLE_CONTACTLESS, true)
        bundle.putBoolean(EmvTransDataConstrants.EMV_TRANS_ENABLE_CONTACT, true)

        bundle.putBoolean(EmvTransDataConstrants.SELECT_APP_RETURN_AID, false)
        bundle.putBoolean(EmvTransDataConstrants.SELECT_APP_RETURN_PRIORITY, true)

        bundle.putInt(EmvTransDataConstrants.CHECK_CARD_TIME_OUT, 30)
        bundle.putInt(EmvTransDataConstrants.ISQPBOCFORCEONLINE, 1)

        bundle.putByte(EmvTransDataConstrants.B9C, 0x00.toByte())


        //        bundle.putString(EmvTransDataConstrants.SEQNO, "0001");
        bundle.putString(EmvTransDataConstrants.TRANSAMT, amount)
        bundle.putString(EmvTransDataConstrants.MERNAME, "MOREFUN")
        bundle.putString(EmvTransDataConstrants.MERID, "488923")
        bundle.putString(EmvTransDataConstrants.TERMID, "500")

        bundle.putStringArrayList(
            EmvTransDataConstrants.TERMINAL_TLVS,
            createArrayList("DF840B06000000000001", "DF81190118")
        )

        return bundle
    }

    fun startPayment(
        context: Context,
        transConfig: TransConfig?,
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
                //it.currencyCode?.let { data["currencyCode"] = it.takeLast(3) }
                it.transactionType?.let {
                    bundle.putByte(EmvTransDataConstrants.B9C, it.take(2).toByte())
                }    //00-goods 01-cash 09-cashback 20-refund
                (it.cardCheckMode ?: CardCheckMode.SWIPE_OR_INSERT_OR_TAP).let {
                    //data["checkCardMode"] = it.sdkValue
                    if(it in listOf(CardCheckMode.INSERT, CardCheckMode.SWIPE_OR_INSERT, CardCheckMode.INSERT_OR_TAP, CardCheckMode.SWIPE_OR_INSERT_OR_TAP))
                        bundle.putBoolean(EmvTransDataConstrants.EMV_TRANS_ENABLE_CONTACT, true)
                    else
                        bundle.putBoolean(EmvTransDataConstrants.EMV_TRANS_ENABLE_CONTACT, false)

                    if(it in listOf(CardCheckMode.TAP, CardCheckMode.INSERT_OR_TAP, CardCheckMode.SWIPE_OR_INSERT_OR_TAP, CardCheckMode.SWIPE_OR_TAP))
                        bundle.putBoolean(EmvTransDataConstrants.EMV_TRANS_ENABLE_CONTACTLESS, true)
                    else
                        bundle.putBoolean(EmvTransDataConstrants.EMV_TRANS_ENABLE_CONTACTLESS, false)

                    if(it in listOf(CardCheckMode.SWIPE, CardCheckMode.SWIPE_OR_INSERT, CardCheckMode.SWIPE_OR_INSERT_OR_TAP, CardCheckMode.SWIPE_OR_TAP))
                        bundle.putBoolean(EmvTransDataConstrants.SUPPORT_MAG_CARD, true)
                    else
                        bundle.putBoolean(EmvTransDataConstrants.SUPPORT_MAG_CARD, false)
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
                //data["isEnterAmtAfterReadRecord"] = false
                /*bundle.putStringArrayList(
                    EmvTransDataConstrants.TERMINAL_TLVS,
                    createArrayList("DF840B06000000000001", "DF81190118")
                )*/
            }
            initEncryption()
            //EmvNfcKernelApi.getInstance().setContext(context)
            //EmvNfcKernelApi.getInstance().setListener(this)

            job = CoroutineScope(Dispatchers.Default).launch {
                getDeviceService(context)?.emvHandler?.emvTrans(bundle, emvListener)
                //bindDeviceService(context)                //EmvNfcKernelApi.getInstance().startKernel(data)
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
            //EmvNfcKernelApi.getInstance().updateCAPK(ContantPara.Operation.CLEAR, null)
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
        val pan = getPbocData("5A")
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
                    if (isHex==true) it.toHexString() else it.decodeToString()
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
        private var checkCardResult: ContantPara.CheckCardResult? = null
        private var deviceService: DeviceServiceEngine? = null
        private var serviceConnected = CompletableDeferred<Boolean>()
        private var TAG = "MOREFUN"

        private suspend fun bindService(context: Context?=null, recreate : Boolean? = false) {
            try {
                //Log.d(TAG, "Binding Service with context $context")
                deviceService?.emvHandler?.endPBOC()
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
                        ContantPara.CheckCardResult.MSR -> {
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
            var hashMap = HashMap<String, String>()
            var trackData = (getEmvTag(EmvConstants.EMV_TAG_TRACK2)?:"").uppercase()
            trackData.takeIf { it.isEmpty() == true && tlvMap.containsKey(EmvConstants.EMV_TAG_TRACK2) }
                ?.let {
                    trackData = tlvMap[EmvConstants.EMV_TAG_TRACK2] ?: ""
                }
            trackData = trackData.replace('D', '=').removeSuffix("F")
            var cardPan = trackData.substringBefore('=')

            /* Clear PAN is OK to send to service layer. Service layer will filter it */
            cardPan.let {
                hashMap[EmvConstants.EMV_TAG_PAN] = it
            }

            trackData.takeIf {
                (it.length % 8) != 0
            }?.let {
                trackData = it.padStart(it.length + (8 - it.length % 8), '0')
            }

            cardPan.takeIf {
                (it.length % 8) != 0
            }?.let {
                cardPan = it.padStart(it.length + (8 - it.length % 8), '0')
            }

            var trackDataBytes = trackData.toByteArray()
            var cardPanBytes = cardPan.toByteArray()

            /* Encrypt Track2 Data */
            try {
                val trackCalcObj = DukptCalcObj(
                    EncryptionConstants.KEY_INDEX_DATA_KEY,
                    DukptCalcObj.DukptTypeEnum.DUKPT_DES_KEY_DATA1,
                    DukptCalcObj.DukptOperEnum.DUKPT_ENCRYPT,
                    DukptCalcObj.DukptAlgEnum.DUKPT_ALG_ECB,
                    trackDataBytes.toHexString()
                )
                Log.d("ENCRYPTION", "🚀 Encrypting Track2: $trackCalcObj")
                deviceService?.pinPad?.dukptCalcDes(trackCalcObj)?.let {
                    val encryptedTrack = it.getString(DukptCalcObj.DUKPT_DATA)?.uppercase()
                    val ksn = it.getString(DukptCalcObj.DUKPT_KSN)?.uppercase()
                    if (!encryptedTrack.isNullOrEmpty()) {
                        hashMap[EmvConstants.EMV_TAG_ENC_TRACK] = encryptedTrack
                        Log.d("ENCRYPTION", "✅ ENCRYPTED TRACK DATA (LYRA): $encryptedTrack")
                    } else {
                        Log.w("ENCRYPTION", "⚠️ Encrypted track data is null or empty.")
                    }
                    if (!ksn.isNullOrEmpty()) {
                        hashMap[EmvConstants.EMV_TAG_ENC_KSN] = ksn
                        Log.d("ENCRYPTION", "🔑 KSN TRACK DATA (LYRA): $ksn")
                    } else {
                        Log.w("ENCRYPTION", "⚠️ KSN is null or empty.")
                    }
                }
            } catch (e: RemoteException) {
                Log.e("ENCRYPTION", "❌ Exception encrypting Track2", e)
            }

            /* Encrypt PAN */
            try {
                val panCalcObj = DukptCalcObj(EncryptionConstants.KEY_INDEX_DATA_KEY, DukptCalcObj.DukptTypeEnum.DUKPT_DES_KEY_DATA1, DukptCalcObj.DukptOperEnum.DUKPT_ENCRYPT, DukptCalcObj.DukptAlgEnum.DUKPT_ALG_ECB, cardPanBytes.toHexString())
                    deviceService?.pinPad?.dukptCalcDes(panCalcObj)?.let {
                        val encryptedPan = it.getString(DukptCalcObj.DUKPT_DATA)?.uppercase()
                        val ksn = it.getString(DukptCalcObj.DUKPT_KSN)?.uppercase()
                        if (!encryptedPan.isNullOrEmpty()) {
                            hashMap[EmvConstants.EMV_TAG_ENC_PAN] = encryptedPan
                            Log.d("ENCRYPTION", "✅ ENCRYPTED PAN (LYRA): $encryptedPan")
                        } else {
                            Log.w("ENCRYPTION", "⚠️ Encrypted PAN is null or empty.")
                        }

                        if (!ksn.isNullOrEmpty()) {
                            hashMap[EmvConstants.EMV_TAG_ENC_KSN] = ksn
                            Log.d("ENCRYPTION", "🔑 KSN TRACK DATA (LYRA): $ksn")
                        } else {
                            Log.w("ENCRYPTION", "⚠️ KSN is null or empty.")
                        }
                    }
            } catch (e: RemoteException) {
                Log.e("ENCRYPTION", "❌ Exception encrypting PAN", e)
            }

            /* Set Pin Block */
            pinBlock?.let {
                hashMap[EmvConstants.EMV_TAG_ENC_PIN_BLOCK] = it
            }

            return hashMap
        }

        fun onRequestSetAmount() {
            Log.d("EMV_APP", "Request Amount:$_amount")
            EmvNfcKernelApi.getInstance().setAmountEx(_amount, _cashbackAmount)
        }

        @OptIn(ExperimentalStdlibApi::class)
        fun onReturnCheckCardResult(
            p0: ContantPara.CheckCardResult?,
            p1: Hashtable<String, String>?
        ) {
            Log.d("EMV_APP", "Check Card Result:" + p0?.toString())
            Log.d("EMV_APP", "Check Card List:" + p1?.toString())

            iEmvSdkResponseListener?.onEmvSdkResponse(
                EmvSdkResult.CardCheckResult(
                    status = urovoToCheckCardStatus(
                        p0
                    )
                )
            )

            /* Process MSR from here only */
            p0.takeIf { it == ContantPara.CheckCardResult.MSR && p1?.containsKey(EmvConstants.UROVO_SDK_KEY_MSR_DATA) == true }
                ?.let {
                    var msrTlv = TlvUtils(p1?.get(EmvConstants.UROVO_SDK_KEY_MSR_DATA) ?: "")
                    var nfcTlv = TlvUtils()
                    checkCardResult = ContantPara.CheckCardResult.MSR

                    msrTlv.tlvMap.containsKey(EmvConstants.UROVO_SDK_KEY_MSR_TRACK2)
                        .takeIf { it == true }?.let {
                            msrTlv.tlvMap[EmvConstants.UROVO_SDK_KEY_MSR_TRACK2]?.let {
                                var trackData =
                                    it.hexToByteArray().decodeToString().replace('=', 'D')
                                nfcTlv.addTagValHex(
                                    EmvConstants.EMV_TAG_TRACK2,
                                    trackData,
                                    0,
                                    trackData.length
                                )
                            }
                        }
                    encryptThenRequestOnline(nfcTlv.toTlvString())
                }
        }

        fun onRequestSelectApplication(p0: ArrayList<String>?) {
            Log.d("EMV_APP", "Select Applications:" + p0.toString())
        }

        fun onRequestPinEntry(p0: ContantPara.PinEntrySource?) {
            Log.d("EMV_APP", "Online PIN Prompt:" + p0.toString())
            if (p0 == ContantPara.PinEntrySource.KEYPAD) {
                emv_proc_onlinePin(true)
                Log.i("EMV_APP", "MainActivity  emv_proc_onlinePin over")
            }
        }

        fun onRequestOfflinePinEntry(p0: ContantPara.PinEntrySource?, p1: Int) {
            Log.d("EMV_APP", "Offline PIN Prompt:" + p0.toString())
        }

        fun onRequestConfirmCardno() {
            EmvNfcKernelApi.getInstance().sendConfirmCardnoResult(true)
        }

        fun onRequestFinalConfirm() {
            EmvNfcKernelApi.getInstance().sendFinalConfirmResult(true)
        }

        fun onRequestOnlineProcess(p0: String?, p1: String?) {
            Log.d("EMV_APP", "Process Online:" + p0?.toString() + "\n" + p1?.toString())
            encryptThenRequestOnline(p0, p1)
        }

        fun onReturnBatchData(p0: String?) {
            Log.d("EMV_APP", "Batch Data:" + p0.toString())
        }

        fun onReturnTransactionResult(p0: ContantPara.TransactionResult?) {
            Log.d("EMV_APP", "Transaction Result:" + p0.toString())
            Log.d("EMV_APP", "TLV Data:" + EmvNfcKernelApi.getInstance().GetField55ForSAMA())
            iEmvSdkResponseListener?.onEmvSdkResponse(
                EmvSdkResult.TransResult(
                    urovoToEmvTransResult(
                        p0
                    )
                )
            )
        }

        fun onRequestDisplayText(p0: ContantPara.DisplayText?) {
            Log.d("EMV_APP", "***** DISPLAY *****\n" + p0.toString() + "*******************")
        }

        fun onRequestOfflinePINVerify(
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

        fun onReturnIssuerScriptResult(
            p0: ContantPara.IssuerScriptResult?,
            p1: String?
        ) {
            Log.d("EMV_APP", "Issuer Script Result:" + p0.toString())
        }

        fun onNFCrequestTipsConfirm(p0: ContantPara.NfcTipMessageID?, p1: String?) {
            Log.d("EMV_APP", "NFC Request Tip:" + p0.toString())
            nfcDisplayMsgId = urovoToDisplayMsgId(p0)
            iEmvSdkResponseListener?.onEmvSdkDisplayMessage(
                nfcDisplayMsgId ?: DisplayMsgId.NONE
            )
        }

        fun onReturnNfcCardData(p0: Hashtable<String, String>?) {
            try {
                p0?.containsKey(EmvConstants.UROVO_SDK_KEY_EMV_DATA)?.takeIf { it == true }
                    ?.let {
                        nfcTlv = TlvUtils(p0[EmvConstants.UROVO_SDK_KEY_EMV_DATA]).toTlvString()
                    }
                Log.d("EMV_APP", "NFC Card Data:" + p0?.toString())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun onNFCrequestOnline() {
            try {
                Log.d("EMV_APP", "NFC Process Online:" + nfcTlv)
                encryptThenRequestOnline(nfcTlv)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun onNFCrequestImportPin(p0: Int, p1: Int, p2: String?) {
            Log.d("EMV_APP", "NFC Import PIN:" + p2.toString())
            EmvNfcKernelApi.getInstance().sendPinEntry()
        }

        fun onNFCTransResult(p0: ContantPara.NfcTransResult?) {
            Log.d("EMV_APP", "NFC Result :" + p0?.toString())
            iEmvSdkResponseListener?.onEmvSdkResponse(
                EmvSdkResult.TransResult(
                    urovoToEmvTransResult(
                        p0
                    ), nfcDisplayMsgId
                )
            )
        }

        fun onNFCErrorInfor(p0: ContantPara.NfcErrMessageID?, p1: String?) {
            Log.d("EMV_APP", "NFC Trans Error:" + p0?.toString())
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

//                ContantPara.TransactionResult.OFFLINE_APPROVAL -> TransStatus.APPROVED_OFFLINE
//                ContantPara.TransactionResult.ONLINE_DECLINED -> TransStatus.DECLINED_ONLINE
//                ContantPara.TransactionResult.OFFLINE_DECLINED -> TransStatus.DECLINED_OFFLINE
//                ContantPara.TransactionResult.CANCELED -> TransStatus.CANCELED
//                ContantPara.TransactionResult.CANCELED_OR_TIMEOUT -> TransStatus.CANCELED
//                ContantPara.TransactionResult.TERMINATED -> TransStatus.TERMINATED
//                ContantPara.TransactionResult.CARD_BLOCKED_APP_FAIL -> TransStatus.CARD_BLOCKED
//                ContantPara.TransactionResult.APPLICATION_BLOCKED_APP_FAIL -> TransStatus.APP_BLOCKED
//                ContantPara.TransactionResult.NO_EMV_APPS -> TransStatus.NO_EMV_APPS
//                ContantPara.TransactionResult.SELECT_APP_FAIL -> TransStatus.APP_SELECTION_FAILED
//                ContantPara.TransactionResult.INVALID_ICC_DATA -> TransStatus.INVALID_ICC_CARD
//                ContantPara.TransactionResult.ICC_CARD_REMOVED -> TransStatus.CARD_REMOVED

                else -> TransStatus.ERROR
            }
        }

        fun urovoToEmvTransResult(transactionResult: ContantPara.TransactionResult?): TransStatus? {
            return when (transactionResult) {
                ContantPara.TransactionResult.ONLINE_APPROVAL -> TransStatus.APPROVED_ONLINE
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

        fun urovoToEmvTransResult(transactionResult: ContantPara.NfcTransResult?): TransStatus? {
            return when (transactionResult) {
                ContantPara.NfcTransResult.ONLINE_APPROVAL -> TransStatus.APPROVED_ONLINE
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

        fun urovoToCheckCardStatus(checkCardResult: ContantPara.CheckCardResult?): EmvSdkResult.CardCheckStatus? {
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

        fun urovoToDisplayMsgId(displayMsgId: ContantPara.NfcTipMessageID?): DisplayMsgId? {
            return when (displayMsgId) {
                ContantPara.NfcTipMessageID.USE_MAG_STRIPE -> DisplayMsgId.USE_MAG_STRIPE
                ContantPara.NfcTipMessageID.DISPLAY_BALANCE -> DisplayMsgId.DISPLAY_BALANCE
                ContantPara.NfcTipMessageID.END_APPLICATION -> DisplayMsgId.END_APPLICATION
                ContantPara.NfcTipMessageID.INSERT_SWIPE_OR_TRY_ANOTHER_CARD -> DisplayMsgId.INSERT_SWIPE_OR_TRY_ANOTHER_CARD
                ContantPara.NfcTipMessageID.PLS_REMOVE_CARD -> DisplayMsgId.REMOVE_CARD
                ContantPara.NfcTipMessageID.PLS_USE_CONTACT_IC_CARD -> DisplayMsgId.USE_CONTACT_IC_CARD
                ContantPara.NfcTipMessageID.SEE_PHONE_REMOVE_AND_PRESENT_CARD -> DisplayMsgId.SEE_PHONE_AND_PRESENT_CARD_AGAIN
                ContantPara.NfcTipMessageID.NEED_SIGNATURE -> DisplayMsgId.NEED_SIGNATURE
                ContantPara.NfcTipMessageID.CARD_READ_OK -> DisplayMsgId.CARD_READ_OK
                ContantPara.NfcTipMessageID.APPLICATION_BLOCKED -> DisplayMsgId.APP_BLOCKED
                ContantPara.NfcTipMessageID.TERMINATE -> DisplayMsgId.TERMINATED
                ContantPara.NfcTipMessageID.TRY_AGAIN_RESENT_CARD, ContantPara.NfcTipMessageID.PLS_SECOND_TAP_CARD -> DisplayMsgId.TAP_CARD_AGAIN
                ContantPara.NfcTipMessageID.CARD_ERROR -> DisplayMsgId.ERR_CARD_READ
                ContantPara.NfcTipMessageID.PROCESSING_ERROR, ContantPara.NfcTipMessageID.UNKNOW -> DisplayMsgId.ERR_PROCESSING

                else -> DisplayMsgId.NONE
            }
        }

        fun getSdkCardType(aid: String): String {
            return when (aid.substring(0, 10)) {
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
            var cardno: String? = null

            getEmvTag(EmvConstants.EMV_TAG_PAN)?.let {
                cardno = it
                Log.i("EMV_APP", "emv_proc_onlinePin cardno $cardno")
            }

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

//            if (isDUKPT) {
//                PinPadProviderImpl.getInstance().GetDukptPinBlock(param, this)
//            } else
//                PinPadProviderImpl.getInstance().getPinBlockEx(param, this)
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
                else paramVar.putString(
                    "message",
                    "Please input PIN \nWrong PIN \nLast Pin Try"
                )
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

                Log.d(
                    "applog",
                    "ModuleLen = " + publen!![0] + ": " + Funs.bytesToHexString(pub)
                )
                Log.d(
                    "applog",
                    "ExponentLen = " + explen!![0] + ": " + Funs.bytesToHexString(exp)
                )


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
                                EmvNfcKernelApi.getInstance()
                                    .sendOfflinePINVerifyResult(1) //bypass
                            } else {
                                EmvNfcKernelApi.getInstance()
                                    .sendOfflinePINVerifyResult(-198) //return code error
                            }
                        } else if (type == 3) //Offline plaintext
                        {
                            Log.d("applog", "proc_offlinePin Plaintext offline")
                            if (result == 0) {
                                EmvNfcKernelApi.getInstance()
                                    .sendOfflinePINVerifyResult(0) //Offline plaintext verify successfully
                            } else { //Incorrect PIN, try again
                                val arg1Str = result.toString() + ""
                                if (arg1Str.length >= 4 && "71" == arg1Str.subSequence(0, 2)) {
                                    if ("7101" == arg1Str) {
                                        EmvNfcKernelApi.getInstance()
                                            .sendOfflinePINVerifyResult(-192) //PIN BLOCKED
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
                                    EmvNfcKernelApi.getInstance()
                                        .sendOfflinePINVerifyResult(-192) //PIN BLOCKED
                                } else if ("7072" == arg1Str || "7073" == arg1Str) {
                                    EmvNfcKernelApi.getInstance()
                                        .sendOfflinePINVerifyResult(-202) //IC command failed
                                } else {
                                    EmvNfcKernelApi.getInstance()
                                        .sendOfflinePINVerifyResult(-198) //Return code error
                                }
                            }
                        } else if (type == 4) //Offline encryption PIN
                        {
                            Log.d("applog", "proc_offlinePin Offline encryption")
                            if (result == 0) {
                                EmvNfcKernelApi.getInstance()
                                    .sendOfflinePINVerifyResult(0) //Offline encryption PIN verify successfully
                            } else {
                                val arg1Str = result.toString() + ""
                                if (arg1Str.length >= 4 && "71" == arg1Str.subSequence(0, 2)) {
                                    if ("7101" == arg1Str) {
                                        EmvNfcKernelApi.getInstance()
                                            .sendOfflinePINVerifyResult(-192) //PIN BLOCKED
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
                                    EmvNfcKernelApi.getInstance()
                                        .sendOfflinePINVerifyResult(-192) //PIN BLOCKED
                                } else if ("7072" == arg1Str || "7073" == arg1Str) {
                                    EmvNfcKernelApi.getInstance()
                                        .sendOfflinePINVerifyResult(-202) //IC command failed(card removed)
                                } else {
                                    EmvNfcKernelApi.getInstance()
                                        .sendOfflinePINVerifyResult(-198) //Return code error
                                }
                            }
                        } else if (type == 0x10) // click Cancel button
                        {
                            EmvNfcKernelApi.getInstance()
                                .sendOfflinePINVerifyResult(-199) //cancel
                        } else if (type == 0x11) // pinpad timed out
                        {
                            EmvNfcKernelApi.getInstance()
                                .sendOfflinePINVerifyResult(-199) //timeout
                        } else {
                            EmvNfcKernelApi.getInstance()
                                .sendOfflinePINVerifyResult(-198) //Return code error
                        }
                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                        Log.d("applog", "proc_offlinePin exception")
                    }
                }
            })
            if (iret == -3 || iret == -4) EmvNfcKernelApi.getInstance()
                .sendOfflinePINVerifyResult(-198)
            return iret
        }

        fun onInput(p0: Int, p1: Int) {
            Log.d("EMV_LOG", "On Input: $p0, $p1")
        }

        fun onConfirm(p0: ByteArray?, p1: Boolean) {
            if (p1) {
                EmvNfcKernelApi.getInstance().bypassPinEntry() //bypass
            } else {
                Log.d("EMV_APP", "PinBlock:" + p0.contentToString())
                EmvNfcKernelApi.getInstance().sendPinEntry()
            }
        }

        @OptIn(ExperimentalStdlibApi::class)
        fun onConfirm_dukpt(p0: ByteArray?, p1: ByteArray?) {
            iEmvSdkResponseListener?.onEmvSdkDisplayMessage(DisplayMsgId.PROCESSING_ONLINE)
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

        fun onCancel() {
            EmvNfcKernelApi.getInstance().cancelPinEntry()
        }

        fun onTimeOut() {
            EmvNfcKernelApi.getInstance().cancelPinEntry()
        }

        fun onError(p0: Int) {
            EmvNfcKernelApi.getInstance().cancelPinEntry()
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