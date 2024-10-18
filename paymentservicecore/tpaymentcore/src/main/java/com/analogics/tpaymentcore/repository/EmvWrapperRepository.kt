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
import com.analogics.tpaymentcore.listener.requestListener.IEmvWrapperRequestListener
import com.analogics.tpaymentcore.listener.responseListener.IEmvSdkResponseListener
import com.analogics.tpaymentcore.model.emv.AidConfig
import com.analogics.tpaymentcore.model.emv.CAPKey
import com.analogics.tpaymentcore.utils.TlvUtils
import com.urovo.i9000s.api.emv.ContantPara
import com.urovo.i9000s.api.emv.EmvListener
import com.urovo.i9000s.api.emv.EmvNfcKernelApi
import com.urovo.i9000s.api.emv.Funs
import com.urovo.sdk.pinpad.PinPadProviderImpl
import com.urovo.sdk.pinpad.listener.PinInputListener
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
    override fun initializeSdk(aidConfig : AidConfig?, capKeys: List<CAPKey>?) {
        Thread {
            var result = true
            try {
                aidConfig?.let { result = result && initAidConfig(it) }
                capKeys?.let { result = result && initCAPKeys(it) }
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

                EmvNfcKernelApi.getInstance().updateTerminalParamters(
                    ContantPara.CardSlot.UNKNOWN,
                    termTlvParams
                )

                Log.d("EMV_APP", "termTlvParams: $termTlvParams")
/*
                "9F4E1755524F564F5F544553545F4D454348414E545F4E414D459F150211229F160F1234567890123451234567890123459F1C0831323334353637389F4005F000F0A0019F1A0206829F3303E068009F3501225F360102DF020101DF030101DF050100" + "9F1E08" + "1122334455667788"

                TlvUtils().parseTlv("9F4E1755524F564F5F544553545F4D454348414E545F4E414D459F150211229F160F1234567890123451234567890123459F1C0831323334353637389F4005F000F0A0019F1A0206829F3303E068009F3501225F360102DF020101DF030101DF050100")
*/
                when(result) {
                    true->
                        iEmvSdkResponseListener.onEmvSdkResponse("SUCCESS")//DF02---random trans select enable  DF03--Except file check enable DF04--Support SM DF05-- Valocity Check enable
                    else ->
                        iEmvSdkResponseListener.onEmvSdkResponse("FAILURE")//DF02---random trans select enable  DF03--Except file check enable DF04--Support SM DF05-- Valocity Check enable
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    companion object : EmvListener, PinInputListener {
        var iEmvSdkResponseListener: IEmvSdkResponseListener? = null

        fun startPayment(context: Context, iEmvSdkResponseListener: IEmvSdkResponseListener) {
            Thread {
                try {
                    this.iEmvSdkResponseListener = iEmvSdkResponseListener
                    val data = Hashtable<String, Any>()
                    data["checkCardMode"] = ContantPara.CheckCardMode.INSERT_OR_TAP //
                    data["currencyCode"] = "682" //682
                    data["emvOption"] = ContantPara.EmvOption.START // START_WITH_FORCE_ONLINE
                    data["amount"] = "0.01"
                    data["cashbackAmount"] = "0"
                    data["checkCardTimeout"] = "30" // Check Card time out .Second
                    data["transactionType"] = "00" //00-goods 01-cash 09-cashback 20-refund
                    data["isEnterAmtAfterReadRecord"] = false
                    data["FallbackSwitch"] = "0" //0- close fallback 1-open fallback
                    data["supportDRL"] = false // support Visa DRL?

                    EmvNfcKernelApi.getInstance().setContext(context)
                    EmvNfcKernelApi.getInstance().setListener(this)
                    EmvNfcKernelApi.getInstance().startKernel(data)
                    //EmvNfcKernelApi.getInstance().getEMVLibVers(ContantPara.CardSlot.ICC)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }.start()
        }

        override fun onRequestSetAmount() {
            Log.d("EMV_APP", "Request Amount:")

//            Log.e(TAG, TAG + "===onRequestSetAmount");
            EmvNfcKernelApi.getInstance().setAmountEx(1L, 0L)
        }

        override fun onReturnCheckCardResult(
            p0: ContantPara.CheckCardResult?,
            p1: Hashtable<String, String>?
        ) {
            Log.d("EMV_APP", "Check Card Result:" + p0.toString())
            Log.d("EMV_APP", "Check Card List:" + p1.toString())
            if (p0 == ContantPara.CheckCardResult.INSERTED_CARD)
                iEmvSdkResponseListener?.onEmvSdkDisplayMessage("Card Detected")
        }

        override fun onRequestSelectApplication(p0: ArrayList<String>?) {
            Log.d("EMV_APP", "Select Applications:" + p0.toString())
        }

        override fun onRequestPinEntry(p0: ContantPara.PinEntrySource?) {
            Log.d("EMV_APP", "Online PIN Prompt:" + p0.toString())
            iEmvSdkResponseListener?.onEmvSdkDisplayMessage("")
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
            Log.d("EMV_APP", "Process Online:" + p0.toString() + "\n" + p1?.toString())
            EmvNfcKernelApi.getInstance().sendOnlineProcessResult(true, "8A023030")
        }

        override fun onReturnBatchData(p0: String?) {
            Log.d("EMV_APP", "Batch Data:" + p0.toString())
        }

        override fun onReturnTransactionResult(p0: ContantPara.TransactionResult?) {
            Log.d("EMV_APP", "Transaction Result:" + p0.toString())
            Log.d("EMV_APP", "TLV Data:" + EmvNfcKernelApi.getInstance().GetField55ForSAMA())
            if (p0 == ContantPara.TransactionResult.ONLINE_APPROVAL || p0 == ContantPara.TransactionResult.OFFLINE_APPROVAL)
                iEmvSdkResponseListener?.onEmvSdkResponse("SUCCESS")
            else
                iEmvSdkResponseListener?.onEmvSdkResponse("FAILURE")
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
            /*
            if (p0 == ContantPara.PinEntrySource.KEYPAD)
                promptPin(null, false, 3, null, false);
*/
        }

        override fun onReturnIssuerScriptResult(p0: ContantPara.IssuerScriptResult?, p1: String?) {
            Log.d("EMV_APP", "Issuer Script Result:" + p0.toString())
        }

        override fun onNFCrequestTipsConfirm(p0: ContantPara.NfcTipMessageID?, p1: String?) {
            Log.d("EMV_APP", "NFC Request Tip:" + p0.toString())
        }

        override fun onReturnNfcCardData(p0: Hashtable<String, String>?) {
            Log.d("EMV_APP", "NFC Card Data:" + p0.toString())
        }

        override fun onNFCrequestOnline() {
            Log.d("EMV_APP", "NFC Process Online:")
            EmvNfcKernelApi.getInstance().sendOnlineProcessResult(true, "8A023030")
        }

        override fun onNFCrequestImportPin(p0: Int, p1: Int, p2: String?) {
            Log.d("EMV_APP", "NFC Import PIN:" + p2.toString())

//            Log.e(TAG, TAG + "===onNFCrequestImportPin, type:" + type + ", lasttimeFlag:" + lasttimeFlag + ", amt:" + amt);
            EmvNfcKernelApi.getInstance().sendPinEntry()
        }

        override fun onNFCTransResult(p0: ContantPara.NfcTransResult?) {
            Log.d("EMV_APP", "NFC Trans Result:" + p0.toString())
            if (p0 == ContantPara.NfcTransResult.ONLINE_APPROVAL || p0 == ContantPara.NfcTransResult.OFFLINE_APPROVAL)
                iEmvSdkResponseListener?.onEmvSdkResponse("SUCCESS")
            else
                iEmvSdkResponseListener?.onEmvSdkResponse("FAILURE")
        }

        override fun onNFCErrorInfor(p0: ContantPara.NfcErrMessageID?, p1: String?) {
            Log.d("EMV_APP", "NFC Trans Error:" + p0.toString())
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

        fun addContactlessAid(config: AidConfig) : Boolean {
            var result = true
            try {
                /* Add Contactless Configuration */
                for (aid in config.contactless?.aidList ?: emptyList()) {
                    val aidData = Hashtable<String, String>()

                    (aid.aid ?: config.contactless?.aid ?: config.aid)?.let{ aidData["ApplicationIdentifier"] = it }
                    getCardType(aidData["ApplicationIdentifier"]?:"").let{ aidData["CardType"] = it }
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
                    (aid.ifdSerialNumber ?: config.contactless?.ifdSerialNumber ?: config.ifdSerialNumber)?.let{ aidData["IFDsn"] = it }
                    (aid.merchantCategoryCode ?: config.contactless?.merchantCategoryCode ?: config.merchantCategoryCode)?.let{ aidData["MerchantCategoryCode"] = it }
                    (aid.merchantIdentifier ?: config.contactless?.merchantIdentifier ?: config.merchantIdentifier)?.let{ aidData["MerchantIdentifier"] = it }
                    (aid.merchantNameLocation ?: config.contactless?.merchantNameLocation ?: config.merchantNameLocation)?.let{ aidData["MerchantNameAndLocation"] = it }
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

        fun addEMV_AID_Paramters() {
            val data = Hashtable<String, String>()
            data["CardType"] = "IcCard"
            data["aid"] = "A0000000041010"
            data["appVersion"] = "0002"
            data["terminalFloorLimit"] = "00000000"
            //data.put("terminalFloorLimit", Funs.DecNumStrToHexNumStr("000000010000"));
            //data.put("contactTACDefault", "D84000A800");
            data["contactTACDefault"] = "0000000000"
            data["contactTACDenial"] = "0000000000"
            data["contactTACOnline"] = "DC4004F800"
            data["defaultDDOL"] = "9F3704"
            data["AcquirerIdentifier"] = "112233" // 9f01
            data["defaultTDOL"] = "9F0206"
            data["ThresholdValue"] = "000000002000"
            data["TargetPercentage"] = "00"
            data["MaxTargetPercentage"] = "00"
            data["AppSelIndicator"] = "00" //default 00 -part match 01 -full match
            data["TerminalAppPriority"] = "00" //TerminalCapabilities
            data["TerminalCapabilities"] = "E0F8C8"

            data["terminalCountryCode"] = "0356"

            EmvNfcKernelApi.getInstance().updateAID(ContantPara.Operation.ADD, data) //master

            data["aid"] = "A0000000043060" //Maestro
            EmvNfcKernelApi.getInstance().updateAID(ContantPara.Operation.ADD, data)

            data["aid"] = "A00000002501" //amex
            EmvNfcKernelApi.getInstance().updateAID(ContantPara.Operation.ADD, data)

            data["terminalFloorLimit"] = "000003E0"
            data["aid"] = "A0000000651010" //jcb
            EmvNfcKernelApi.getInstance().updateAID(ContantPara.Operation.ADD, data)

            data["terminalFloorLimit"] = "00000000"
            data["aid"] = "A0000000046000" //Cirrus
            EmvNfcKernelApi.getInstance().updateAID(ContantPara.Operation.ADD, data)

            data["aid"] = "A0000001523010" // Diners Club/Discover
            EmvNfcKernelApi.getInstance().updateAID(ContantPara.Operation.ADD, data)

            data["aid"] = "A0000006581010" // Mir
            EmvNfcKernelApi.getInstance().updateAID(ContantPara.Operation.ADD, data)

            data["aid"] = "A0860001000001" //humo card
            EmvNfcKernelApi.getInstance().updateAID(ContantPara.Operation.ADD, data)

            data["aid"] = "A0000001523010" // Diners Club/Discover
            EmvNfcKernelApi.getInstance().updateAID(ContantPara.Operation.ADD, data)

            data["contactTACDenial"] = "0010000000"
            data["aid"] = "A0000000031010" //visa
            EmvNfcKernelApi.getInstance().updateAID(ContantPara.Operation.ADD, data)

            data["aid"] = "A0000000032010"
            EmvNfcKernelApi.getInstance().updateAID(ContantPara.Operation.ADD, data)

            data["aid"] = "A0000005241010" //
            EmvNfcKernelApi.getInstance().updateAID(ContantPara.Operation.ADD, data)

            data["aid"] = "A000000054480001" //TBD citizen
            EmvNfcKernelApi.getInstance().updateAID(ContantPara.Operation.ADD, data)

            data["terminalCountryCode"] = "0608"
            data["aid"] = "A0000006351010" //BancNet 菲律宾
            EmvNfcKernelApi.getInstance().updateAID(ContantPara.Operation.ADD, data)

            data["terminalCountryCode"] = "0682"
            data["aid"] = "A0000002281010" //Mada
            EmvNfcKernelApi.getInstance().updateAID(ContantPara.Operation.ADD, data)

            data["terminalCountryCode"] = "0682"
            data["aid"] = "A0000002282010" //mada
            EmvNfcKernelApi.getInstance().updateAID(ContantPara.Operation.ADD, data)
        }

        fun getCardType(aid : String) : String
        {
            return when(aid.substring(0,10))
            {
                "A000000003" -> "VisaCard"
                "A000000004" -> "MasterCard"
                "A000000524" -> "RupayCard"
                "A000000025" -> "AmexCard"
                "A000000065" -> "JcbCard"
                "A000000152" -> "DiscoverCard"
                else -> ""
            }
        }

        fun init_NfcAid_CAPK() {
            var bret: Boolean


            val aidData = Hashtable<String, String>()
            //-------------------------------------------------
            //------------MasterCard----------------
            //-------------------------------------------------
            aidData.clear()
            aidData["CardType"] = "MasterCard"
            aidData["ApplicationIdentifier"] = "A0000000041010" //9F06
            aidData["ApplicationVersionNumber"] = "0002" //9F09 or "ApplicationVersion"
            aidData["FloorLimit"] = "000000000000" //DF8123
            aidData["NoOnDeviceCVM"] = "999999999999" //contactless transaction limit DF8124
            aidData["OnDeviceCVM"] = "999999999999" // contactless transaction limit
            aidData["ReaderCVMRequiredLimit"] = "000000500000" //DF8126 or "CvmRequiredLimit"
            aidData["DefaultUDOL"] = "9F6A04" //DF811A
            aidData["TerminalActionCodesOnLine"] = "F45084800C" //DF8122 F45084800C
            aidData["TerminalActionCodesDenial"] = "0000000000" //DF8121
            aidData["TerminalActionCodesDefault"] = "F45084800C" //DF8120
            aidData["TerminalRiskManagement"] = "007A800000000000" //9F1D
            aidData["KernelConfiguration"] = "30" //  20 normal // 30 for RRP support
            aidData["CardDataInputCapability"] = "60" //DF8117  // 60
            aidData["CVMCapabilityPerCVMRequired"] = "60" //DF8118 //60 support online pin
            aidData["CVMCapabilityNoCVMRequired"] = "08" //DF8119
            aidData["MagStripeCVMCapabilityCVMRequired"] = "10" // DF811E = "10";
            aidData["SecurityCapability"] = "08" //DF811F
            aidData["MagStripeCVMCapabilityPerNoCVMRequired"] = "00" // DF812C = "00";
            //aidData.put("TerminalCountryCode", "0840");
            //aidData.put("IFDsn", "3030303030303030");
            bret = EmvNfcKernelApi.getInstance()
                .updateAID(ContantPara.Operation.ADD, aidData) //MasterCard


            Log.d("applog", "updateAID MasterCard:$bret")

            aidData["CVMCapabilityPerCVMRequired"] = "60"
            aidData["TerminalActionCodesOnLine"] = "F45004800C" //DF8122
            aidData["TerminalActionCodesDenial"] = "0000800000" //DF8121
            aidData["TerminalActionCodesDefault"] = "F45004800C" //DF8120
            aidData["TerminalRiskManagement"] = "4C7A800000000000" //9F1D
            aidData["ApplicationIdentifier"] = "A0000000043060" //9F06
            aidData["KernelConfiguration"] = "B0" //Maestro card not support MS mode

            bret = EmvNfcKernelApi.getInstance().updateAID(ContantPara.Operation.ADD, aidData)
            Log.d("applog", "updateAID Maestro:$bret")


            //-------------------------------------------------------
            //--------------VISACARD-----------------------------
            // -------------------------------------------------
            aidData.clear()
            aidData["CardType"] = "VisaCard"
            aidData["ApplicationIdentifier"] = "A0000000031010" //9F06
            aidData["TerminalTransactionQualifiers"] =
                "36004000" //9F66  //36004000    // 32204000 not support online PIN

            aidData["TransactionLimit"] = "999999999999" //9F92810D
            aidData["FloorLimit"] = "000000000000" //9F92810F  //000000040000
            aidData["CvmRequiredLimit"] = "000000500000" //9F92810E   //000000030000

            aidData["LimitSwitch"] = "FE00" //9F92810A
            aidData["EmvTerminalFloorLimit"] = "00000000" //9F1B
            aidData["ProRestrictionDisable"] = "01"
            bret = EmvNfcKernelApi.getInstance().updateAID(ContantPara.Operation.ADD, aidData)
            Log.d("applog", "VisaCard updateAID1 $bret")

            aidData["ApplicationIdentifier"] = "A0000000032010" //9F06
            bret = EmvNfcKernelApi.getInstance().updateAID(ContantPara.Operation.ADD, aidData)
            Log.d("applog", "VisaCard updateAID2 $bret")
            aidData["ApplicationIdentifier"] = "A0000000033010" //9F06
            bret = EmvNfcKernelApi.getInstance().updateAID(ContantPara.Operation.ADD, aidData)
            Log.d("applog", "VisaCard updateAID3 $bret")

            aidData["ApplicationIdentifier"] = "A0000006351010" //BancNet 菲律宾
            bret = EmvNfcKernelApi.getInstance().updateAID(ContantPara.Operation.ADD, aidData)
            Log.d("applog", "Visa-BancNet updateAID4 $bret")


            //init_Visa_DRL();// need update it after default paramters


            //--------------------------------------------------------------
            //------------AMEXCARD--------------------------
            //------------------------------------------------------
            aidData.clear()
            aidData["CardType"] = "AmexCard"
            aidData["ApplicationIdentifier"] = "A00000002501" //9F06
            aidData["TerminalTransactionQualifiers"] =
                "DCE00003" //9F6E  //58E00003  // D8E00003 support contact // Enhanced Contactless Reader Capabilities
            aidData["TransactionLimit"] = "999999999999" //9F92810D
            aidData["FloorLimit"] = "000000000000" //9F92810F   //000000001200
            aidData["CvmRequiredLimit"] = "000000500000" //9F92810E
            aidData["LimitSwitch"] = "6800" //9F92810A
            aidData["EmvTerminalFloorLimit"] = "00000000" //9F1B
            aidData["ApplicationVersion"] = "0001" //9f09
            aidData["TerminalActionCodesOnLine"] = "DE00FC9800" //DF8122 //DE00FC9800
            aidData["TerminalActionCodesDenial"] = "0010000000" //DF8121 //0010000000
            aidData["TerminalActionCodesDefault"] = "DC50840000" //DF8120 //DC50FC9800


            /*
        ////////////////Dynamic Limit Set default//////////////////// if not support DRL set ,you need not set
        String defaultDRL="";
        String DRLset="";
        AmexDRL amexDRL=new AmexDRL();
        amexDRL.setCVMLimit("000000001000");
        amexDRL.setFloorLimit("000000000000");
        amexDRL.setTransLimit("000000001500");
        amexDRL.setIndex("00");
        amexDRL.setDefault(true);
        defaultDRL+=amexDRL.formTLVFormat();
        aidData.put("DefaultDRL", defaultDRL);
        Log.d("applog", "DefaultDRL:"+defaultDRL);
        ///////////Dynamic Limit Set 11////////////
        amexDRL.setCVMLimit("000000000200");
        amexDRL.setFloorLimit("000000000000");
        amexDRL.setTransLimit("000000000300");
        amexDRL.setIndex("0B");
        amexDRL.setDefault(false);
        DRLset+=amexDRL.formTLVFormat();
        ///////////Dynamic Limit Set 6////////////
        amexDRL.setCVMLimit("000000000200");
        amexDRL.setFloorLimit("000000000000");
        amexDRL.setTransLimit("000000000700");
        amexDRL.setIndex("06");
        amexDRL.setDefault(false);
        DRLset+=amexDRL.formTLVFormat();
        aidData.put("DRLSet", DRLset);
        Log.d("applog", "DRLSet:"+DRLset);
        ////////////////////////////Dynamic Limit Set ///////////////////
*/
            bret = EmvNfcKernelApi.getInstance().updateAID(ContantPara.Operation.ADD, aidData)
            Log.d("applog", "AmexCard updateAID $bret")
            //----------------------------------------------------------
            //-----------------JCBCARD-------------------------------
            //-------------------------------------------------------
            aidData.clear()
            aidData["CardType"] = "JcbCard"
            aidData["ApplicationIdentifier"] = "A0000000651010" //9F06
            aidData["ConfigurationCombinationOptions"] = "7B00" //
            aidData["StaticTerminalInterchangeProfile"] = "708000" //

            aidData["TransactionLimit"] = "999999999999" //9F92810D
            aidData["FloorLimit"] = "000000000000" //9F92810F   //000000001200
            aidData["CvmRequiredLimit"] = "000000500000" //9F92810E

            aidData["EmvTerminalFloorLimit"] = "00004E20" //9F1B
            aidData["ApplicationVersion"] = "0200" //9f09
            aidData["TerminalActionCodesOnLine"] = "FC60ACF800" //DF8122
            aidData["TerminalActionCodesDenial"] = "0010000000" //DF8121
            aidData["TerminalActionCodesDefault"] = "FC6024A800" //DF8120

            aidData["ThresholdValue"] = "000000002000"
            aidData["TargetPercentage"] = "00"
            aidData["MaxTargetPercentage"] = "00"

            aidData["AcquirerIdentifier"] = "000000000010"
            //aidData.put("MerchantCategoryCode", "7032");
            //aidData.put("MerchantNameAndLocation", "5858204D45524348414E54205959204C4F434154494F4E");
            aidData["TerminalCapabilities"] = "E068C8"

            bret = EmvNfcKernelApi.getInstance().updateAID(ContantPara.Operation.ADD, aidData)
            Log.d("applog", "JcbCard updateAID $bret")


            //-------------------------------------------------
            //--------------DISCOVERCARD-----------------------
            //-------------------------------------------------
            aidData.clear()
            aidData["CardType"] = "DiscoverCard"
            aidData["ApplicationIdentifier"] = "A000000152301002" //9F06
            aidData["TerminalTransactionQualifiers"] = "3600C000" // TTQ
            aidData["TransactionLimit"] = "999999999999" //9F92810D
            aidData["FloorLimit"] = "000000000000" //9F92810F   //000000000000
            aidData["CvmRequiredLimit"] = "000000500000" //9F92810E
            aidData["EmvTerminalFloorLimit"] = "00000000" //9F1B
            aidData["ApplicationVersion"] = "0001" //9f09
            aidData["TerminalActionCodesOnLine"] = "0000000000" //DF8122 FC60ACF800
            aidData["TerminalActionCodesDenial"] = "0000000000" //DF8121 0010000000
            aidData["TerminalActionCodesDefault"] = "0000000000" //DF8120 FC6024A800

            bret = EmvNfcKernelApi.getInstance().updateAID(ContantPara.Operation.ADD, aidData)
            Log.d("applog", "DiscoverCard updateAID $bret") //A0000003241010
            aidData["ApplicationIdentifier"] = "A0000001523010" //9F06
            bret = EmvNfcKernelApi.getInstance().updateAID(ContantPara.Operation.ADD, aidData)


            aidData["ApplicationIdentifier"] = "A0000003241010" //9F06
            bret = EmvNfcKernelApi.getInstance().updateAID(ContantPara.Operation.ADD, aidData)
            Log.d("applog", "DinnerCard updateAID $bret")

        }

        fun emv_proc_onlinePin(isDUKPT: Boolean) {
            Log.i("applog", "emv_proc_onlinePin")

            val param: Bundle = Bundle()

            if (isDUKPT) param.putInt("PINKeyNo", 3)
            else param.putInt("PINKeyNo", 10)
            val cardno: String = "1122334455667788"


            Log.i("applog", "emv_proc_onlinePin cardno $cardno")
            param.putString("cardNo", cardno)
            param.putBoolean("sound", true)
            param.putInt("soundVolume", 1)
            param.putBoolean("onlinePin", true)
            param.putBoolean("FullScreen", true)
            param.putLong("timeOutMS", 30000)
            param.putString("supportPinLen", "0,4,5,6,7,8,9,10,11,12") // "4,4");   //
            param.putString("title", "Security PINPAD")
            param.putString(
                "message", "Please Enter PIN, \n $0.01"
            ) // use your real amount

/*            param.putBoolean("ShowLine", false)
            param.putShortArray("textSize", shortArrayOf(20, 30, 40, 50, 40, 30, 20))
            param.putShortArray("leftMargin", shortArrayOf(20, 30, 40, 50, 40, 30, 20))
            param.putShortArray("topMargin", shortArrayOf(20, 30, 40, 50, 40, 30, 20))
            param.putShortArray("rightMargin", shortArrayOf(20, 30, 40, 50, 40, 30, 20))
            param.putShortArray("bottomMargin", shortArrayOf(20, 30, 40, 50, 40, 30, 20))
            param.putStringArray(
                "numberText",
                arrayOf<String>(
                    "zero",
                    "one",
                    "two",
                    "three",
                    "four",
                    "five",
                    "six",
                    "seven",
                    "eight",
                    "nine"
                )
            )
            param.putIntArray(
                "backgroundColor",
                intArrayOf(
                    Color.BLUE,
                    Color.YELLOW,
                    Color.GREEN,
                    MaterialTheme.colorScheme.onSecondary,
                    Color.RED,
                    MaterialTheme.colorScheme.tertiary,
                    Color.LTGRAY
                )
            )
            param.putString("deleteText", "Delete1")
            param.putString("cancelText", "Cancel")
            param.putString("okText", "OK1")*/

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

                //    textSize, margin
                //    each index(0-6):
                //    public static final int SECURITY_KEYBOARD_TITLE = 0;
                //    public static final int SECURITY_KEYBOARD_INFO = 1;
                //    public static final int SECURITY_KEYBOARD_PASSWORD = 2;
                //    public static final int SECURITY_KEYBOARD_KEY_NUMBER = 3;
                //    public static final int SECURITY_KEYBOARD_KEY_CANCEL = 4;
                //    public static final int SECURITY_KEYBOARD_KEY_DELETE = 5;
                //    public static final int SECURITY_KEYBOARD_KEY_OK = 6;
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

            if (isDUKPT)
                PinPadProviderImpl.getInstance().GetDukptPinBlock(param, this)
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

            paramVar.putBoolean("sound", true)
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
                pinpadBundle.putBoolean("sound", true)
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

        override fun onConfirm_dukpt(p0: ByteArray?, p1: ByteArray?) {
            //iEmvSdkResponseListener?.onEmvSdkDisplayMessage("Processing")
            if (p0 == null) {
                EmvNfcKernelApi.getInstance().bypassPinEntry() //bypass
            } else {
                Log.d("EMV_APP", "PinBlock:" + p0.decodeToString())
                Log.d("EMV_APP", "KSN     :" + p1?.decodeToString())
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