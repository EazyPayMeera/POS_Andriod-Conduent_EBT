package com.analogics.builder_core.builder

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.analogics.builder_core.data.constants.BuilderConstants
import com.analogics.builder_core.data.model.SavedTxnData
import com.analogics.builder_core.data.model.BuilderServiceTxnDetails
import com.analogics.builder_core.data.model.CardEntryMode
import com.analogics.builder_core.utils.BuilderUtils
import com.analogics.builder_core.utils.toCurrencyLong
import com.analogics.securityframework.data.repository.TxnDBRepository
import com.analogics.securityframework.data.model.TxnType
import com.solab.iso8583.IsoMessage
import com.solab.iso8583.IsoType
import com.solab.iso8583.MessageFactory
import com.solab.iso8583.parse.ConfigParser
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.ByteArrayOutputStream
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class ApiRequestBuilder@Inject constructor(@ApplicationContext val context: Context, var dbRepository: TxnDBRepository) {
    val messageFactory = MessageFactory<IsoMessage>()
    var builderServiceTxnDetails = BuilderServiceTxnDetails()
    lateinit var message : IsoMessage

    /**
     * Initializes ISO configuration when class is created.
     */
    init {
        setIsoConfig()
    }

    /**
     * Sets ISO8583 configuration like config path and message format.
     * Enables binary messages and headers.
     */
    fun setIsoConfig()
    {
        messageFactory.setConfigPath(BuilderConstants.ISO_CONFIG_PATH)
        messageFactory.useBinaryMessages = true
        messageFactory.isBinaryHeader = true
    }

    /**
     * Returns POS Entry Mode (DE22) based on transaction and card type.
     */
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

    /**
     * Builds cashback amount field in ISO format (used in EBT).
     */
    fun cashbackAmount(cashbackAmt: Long): String {
        Log.d("EBT", "cashbackAmount() input: $cashbackAmt")
        val accountType = BuilderConstants.DEFAULT_ACCOUNT_TYPE
        val amountType = "02"  // Fix: Available Balance (was DEFAULT_AMOUNT_TYPE = "40")
        val currency = BuilderConstants.DEFAULT_ISO8583_CURRENCY_CODE
        val sign = BuilderConstants.DEFAULT_AMOUNT_SIGN
        val amt = cashbackAmt
            .toString()
            .padStart(12, '0')
        return accountType + amountType + currency + sign + amt
    }

    /**
     * Returns masked PAN (Primary Account Number).
     */
    fun getMaskedPAN() : String?
    {
        var pan : String? =null
        builderServiceTxnDetails.cardMaskedPan?.let {
            pan = it
        }
        return pan
    }

    /**
     * Returns encrypted Track2 data.
     */
    fun getEncryptedTrack2Data() : String?
    {
        var trackData : String? =null
        builderServiceTxnDetails.trackData?.let {
            trackData = it
        }
        return trackData
    }


    /**
     * Returns formatted currency code (DE49).
     */
    fun getCurrencyCode() : String?
    {
        var currencyCode: String? =
            builderServiceTxnDetails.txnCurrencyCode?.toInt()?.toString()?: BuilderConstants.DEFAULT_ISO8583_CURRENCY_CODE
        currencyCode?.padStart(BuilderConstants.ISO_FIELD_CURRENCY_CODE_LEN, '0')?.let {
            currencyCode = it
        }
        return currencyCode
    }

    /**
     * Returns processing code (DE3) based on transaction type.
     */
    fun getProcessingCode(txnType: String?): String {
        return when {
            builderServiceTxnDetails.txnType == TxnType.E_VOUCHER.toString()
                    && builderServiceTxnDetails.isReturn == true -> "200098"
            txnType == TxnType.CASH_PURCHASE.toString() -> "009600"
            txnType == TxnType.FOOD_PURCHASE.toString() -> "009800"
            txnType == TxnType.PURCHASE_CASHBACK.toString() -> "099600"
            txnType == TxnType.FOODSTAMP_RETURN.toString() -> "200098"
            txnType == TxnType.BALANCE_ENQUIRY_CASH.toString() -> "310000"
            txnType == TxnType.BALANCE_ENQUIRY_SNAP.toString() -> "310000"
            txnType == TxnType.VOID_LAST.toString() -> "0000"
            txnType == TxnType.E_VOUCHER.toString() -> "009800"
            txnType == TxnType.CASH_WITHDRAWAL.toString() -> "019600"
            else -> "0000"
        }
    }

    /**
     * Returns PIN block padded as per ISO format.
     */
    fun getPinBlock() : String?
    {
        var pinBlock: String? =
            builderServiceTxnDetails.pinBlock
        pinBlock?.padEnd(BuilderConstants.ISO_FIELD_PAN_SEQ_NO_LENGTH, 'F')?.let {
            pinBlock = it
        }
        return pinBlock
    }

    /**
     * Returns card sequence number (DE23).
     */
    fun getCardSeqNum(): String {
        val seq = builderServiceTxnDetails.cardSeqNum
        val result = if (seq.isNullOrBlank()) {
            "000"
        } else {
            seq.toIntOrNull()?.toString()
                ?.padStart(BuilderConstants.ISO_FIELD_PAN_SEQ_NO_LENGTH, '0')
                ?: "000"
        }
        return result
    }

    /**
     * Converts HEX string to ByteArray.
     */
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

    /**
     * Cleans HEX string by removing invalid characters and fixing length.
     */
    fun cleanHex(input: String): String {
        val clean = input
            .replace("\\s+".toRegex(), "")
            .replace(Regex("[^0-9A-Fa-f]"), "")
        require(clean.isNotEmpty()) { "Empty HEX string after cleaning" }
        val padded = if (clean.length % 2 != 0) {
            Log.w("cleanHex", "Odd length HEX, padding with leading zero: ${clean.length}")
            "0$clean"
        } else {
            clean
        }
        return padded.uppercase()
    }

    /**
     * Validates a TLV tag value inside HEX data.
     */
    fun checkTag(hex: String, tag: String, lenHex: String, expected: String, label: String) {
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

    /**
     * Returns ICC (EMV) data (DE55).
     */
    fun getIccData() : String?
    {
        //Log.d("ICC_DATA", "emvData raw: ${builderServiceTxnDetails.emvData}")
        var iccData : String? =null
        builderServiceTxnDetails.emvData?.let {
            iccData = it
        }
        return iccData
    }

    /**
     * Generates or reuses STAN (DE11).
     */
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

    /**
     * Returns National POS Condition Code.
     */
    fun getNationalPosConditionCode(): String {
        val terminalClass = "000"      // as per your current config
        val presentationType = "0000"  // customer + card present
        val securityCondition = "0"
        val terminalType = "01"        // fixed as per spec

        val terminalCapability = when (builderServiceTxnDetails.cardEntryMode) {
            CardEntryMode.CONTACT.toString() -> "5"        // Chip
            CardEntryMode.MAGSTRIPE.toString() -> "2"      // Magstripe
            CardEntryMode.MANUAL.toString() -> "6"         // Manual entry
            CardEntryMode.CONTACLESS.toString() -> "5"     // Contactless
            else -> "0"
        }

        return terminalClass +
                presentationType +
                securityCondition +
                terminalType +
                terminalCapability
    }

    /**
     * Builds NPS Geographic Data string.
     */
    fun getNPSGeographicData(): String {
        val stateCode = builderServiceTxnDetails.stateCode?.padStart(2, '0') ?: "00"
        val countyCode = builderServiceTxnDetails.countyCode?.padStart(3, '0') ?: "000"
        val postalServiceCode = builderServiceTxnDetails.postalServiceCode?.padStart(9, '0') ?: "00000"
        val countryCode = builderServiceTxnDetails.currencyCode?.padStart(3, '0') ?: "000"
        val npsGeoData = stateCode + countyCode + postalServiceCode + countryCode
        return npsGeoData
    }

    /**
     * Generates Retrieval Reference Number (RRN - DE37).
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun generateRRN(stan: String): String {
        val now = LocalDateTime.now()
        val yearLastDigit = now.year.toString().last()           // 1 digit
        val dayOfYear = "%03d".format(now.dayOfYear)           // 3 digits
        val hour = "%02d".format(now.hour)                     // 2 digits
        val minute = "%02d".format(now.minute)                 // 2 digits
        val stanPart = stan.takeLast(4).padStart(4, '0')       // 4 digits from STAN

        return "$yearLastDigit$dayOfYear$hour$minute$stanPart" // 1 + 3 + 2 + 2 + 4 = 12 digits
    }

    /**
     * Creates and configures an ISO8583 MessageFactory instance.
     *
     * Responsibilities:
     * - Loads ISO configuration from `iso_config.xml` in assets
     * - Applies field definitions using ConfigParser
     * - Sets message format and encoding options
     *
     * Configuration Details:
     * - Bitmap: ASCII (not binary)
     * - Character Encoding: ASCII
     *
     * @param context Used to access assets folder for ISO config file
     * @return Configured MessageFactory instance for ISO message creation
     *
     * Note:
     * - Ensure `iso_config.xml` is present in assets directory
     * - This factory is used for parsing and building ISO8583 messages
     */
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

    /**
     * Creates ISO8583 Sign-On request message (MTI 0800).
     *
     * Fields Used:
     * - DE7  : Transmission Date & Time
     * - DE11 : STAN
     * - DE32 : Acquirer ID
     * - DE70 : Network Management Code (Sign-On)
     * - DE96 : Key Management Data (optional)
     *
     * @param builderServiceTxnDetails Transaction details containing procId
     * @return ByteArray ISO message ready for transmission
     */
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

    /**
     * Creates ISO8583 Sign-Off request message (MTI 0800).
     *
     * Fields Used:
     * - DE7  : Transmission Date & Time
     * - DE11 : STAN
     * - DE32 : Acquirer ID
     * - DE70 : Network Management Code (Sign-Off)
     * - DE96 : Key Management Data (optional)
     *
     * @param builderServiceTxnDetails Transaction details containing procId
     * @return ByteArray ISO message
     */
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

    /**
     * Creates ISO8583 Handshake request message (MTI 0800).
     *
     * Used to establish communication with host before transactions.
     *
     * Fields Used:
     * - DE7  : Transmission Date & Time
     * - DE11 : STAN
     * - DE32 : Acquirer ID
     * - DE70 : Network Management Code (Handshake)
     * - DE96 : Key Management Data
     *
     * @param builderServiceTxnDetails Transaction details
     * @return ByteArray ISO message
     */
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

    /**
     * Creates ISO8583 Key Request message.
     *
     * Used for requesting encryption keys from host.
     *
     * Fields Used:
     * - DE7  : Transmission Date & Time
     * - DE11 : STAN
     * - DE32 : Acquirer ID
     * - DE39 : Response Code (default "00")
     * - DE70 : Network Management Code (Key Change)
     * - DE96 : Key Management Data
     *
     * @param builderServiceTxnDetails Transaction details
     * @return ByteArray ISO message
     */
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

    /**
     * Creates ISO8583 Key Change request message.
     *
     * Used to trigger key rotation/update with host.
     *
     * Fields Used:
     * - DE7  : Transmission Date & Time
     * - DE11 : STAN
     * - DE32 : Acquirer ID
     * - DE70 : Network Management Code (Key Change Request)
     * - DE96 : Key Management Data
     *
     * @param builderServiceTxnDetails Transaction details
     * @return ByteArray ISO message
     */
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

    /**
     * Creates ISO8583 Voucher Settlement (Financial Transaction) request.
     *
     * MTI: 0200 (Financial Request)
     *
     * Fields Used:
     * - DE2   : PAN (masked)
     * - DE3   : Processing Code
     * - DE4   : Transaction Amount
     * - DE7   : Transmission Date & Time
     * - DE11  : STAN
     * - DE12  : Local Time
     * - DE13  : Local Date
     * - DE15  : Settlement Date
     * - DE17  : Capture Date
     * - DE18  : Merchant Type
     * - DE22  : POS Entry Mode
     * - DE32  : Acquirer ID
     * - DE37  : Retrieval Reference Number (RRN)
     * - DE38  : Authorization Code
     * - DE41  : Terminal ID
     * - DE42  : Merchant ID
     * - DE43  : Merchant Name & Location
     * - DE48  : Merchant Bank Info
     * - DE49  : Currency Code
     * - DE58  : Additional Data (Voucher Info)
     * - DE127 : Acquirer Trace Data
     *
     * Special Logic:
     * - Builds voucher-specific additional data (FNS + Voucher Number)
     * - Generates RRN dynamically using STAN + timestamp
     * - Handles padding and ISO formatting
     *
     * @param builderServiceTxnDetails Transaction details (voucher, merchant, etc.)
     * @return ByteArray ISO financial message
     */
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
        iso.setValue(BuilderConstants.ISO_FIELD_AUTH_ID, builderServiceTxnDetails?.approvalCode, IsoType.NUMERIC, BuilderConstants.ISO_FIELD_AUTH_ID_LENGTH)
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

    /**
     * Creates ISO8583 Reversal/Void request message.
     *
     * MTI: 0420 (Reversal Request)
     *
     * Purpose:
     * - Reverses a previous transaction using original transaction details
     *
     * Fields Used:
     * - DE2   : PAN
     * - DE3   : Processing Code
     * - DE4   : Amount
     * - DE7   : Original Transmission DateTime
     * - DE11  : Original STAN
     * - DE12  : Local Time
     * - DE13  : Local Date
     * - DE15  : Settlement Date
     * - DE18  : Merchant Type
     * - DE22  : POS Entry Mode
     * - DE23  : Card Sequence Number (for chip)
     * - DE32  : Acquirer ID
     * - DE37  : Original RRN
     * - DE38  : Authorization Code
     * - DE39  : Response Code
     * - DE41  : Terminal ID
     * - DE42  : Merchant ID
     * - DE43  : Merchant Name
     * - DE48  : Merchant Bank Info
     * - DE49  : Currency Code
     * - DE55  : ICC Data (for chip cards)
     * - DE59  : NPS Geographic Data
     * - DE90  : Original Data Elements
     * - DE58  : Additional Data
     *
     * Special Handling:
     * - Includes EMV data (DE55) for chip/contactless cards
     * - Reuses original transaction data
     * - Dynamically replaces DE55 placeholder with raw TLV bytes
     *
     * @param builderServiceTxnDetails Original transaction details
     * @return ByteArray ISO reversal message
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun createVoidRequest(builderServiceTxnDetails: BuilderServiceTxnDetails?): ByteArray {
        this.builderServiceTxnDetails = builderServiceTxnDetails?: BuilderServiceTxnDetails()
        val amount = this.builderServiceTxnDetails.ttlAmount?.toDoubleOrNull()?.toCurrencyLong() ?: 0
        val lastTxn = IsoMessageBuilder.getLastTxn()
        val posConditionCode = getNationalPosConditionCode()
        val npsGeoData = getNPSGeographicData()
        val iccData = lastTxn?.emvData
        var de55RawBytes: ByteArray? = null
        val isChipCard = builderServiceTxnDetails?.cardEntryMode == CardEntryMode.CONTACT.toString() ||
                builderServiceTxnDetails?.cardEntryMode == CardEntryMode.CONTACLESS.toString()

        if (isChipCard && !iccData.isNullOrEmpty()) {
            val cleanHex = cleanHex(iccData)  // ← uncomment this
            val iccByteArray = hexStringToByteArray(cleanHex)  // ← use cleanHex not rearrangedIccData
            checkTag(cleanHex, "9F34", "03", "020002", "CVM Result")
            val lllPrefix = "%03d".format(iccByteArray.size).toByteArray(Charsets.US_ASCII)
            de55RawBytes = lllPrefix + iccByteArray
        }
        val iso = IsoMessage()
        iso.setType(BuilderConstants.MTI_REVERSAL_REQ)
        iso.setValue(BuilderConstants.ISO_FIELD_PAN_NO, builderServiceTxnDetails?.cardMaskedPan, IsoType.LLVAR, BuilderConstants.ISO_FIELD_PAN_NO_LENGTH)
        iso.setValue(BuilderConstants.ISO_FIELD_PROCESSING_CODE, builderServiceTxnDetails?.processingCode?.padStart(6,'0'), IsoType.NUMERIC, BuilderConstants.ISO_FIELD_PROCESSING_CODE_LENGTH)
        iso.setValue(BuilderConstants.ISO_FIELD_AMOUNT, amount, IsoType.NUMERIC, BuilderConstants.ISO_FIELD_AMOUNT_LENGTH)
        iso.setValue(BuilderConstants.ISO_FIELD_TRANSMISSION_DATE, builderServiceTxnDetails?.originalDateTime, IsoType.NUMERIC, BuilderConstants.ISO_FIELD_TRANSMISSION_DATE_LENGTH)
        iso.setValue(BuilderConstants.ISO_FIELD_STAN,builderServiceTxnDetails?.stan?.padStart(6,'0') , IsoType.NUMERIC, BuilderConstants.ISO_FIELD_STAN_LENGTH)
        iso.setValue(BuilderConstants.ISO_FIELD_LOC_TIME, builderServiceTxnDetails?.localTime, IsoType.NUMERIC, BuilderConstants.ISO_FIELD_LOC_TIME_LENGTH)
        iso.setValue(BuilderConstants.ISO_FIELD_LOC_DATE, builderServiceTxnDetails?.localDate, IsoType.NUMERIC, BuilderConstants.ISO_FIELD_LOC_DATE_LENGTH)
        iso.setValue(BuilderConstants.ISO_FIELD_SET_DATE, builderServiceTxnDetails?.settlementDate, IsoType.NUMERIC, BuilderConstants.ISO_FIELD_LOC_DATE_LENGTH)
        iso.setValue(BuilderConstants.ISO_FIELD_MERCHANT_TYPE, builderServiceTxnDetails?.merchantType, IsoType.NUMERIC, BuilderConstants.ISO_FIELD_MERCHANT_TYPE_LENGTH)
        iso.setValue(BuilderConstants.ISO_FIELD_ENTRY_MODE, builderServiceTxnDetails?.posEntryMode, IsoType.NUMERIC, BuilderConstants.ISO_FIELD_ENTRY_MODE_LENGTH)
        if (builderServiceTxnDetails?.cardEntryMode == CardEntryMode.CONTACT.toString() || builderServiceTxnDetails?.cardEntryMode == CardEntryMode.CONTACLESS.toString()) {
            iso.setValue(BuilderConstants.ISO_FIELD_PAN_SEQ_NO, "000", IsoType.NUMERIC, BuilderConstants.ISO_FIELD_PAN_SEQ_NO_LENGTH)
        }
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
        iso.setValue(BuilderConstants.ISO_FIELD_CURRENCY_CODE, builderServiceTxnDetails?.currencyCode, IsoType.NUMERIC, BuilderConstants.ISO_FIELD_CURRENCY_CODE_LENGTH)
        if (isChipCard && de55RawBytes != null) {
            iso.setValue(
                BuilderConstants.ISO_FIELD_ICC_DATA,
                "DE55_PLACEHOLDER",   // dummy, will be replaced
                IsoType.LLLVAR,
                de55RawBytes.size
            )
        }
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

        return if (isChipCard && de55RawBytes != null) {
            val isoBytes = iso.writeData()
            spliceDE55(isoBytes, de55RawBytes, "DE55_PLACEHOLDER")
        } else {
            iso.writeData()
        }
    }

    /**
     * Parses Echo/Network response from host into transaction details.
     *
     * Extracted Fields:
     * - DE7   : Transmission DateTime
     * - DE11  : STAN
     * - DE41  : Terminal ID
     * - DE70  : Network Management Info Code
     * - DE96  : Device Serial Number
     * - DE125 : Work Key (Encryption Key)
     *
     * @param context Required for ISO message parsing
     * @param response Raw ISO response in ByteArray
     * @return Parsed BuilderServiceTxnDetails object
     *
     * Note:
     * - Handles parsing exceptions safely
     * - Used for sign-on / handshake / key exchange responses
     */
    fun parseEcoResponse(context: Context, response: ByteArray): BuilderServiceTxnDetails {
        val details = BuilderServiceTxnDetails()
        try {
            val isoStr = String(response, Charsets.US_ASCII)
            val mti = isoStr.take(4)
            val mf = createMessageFactory(context)
            val isoMsg = mf.parseMessage(response, 0)

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

    /**
     * Creates ISO8583 Financial Transaction request message.
     *
     * MTI: 0200 (Financial Request)
     *
     * Supported Transactions:
     * - Purchase
     * - Cashback
     * - EBT (Cash / SNAP)
     *
     * Fields Used:
     * - DE2   : PAN
     * - DE3   : Processing Code
     * - DE4   : Amount
     * - DE7   : Transmission DateTime
     * - DE11  : STAN
     * - DE12  : Local Time
     * - DE13  : Local Date
     * - DE15  : Settlement Date
     * - DE17  : Capture Date
     * - DE18  : Merchant Type
     * - DE22  : POS Entry Mode
     * - DE23  : Card Sequence Number (chip cards)
     * - DE32  : Acquirer ID
     * - DE35  : Track 2 Data
     * - DE37  : RRN
     * - DE41  : Terminal ID
     * - DE42  : Merchant ID
     * - DE43  : Merchant Name
     * - DE48  : Merchant Bank Info
     * - DE49  : Currency Code
     * - DE52  : PIN Block
     * - DE54  : Additional Amount (Cashback)
     * - DE55  : ICC Data (EMV)
     * - DE58  : Additional Data
     * - DE127 : Acquirer Trace Data
     *
     * Special Handling:
     * - Dynamically generates STAN and RRN
     * - Supports chip, magstripe, manual, contactless modes
     * - Injects DE55 (EMV TLV data) using placeholder replacement
     * - Saves transaction locally for reversal/void
     *
     * @param builderServiceTxnDetails Transaction input data
     * @return ByteArray ISO financial request message
     */
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun createFinancialRequest(builderServiceTxnDetails: BuilderServiceTxnDetails?): ByteArray {
        this.builderServiceTxnDetails = builderServiceTxnDetails ?: BuilderServiceTxnDetails()
        val amount = this.builderServiceTxnDetails.ttlAmount?.toDoubleOrNull()?.toCurrencyLong() ?: 0
        val pinBlock = getPinBlock()
        val maskedPan = getMaskedPAN()
        val iccData = getIccData()
        val cardSeqNumber = getCardSeqNum()
        Log.d("TxnDebug", "cardSeqNumber = $cardSeqNumber")
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
        val originalData = builderServiceTxnDetails?.dateTime?.padEnd(20, '0')
        builderServiceTxnDetails?.posConditionCode = getNationalPosConditionCode()

        var de55RawBytes: ByteArray? = null
        val isChipCard = builderServiceTxnDetails?.cardEntryMode == CardEntryMode.CONTACT.toString() ||
                builderServiceTxnDetails?.cardEntryMode == CardEntryMode.CONTACLESS.toString()

        if (isChipCard && !iccData.isNullOrEmpty()) {
            val cleanHex = cleanHex(iccData)
            val iccByteArray = hexStringToByteArray(cleanHex)
            checkTag(cleanHex, "9F34", "03", "020002", "CVM Result")
            val lllPrefix = "%03d".format(iccByteArray.size).toByteArray(Charsets.US_ASCII)
            de55RawBytes = lllPrefix + iccByteArray
        }

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
        iso.setValue(BuilderConstants.ISO_FIELD_MERCHANT_TYPE, builderServiceTxnDetails?.merchantType, IsoType.NUMERIC, BuilderConstants.ISO_FIELD_MERCHANT_TYPE_LENGTH)
        iso.setValue(BuilderConstants.ISO_FIELD_ENTRY_MODE, builderServiceTxnDetails?.posEntryMode, IsoType.NUMERIC, BuilderConstants.ISO_FIELD_ENTRY_MODE_LENGTH)
        if (builderServiceTxnDetails?.cardEntryMode == CardEntryMode.CONTACT.toString() || builderServiceTxnDetails?.cardEntryMode == CardEntryMode.CONTACLESS.toString()) {
            iso.setValue(BuilderConstants.ISO_FIELD_PAN_SEQ_NO, cardSeqNumber, IsoType.NUMERIC, BuilderConstants.ISO_FIELD_PAN_SEQ_NO_LENGTH)
        }

        iso.setValue(BuilderConstants.ISO_FIELD_ACQUIRER_ID, builderServiceTxnDetails?.procId, IsoType.LLVAR, BuilderConstants.ISO_FIELD_ACQUIRER_ID_LENGTH)

        if (builderServiceTxnDetails?.cardEntryMode != CardEntryMode.MANUAL.toString()) {
            iso.setValue(
                BuilderConstants.ISO_FIELD_TRACK2_DATA,
                encryptedTrack2Data,
                IsoType.LLVAR,
                BuilderConstants.ISO_FIELD_TRACK2_DATA_LENGTH
            )
        }
        iso.setValue(BuilderConstants.ISO_FIELD_RRN, builderServiceTxnDetails?.rrn, IsoType.ALPHA, BuilderConstants.ISO_FIELD_RRN_LENGTH)
        iso.setValue(BuilderConstants.ISO_FIELD_TERMINAL_ID, builderServiceTxnDetails?.terminalId, IsoType.ALPHA, BuilderConstants.ISO_FIELD_TERMINAL_ID_LENGTH)
        iso.setValue(BuilderConstants.ISO_FIELD_MERCHANT_ID, builderServiceTxnDetails?.merchantId, IsoType.ALPHA, BuilderConstants.ISO_FIELD_MERCHANT_ID_LENGTH)
        iso.setValue(
            BuilderConstants.ISO_FIELD_MERCHANT_NAME,
            builderServiceTxnDetails?.merchantNameLocation?.padEnd(BuilderConstants.ISO_FIELD_MERCHANT_NAME_LENGTH),
            IsoType.ALPHA,
            BuilderConstants.ISO_FIELD_MERCHANT_NAME_LENGTH
        )
        iso.setValue(BuilderConstants.ISO_FIELD_MERCHANT_BANK, builderServiceTxnDetails?.merchantBankName, IsoType.LLLVAR, BuilderConstants.ISO_FIELD_MERCHANT_BANK_LENGTH)
        iso.setValue(BuilderConstants.ISO_FIELD_CURRENCY_CODE, builderServiceTxnDetails?.currencyCode, IsoType.NUMERIC, BuilderConstants.ISO_FIELD_CURRENCY_CODE_LENGTH)
        iso.setValue(BuilderConstants.ISO_FIELD_PIN_BLOCK, pinBlock, IsoType.ALPHA, BuilderConstants.ISO_FIELD_PIN_BLOCK_LENGTH)

        if (this.builderServiceTxnDetails.txnType == TxnType.PURCHASE_CASHBACK.toString()) {
            iso.setValue(BuilderConstants.ISO_FIELD_ADD_AMOUNT, builderServiceTxnDetails?.cashback, IsoType.LLLVAR, builderServiceTxnDetails?.cashback!!.length)
        }

        if (isChipCard && de55RawBytes != null) {
            iso.setValue(
                BuilderConstants.ISO_FIELD_ICC_DATA,
                "DE55_PLACEHOLDER",   // dummy, will be replaced
                IsoType.LLLVAR,
                de55RawBytes.size
            )
        }
        iso.setValue(BuilderConstants.ISO_FIELD_POS_CONDITION_CODE, builderServiceTxnDetails?.posConditionCode, IsoType.LLLVAR, BuilderConstants.ISO_FIELD_POS_CONDITION_CODE_LENGTH)
        if(builderServiceTxnDetails?.txnType != TxnType.CASH_PURCHASE.toString() && builderServiceTxnDetails?.txnType != TxnType.CASH_WITHDRAWAL.toString() && builderServiceTxnDetails?.txnType != TxnType.PURCHASE_CASHBACK.toString()) {
            iso.setValue(
                BuilderConstants.ISO_FIELD_ADDITIONAL_DATA,
                builderServiceTxnDetails?.fnsNumber,
                IsoType.LLLVAR,
                BuilderConstants.ISO_FIELD_ADDITIONAL_DATA_LENGTH
            )
        }
        iso.setValue(BuilderConstants.ISO_FIELD_ACQ_TRACE_DATA, originalData, IsoType.LLLVAR, BuilderConstants.ISO_FIELD_ACQ_TRACE_DATA_LENGTH)
        iso.setBinaryHeader(false)
        iso.setForceStringEncoding(true)
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
            emvData = builderServiceTxnDetails?.emvData,
            pan = maskedPan,
            track2Data = encryptedTrack2Data,
            entryMode = builderServiceTxnDetails?.posEntryMode,
            posConditionCode = builderServiceTxnDetails?.posConditionCode,
            acquirerId = builderServiceTxnDetails?.procId,
            additionalData = builderServiceTxnDetails?.fnsNumber,
            originalData = originalData
        )
        IsoMessageBuilder.saveTxn(savedTxn)
        return if (isChipCard && de55RawBytes != null) {
            val isoBytes = iso.writeData()
            spliceDE55(isoBytes, de55RawBytes, "DE55_PLACEHOLDER")
        } else {
            iso.writeData()
        }
    }

    /**
     * Replaces placeholder DE55 data in ISO message with actual EMV TLV bytes.
     *
     * Logic:
     * - Finds placeholder pattern in ISO byte array
     * - Replaces it with correctly formatted DE55 (LLLVAR + TLV)
     *
     * @param isoBytes Original ISO message bytes
     * @param de55RawBytes Actual DE55 data (with LLL prefix)
     * @param placeholder Placeholder string to replace
     * @return Updated ISO message with correct DE55
     *
     * Note:
     * - Required because ISO library does not handle raw TLV properly
     */
    private fun spliceDE55(isoBytes: ByteArray, de55RawBytes: ByteArray, placeholder: String): ByteArray {
        val placeholderBytes = placeholder.toByteArray(Charsets.US_ASCII)
        val lllOfPlaceholder = "%03d".format(placeholderBytes.size).toByteArray(Charsets.US_ASCII)
        val searchPattern = lllOfPlaceholder + placeholderBytes

        val idx = findPattern(isoBytes, searchPattern)
        if (idx == -1) {
            Log.e("DE55", "Placeholder not found in ISO bytes! Returning unmodified.")
            return isoBytes
        }
        Log.d("DE55", "Placeholder found at index: $idx, replacing ${searchPattern.size} bytes with ${de55RawBytes.size} bytes")

        val result = ByteArrayOutputStream()
        result.write(isoBytes, 0, idx)                          // before DE55
        result.write(de55RawBytes)                              // real DE55 (LLL prefix already included)
        result.write(isoBytes, idx + searchPattern.size,        // after DE55
            isoBytes.size - idx - searchPattern.size)
        return result.toByteArray()
    }

    /**
     * Finds the index of a byte pattern inside a byte array.
     *
     * @param data Source byte array
     * @param pattern Pattern to search
     * @return Starting index of pattern, or -1 if not found
     *
     * Used in:
     * - Locating DE55 placeholder inside ISO message
     */
    private fun findPattern(data: ByteArray, pattern: ByteArray): Int {
        outer@ for (i in 0..data.size - pattern.size) {
            for (j in pattern.indices) {
                if (data[i + j] != pattern[j]) continue@outer
            }
            return i
        }
        return -1
    }

    /**
     * Parses Network Management ISO response (e.g., Echo, Key Exchange).
     *
     * Extracts key device and host-related fields from ISO message.
     *
     * @param context Application context (used to load ISO config)
     * @param response Raw ISO8583 response in ByteArray
     * @return Parsed transaction details object
     */
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

    /**
     * Parses a generic ISO8583 response message.
     *
     * Handles multiple field types and safely formats date/time fields.
     *
     * @param context Application context
     * @param response Raw ISO8583 response
     * @return Parsed transaction details
     */
    fun parseISOMessage(context: Context, response: ByteArray): BuilderServiceTxnDetails {
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

    /**
     * Updates transaction record in local database after ISO request creation.
     *
     * Only updates minimal required fields such as STAN, RRN, and timestamps.
     *
     * @param builderServiceTxnDetails Transaction details to update
     */
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
        txn.posEntryMode = builderServiceTxnDetails.posEntryMode
        txn.stan = builderServiceTxnDetails.stan
        txn.rrn = builderServiceTxnDetails.rrn
        txn.processingCode = builderServiceTxnDetails.processingCode
        txn.localTime = builderServiceTxnDetails.localTime
        txn.localDate = builderServiceTxnDetails.localDate
        txn.currencyCode = builderServiceTxnDetails.currencyCode
        dbRepository.updateTxn(txn)
    }

}