package com.eazypaytech.tpaymentcore.repository

import android.R
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
import com.morefun.yapi.device.pinpad.DispTextMode
import com.morefun.yapi.device.pinpad.OnPinPadInputListener
import com.morefun.yapi.device.pinpad.PinAlgorithmMode
import com.morefun.yapi.device.pinpad.PinPadConstrants
import com.morefun.yapi.emv.EmvAidPara
import com.morefun.yapi.emv.EmvTermCfgConstrants
import com.morefun.yapi.emv.EmvTransDataConstrants
import com.morefun.yapi.emv.OnEmvProcessListener
import com.morefun.yapi.engine.DeviceServiceEngine
import com.urovo.i9000s.api.emv.ContantPara
import com.urovo.i9000s.api.emv.EmvListener
import com.urovo.i9000s.api.emv.EmvNfcKernelApi
import com.urovo.i9000s.api.emv.Funs
import com.urovo.sdk.pinpad.PinPadProviderImpl
import com.urovo.sdk.pinpad.listener.PinInputListener
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.awaitAll
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
    IEmvWrapperRequestListener, ServiceConnection {
        private var TAG = "MOREFUN"
    private var deviceService: DeviceServiceEngine? = null
    private var serviceConnected = CompletableDeferred<Boolean>()

    init {
        bindService(context)
    }

    private fun bindService(context: Context)
    {
        val intent = Intent(EmvConstants.MF_SERVICE_ACTION).apply {
            setPackage(EmvConstants.MF_SERVICE_PACKAGE)
        }
        val connected = context.bindService(intent, this, Context.BIND_AUTO_CREATE)
        if (connected) {
            Log.d(TAG, "Service binding successful")
        } else {
            Log.d(TAG, "Service binding failed")
        }
    }

    override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
        deviceService = DeviceServiceEngine.Stub.asInterface(binder)
        deviceService?.emvHandler?.endPBOC()
        var bundle = Bundle()
        Log.d(TAG, "Service connected")
        if(deviceService?.login(bundle,"09100000") == 0) {
            serviceConnected.complete(true)
            Log.d(TAG, "Login Successful")
        }
        else
        {
            serviceConnected.complete(false)
            Log.d(TAG, "Login Failure")
        }

    }

    override fun onServiceDisconnected(name: ComponentName?) {
        deviceService = null
        serviceConnected.complete(false)
        Log.d(TAG, "Service Disconnected")
    }

    fun     getInitTermConfig(): Bundle {
        val bundle = Bundle()
        bundle.putByteArray(EmvTermCfgConstrants.TERMCAP, byteArrayOf(0xE0.toByte(), 0xE0.toByte(), 0xC8.toByte()))
        bundle.putByteArray(
            EmvTermCfgConstrants.ADDTERMCAP,
            byteArrayOf(0xF2.toByte(), 0x00.toByte(), 0xF0.toByte(), 0xA0.toByte(), 0x01.toByte())
        )
        bundle.putByteArray(
            EmvTermCfgConstrants.ADD_TERMCAP_EX,
            byteArrayOf(0xF2.toByte(), 0x00.toByte(), 0xF0.toByte(), 0xA0.toByte(), 0x01.toByte())
        )
        bundle.putByte(EmvTermCfgConstrants.TERMTYPE, 0x22.toByte())
        bundle.putByteArray(
            EmvTermCfgConstrants.COUNTRYCODE,
            byteArrayOf(0x03.toByte(), 0x56.toByte())
        )
        bundle.putByteArray(
            EmvTermCfgConstrants.CURRENCYCODE,
            byteArrayOf(0x03.toByte(), 0x56.toByte())
        )
        bundle.putByteArray(
            EmvTermCfgConstrants.TRANS_PROP_9F66,
            byteArrayOf(0x36, 0x00.toByte(), 0xc0.toByte(), 0x00.toByte())
        )

        //bundle.putByteArray(EmvTermCfgConstrants.PURE_ATOL, new byte[]{(byte) 0x01, (byte) 0x02});
        //bundle.putByte(EmvTermCfgConstrants.PURE_ATOL_LEN, (byte) 0x02);
        //bundle.putByteArray(EmvTermCfgConstrants.PURE_MTOL, new byte[]{(byte) 0x01, (byte) 0x02, (byte) 0x03});
        //bundle.putByte(EmvTermCfgConstrants.PURE_MTOL_LEN, (byte) 0x03);
        //bundle.putByteArray(EmvTermCfgConstrants.PURE_ATDOL, new byte[]{0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04});
        //bundle.putByte(EmvTermCfgConstrants.PURE_ATDOL_LEN, (byte) 0x04);
        //bundle.putByte(EmvTermCfgConstrants.PURE_POSIO, (byte) 0x00);
        //bundle.putByteArray(EmvTermCfgConstrants.PURE_CONTACTLESS_CAPABILITY, new byte[]{(byte) 0x03, (byte) 0x56});
        return bundle
    }
    @OptIn(ExperimentalStdlibApi::class)
    fun TerminalConfig(aidConfig: AidConfig?): Boolean {
        var result = true
        try {
            aidConfig?.merchantIdentifier?.let {
                result = deviceService?.emvHandler?.setTlv(EmvConstants.EMV_TAG_MERCH_ID.hexToByteArray(),aidConfig.merchantIdentifier?.toByteArray()) == 0
            }
            aidConfig?.terminalIdentifier?.let {
                result = result && deviceService?.emvHandler?.setTlv(
                    EmvConstants.EMV_TAG_TERM_ID.hexToByteArray(),
                    aidConfig.terminalIdentifier?.toByteArray()
                ) == 0
            }
                aidConfig?.merchantNameLocation?.let {
                    result = result && deviceService?.emvHandler?.setTlv(
                        EmvConstants.EMV_TAG_MERCH_NAME_LOC.hexToByteArray(),
                        aidConfig.merchantNameLocation?.toByteArray()
                    ) == 0
                }
            aidConfig?.merchantCategoryCode?.let {
                result = result && deviceService?.emvHandler?.setTlv(
                    EmvConstants.EMV_TAG_MERCH_CATEGORY_CODE.hexToByteArray(),
                    aidConfig.merchantCategoryCode?.hexToByteArray()
                ) == 0
            }
            aidConfig?.ifdSerialNumber?.let {
                result = result && deviceService?.emvHandler?.setTlv(
                    EmvConstants.EMV_TAG_IFD_SERIAL_NO.hexToByteArray(),
                    aidConfig.ifdSerialNumber?.hexToByteArray()
                ) == 0
            }
            aidConfig?.terminalCapabilities?.let {
                result = result && deviceService?.emvHandler?.setTlv(
                    EmvConstants.EMV_TAG_TERM_CAP.hexToByteArray(),
                    aidConfig.terminalCapabilities.hexToByteArray()
                ) == 0
            }
            aidConfig?.terminalCountryCode?.let {
                result = result && deviceService?.emvHandler?.setTlv(
                    EmvConstants.EMV_TAG_TERM_COUNTRY_CODE.hexToByteArray(),
                    aidConfig.terminalCountryCode.hexToByteArray()
                ) == 0
            }
            aidConfig?.terminalType?.let {
                result = result && deviceService?.emvHandler?.setTlv(
                    EmvConstants.EMV_TAG_TERM_TYPE.hexToByteArray(),
                    aidConfig.terminalType.hexToByteArray()
                ) == 0
            }


/*
            var termTlvParams = TlvUtils()
                .addTagValAscii(
                    EmvConstants.EMV_TAG_MERCH_ID,
                    aidConfig?.merchantIdentifier,
                    15,
                    15
                )
                .addTagValAscii(EmvConstants.EMV_TAG_TERM_ID, aidConfig?.terminalIdentifier, 8, 8)
                .addTagValAscii(
                    EmvConstants.EMV_TAG_MERCH_NAME_LOC,
                    aidConfig?.merchantNameLocation,
                    0,
                    40
                )
                .addTagValHex(
                    EmvConstants.EMV_TAG_MERCH_CATEGORY_CODE,
                    aidConfig?.merchantCategoryCode,
                    2,
                    2
                )
                .addTagValHex(EmvConstants.EMV_TAG_IFD_SERIAL_NO, aidConfig?.ifdSerialNumber, 8, 8)
                .addTagValHex(EmvConstants.EMV_TAG_TERM_CAP, aidConfig?.terminalCapabilities, 3, 3)
                .addTagValHex(
                    EmvConstants.EMV_TAG_ADDL_TERM_CAP,
                    aidConfig?.addlTerminalCapabilities,
                    5,
                    5
                )
                .addTagValHex(
                    EmvConstants.EMV_TAG_TERM_COUNTRY_CODE,
                    aidConfig?.terminalCountryCode,
                    2,
                    2
                )
                .addTagValHex(EmvConstants.EMV_TAG_TERM_TYPE, aidConfig?.terminalType, 1, 1)
                .addTagValHex(
                    EmvConstants.EMV_TAG_TRANS_CURRENCY_EXPONENT,
                    aidConfig?.currencyExponent,
                    1,
                    1
                )
                .addTagValBoolean(
                    EmvConstants.EMV_TAG_SUPPORT_RANDOM_TRANS,
                    aidConfig?.enableRandomTrans
                )
                .addTagValBoolean(
                    EmvConstants.EMV_TAG_SUPPORT_EXCEP_FILE_CHECK,
                    aidConfig?.supportExceptionFile
                )
                .addTagValBoolean(EmvConstants.EMV_TAG_SUPPORT_SM, aidConfig?.supportSM)
                .addTagValBoolean(
                    EmvConstants.EMV_TAG_SUPPORT_VELOCITY_CHECK,
                    aidConfig?.supportVelocityCheck
                )
                .toTlvString()
           result = EmvNfcKernelApi.getInstance().updateTerminalParamters(
                ContantPara.CardSlot.UNKNOWN,
                termTlvParams
            )*/


//            EmvNfcKernelApi.getInstance()
//                .LogOutEnable(if (aidConfig?.enableEmvLogs == true) EmvConstants.UROVO_SDK_EMV_LOG_ENABLE else EmvConstants.UROVO_SDK_EMV_LOG_DISABLE)

            //Log.d("EMV_APP", "Term Config : $termTlvParams")
        } catch (exception: Exception) {
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
                    result = result && initAidConfig(it) && TerminalConfig(it)
                }
                //capKeys?.let { result = result && initCAPKeys(it) }

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



    val emvListener  = object:  OnEmvProcessListener.Stub() {
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
            /* if (p0) {
             inputOnlinePin(
                 "476173200020023",
                 amount = "1545.54") { pinBlock ->
                 try {
                     deviceService?.emvHandler?.onSetCardHolderInputPin(pinBlock)
                 } catch (e: RemoteException) {
                     e.printStackTrace()
                 }
             }
         } else {
             inputOfflinePin("476173200020023") { pinBlock ->
                 try {
                     deviceService?.emvHandler?.onSetCardHolderInputPin(pinBlock)
                 } catch (e: RemoteException) {
                     e.printStackTrace()
                 }
             }
         }*/
        }

        override fun onPinPress(p0: Byte) {
            Log.d(TAG, "onPinPress ${p0}")
        }

        override fun onCertVerify(p0: String?, p1: String?) {
            Log.d(TAG, "onCertVerify ${p0} ${p1}")
        }

        override fun onOnlineProc(p0: Bundle?) {
            Log.d(TAG, "onOnlineProc ${p0}")
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

    private fun inputOnlinePin(
        pan: String,
        amount: String,
        onResult: (pinBlock: ByteArray?) -> Unit
    ) {
        val panBlock = pan.toByteArray()

        val bundle = Bundle().apply {
            putBoolean(PinPadConstrants.COMMON_IS_RANDOM, false)

            /*                if (HardwareUtils.getDeviceModel().contains("MF960") ||
                                HardwareUtils.getDeviceModel().contains("H9PRO")) {
                                putBoolean(PinPadConstrants.COMMON_IS_PHYSICAL_KEYBOARD, true)
                            }*/

            putString(
                PinPadConstrants.TITLE_HEAD_CONTENT,
                "Please input the online pin \nAmount: $amount"
            )
        }

        try {
            deviceService?.pinPad?.apply {
                setTimeOut(10)
                setSupportPinLen(intArrayOf(0, 6))
                inputOnlinePin(bundle, panBlock, 0, PinAlgorithmMode.ISO9564FMT1,
                    object : OnPinPadInputListener.Stub() {
                        override fun onInputResult(
                            ret: Int,
                            pinBlock: ByteArray?,
                            ksn: String?
                        ) {
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

    private fun inputOfflinePin(pan: String, onResult: (pinBlock: ByteArray?) -> Unit) {
        Log.d("PIN", "INPUT OFFLINE PIN: $pan")

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

    fun getTransBundle(amount: String?): Bundle {
        val bundle = Bundle()

        val date: String = getCurrentTime("yyMMddHHmmss")

        bundle.putBoolean(EmvTransDataConstrants.FORCE_ONLINE_CALL_PIN, true)
        bundle.putBoolean(EmvTransDataConstrants.EMV_TRANS_ENABLE_CONTACTLESS, true)
        bundle.putBoolean(EmvTransDataConstrants.EMV_TRANS_ENABLE_CONTACT, true)
        bundle.putBoolean(EmvTransDataConstrants.CONTACT_SERVICE_SWITCH, false)
        bundle.putBoolean(EmvTransDataConstrants.SELECT_APP_RETURN_AID, false)
        bundle.putBoolean(EmvTransDataConstrants.SELECT_APP_RETURN_PRIORITY, true)

        bundle.putInt(EmvTransDataConstrants.CHECK_CARD_TIME_OUT, 30)
        bundle.putInt(EmvTransDataConstrants.ISQPBOCFORCEONLINE, 1)

        bundle.putByte(EmvTransDataConstrants.B9C, 0x00.toByte())

        bundle.putString(EmvTransDataConstrants.TRANSDATE, date.substring(0, 6))
        bundle.putString(EmvTransDataConstrants.TRANSTIME, date.substring(6, 12))
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
            val data = Hashtable<String, Any>()
            transConfig?.let {
                it.amount?.let { data["amount"] = it; _amount = it.toLongOrNull() ?: 0L }
                it.cashbackAmount?.let {
                    data["cashbackAmount"] = it; _cashbackAmount = it.toLongOrNull() ?: 0L
                }
                it.currencyCode?.let { data["currencyCode"] = it.takeLast(3) }
                it.transactionType?.let {
                    data["transactionType"] = it.takeLast(2)
                }    //00-goods 01-cash 09-cashback 20-refund
                (it.cardCheckMode ?: CardCheckMode.SWIPE_OR_INSERT_OR_TAP).let {
                    data["checkCardMode"] = it.sdkValue
                }
                it.cardCheckTimeout?.let { data["checkCardTimeout"] = it }
                it.enableBeeper?.let { data["enableBeeper"] = it }
                it.supportFallback?.let {
                    if (it == true) data["FallbackSwitch"] =
                        "1" else data["FallbackSwitch"] =
                        "0"
                }
                it.supportDRL?.let { data["supportDRL"] = it }
                data["emvOption"] =
                    if (it.forceOnline == true) ContantPara.EmvOption.START_WITH_FORCE_ONLINE else ContantPara.EmvOption.START // START_WITH_FORCE_ONLINE
                data["isEnterAmtAfterReadRecord"] = false
            }
            //initEncryption()
            //EmvNfcKernelApi.getInstance().setContext(context)
            //EmvNfcKernelApi.getInstance().setListener(this)

            job = CoroutineScope(Dispatchers.Default).launch {
                serviceConnected.await()
                deviceService?.emvHandler?.emvTrans(getTransBundle("552.00"), emvListener)
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
    fun printAidInfo()
    {
        for (it in deviceService?.emvHandler?.getAidParaList() ?: listOf<EmvAidPara>()) {
            Log.d(TAG, "------------------ AID PARAM ------------------")
            Log.d(TAG, "AID: ${it.aid.copyOf(it.aiD_Length).toHexString()}") // AID trimmed to actual length
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
        printAidInfo()
        deviceService?.emvHandler?.clearAIDParam()

        aidConfig?.let {
            result = addContactAid(it) //&& addContactlessAid(it)
        }
        return result
    }

    fun addCapKey(key: CAPKey): Boolean {
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
        } catch (exception: Exception) {
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

    @OptIn(ExperimentalStdlibApi::class)
    fun addContactAid(config: AidConfig): Boolean {
        var result = true
        try {
            /* Add Contact Configuration */
            var aidParaList = listOf<EmvAidPara>()
            for (aid in config.contact?.aidList ?: emptyList()) {
                val aidPara = EmvAidPara()
                (aid.aid ?: config.contact?.aid ?: config.aid)?.let {
                    aidPara.aid = it.hexToByteArray()
                    aidPara.aiD_Length = it.hexToByteArray().size
                }
                (aid.appVersion ?: config.contact?.appVersion
                ?: config.appVersion)?.let {
                    aidPara.termAppVer = it.hexToByteArray().takeLast(aidPara.termAppVer.size).toByteArray()
                }
                (aid.terminalFloorLimit ?: config.contact?.terminalFloorLimit
                ?: config.terminalFloorLimit)?.let {
                    //aidPara.tfL_Domestic = it.hexToByteArray().takeLast(4).toByteArray()
                    //aidPara.tfL_International = it.hexToByteArray().takeLast(4).toByteArray()
                    //aidPara.eC_TFL = it.hexToByteArray().takeLast(4).toByteArray()
                }
                (aid.terminalFloorLimitCheck ?: config.contact?.terminalFloorLimitCheck
                ?: config.terminalFloorLimitCheck)?.let {
                }
                (aid.tacDefault ?: config.contact?.tacDefault
                ?: config.tacDefault)?.let {
                    aidPara.taC_Default = it.hexToByteArray()
                }
                (aid.tacDenial ?: config.contact?.tacDenial
                ?: config.tacDenial)?.let {
                    aidPara.taC_Denial = it.hexToByteArray()
                }
                (aid.tacOnline ?: config.contact?.tacOnline
                ?: config.tacOnline)?.let {
                    aidPara.taC_Online = it.hexToByteArray()
                }

                (aid.defaultDDOL ?: config.contact?.defaultDDOL
                ?: config.defaultDDOL)?.let {
                    //aidPara.ddol = it.hexToByteArray()
                    //aidPara.ddoL_Length = it.hexToByteArray().size.toByte()
                }
                (aid.defaultTDOL ?: config.contact?.defaultTDOL
                ?: config.defaultTDOL)?.let {
                    //aidPara.tdol = it.hexToByteArray()
                    //aidPara.tdoL_Length = it.hexToByteArray().size
                }
                (aid.acquirerId ?: config.contact?.acquirerId
                ?: config.acquirerId)?.let {
                    //aidPara.acquirerID = it.hexToByteArray()
                }
                (aid.threshold ?: config.contact?.threshold
                ?: config.threshold)?.let {
                    //aidPara.thresholdValueDomestic = it.hexToByteArray()
                    //aidPara.thresholdValueInt = it.hexToByteArray()
                }
                (aid.targetPercentage ?: config.contact?.targetPercentage
                ?: config.targetPercentage)?.let {
                    //aidPara.targetPercentageDomestic = it.hexToByte()
                    //aidPara.targetPercentageInt = it.hexToByte()
                }
                (aid.maxTargetPercentage ?: config.contact?.maxTargetPercentage
                ?: config.maxTargetPercentage)?.let {
                    //aidPara.maxTargetDomestic = it.hexToByte()
                    //aidPara.maxTargetPercentageInt = it.hexToByte()
                }
                (aid.appSelIndicator ?: config.contact?.appSelIndicator
                ?: config.appSelIndicator)?.let {
                    //aidPara.appSelIndicator = it.hexToByte()
                }
                (aid.terminalAppPriority ?: config.contact?.terminalAppPriority
                ?: config.terminalAppPriority)?.let {
                    //aidPara.terminalPriority = it.hexToByte()
                }
                (aid.terminalCapabilities ?: config.contact?.terminalCapabilities
                ?: config.terminalCapabilities)?.let {
                    aidPara.termCap = it.hexToByteArray()
                }
                (aid.terminalCountryCode ?: config.contact?.terminalCountryCode
                ?: config.terminalCountryCode)?.let {
                    aidPara.termCountryCode = it.hexToByteArray()
                }
                (aid.rdrCVMRequiredLimit ?: config.contact?.rdrCVMRequiredLimit
                ?: config.rdrCVMRequiredLimit)?.let {
                    //aidPara.rfcvmLimit = it.hexToByteArray()
                }
                (aid.rdrCtlsFloorLimit ?: config.contact?.rdrCtlsFloorLimit
                ?: config.rdrCtlsFloorLimit)?.let {
                    //aidPara.rfOfflineLimit = it.hexToByteArray()
                }
                (aid.rdrCtlsTransLimit ?: config.contact?.rdrCtlsTransLimit
                ?: config.rdrCtlsTransLimit)?.let {
                    //aidPara.rfTransLimit = it.hexToByteArray()
                }

                aidParaList = aidParaList.plus(aidPara)
            }

            result = result && deviceService?.emvHandler?.setAidParaList(aidParaList)==0
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
            /* Add Contactless Configuration */
            for (aid in config.contactless?.aidList ?: emptyList()) {
                val aidData = Hashtable<String, String>()

                (aid.aid ?: config.contactless?.aid
                ?: config.aid)?.let { aidData["ApplicationIdentifier"] = it }
                getSdkCardType(
                    aidData["ApplicationIdentifier"] ?: ""
                ).let { aidData["CardType"] = it }
                (aid.transactionType ?: config.contactless?.transactionType
                ?: config.transactionType)?.let { aidData["TransactionType"] = it }
                (aid.acquirerId ?: config.contactless?.acquirerId
                ?: config.acquirerId)?.let { aidData["AcquirerIdentifier"] = it }
                (aid.addlTerminalCapabilities
                    ?: config.contactless?.addlTerminalCapabilities
                    ?: config.addlTerminalCapabilities)?.let {
                    aidData["AdditionalTerminalCapabilities"] = it
                }
                (aid.appVersion ?: config.contactless?.appVersion
                ?: config.appVersion)?.let { aidData["ApplicationVersionNumber"] = it }
                (aid.cardDataInputCapability ?: config.contactless?.cardDataInputCapability
                ?: config.cardDataInputCapability)?.let {
                    aidData["CardDataInputCapability"] = it
                }
                (aid.kernelConfiguration ?: config.contactless?.kernelConfiguration
                ?: config.kernelConfiguration)?.let { aidData["KernelConfiguration"] = it }
                (aid.cvmCapabilityCVMRequired
                    ?: config.contactless?.cvmCapabilityCVMRequired
                    ?: config.cvmCapabilityCVMRequired)?.let {
                    aidData["CVMCapabilityPerCVMRequired"] = it
                }
                (aid.cvmCapabilityNoCVMRequired
                    ?: config.contactless?.cvmCapabilityNoCVMRequired
                    ?: config.cvmCapabilityNoCVMRequired)?.let {
                    aidData["CVMCapabilityNoCVMRequired"] = it
                }
                (aid.magCVMCapabilityCVMRequired
                    ?: config.contactless?.magCVMCapabilityCVMRequired
                    ?: config.magCVMCapabilityCVMRequired)?.let {
                    aidData["MagStripeCVMCapabilityCVMRequired"] = it
                }
                (aid.magCVMCapabilityNoCVMRequired
                    ?: config.contactless?.magCVMCapabilityNoCVMRequired
                    ?: config.magCVMCapabilityNoCVMRequired)?.let {
                    aidData["MagStripeCVMCapabilityPerNoCVMRequired"] = it
                }
                (aid.securityCapability ?: config.contactless?.securityCapability
                ?: config.securityCapability)?.let { aidData["SecurityCapability"] = it }
                (aid.ifdSerialNumber ?: config.contactless?.ifdSerialNumber
                ?: config.ifdSerialNumber)?.let {
                    aidData["IFDsn"] = it.toByteArray().toHexString()
                }
                (aid.merchantCategoryCode ?: config.contactless?.merchantCategoryCode
                ?: config.merchantCategoryCode)?.let {
                    aidData["MerchantCategoryCode"] = it
                }
                (aid.merchantIdentifier ?: config.contactless?.merchantIdentifier
                ?: config.merchantIdentifier)?.let {
                    aidData["MerchantIdentifier"] = it.toByteArray().toHexString()
                }
                (aid.merchantNameLocation ?: config.contactless?.merchantNameLocation
                ?: config.merchantNameLocation)?.let {
                    aidData["MerchantNameAndLocation"] = it.toByteArray().toHexString()
                }
                (aid.defaultUDOL ?: config.contactless?.defaultUDOL
                ?: config.defaultUDOL)?.let { aidData["DefaultUDOL"] = it }
                (aid.terminalFloorLimit ?: config.contactless?.terminalFloorLimit
                ?: config.terminalFloorLimit)?.let { aidData["FloorLimit"] = it }
                (aid.rdrCtlsFloorLimit ?: config.contactless?.rdrCtlsFloorLimit
                ?: config.rdrCtlsFloorLimit)?.let {
                    aidData["ReaderContactlessFloorLimit"] = it
                }
                (aid.rdrCtlsTransLimitNoODCVM
                    ?: config.contactless?.rdrCtlsTransLimitNoODCVM
                    ?: config.rdrCtlsTransLimitNoODCVM)?.let {
                    aidData["NoOnDeviceCVM"] = it
                }
                (aid.rdrCtlsTransLimitODCVM ?: config.contactless?.rdrCtlsTransLimitODCVM
                ?: config.rdrCtlsTransLimitODCVM)?.let { aidData["OnDeviceCVM"] = it }
                (aid.rdrCVMRequiredLimit ?: config.contactless?.rdrCVMRequiredLimit
                ?: config.rdrCVMRequiredLimit)?.let {
                    aidData["ReaderCVMRequiredLimit"] = it
                }
                (aid.tacDefault ?: config.contactless?.tacDefault
                ?: config.tacDefault)?.let { aidData["TerminalActionCodesDefault"] = it }
                (aid.tacDenial ?: config.contactless?.tacDenial
                ?: config.tacDenial)?.let { aidData["TerminalActionCodesDenial"] = it }
                (aid.tacOnline ?: config.contactless?.tacOnline
                ?: config.tacOnline)?.let { aidData["TerminalActionCodesOnLine"] = it }
                (aid.riskManagementData ?: config.contactless?.riskManagementData
                ?: config.riskManagementData)?.let {
                    aidData["TerminalRiskManagement"] = it
                }
                (aid.terminalCountryCode ?: config.contactless?.terminalCountryCode
                ?: config.terminalCountryCode)?.let { aidData["TerminalCountryCode"] = it }
                (aid.terminalType ?: config.contactless?.terminalType
                ?: config.terminalType)?.let { aidData["TerminalType"] = it }
                (aid.dsvnTerm ?: config.contactless?.dsvnTerm
                ?: config.dsvnTerm)?.let { aidData["DSVNTerm"] = it }
                (aid.appSelIndicator ?: config.contactless?.appSelIndicator
                ?: config.appSelIndicator)?.let { aidData["AppSelIndicator"] = it }
                (aid.defaultDDOL ?: config.contactless?.defaultDDOL
                ?: config.defaultDDOL)?.let { aidData["DefaultDDOL"] = it }
                (aid.defaultTDOL ?: config.contactless?.defaultTDOL
                ?: config.defaultTDOL)?.let { aidData["DefaultTDOL"] = it }

                /* Visa Specific */
                (aid.ttq ?: config.contactless?.ttq
                ?: config.ttq)?.let { aidData["TerminalTransactionQualifiers"] = it }
                (aid.rdrCVMRequiredLimit ?: config.contactless?.rdrCVMRequiredLimit
                ?: config.rdrCVMRequiredLimit)?.let { aidData["CvmRequiredLimit"] = it }
                (aid.rdrCtlsTransLimit ?: config.contactless?.rdrCtlsTransLimit
                ?: config.rdrCtlsTransLimit)?.let { aidData["TransactionLimit"] = it }
                (aid.disableProcRestrictions ?: config.contactless?.disableProcRestrictions
                ?: config.disableProcRestrictions)?.let {
                    aidData["ProRestrictionDisable"] = it
                }
                (aid.limitSwitch ?: config.contactless?.limitSwitch
                ?: config.limitSwitch)?.let { aidData["LimitSwitch"] = it }
                (aid.programID ?: config.contactless?.programID
                ?: config.programID)?.let { aidData["ProgramID"] = it }
                (aid.terminalCapabilities ?: config.contactless?.terminalCapabilities
                ?: config.terminalCapabilities)?.let {
                    aidData["TerminalCapabilities"] = it
                }

                /* Amex Specific */
                (aid.ctlsRdrCapabilities ?: config.contactless?.ctlsRdrCapabilities
                ?: config.ctlsRdrCapabilities)?.let {
                    aidData["ContactlessReaderCapabilities"] = it
                }

                /* Rupay Specific */
                (aid.addlTerminalCapabilitiesExtension
                    ?: config.contactless?.addlTerminalCapabilitiesExtension
                    ?: config.addlTerminalCapabilitiesExtension)?.let {
                    aidData["AdditionalTerminalCapabilitiesExtension"] = it
                }
                (aid.serviceDataFormat ?: config.contactless?.serviceDataFormat
                ?: config.serviceDataFormat)?.let { aidData["ServiceFormatData"] = it }
                (aid.threshold ?: config.contactless?.threshold
                ?: config.threshold)?.let { aidData["ThresholdValue"] = it }
                (aid.targetPercentage ?: config.contactless?.targetPercentage
                ?: config.targetPercentage)?.let { aidData["TargetPercentage"] = it }
                (aid.maxTargetPercentage ?: config.contactless?.maxTargetPercentage
                ?: config.maxTargetPercentage)?.let { aidData["MaxTargetPercentage"] = it }

                /* Entry Point Specific */
                (aid.zeroAmountAllowed ?: config.contactless?.zeroAmountAllowed
                ?: config.zeroAmountAllowed)?.let { aidData["ZeroAmountCheckFlag"] = it }
                (aid.zeroAmountOfflineAllowed
                    ?: config.contactless?.zeroAmountOfflineAllowed
                    ?: config.zeroAmountOfflineAllowed)?.let {
                    aidData["ZeroAmountAllowedOfflineFlag"] = it
                }
                (aid.statusCheckSupported ?: config.contactless?.statusCheckSupported
                ?: config.statusCheckSupported)?.let { aidData["StatusCheckFlag"] = it }

                result = result && EmvNfcKernelApi.getInstance()
                    .updateAID(ContantPara.Operation.ADD, aidData) //master
                aidData.clear()
            }
        } catch (exception: Exception) {
            result = false
            exception.printStackTrace()
        }

        return result
    }


    companion object {
        var iEmvSdkResponseListener: IEmvSdkResponseListener? = null
        var job: Job? = null
        var _amount: Long = 0L
        var _cashbackAmount: Long = 0L
        var pinBlock: String? = null
        var ksn: String? = null
        var nfcTlv: String? = null
        var nfcDisplayMsgId: DisplayMsgId? = null
        var checkCardResult: ContantPara.CheckCardResult? = null

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



        @OptIn(ExperimentalStdlibApi::class)
        fun getEmvTag(tag: String?): String? {
            var tagVal: String? = null
            try {
                tag?.let {
                    EmvNfcKernelApi.getInstance().getValByTag(tag.hexToInt())
                        ?.takeIf { it.isNotEmpty() }?.let {
                            tagVal = it
                        }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return tagVal
        }

        fun abortPayment() {
            job?.cancel()
            job = null
            //EmvNfcKernelApi.getInstance().abortKernel()
        }

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
                    when (checkCardResult) {
                        ContantPara.CheckCardResult.MSR -> {
                            /* Magstripe Conditions */
                            if (hasOnlineResp
                                && tlvTags.tlvMap.containsKey(EmvConstants.EMV_TAG_RESP_CODE)
                                && tlvTags.tlvMap[EmvConstants.EMV_TAG_RESP_CODE] == EmvConstants.EMV_TAG_VAL_APPROVED_ONLINE
                            ) {
                                iEmvSdkResponseListener?.onEmvSdkResponse(
                                    EmvSdkResult.TransResult(
                                        TransStatus.APPROVED_ONLINE
                                    )
                                )
                            } else if (hasOnlineResp == true) {
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

                        else -> EmvNfcKernelApi.getInstance()
                            .sendOnlineProcessResult(hasOnlineResp, tlvTags.toTlvString())
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun initEncryption() {
            var ksnBytes = ByteArray(EncryptionConstants.DUKPT_KSN_MAX_LENGTH / 2)
            PinPadProviderImpl.getInstance()
                .DukptGetKsn(EncryptionConstants.DUKPT_KEY_SET_TDK, ksnBytes)
            PinPadProviderImpl.getInstance()
                .DukptGetKsn(EncryptionConstants.DUKPT_KEY_SET_EMV, ksnBytes)
            PinPadProviderImpl.getInstance()
                .DukptGetKsn(EncryptionConstants.DUKPT_KEY_SET_PIN, ksnBytes)
            PinPadProviderImpl.getInstance()
                .DukptGetKsn(EncryptionConstants.DUKPT_KEY_SET_MAC, ksnBytes)
            pinBlock = null
        }

        @OptIn(ExperimentalStdlibApi::class)
        fun getEncryptedData(tlvMap: HashMap<String, String>): HashMap<String, String> {
            var hashMap = HashMap<String, String>()
            var trackData =
                EmvNfcKernelApi.getInstance().getValByTag(EmvConstants.EMV_TAG_TRACK2_HEX)
            trackData.takeIf { it.isEmpty() == true && tlvMap.containsKey(EmvConstants.EMV_TAG_TRACK2) }
                ?.let {
                    trackData = tlvMap[EmvConstants.EMV_TAG_TRACK2] ?: ""
                }
            trackData = trackData.replace('D', '=').removeSuffix("F")
            var cardPan = trackData.substringBefore('=')

            /* Clear PAN is OK to send to service layer. Service layer will filter it */
            hashMap[EmvConstants.EMV_TAG_PAN] = cardPan

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
            var encryptedBytes = ByteArray(trackDataBytes.size)
            var encryptedLen = IntArray(1)
            var ksnBytes = ByteArray(EncryptionConstants.DUKPT_KSN_MAX_LENGTH / 2)
            var ksnLen = IntArray(1)
            var ivBytes = ByteArray(EncryptionConstants.TDES_IV_LENGTH)

            /* Encrypt Track2 */
            if (PinPadProviderImpl.getInstance().DukptEncryptDataIV(
                    EncryptionConstants.DUKPT_KEY_TYPE_TRACK_DATA,
                    EncryptionConstants.DUKPT_KEY_SET_PIN,
                    EncryptionConstants.DUKPT_MODE_ENCRYPT_ECB,
                    ivBytes,
                    ivBytes.size,
                    trackDataBytes,
                    trackDataBytes.size,
                    encryptedBytes,
                    encryptedLen,
                    ksnBytes,
                    ksnLen
                ) == 0
            ) {
                hashMap[EmvConstants.EMV_TAG_ENC_TRACK] =
                    encryptedBytes.toHexString().uppercase()
                hashMap[EmvConstants.EMV_TAG_ENC_KSN] =
                    ksnBytes.slice(0 until ksnLen[0]).toByteArray().toHexString().uppercase()
                //Log.d("ENCRYPTION", "INPUT TRACK DATA (ASCII)    : "+trackDataBytes.decodeToString())
                Log.d(
                    "ENCRYPTION",
                    "ENCRYPTED TRACK DATA (LYRA) : " + encryptedBytes.toHexString().uppercase()
                )
                Log.d(
                    "ENCRYPTION",
                    "KSN TRACK DATA (LYRA)       : " + ksnBytes.slice(0 until ksnLen[0])
                        .toByteArray().toHexString().uppercase()
                )
            }

            /* Encrypt PAN */
            if (PinPadProviderImpl.getInstance().DukptEncryptDataIV(
                    EncryptionConstants.DUKPT_KEY_TYPE_TRACK_DATA,
                    EncryptionConstants.DUKPT_KEY_SET_PIN,
                    EncryptionConstants.DUKPT_MODE_ENCRYPT_ECB,
                    ivBytes,
                    ivBytes.size,
                    cardPanBytes,
                    cardPanBytes.size,
                    encryptedBytes,
                    encryptedLen,
                    ksnBytes,
                    ksnLen
                ) == 0
            ) {
                hashMap[EmvConstants.EMV_TAG_ENC_PAN] =
                    encryptedBytes.sliceArray(0 until encryptedLen[0]).toHexString().uppercase()
                //Log.d("ENCRYPTION", "INPUT PAN (ASCII)    : "+cardPanBytes.decodeToString())
                Log.d(
                    "ENCRYPTION",
                    "ENCRYPTED PAN (LYRA) : " + encryptedBytes.sliceArray(0 until encryptedLen[0])
                        .toHexString().uppercase()
                )
                Log.d(
                    "ENCRYPTION",
                    "KSN PAN (LYRA)       : " + ksnBytes.slice(0 until ksnLen[0]).toByteArray()
                        .toHexString().uppercase()
                )
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
}