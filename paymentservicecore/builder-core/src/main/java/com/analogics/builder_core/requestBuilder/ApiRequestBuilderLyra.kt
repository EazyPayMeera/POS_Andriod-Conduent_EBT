package com.eazypaytech.builder_core.requestBuilder

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.analogics.builder_core.requestBuilder.IsoMessageBuilder
import com.analogics.builder_core.requestBuilder.SavedTxnData
import com.eazypaytech.builder_core.constants.BuilderConstants
import com.eazypaytech.builder_core.model.BuilderServiceTxnDetails
import com.eazypaytech.builder_core.model.CardEntryMode
import com.eazypaytech.builder_core.utils.BuilderUtils
import com.eazypaytech.builder_core.utils.toCurrencyLong
import com.eazypaytech.securityframework.database.dbRepository.TxnDBRepository
import com.eazypaytech.securityframework.model.TxnType
import com.solab.iso8583.IsoMessage
import com.solab.iso8583.IsoType
import com.solab.iso8583.MessageFactory
import com.solab.iso8583.parse.ConfigParser
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class ApiRequestBuilderLyra @Inject constructor(@ApplicationContext val context: Context,var dbRepository: TxnDBRepository) {
    val messageFactory = MessageFactory<IsoMessage>()
    var builderServiceTxnDetails = BuilderServiceTxnDetails()
    lateinit var message : IsoMessage


    init {
        setIsoConfig()
    }

    private fun extractIsoPayload(response : ByteArray?) : ByteArray
    {
        var isoPacketLength : Int = if((response?.size ?: 0) > 2) response?.size?.minus(2)?:0 else 0
        var isoPacket = ByteArray(isoPacketLength)
        if(response?.get(0) == (isoPacketLength/256).toByte() &&
            response.get(1) == (isoPacketLength%256).toByte()) {
            response.copyInto(isoPacket, 0, 2, response.size)
        }
        return isoPacket
    }

    fun setIsoConfig()
    {
        messageFactory.setConfigPath(BuilderConstants.ISO_CONFIG_PATH)
        messageFactory.useBinaryMessages = true
        messageFactory.isBinaryHeader = true
    }


    private fun getIsoPosEntryMode(): String {
        return when {
            builderServiceTxnDetails.txnType == TxnType.E_VOUCHER.toString() &&
                    builderServiceTxnDetails.cardEntryMode == CardEntryMode.MANUAL.toString() -> "012"
            else -> when (builderServiceTxnDetails.cardEntryMode) {
                CardEntryMode.MAGSTRIPE.toString() -> "021"
                CardEntryMode.CONTACT.toString() -> "051"
                CardEntryMode.CONTACLESS.toString() -> "071"
                CardEntryMode.FALLBACK_MAGSTRIPE.toString() -> "801"
                CardEntryMode.MANUAL.toString() -> "011"
                else -> "010"
            }
        }
    }

    fun cashbackAmount(cashbackAmt: Long): String {
        val accountType = BuilderConstants.DEFAULT_ACCOUNT_TYPE   // EBT account
        val amountType = BuilderConstants.DEFAULT_AMOUNT_TYPE    // Cash Benefit
        val currency = BuilderConstants.DEFAULT_ISO8583_CURRENCY_CODE
        val sign = BuilderConstants.DEFAULT_AMOUNT_SIGN           // Credit
        val amt = (cashbackAmt * 100).toLong()
            .toString()
            .padStart(12, '0')
        return accountType + amountType + currency + sign + amt
    }


    fun getMaskedPAN() : String?
    {
        var pan : String? =null
        builderServiceTxnDetails.cardMaskedPan?.let {
            pan = it
        }
        return pan
    }

    fun getEncryptedTrack2Data() : String?
    {
        var trackData : String? =null
        builderServiceTxnDetails.trackData?.let {
            trackData = it
        }
        return trackData
    }



    fun getCurrencyCode() : String?
    {
        var currencyCode: String? =
            builderServiceTxnDetails.txnCurrencyCode?.toInt()?.toString()?: BuilderConstants.DEFAULT_ISO8583_CURRENCY_CODE
        currencyCode?.padStart(BuilderConstants.ISO_FIELD_CURRENCY_CODE_LEN, '0')?.let {
            currencyCode = it
        }
        return currencyCode
    }

    fun getProcessingCode(txnType: String?): String {
        return when {
            builderServiceTxnDetails.txnType == TxnType.E_VOUCHER.toString()
                    && builderServiceTxnDetails.isReturn == true -> "200098"
            txnType == TxnType.CASH_PURCHASE.toString() -> "009600"
            txnType == TxnType.FOOD_PURCHASE.toString() -> "009800"
            txnType == TxnType.PURCHASE_CASHBACK.toString() -> "099600"
            txnType == TxnType.FOODSTAMP_RETURN.toString() -> "200098"
            txnType == TxnType.BALANCE_ENQUIRY_CASH.toString() -> "319600"
            txnType == TxnType.BALANCE_ENQUIRY_SNAP.toString() -> "319800"
            txnType == TxnType.VOID_LAST.toString() -> "0000"
            txnType == TxnType.E_VOUCHER.toString() -> "009800"
            txnType == TxnType.CASH_WITHDRAWAL.toString() -> "019600"
            else -> "0000"
        }
    }


    fun getPinBlock() : String?
    {
        var pinBlock: String? =
            builderServiceTxnDetails.pinBlock
        pinBlock?.padEnd(BuilderConstants.ISO_FIELD_PAN_SEQ_NO_LENGTH, 'F')?.let {
            pinBlock = it
        }
        return pinBlock
    }

    fun getCardSeqNum() : String?
    {
        var cardSeqNumber: String? =
            builderServiceTxnDetails.cardSeqNum?.toInt()?.toString()
        cardSeqNumber?.padStart(BuilderConstants.ISO_FIELD_PAN_SEQ_NO_LENGTH, '0')?.let {
            cardSeqNumber = it
        }
        return cardSeqNumber
    }

    fun hexStringToByteArray(s: String): ByteArray {

        require(s.length % 2 == 0) { "HEX length must be even" }

        val data = ByteArray(s.length / 2)
        var i = 0

        while (i < s.length) {

            val high = Character.digit(s[i], 16)
            val low = Character.digit(s[i + 1], 16)

            require(high != -1 && low != -1) { "Invalid HEX character" }

            data[i / 2] = ((high shl 4) + low).toByte()
            i += 2
        }

        return data
    }


    // ================= HELPERS =================
    fun cleanHex(input: String): String {
        val clean = input.replace("\\s+".toRegex(), "")

        require(clean.isNotEmpty()) { "Empty HEX string" }
        require(clean.length % 2 == 0) { "HEX length must be even" }
        require(clean.matches(Regex("[0-9A-Fa-f]+"))) { "Invalid HEX characters" }

        return clean.uppercase()
    }

    fun checkTag(
        hex: String,
        tag: String,
        lenHex: String,
        expected: String,
        label: String
    ) {
        val search = (tag + lenHex).uppercase()
        val idx = hex.indexOf(search)

        if (idx < 0) {
            println("? $label not found")
            return
        }

        val start = idx + search.length
        val len = lenHex.toInt(16) * 2
        val actual = hex.substring(start, start + len)

        if (actual.equals(expected, true)) {
            println("✓ $label = $actual")
        } else {
            println("✗ $label = $actual (expected $expected)")
        }
    }

    fun rearrangeTLV(tlvHex: String): String {
        val conductOrderedTags = arrayOf(
            "9F26", // Application Cryptogram (AC)
            "9F27", // Cryptogram Information Data
            "9F10", // Issuer Application Data (IAD)
            "9F34", // CVM Results
            "9F33", // Terminal Capabilities
            "9F37", // Unpredictable Number
            "9F36", // ATC
            "95",   // TVR
            "9A",   // Transaction Date
            "9C",   // Transaction Type
            "9F02", // Amount Authorized
            "5F2A", // Transaction Currency Code
            "82",   // AIP
            "84",   // DF Name
            "9F1A", // Terminal Country Code
            "9F03"  // Amount Other
        )

        val tlvMap = mutableMapOf<String, String>()
        var index = 0

        while (index < tlvHex.length) {
            try {
                val firstByte = tlvHex.substring(index, index + 2).toInt(16)
                val tag = if ((firstByte and 0x1F) == 0x1F) {
                    tlvHex.substring(index, index + 4).also { index += 4 }
                } else {
                    tlvHex.substring(index, index + 2).also { index += 2 }
                }

                val lenByte = tlvHex.substring(index, index + 2).toInt(16)
                index += 2
                val len = if (lenByte <= 0x7F) {
                    lenByte
                } else {
                    val extraBytes = lenByte and 0x7F
                    tlvHex.substring(index, index + extraBytes * 2)
                        .toInt(16)
                        .also { index += extraBytes * 2 }
                }

                val value = tlvHex.substring(index, index + len * 2)
                index += len * 2

                tlvMap[tag.uppercase()] = value
                Log.d("TLV_PARSE", "Parsed → tag=${tag.uppercase()} len=$len value=$value")

            } catch (e: Exception) {
                Log.e("TLV_PARSE", "Parse error at index=$index: ${e.message}")
                break
            }
        }

        tlvMap["95"] = "0000000000".also {
            Log.d("TLV_OVERRIDE", "95 TVR overridden → $it")
        }
        tlvMap["9F34"] = "020000".also {
            Log.d("TLV_OVERRIDE", "9F34 CVM overridden → $it")
        }

        tlvMap["9F10"] = "0110A00001020000000000000000000000FF".also {
            Log.d("TLV_OVERRIDE", "9F10 IAD overridden → $it")
        }

        tlvMap["9F33"] = "604000".also {
            Log.d("TLV_OVERRIDE", "9F33 IAD overridden → $it")
        }

        Log.d("TLV_REARRANGE", "Total tags parsed: ${tlvMap.size}")
        Log.d("TLV_REARRANGE", "Tags found: ${tlvMap.keys}")

        val result = StringBuilder()
        for (tag in conductOrderedTags) {
            tlvMap[tag.uppercase()]?.let { value ->
                val len = (value.length / 2).toString(16).padStart(2, '0')
                result.append(tag.uppercase()).append(len).append(value)
                Log.d("TLV_REARRANGE", "Added → $tag : $value")
            } ?: Log.w("TLV_REARRANGE", "⚠️ Missing tag: $tag")
        }

        Log.d("TLV_REARRANGE", "Original  : $tlvHex")
        Log.d("TLV_REARRANGE", "Rearranged: ${result.toString()}")

        return result.toString()
    }

    fun getIccData() : String?
    {
        //Log.d("ICC_DATA", "emvData raw: ${builderServiceTxnDetails.emvData}")
        var iccData : String? =null
        builderServiceTxnDetails.emvData?.let {
            iccData = it
        }
        return iccData
    }

    fun getSTAN() : Long
    {
        var stan : Long = 0
        builderServiceTxnDetails.stan?.toLongOrNull()?.let {
            stan = it   /* Reuse if received from application. Ex : Void or Reversal */
        }?:let {
            stan = BuilderUtils.getSTAN(context)
            builderServiceTxnDetails.stan = stan.toString()
        }
        return stan%(BuilderConstants.ISO_FIELD_STAN_MAX_VAL+1)
    }


    fun getNationalPosConditionCode(): String {
        val terminalClass = "000"      // Attended, customer-operated, on-premise
        val presentationType = "0000"  // Customer present + card present
        val securityCondition = "0"    // No security concern
        val terminalType = "01"        // POS terminal
        val terminalCapability = when (builderServiceTxnDetails.cardEntryMode) {
            CardEntryMode.CONTACT.toString() -> "5"        // Chip
            CardEntryMode.MAGSTRIPE.toString() -> "2"      // Magstripe
            CardEntryMode.MANUAL.toString() -> "6"         // Manual entry
            CardEntryMode.CONTACLESS.toString() -> "7"     // Contactless
            else -> "0"
        }
        return terminalClass +
                presentationType +
                securityCondition +
                terminalType +
                terminalCapability
    }


    fun getNPSGeographicData(): String {
        val stateCode = builderServiceTxnDetails.stateCode?.padStart(2, '0') ?: "00"
        val countyCode = builderServiceTxnDetails.countyCode?.padStart(3, '0') ?: "000"
        val postalServiceCode = builderServiceTxnDetails.postalServiceCode?.padStart(5, '0') ?: "00000"
        val countryCode = builderServiceTxnDetails.currencyCode?.padStart(3, '0') ?: "000"
        val npsGeoData = stateCode + countyCode + postalServiceCode + countryCode
        return npsGeoData
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun generateRRN(stan: String): String {
        val now = java.time.LocalDateTime.now()
        val yearLastDigit = now.year.toString().last()           // 1 digit
        val dayOfYear = "%03d".format(now.dayOfYear)           // 3 digits
        val hour = "%02d".format(now.hour)                     // 2 digits
        val minute = "%02d".format(now.minute)                 // 2 digits
        val stanPart = stan.takeLast(4).padStart(4, '0')       // 4 digits from STAN

        return "$yearLastDigit$dayOfYear$hour$minute$stanPart" // 1 + 3 + 2 + 2 + 4 = 12 digits
    }




    fun createMessageFactory(context: Context): MessageFactory<IsoMessage> {

        val mf = MessageFactory<IsoMessage>()

        val reader = InputStreamReader(
            context.assets.open("iso_config.xml")
        )

        ConfigParser.configureFromReader(mf, reader)

        mf.isUseBinaryBitmap = false
        mf.characterEncoding = "ASCII"

        return mf
    }



    fun createSignOnRequest(builderServiceTxnDetails: BuilderServiceTxnDetails?): ByteArray {
        val msg_sec_code = builderServiceTxnDetails?.procId?.drop(3)
        val time = BuilderUtils.getCurrentDateTime(BuilderConstants.ISO_DATE_FORMAT)
        val stan = getSTAN().toString().padStart(6, '0')
        val iso = IsoMessage()
        iso.setType(BuilderConstants.ISO_TYPE_SIGN_ON)  // MTI 0800
        iso.setValue(BuilderConstants.ISO_FIELD_TRANSMISSION_DATE, time, IsoType.NUMERIC, 10) // Fixed-length numeric 10 digits
        iso.setValue(BuilderConstants.ISO_FIELD_STAN, stan, IsoType.NUMERIC, 6) // Fixed-length numeric 6 digits
        iso.setValue(BuilderConstants.ISO_FIELD_ACQUIRER_ID,builderServiceTxnDetails?.procId , IsoType.LLVAR, BuilderConstants.ISO_FIELD_PROC_ID_LENGTH) // LLVAR length auto-handled
        iso.setValue(BuilderConstants.ISO_FIELD_NET_MGMT_INFO_CODE, BuilderConstants.SIGN_ON_REQUEST, IsoType.NUMERIC, 3) // Fixed-length numeric
        msg_sec_code?.length?.let { iso.setValue(BuilderConstants.ISO_FIELD_KEY_MGMT_DATA, msg_sec_code, IsoType.ALPHA, it ) }
        iso.setBinaryHeader(false)
        iso.setBinaryFields(false)
        iso.setForceStringEncoding(true)
        iso.setIsoHeader("")
        return iso.writeData()
    }

    fun createSignOffRequest(builderServiceTxnDetails: BuilderServiceTxnDetails?): ByteArray {
        val msg_sec_code = builderServiceTxnDetails?.procId?.drop(3)
        val time = BuilderUtils.getCurrentDateTime(BuilderConstants.ISO_DATE_FORMAT)
        val stan = getSTAN().toString().padStart(6, '0')
        val iso = IsoMessage()
        iso.setType(BuilderConstants.ISO_TYPE_SIGN_ON)  // MTI 0800
        iso.setValue(BuilderConstants.ISO_FIELD_TRANSMISSION_DATE, time, IsoType.NUMERIC, 10) // Fixed-length numeric 10 digits
        iso.setValue(BuilderConstants.ISO_FIELD_STAN, stan, IsoType.NUMERIC, 6) // Fixed-length numeric 6 digits
        iso.setValue(BuilderConstants.ISO_FIELD_ACQUIRER_ID, builderServiceTxnDetails?.procId, IsoType.LLVAR, BuilderConstants.ISO_FIELD_PROC_ID_LENGTH) // LLVAR length auto-handled
        iso.setValue(BuilderConstants.ISO_FIELD_NET_MGMT_INFO_CODE, BuilderConstants.SIGN_OFF_REQUEST, IsoType.NUMERIC, 3) // Fixed-length numeric
        msg_sec_code?.length?.let { iso.setValue(BuilderConstants.ISO_FIELD_KEY_MGMT_DATA, msg_sec_code, IsoType.ALPHA, it ) }
        iso.setBinaryHeader(false)
        iso.setBinaryFields(false)
        iso.setForceStringEncoding(true)
        iso.setIsoHeader("")
        return iso.writeData()
    }

    fun createHandShakeRequest(builderServiceTxnDetails: BuilderServiceTxnDetails?): ByteArray {
        val stan = getSTAN().toString().padStart(6, '0')
        val time = BuilderUtils.getCurrentDateTime(BuilderConstants.ISO_DATE_FORMAT)
        val msg_sec_code = builderServiceTxnDetails?.procId?.drop(3)
        val iso = IsoMessage()
        iso.setType(BuilderConstants.ISO_TYPE_SIGN_ON)
        iso.setValue(BuilderConstants.ISO_FIELD_TRANSMISSION_DATE, time, IsoType.NUMERIC, 10) // Fixed-length numeric 10 digits
        iso.setValue(BuilderConstants.ISO_FIELD_STAN, stan, IsoType.NUMERIC, 6) // Fixed-length numeric 6 digits
        iso.setValue(BuilderConstants.ISO_FIELD_ACQUIRER_ID, builderServiceTxnDetails?.procId, IsoType.LLVAR, BuilderConstants.ISO_FIELD_PROC_ID_LENGTH) // LLVAR length auto-handled
        iso.setValue(BuilderConstants.ISO_FIELD_NET_MGMT_INFO_CODE, BuilderConstants.HANDSHAKE_REQUEST, IsoType.NUMERIC, 3) // Fixed-length numeric
        msg_sec_code?.length?.let { iso.setValue(BuilderConstants.ISO_FIELD_KEY_MGMT_DATA, msg_sec_code, IsoType.ALPHA, it ) }
        iso.setBinaryHeader(false)
        iso.setBinaryFields(false)
        iso.setForceStringEncoding(true)
        iso.setIsoHeader("")
        return iso.writeData()
    }

    fun createKeyRequest(builderServiceTxnDetails: BuilderServiceTxnDetails?): ByteArray {
        val time = BuilderUtils.getCurrentDateTime(BuilderConstants.ISO_DATE_FORMAT)
        val stan = getSTAN().toString().padStart(6, '0')
        val msg_sec_code = builderServiceTxnDetails?.procId?.drop(3)
        val iso = IsoMessage()
        iso.setType(BuilderConstants.ISO_TYPE_NTW_RES)  // MTI 0800
        iso.setValue(BuilderConstants.ISO_FIELD_TRANSMISSION_DATE, time, IsoType.NUMERIC, 10) // Fixed-length numeric 10 digits
        iso.setValue(BuilderConstants.ISO_FIELD_STAN, stan, IsoType.NUMERIC, BuilderConstants.ISO_FIELD_STAN_LENGTH) // Fixed-length numeric 6 digits
        iso.setValue(BuilderConstants.ISO_FIELD_ACQUIRER_ID, builderServiceTxnDetails?.procId, IsoType.LLVAR, BuilderConstants.ISO_FIELD_ACQUIRER_ID_LENGTH)
        iso.setValue(BuilderConstants.ISO_FIELD_RESPONSE_CODE, "00", IsoType.NUMERIC, 2)// DE032 Acquirer ID // LLVAR length auto-handled
        iso.setValue(BuilderConstants.ISO_FIELD_NET_MGMT_INFO_CODE, BuilderConstants.KEY_CHANGE, IsoType.NUMERIC, BuilderConstants.ISO_FIELD_NET_MGMT_INFO_CODE_LENGTH) // Fixed-length numeric
        msg_sec_code?.length?.let { iso.setValue(BuilderConstants.ISO_FIELD_KEY_MGMT_DATA, msg_sec_code, IsoType.ALPHA, it ) }
        iso.setBinaryHeader(false)
        iso.setBinaryFields(false)
        iso.setForceStringEncoding(true)
        iso.setIsoHeader("")
        return iso.writeData()
    }

    fun createKeyChangeRequest(builderServiceTxnDetails: BuilderServiceTxnDetails?): ByteArray {
        val stan = getSTAN().toString().padStart(6, '0')
        val time = BuilderUtils.getCurrentDateTime(BuilderConstants.ISO_DATE_FORMAT)
        val msg_sec_code = builderServiceTxnDetails?.procId?.drop(3)
        val iso = IsoMessage()
        iso.setType(BuilderConstants.ISO_TYPE_SIGN_ON)  // MTI 0800
        iso.setValue(BuilderConstants.ISO_FIELD_TRANSMISSION_DATE, time, IsoType.NUMERIC, 10) // Fixed-length numeric 10 digits
        iso.setValue(BuilderConstants.ISO_FIELD_STAN, stan, IsoType.NUMERIC, 6) // Fixed-length numeric 6 digits
        iso.setValue(BuilderConstants.ISO_FIELD_ACQUIRER_ID, builderServiceTxnDetails?.procId, IsoType.LLVAR, BuilderConstants.ISO_FIELD_PROC_ID_LENGTH) // LLVAR length auto-handled
        iso.setValue(BuilderConstants.ISO_FIELD_NET_MGMT_INFO_CODE, BuilderConstants.KEY_CHANGE_REQUEST, IsoType.NUMERIC, 3) // Fixed-length numeric
        msg_sec_code?.length?.let { iso.setValue(BuilderConstants.ISO_FIELD_KEY_MGMT_DATA, msg_sec_code, IsoType.ALPHA, it ) }
        iso.setBinaryHeader(false)
        iso.setBinaryFields(false)
        iso.setForceStringEncoding(true)
        iso.setIsoHeader("")
        return iso.writeData()
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun voucherSettlement(builderServiceTxnDetails: BuilderServiceTxnDetails?): ByteArray {
        this.builderServiceTxnDetails = builderServiceTxnDetails?: BuilderServiceTxnDetails()
        val amount = this.builderServiceTxnDetails.ttlAmount?.toDoubleOrNull()?.toCurrencyLong() ?: 0
        val posEntryMode = getIsoPosEntryMode()
        val encryptedTrack2Data = getEncryptedTrack2Data()
        val stan = getSTAN()
        val iso = IsoMessage()
        val dateTime = BuilderUtils.getCurrentDateTime(BuilderConstants.DEFAULT_ISO8583_TIME_FORMAT)
        val maskedPan = getMaskedPAN()
        var localTime = BuilderUtils.getLocalTime()
        val localDate = BuilderUtils.getLocalDate()
        val currencyCode = getCurrencyCode()
        val processingCode = getProcessingCode(this.builderServiceTxnDetails.txnType)
        val rrn = generateRRN(stan.toString())
        val originalData = dateTime.padEnd(20,'0')
        val posConditionCode = getNationalPosConditionCode()
        iso.setType(BuilderConstants.MTI_FINANCIAL_REQ)   // Financial transaction request
        iso.setValue(BuilderConstants.ISO_FIELD_PAN_NO, maskedPan, IsoType.LLVAR, BuilderConstants.ISO_FIELD_PAN_NO_LENGTH)          // DE002 PAN
        iso.setValue(BuilderConstants.ISO_FIELD_PROCESSING_CODE, processingCode, IsoType.NUMERIC, BuilderConstants.ISO_FIELD_PROCESSING_CODE_LENGTH)          // DE003 Processing Code
        iso.setValue(BuilderConstants.ISO_FIELD_AMOUNT, amount, IsoType.NUMERIC, BuilderConstants.ISO_FIELD_AMOUNT_LENGTH)            // DE004 Amount
        iso.setValue(BuilderConstants.ISO_FIELD_TRANSMISSION_DATE, dateTime, IsoType.NUMERIC, BuilderConstants.ISO_FIELD_TRANSMISSION_DATE_LENGTH)              // DE007 Transmission DateTime
        iso.setValue(BuilderConstants.ISO_FIELD_STAN, stan, IsoType.NUMERIC, BuilderConstants.ISO_FIELD_STAN_LENGTH)                  // DE011 STAN
        iso.setValue(BuilderConstants.ISO_FIELD_LOC_TIME, localTime, IsoType.NUMERIC, BuilderConstants.ISO_FIELD_LOC_TIME_LENGTH)                  // DE012 Local Time
        iso.setValue(BuilderConstants.ISO_FIELD_LOC_DATE, localDate, IsoType.NUMERIC, BuilderConstants.ISO_FIELD_LOC_DATE_LENGTH)                    // DE013 Local Date
        iso.setValue(BuilderConstants.ISO_FIELD_SET_DATE, localDate, IsoType.NUMERIC, 4)                    // DE015 Settlement Date
        iso.setValue(BuilderConstants.ISO_FIELD_CAP_DATE, localDate, IsoType.NUMERIC, 4)                    // DE017 Capture Date
        iso.setValue(BuilderConstants.ISO_FIELD_MERCHANT_TYPE, builderServiceTxnDetails?.merchantType, IsoType.NUMERIC, BuilderConstants.ISO_FIELD_MERCHANT_TYPE_LENGTH)                    // DE018 Merchant Type
        iso.setValue(BuilderConstants.ISO_FIELD_ENTRY_MODE, posEntryMode, IsoType.NUMERIC, BuilderConstants.ISO_FIELD_ENTRY_MODE_LENGTH)                     // DE022 POS Entry Mode
        iso.setValue(BuilderConstants.ISO_FIELD_ACQUIRER_ID, builderServiceTxnDetails?.procId, IsoType.LLVAR, BuilderConstants.ISO_FIELD_ACQUIRER_ID_LENGTH)              // DE032 Acquirer ID
        iso.setValue(BuilderConstants.ISO_FIELD_RRN, rrn, IsoType.ALPHA, BuilderConstants.ISO_FIELD_RRN_LENGTH)
        iso.setValue(BuilderConstants.ISO_FIELD_AUTH_ID, builderServiceTxnDetails?.hostAuthCode, IsoType.NUMERIC, BuilderConstants.ISO_FIELD_AUTH_ID_LENGTH)
        iso.setValue(BuilderConstants.ISO_FIELD_TERMINAL_ID, builderServiceTxnDetails?.terminalId, IsoType.ALPHA, BuilderConstants.ISO_FIELD_TERMINAL_ID_LENGTH)                   // DE041 Terminal ID
        iso.setValue(BuilderConstants.ISO_FIELD_MERCHANT_ID, builderServiceTxnDetails?.merchantId, IsoType.ALPHA, BuilderConstants.ISO_FIELD_MERCHANT_ID_LENGTH)           // DE042 Merchant ID
        iso.setValue(BuilderConstants.ISO_FIELD_MERCHANT_NAME,
            builderServiceTxnDetails?.merchantNameLocation?.padEnd(BuilderConstants.ISO_FIELD_MERCHANT_NAME_LENGTH),
            IsoType.ALPHA,
            BuilderConstants.ISO_FIELD_MERCHANT_NAME_LENGTH
        )
        iso.setValue(BuilderConstants.ISO_FIELD_MERCHANT_BANK, builderServiceTxnDetails?.merchantBankName, IsoType.LLLVAR, BuilderConstants.ISO_FIELD_MERCHANT_BANK_LENGTH)         // DE048
        iso.setValue(BuilderConstants.ISO_FIELD_CURRENCY_CODE, currencyCode, IsoType.NUMERIC, BuilderConstants.ISO_FIELD_CURRENCY_CODE_LENGTH)                      // DE049 Currency
        iso.setValue(BuilderConstants.ISO_FIELD_POS_CONDITION_CODE, posConditionCode, IsoType.LLLVAR, BuilderConstants.ISO_FIELD_POS_CONDITION_CODE_LENGTH)
        val additional_Data = buildString {
            val fns = builderServiceTxnDetails?.fnsNumber.orEmpty()
            val voucherNo = builderServiceTxnDetails?.voucherNumber.orEmpty()
            val lengthFormatted = voucherNo.length.toString().padStart(3, '0')
            append(fns)
            append("VN")
            append(lengthFormatted)
            append(voucherNo)
        }
        iso.setValue(BuilderConstants.ISO_FIELD_ADDITIONAL_DATA,additional_Data, IsoType.LLLVAR, BuilderConstants.ISO_FIELD_ADDITIONAL_DATA_LENGTH)// DE058
        iso.setValue(BuilderConstants.ISO_FIELD_ACQ_TRACE_DATA, originalData, IsoType.LLLVAR, BuilderConstants.ISO_FIELD_ACQ_TRACE_DATA_LENGTH)    // DE127
        iso.setBinaryHeader(false)
        iso.setBinaryFields(false)
        iso.setForceStringEncoding(true)
        iso.setIsoHeader("")

        return iso.writeData()
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun createVoidRequest(builderServiceTxnDetails: BuilderServiceTxnDetails?): ByteArray {
        Log.d("VOID_REQUEST", "builderServiceTxnDetails: ${this.builderServiceTxnDetails}")
        this.builderServiceTxnDetails = builderServiceTxnDetails?: BuilderServiceTxnDetails()
        val amount = this.builderServiceTxnDetails.ttlAmount?.toDoubleOrNull()?.toCurrencyLong() ?: 0
        val dateTime = BuilderUtils.getCurrentDateTime(BuilderConstants.DEFAULT_ISO8583_TIME_FORMAT)
        val lastTxn = IsoMessageBuilder.getLastTxn()
        val posConditionCode = getNationalPosConditionCode()
        val npsGeoData = getNPSGeographicData()
        val iso = IsoMessage()
        iso.setType(BuilderConstants.MTI_REVERSAL_REQ)
        iso.setValue(BuilderConstants.ISO_FIELD_PAN_NO, builderServiceTxnDetails?.cardPan, IsoType.LLVAR, BuilderConstants.ISO_FIELD_PAN_NO_LENGTH)
        iso.setValue(BuilderConstants.ISO_FIELD_PROCESSING_CODE, builderServiceTxnDetails?.processingCode?.padStart(6,'0'), IsoType.NUMERIC, BuilderConstants.ISO_FIELD_PROCESSING_CODE_LENGTH)
        iso.setValue(BuilderConstants.ISO_FIELD_AMOUNT, amount, IsoType.NUMERIC, BuilderConstants.ISO_FIELD_AMOUNT_LENGTH)
        iso.setValue(BuilderConstants.ISO_FIELD_TRANSMISSION_DATE, builderServiceTxnDetails?.originalDateTime, IsoType.NUMERIC, BuilderConstants.ISO_FIELD_TRANSMISSION_DATE_LENGTH)
        iso.setValue(BuilderConstants.ISO_FIELD_STAN,builderServiceTxnDetails?.stan?.padStart(6,'0') , IsoType.NUMERIC, BuilderConstants.ISO_FIELD_STAN_LENGTH)
        iso.setValue(BuilderConstants.ISO_FIELD_LOC_TIME, builderServiceTxnDetails?.localTime, IsoType.NUMERIC, BuilderConstants.ISO_FIELD_LOC_TIME_LENGTH)
        iso.setValue(BuilderConstants.ISO_FIELD_LOC_DATE, builderServiceTxnDetails?.localDate, IsoType.NUMERIC, BuilderConstants.ISO_FIELD_LOC_DATE_LENGTH)
        iso.setValue(BuilderConstants.ISO_FIELD_SET_DATE, builderServiceTxnDetails?.settlementDate, IsoType.NUMERIC, BuilderConstants.ISO_FIELD_LOC_DATE_LENGTH)
        iso.setValue(BuilderConstants.ISO_FIELD_MERCHANT_TYPE, builderServiceTxnDetails?.merchantType, IsoType.NUMERIC, BuilderConstants.ISO_FIELD_MERCHANT_TYPE_LENGTH)
        iso.setValue(BuilderConstants.ISO_FIELD_ENTRY_MODE, builderServiceTxnDetails?.posEntryMode, IsoType.NUMERIC, BuilderConstants.ISO_FIELD_ENTRY_MODE_LENGTH)
        iso.setValue(BuilderConstants.ISO_FIELD_ACQUIRER_ID, builderServiceTxnDetails?.procId, IsoType.LLVAR, BuilderConstants.ISO_FIELD_ACQUIRER_ID_LENGTH)
        iso.setValue(BuilderConstants.ISO_FIELD_RRN, builderServiceTxnDetails?.rrn, IsoType.ALPHA, BuilderConstants.ISO_FIELD_RRN_LENGTH)      // Original RRN
        iso.setValue(BuilderConstants.ISO_FIELD_AUTH_ID, builderServiceTxnDetails?.hostAuthCode, IsoType.ALPHA, BuilderConstants.ISO_FIELD_AUTH_ID_LENGTH)
        iso.setValue(BuilderConstants.ISO_FIELD_RESPONSE_CODE, BuilderConstants.ISO_RESP_CODE_APPROVED, IsoType.ALPHA, BuilderConstants.ISO_FIELD_RESPONSE_CODE_LENGTH)
        iso.setValue(BuilderConstants.ISO_FIELD_TERMINAL_ID, builderServiceTxnDetails?.terminalId, IsoType.ALPHA, BuilderConstants.ISO_FIELD_TERMINAL_ID_LENGTH)
        iso.setValue(BuilderConstants.ISO_FIELD_MERCHANT_ID, builderServiceTxnDetails?.merchantId, IsoType.ALPHA, BuilderConstants.ISO_FIELD_MERCHANT_ID_LENGTH)
        iso.setValue(BuilderConstants.ISO_FIELD_MERCHANT_NAME,
            builderServiceTxnDetails?.merchantNameLocation?.padEnd(BuilderConstants.ISO_FIELD_MERCHANT_NAME_LENGTH),
            IsoType.ALPHA,
            BuilderConstants.ISO_FIELD_MERCHANT_NAME_LENGTH
        )
        iso.setValue(BuilderConstants.ISO_FIELD_MERCHANT_BANK, builderServiceTxnDetails?.merchantBankName, IsoType.LLLVAR, BuilderConstants.ISO_FIELD_MERCHANT_BANK_LENGTH)         // DE048
        iso.setValue(BuilderConstants.ISO_FIELD_CURRENCY_CODE, builderServiceTxnDetails?.currencyCode, IsoType.ALPHA, BuilderConstants.ISO_FIELD_CURRENCY_CODE_LENGTH)
        iso.setValue(BuilderConstants.ISO_FIELD_POS_CONDITION_CODE, posConditionCode, IsoType.LLLVAR, BuilderConstants.ISO_FIELD_POS_CONDITION_CODE_LENGTH)
        iso.setValue(59, npsGeoData, IsoType.LLLVAR, 17)
        iso.setValue(BuilderConstants.ISO_FIELD_RESERVED_PRIVATE, "8018", IsoType.LLLVAR, 6)
        val originalData =
            "0200" +
                    builderServiceTxnDetails?.stan?.padStart(6,'0') +
                    builderServiceTxnDetails?.originalDateTime +
                    builderServiceTxnDetails?.procId?.padStart(11, '0') +
                    "00000000000"   // forwarding ID if not used
        iso.setValue(BuilderConstants.ISO_FIELD_ORIGINAL_DATA, originalData, IsoType.NUMERIC, BuilderConstants.ISO_FIELD_ORIGINAL_DATA_LENGTH)
        iso.setValue(BuilderConstants.ISO_FIELD_ADDITIONAL_DATA, builderServiceTxnDetails?.fnsNumber, IsoType.LLLVAR, BuilderConstants.ISO_FIELD_ADDITIONAL_DATA_LENGTH)// DE058
        iso.setBinaryHeader(false)
        iso.setBinaryFields(false)
        iso.setForceStringEncoding(true)
        iso.setIsoHeader("")

        return iso.writeData()
    }


    fun parseEcoResponse(context: Context, response: ByteArray): BuilderServiceTxnDetails {
        val details = BuilderServiceTxnDetails()
        try {
            val isoStr = String(response, Charsets.US_ASCII)
            val mti = isoStr.take(4)
            Log.d("ISO", "Received MTI: $mti, full message: $isoStr")

            val mf = createMessageFactory(context)
            val isoMsg = mf.parseMessage(response, 0)

            // 🔹 Fill BuilderServiceTxnDetails safely
            details.apply {
                if (isoMsg.hasField(7))   dateTime    = isoMsg.getObjectValue<String>(7)
                if (isoMsg.hasField(11))  stan        = isoMsg.getObjectValue<String>(11)
                if (isoMsg.hasField(41))  terminalId  = isoMsg.getObjectValue<String>(41)
                if (isoMsg.hasField(70))  hostTxnRef  = isoMsg.getObjectValue<String>(70)
                if (isoMsg.hasField(96))  deviceSN    = isoMsg.getObjectValue<String>(96)
                if (isoMsg.hasField(125)) workKey     = isoMsg.getObjectValue<String>(125) // 36-char key
            }


        } catch (e: Exception) {
            Log.e("ISO", "Failed to parse Echo response", e)
        }

        return details
    }



    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun createFinancial0200Request(builderServiceTxnDetails: BuilderServiceTxnDetails?): ByteArray {
        this.builderServiceTxnDetails = builderServiceTxnDetails?: BuilderServiceTxnDetails()
        val amount = this.builderServiceTxnDetails.ttlAmount?.toDoubleOrNull()?.toCurrencyLong() ?: 0
        val pinBlock = getPinBlock()
        val maskedPan = getMaskedPAN()
        val iccData = getIccData()
        val rearrangedIccData = iccData?.let { rearrangeTLV(it) }
        val cardSeqNumber = getCardSeqNum()
        val encryptedTrack2Data = getEncryptedTrack2Data()
        builderServiceTxnDetails?.cashback = cashbackAmount((this.builderServiceTxnDetails.cashback?.toDoubleOrNull()?.toCurrencyLong() ?: 0))
        builderServiceTxnDetails?.posEntryMode = getIsoPosEntryMode()
        builderServiceTxnDetails?.stan = getSTAN().toString()
        builderServiceTxnDetails?.dateTime = BuilderUtils.getCurrentDateTime(BuilderConstants.DEFAULT_ISO8583_TIME_FORMAT)
        builderServiceTxnDetails?.localTime = BuilderUtils.getLocalTime()
        builderServiceTxnDetails?.localDate = BuilderUtils.getLocalDate()
        builderServiceTxnDetails?.currencyCode = getCurrencyCode()
        builderServiceTxnDetails?.processingCode = getProcessingCode(this.builderServiceTxnDetails.txnType)
        builderServiceTxnDetails?.rrn = generateRRN(this.builderServiceTxnDetails.stan!!)
        val originalData = builderServiceTxnDetails?.dateTime?.padEnd(20,'0')
        builderServiceTxnDetails?.posConditionCode = getNationalPosConditionCode()
        val iso = IsoMessage()
        iso.setType(BuilderConstants.MTI_FINANCIAL_REQ)
        iso.setValue(BuilderConstants.ISO_FIELD_PAN_NO, maskedPan, IsoType.LLVAR, BuilderConstants.ISO_FIELD_PAN_NO_LENGTH)
        iso.setValue(BuilderConstants.ISO_FIELD_PROCESSING_CODE, builderServiceTxnDetails?.processingCode, IsoType.NUMERIC, BuilderConstants.ISO_FIELD_PROCESSING_CODE_LENGTH)
        iso.setValue(BuilderConstants.ISO_FIELD_AMOUNT, amount, IsoType.NUMERIC, BuilderConstants.ISO_FIELD_AMOUNT_LENGTH)
        iso.setValue(BuilderConstants.ISO_FIELD_TRANSMISSION_DATE, builderServiceTxnDetails?.dateTime, IsoType.NUMERIC, BuilderConstants.ISO_FIELD_TRANSMISSION_DATE_LENGTH)
        iso.setValue(BuilderConstants.ISO_FIELD_STAN, builderServiceTxnDetails?.stan, IsoType.NUMERIC, BuilderConstants.ISO_FIELD_STAN_LENGTH)
        iso.setValue(BuilderConstants.ISO_FIELD_LOC_TIME, builderServiceTxnDetails?.localTime, IsoType.NUMERIC, BuilderConstants.ISO_FIELD_LOC_TIME_LENGTH)
        iso.setValue(BuilderConstants.ISO_FIELD_LOC_DATE, builderServiceTxnDetails?.localDate, IsoType.NUMERIC, BuilderConstants.ISO_FIELD_LOC_DATE_LENGTH)
        iso.setValue(BuilderConstants.ISO_FIELD_SET_DATE, builderServiceTxnDetails?.localDate, IsoType.NUMERIC, 4)
        iso.setValue(BuilderConstants.ISO_FIELD_CAP_DATE, builderServiceTxnDetails?.localDate, IsoType.NUMERIC, 4)
        iso.setValue(BuilderConstants.ISO_FIELD_MERCHANT_TYPE, builderServiceTxnDetails?.merchantType, IsoType.NUMERIC, BuilderConstants.ISO_FIELD_MERCHANT_TYPE_LENGTH)                    // DE018 Merchant Type
        iso.setValue(BuilderConstants.ISO_FIELD_ENTRY_MODE, builderServiceTxnDetails?.posEntryMode, IsoType.NUMERIC, BuilderConstants.ISO_FIELD_ENTRY_MODE_LENGTH)                     // DE022 POS Entry Mode
        if (builderServiceTxnDetails?.cardEntryMode == CardEntryMode.CONTACT.toString())
        {
            iso.setValue(BuilderConstants.ISO_FIELD_PAN_SEQ_NO, "000", IsoType.NUMERIC, BuilderConstants.ISO_FIELD_PAN_SEQ_NO_LENGTH)
        }
        iso.setValue(BuilderConstants.ISO_FIELD_ACQUIRER_ID, builderServiceTxnDetails?.procId, IsoType.LLVAR, BuilderConstants.ISO_FIELD_ACQUIRER_ID_LENGTH)              // DE032 Acquirer ID
        if(builderServiceTxnDetails?.cardEntryMode != CardEntryMode.MANUAL.toString() ) {
            iso.setValue(
                BuilderConstants.ISO_FIELD_TRACK2_DATA,
                encryptedTrack2Data,
                IsoType.LLVAR,
                BuilderConstants.ISO_FIELD_TRACK2_DATA_LENGTH
            )
        }
        iso.setValue(BuilderConstants.ISO_FIELD_RRN, builderServiceTxnDetails?.rrn, IsoType.ALPHA, BuilderConstants.ISO_FIELD_RRN_LENGTH)              // DE037 RRN
        iso.setValue(BuilderConstants.ISO_FIELD_TERMINAL_ID, builderServiceTxnDetails?.terminalId, IsoType.ALPHA, BuilderConstants.ISO_FIELD_TERMINAL_ID_LENGTH)                   // DE041 Terminal ID
        iso.setValue(BuilderConstants.ISO_FIELD_MERCHANT_ID, builderServiceTxnDetails?.merchantId, IsoType.ALPHA, BuilderConstants.ISO_FIELD_MERCHANT_ID_LENGTH)           // DE042 Merchant ID
        iso.setValue(BuilderConstants.ISO_FIELD_MERCHANT_NAME,
            builderServiceTxnDetails?.merchantNameLocation?.padEnd(BuilderConstants.ISO_FIELD_MERCHANT_NAME_LENGTH),
            IsoType.ALPHA,
            BuilderConstants.ISO_FIELD_MERCHANT_NAME_LENGTH
        )
        iso.setValue(BuilderConstants.ISO_FIELD_MERCHANT_BANK, builderServiceTxnDetails?.merchantBankName, IsoType.LLLVAR, BuilderConstants.ISO_FIELD_MERCHANT_BANK_LENGTH)         // DE048
        iso.setValue(BuilderConstants.ISO_FIELD_CURRENCY_CODE, builderServiceTxnDetails?.currencyCode, IsoType.NUMERIC, BuilderConstants.ISO_FIELD_CURRENCY_CODE_LENGTH)                      // DE049 Currency
        iso.setValue(BuilderConstants.ISO_FIELD_PIN_BLOCK, pinBlock, IsoType.ALPHA, BuilderConstants.ISO_FIELD_PIN_BLOCK_LENGTH)
        if(this.builderServiceTxnDetails.txnType ==  TxnType.PURCHASE_CASHBACK.toString())
        {
            iso.setValue(BuilderConstants.ISO_FIELD_ADD_AMOUNT, builderServiceTxnDetails?.cashback, IsoType.LLLVAR, builderServiceTxnDetails?.cashback!!.length)
        }

        if (builderServiceTxnDetails?.cardEntryMode == CardEntryMode.CONTACT.toString() ||
            builderServiceTxnDetails?.cardEntryMode == CardEntryMode.CONTACLESS.toString()) {

            if (!iccData.isNullOrEmpty() && !rearrangedIccData.isNullOrEmpty()) {
                Log.d("DE55", "===== INPUT =====")
                Log.d("DE55", rearrangedIccData)

                val cleanHex = cleanHex(rearrangedIccData)

                Log.d("DE55", "===== CLEAN HEX =====")
                Log.d("DE55", cleanHex)
                Log.d("DE55", "Char Length: ${cleanHex.length}")
                Log.d("DE55", "Byte Length: ${cleanHex.length / 2}")

                val byteArray = hexStringToByteArray(cleanHex)

                Log.d("DE55", "===== BYTE ARRAY =====")
                Log.d("DE55", "Length: ${byteArray.size}")
                Log.d("DE55", byteArray.joinToString("") { "%02X".format(it) })

                val lll = "%03d".format(byteArray.size)

                Log.d("DE55", "===== LLLVAR =====")
                Log.d("DE55", "Prefix: $lll")

                Log.d("DE55", "===== VALIDATION =====")
                Log.d("DE55", "LLL matches: ${lll.toInt() == byteArray.size}")

                Log.d("DE55", "===== TAG CHECK =====")
                checkTag(cleanHex, "9F34", "03", "020002", "CVM Result")
                checkTag(cleanHex, "9F33", "03", "E0F8C8", "Terminal Capability")
                iso.setValue(
                    BuilderConstants.ISO_FIELD_ICC_DATA,
                    byteArray,
                    IsoType.LLLBIN,
                    byteArray.size
                )
            }
        }

        iso.setValue(BuilderConstants.ISO_FIELD_POS_CONDITION_CODE, builderServiceTxnDetails?.posConditionCode , IsoType.LLLVAR, BuilderConstants.ISO_FIELD_POS_CONDITION_CODE_LENGTH)
        iso.setValue(BuilderConstants.ISO_FIELD_ADDITIONAL_DATA, builderServiceTxnDetails?.fnsNumber, IsoType.LLLVAR, BuilderConstants.ISO_FIELD_ADDITIONAL_DATA_LENGTH)// DE058
        iso.setValue(BuilderConstants.ISO_FIELD_ACQ_TRACE_DATA, originalData, IsoType.LLLVAR, BuilderConstants.ISO_FIELD_ACQ_TRACE_DATA_LENGTH)    // DE127
        iso.setBinaryHeader(false)
        iso.setBinaryFields(true)
        iso.setForceStringEncoding(false)
        iso.setIsoHeader("")
        updateTransResult(this.builderServiceTxnDetails)
        val savedTxn = SavedTxnData(
            stan = builderServiceTxnDetails?.stan,
            rrn = builderServiceTxnDetails?.rrn,
            amount = amount.toString(),
            ttlAmount = this.builderServiceTxnDetails.ttlAmount,
            procId = this.builderServiceTxnDetails.procId,
            processingCode = builderServiceTxnDetails?.processingCode,
            transmissionDateTime = builderServiceTxnDetails?.dateTime,
            localTime = builderServiceTxnDetails?.localTime,
            localDate = builderServiceTxnDetails?.localDate,
            terminalId = builderServiceTxnDetails?.terminalId,
            merchantId = builderServiceTxnDetails?.merchantId,
            merchantName = builderServiceTxnDetails?.merchantNameLocation,
            merchantBank = builderServiceTxnDetails?.merchantBankName,
            merchantType = builderServiceTxnDetails?.merchantType,
            currencyCode = builderServiceTxnDetails?.currencyCode,
            pan = maskedPan,   // always store masked PAN
            track2Data = encryptedTrack2Data,
            entryMode = builderServiceTxnDetails?.posEntryMode,
            posConditionCode = builderServiceTxnDetails?.posConditionCode,
            acquirerId = builderServiceTxnDetails?.procId,
            additionalData = builderServiceTxnDetails?.fnsNumber,
            originalData = originalData
        )
        IsoMessageBuilder.saveTxn(savedTxn)

        return iso.writeData()

    }


    fun parseNetworkManResponse(context: Context, response: ByteArray): BuilderServiceTxnDetails {
        val details = BuilderServiceTxnDetails()
        try {
            val mf = createMessageFactory(context)
            val isoMsg = mf.parseMessage(response, 0)


            details.apply {
                dateTime     = isoMsg.getObjectValue<String>(BuilderConstants.ISO_FIELD_TRANSMISSION_DATE)
                stan         = isoMsg.getObjectValue<String>(BuilderConstants.ISO_FIELD_STAN)
                merchantId   = isoMsg.getObjectValue<String>(BuilderConstants.ISO_FIELD_ACQUIRER_ID)
                hostRespCode = isoMsg.getObjectValue<String>(BuilderConstants.ISO_FIELD_RESPONSE_CODE)
                hostTxnRef   = isoMsg.getObjectValue<String>(BuilderConstants.ISO_FIELD_NET_MGMT_INFO_CODE)
                deviceSN     = isoMsg.getObjectValue<String>(BuilderConstants.ISO_FIELD_KEY_MGMT_DATA)
                workKey     = isoMsg.getObjectValue<String>(BuilderConstants.ISO_FIELD_KEY_DATA)
                deviceModel     = isoMsg.getObjectValue<String>(BuilderConstants.ISO_FIELD_RESPONSE_TEXT)
            }


        } catch (e: Exception) {
            Log.e("ISO", "Parse error", e)
        }

        return details
    }


    fun parsePurchaseResponse123(context: Context, response: ByteArray): BuilderServiceTxnDetails {
        val details = BuilderServiceTxnDetails()

        fun formatIsoField(obj: Any?, pattern: String = "MMddHHmmss"): String {
            return when (obj) {
                null -> ""
                is String -> obj
                is Date -> SimpleDateFormat(pattern, Locale.getDefault()).format(obj)
                else -> obj.toString()
            }
        }

        try {
            val mf = createMessageFactory(context)
            val isoMsg = mf.parseMessage(response, 0)

            details.apply {
                cardPan        = isoMsg.getObjectValue<String>(BuilderConstants.ISO_FIELD_PAN_NO)
                processingCode = isoMsg.getObjectValue<String>(BuilderConstants.ISO_FIELD_PROCESSING_CODE)
                authAmount     = isoMsg.getObjectValue<String>(BuilderConstants.ISO_FIELD_AMOUNT)
                dateTime       = formatIsoField(isoMsg.getObjectValue<Any>(BuilderConstants.ISO_FIELD_TRANSMISSION_DATE), "MMddHHmmss")
                stan           = isoMsg.getObjectValue<String>(BuilderConstants.ISO_FIELD_STAN)
                localTime      = formatIsoField(isoMsg.getObjectValue<Any>(BuilderConstants.ISO_FIELD_LOC_TIME), "HHmmss")
                localDate      = formatIsoField(isoMsg.getObjectValue<Any>(BuilderConstants.ISO_FIELD_LOC_DATE), "MMdd")
                expiryDate     = formatIsoField(isoMsg.getObjectValue<Any>(BuilderConstants.ISO_FIELD_EXPIRY_DATE), "yyMM")
                settlementDate = formatIsoField(isoMsg.getObjectValue<Any>(BuilderConstants.ISO_FIELD_SET_DATE), "MMdd")
                captureDate    = formatIsoField(isoMsg.getObjectValue<Any>(BuilderConstants.ISO_FIELD_CAP_DATE), "MMdd")
                merchantType   = isoMsg.getObjectValue<String>(BuilderConstants.ISO_FIELD_MERCHANT_TYPE)
                posEntryMode   = isoMsg.getObjectValue<String>(BuilderConstants.ISO_FIELD_ENTRY_MODE)
                acquirerId     = isoMsg.getObjectValue<String>(BuilderConstants.ISO_FIELD_ACQUIRER_ID)
                track2Data     = isoMsg.getObjectValue<String>(BuilderConstants.ISO_FIELD_TRACK2_DATA)
                rrn            = isoMsg.getObjectValue<String>(BuilderConstants.ISO_FIELD_RRN)
                hostAuthCode         = isoMsg.getObjectValue<String>(BuilderConstants.ISO_FIELD_AUTH_ID)
                hostRespCode   = isoMsg.getObjectValue<String>(BuilderConstants.ISO_FIELD_RESPONSE_CODE)
                terminalId     = isoMsg.getObjectValue<String>(BuilderConstants.ISO_FIELD_TERMINAL_ID)
                merchantId     = isoMsg.getObjectValue<String>(BuilderConstants.ISO_FIELD_MERCHANT_ID)
                merchantName   = isoMsg.getObjectValue<String>(BuilderConstants.ISO_FIELD_MERCHANT_NAME)
                merchantBank   = isoMsg.getObjectValue<String>(BuilderConstants.ISO_FIELD_MERCHANT_BANK)
                currencyCode   = isoMsg.getObjectValue<String>(BuilderConstants.ISO_FIELD_CURRENCY_CODE)
                additionalAmt  = isoMsg.getObjectValue<String>(BuilderConstants.ISO_FIELD_ADD_AMOUNT)
                posCondition   = isoMsg.getObjectValue<String>(BuilderConstants.ISO_FIELD_POS_CONDITION_CODE)
                privateData    = isoMsg.getObjectValue<String>(BuilderConstants.ISO_FIELD_ADDITIONAL_DATA)
                hostResMessage  = isoMsg.getObjectValue<String>(BuilderConstants.ISO_FIELD_RESPONSE_TEXT)
            }

        } catch (e: Exception) {
            Log.e("ISO", "Parse error", e)
        }

        return details
    }


    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun updateTransResult(builderServiceTxnDetails: BuilderServiceTxnDetails?) {

        if (builderServiceTxnDetails == null) {
            Log.e("TXN_DEBUG", "builderServiceTxnDetails is NULL")
            return
        }

        val id = builderServiceTxnDetails.id

        if (id == null) {
            Log.e("TXN_DEBUG", "Txn ID is NULL")
            return
        }

        val txn = dbRepository.fetchTxnById(id)

        if (txn == null) {
            Log.e("TXN_DEBUG", "No txn found for ID: $id")
            return
        }
        // ✅ Set values
        txn.posEntryMode = builderServiceTxnDetails.posEntryMode
        txn.stan = builderServiceTxnDetails.stan
        //txn.originalDateTime = builderServiceTxnDetails.dateTime
        txn.rrn = builderServiceTxnDetails.rrn
        txn.processingCode = builderServiceTxnDetails.processingCode
        txn.localTime = builderServiceTxnDetails.localTime
        txn.localDate = builderServiceTxnDetails.localDate
        txn.currencyCode = builderServiceTxnDetails.currencyCode

        dbRepository.updateTxn(txn)
        // 🔍 Fetch again to verify persistence
        val updatedTxn = dbRepository.fetchTxnById(id)
    }

}