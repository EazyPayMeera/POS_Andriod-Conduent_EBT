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
import com.eazypaytech.builder_core.model.PosConditionCode
import com.eazypaytech.builder_core.utils.BuilderUtils
import com.eazypaytech.builder_core.utils.toCurrencyLong
import com.eazypaytech.securityframework.database.dbRepository.TxnDBRepository
import com.eazypaytech.securityframework.model.TxnType
import com.solab.iso8583.IsoMessage
import com.solab.iso8583.IsoType
import com.solab.iso8583.MessageFactory
import com.solab.iso8583.parse.ConfigParser
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.random.Random

class ApiRequestBuilderLyra @Inject constructor(@ApplicationContext val context: Context,var dbRepository: TxnDBRepository) {
    val messageFactory = MessageFactory<IsoMessage>()
    var builderServiceTxnDetails = BuilderServiceTxnDetails()
    lateinit var message : IsoMessage


    init {
        setIsoConfig()
    }

    private fun appendIsoLength(request : ByteArray?) : ByteArray
    {
        var isoPacket = ByteArray(request?.size?.plus(2)?:2)
        isoPacket[0] = ((request?.size?:0)/256).toByte()
        isoPacket[1] = ((request?.size?:0)%256).toByte()
        request?.copyInto(isoPacket,2,0, request.size)
        return isoPacket
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

//    fun getIsoPosEntryMode() : String?
//    {
//        return when(builderServiceTxnDetails.cardEntryMode)
//        {
//            CardEntryMode.MANUAL.toString()->"0110"
//            CardEntryMode.MAGSTRIPE.toString()->"0210"
//            CardEntryMode.CONTACT.toString()->"0510"
//            CardEntryMode.CONTACLESS.toString()->"0710"
//            CardEntryMode.FALLBACK_MAGSTRIPE.toString()->"8010"
//            CardEntryMode.CONTACLESS_MAGSTRIPE.toString()->"9110"
//            else -> "0010"
//        }
//    }

    fun getIsoPosEntryMode(): String {
        Log.d("CARD_ENTRY_MODE", "Raw Entry Mode: ${builderServiceTxnDetails.cardEntryMode}")
        return when (builderServiceTxnDetails.cardEntryMode) {

            CardEntryMode.MAGSTRIPE.toString() -> "022"     // Magstripe + PIN
            CardEntryMode.CONTACT.toString() -> "051"       // Chip + PIN
            CardEntryMode.CONTACLESS.toString() -> "071"    // Contactless + PIN
            CardEntryMode.FALLBACK_MAGSTRIPE.toString() -> "801"
            CardEntryMode.MANUAL.toString() -> "011"
            else -> "010"
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

    fun getTermNameAndLocation(): String? {
        return  builderServiceTxnDetails.ternNameLoc
    }

    fun getIsoPosConditionCode() : String?
    {
        return when(builderServiceTxnDetails.posConditionCode)
        {
            PosConditionCode.NORMAL_PRESENTMENT.toString()->"00"
            PosConditionCode.CUSTOMER_NOT_PRESENT.toString()->"01"
            PosConditionCode.MERCHANT_SUSPICIOUS.toString()->"03"
            PosConditionCode.ECR_INTERFACE.toString()->"04"
            PosConditionCode.CARD_NOT_PRESENT.toString()->"05"
            PosConditionCode.PREAUTH.toString()->"06"
            PosConditionCode.MOTO.toString()->"08"
            PosConditionCode.CARD_PRESENT_BAD_MAG.toString()->"71"
            else -> "00"
        }
    }

    fun getEncryptedPAN() : String?
    {
        var pan : String? =null
        builderServiceTxnDetails.cardPan?.let {
            pan = it
        }
        return pan
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

    fun getIccData() : String?
    {
        var iccData : String? =null
        builderServiceTxnDetails.emvData?.let {
            iccData = it
        }
        return iccData
    }

    fun getKsnTag() : String?
    {
        var ksn :String? = null
        builderServiceTxnDetails.ksn?.padStart(BuilderConstants.ISO_FIELD_KSN_LENGTH,BuilderConstants.ISO_FIELD_KSN_PAD_CHAR)?.let {
            ksn = BuilderConstants.ISO_FIELD_KSN_TAG + it.length.toString().padStart(3,'0') + it
        }
        return ksn
    }

    /*fun getBatchNumber() : String? {
        var batchNumber: String? =
            builderServiceTxnDetails.batchId?.toInt()?.toString()?:"1"
        batchNumber?.padStart(BuilderConstants.ISO_FIELD_PVT_USE_BATCH_LENGTH, '0')?.let {
            batchNumber = it.length.toString()
                .padStart(BuilderConstants.ISO_FIELD_PVT_USE_BATCH_LENGTH_LENGTH, '0') + it
        }
        return batchNumber
    }*/

    /*fun getOrigAmount() : String?
    {
        Log.d("OriginalAmountLog", "originalTtlAmount: ${builderServiceTxnDetails.originalTtlAmount}")
        var amount: String? =
            (builderServiceTxnDetails.originalTtlAmount?.trim('[')?.trim(']')?.toDoubleOrNull()?.toCurrencyLong()?:0).toString()
        amount?.padStart(BuilderConstants.ISO_FIELD_AMOUNT_LENGTH, '0')?.let {
            amount = it
        }
        return amount
    }*/


    /*fun getInvoiceNumber() : String?
    {
        var invoiceNumber: String? =
            builderServiceTxnDetails.invoiceNo?.toInt()?.toString()
        invoiceNumber?.padStart(BuilderConstants.ISO_FIELD_INVOICE_NUMBER_LENGTH, '0')?.let {
            invoiceNumber = it
        }
        return invoiceNumber
    }*/

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
        return when (txnType) {

            TxnType.CASH_PURCHASE.toString() -> "009600"
            TxnType.FOOD_PURCHASE.toString() -> "009800"
            TxnType.PURCHASE_CASHBACK.toString() -> "099600"
            TxnType.FOODSTAMP_RETURN.toString() -> "200098"
            TxnType.BALANCE_ENQUIRY_CASH.toString() -> "319600"
            TxnType.BALANCE_ENQUIRY_SNAP.toString() -> "319800"
            TxnType.VOID_LAST.toString() -> "0000"
            TxnType.E_VOUCHER.toString() -> "009800"
            TxnType.CASH_WITHDRAWAL.toString() -> "019600"

            else -> "0000"   // 🔥 Required for String?
        }
    }

    fun getProcessingCodeForVoid(): String? {
        val originalTxnType = builderServiceTxnDetails.originalTxnType
            ?.replace("[", "")
            ?.replace("]", "")
            ?.trim()
        Log.d("TransactionTypeDebug", "Original Transaction Type: $originalTxnType")

        return when (originalTxnType) {
            "PURCHASE" -> {
                BuilderConstants.PROC_CODE_VOID_SALE.toString().padStart(6, '0') // Ensures the value is 6 digits with leading zeros
            }
            "REFUND" -> {
                BuilderConstants.PROC_CODE_REFUND.toString()
            }
            else -> {
                BuilderConstants.PROC_CODE_VOID_PRE_AUTH.toString()
            }
        }
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

    fun getPinBlock() : String?
    {
        var pinBlock: String? =
            builderServiceTxnDetails.pinBlock
        pinBlock?.padEnd(BuilderConstants.ISO_FIELD_PAN_SEQ_NO_LENGTH, 'F')?.let {
            pinBlock = it
        }
        return pinBlock
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

    fun getAuthCode() : String?
    {
        return builderServiceTxnDetails.hostAuthCode
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

    /* Dummy Response Functions */
    /*fun generateDummyRRN() : String
    {
        val charset = BuilderConstants.DUMMY_RANDOM_CHARSET
        val currentDate = BuilderUtils.getCurrentDateTime(BuilderConstants.DUMMY_DATE_TIME_FORMAT_RRN)
        val encodedDate = currentDate.map { it - '0' + 'A'.code }.joinToString("") { it.toChar().toString() }
        val randomPart = (1..BuilderConstants.ISO_FIELD_RRN_LENGTH)
            .map { charset[Random.nextInt(charset.length)] }
            .joinToString("")

        // Shuffle the date encoding and the random part
        val combined = (encodedDate + randomPart).toList().shuffled().joinToString("")
        return combined.take(BuilderConstants.ISO_FIELD_RRN_LENGTH)
    }*/

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

    fun generateDummyAuthCode() : String?
    {
        return if(isDummyResponseApproval()) {
            val charset = BuilderConstants.DUMMY_RANDOM_CHARSET
            val currentDate =
                BuilderUtils.getCurrentDateTime(BuilderConstants.DUMMY_DATE_TIME_FORMAT_AUTH_CODE)
            val encodedDate =
                currentDate.map { it - '0' + 'A'.code }.joinToString("") { it.toChar().toString() }
            val randomPart = (1..BuilderConstants.ISO_FIELD_AUTH_CODE_LENGTH)
                .map { charset[Random.nextInt(charset.length)] }
                .joinToString("")

            // Shuffle the date encoding and the random part
            val combined = (encodedDate + randomPart).toList().shuffled().joinToString("")
            combined.take(BuilderConstants.ISO_FIELD_AUTH_CODE_LENGTH)
        }
        else
            null
    }

    fun String.hexToByteArray(): ByteArray {
        require(length % 2 == 0)
        return ByteArray(length / 2) { i ->
            ((this[i * 2].digitToInt(16) shl 4) + this[i * 2 + 1].digitToInt(16)).toByte()
        }
    }

    fun isDummyResponseApproval() : Boolean
    {
        /* Approve EVEN amount & decline ODD amount */
        return (builderServiceTxnDetails.ttlAmount.toCurrencyLong()%2)==0L
    }

    fun generateDummyRespCode() : String
    {
        return if(isDummyResponseApproval())
            "00"
        else
            (1..99).random().toString()
    }

    fun generateDummyIccData() : String
    {
        return if(isDummyResponseApproval())
            "8A023030"
        else
            "8A023035"
    }

    fun generateAsciiBitmap(fields: Set<Int>): String {
        val bits = BooleanArray(128)

        fields.forEach { field ->
            if (field in 1..128) {
                bits[field - 1] = true
            }
        }

        // Secondary bitmap present?
        if (fields.any { it > 64 }) {
            bits[0] = true
        }

        val hex = StringBuilder()

        for (i in bits.indices step 4) {
            val nibble =
                (if (bits[i]) 8 else 0) +
                        (if (bits[i + 1]) 4 else 0) +
                        (if (bits[i + 2]) 2 else 0) +
                        (if (bits[i + 3]) 1 else 0)

            hex.append(Integer.toHexString(nibble).uppercase())
        }

        return if (bits[0]) hex.substring(0, 32) + hex.substring(32, 64)
        else hex.substring(0, 32)
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
        iso.setBinaryHeader(false)        // Use ASCII header
        iso.setBinaryFields(false)        // Use ASCII fields
        iso.setForceStringEncoding(true)  // Ensure ASCII encoding
        iso.setIsoHeader("") // no custom header
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
        iso.setBinaryHeader(false)        // Use ASCII header
        iso.setBinaryFields(false)        // Use ASCII fields
        iso.setForceStringEncoding(true)  // Ensure ASCII encoding
        iso.setIsoHeader("") // no custom header
        return iso.writeData()
    }

    fun createHandShakeRequest(builderServiceTxnDetails: BuilderServiceTxnDetails?): ByteArray {
        val stan = getSTAN().toString().padStart(6, '0')
        val time = BuilderUtils.getCurrentDateTime(BuilderConstants.ISO_DATE_FORMAT)
        val msg_sec_code = builderServiceTxnDetails?.procId?.drop(3)
        val iso = IsoMessage()
        iso.setType(BuilderConstants.ISO_TYPE_SIGN_ON)  // MTI 0800
        iso.setValue(BuilderConstants.ISO_FIELD_TRANSMISSION_DATE, time, IsoType.NUMERIC, 10) // Fixed-length numeric 10 digits
        iso.setValue(BuilderConstants.ISO_FIELD_STAN, stan, IsoType.NUMERIC, 6) // Fixed-length numeric 6 digits
        iso.setValue(BuilderConstants.ISO_FIELD_ACQUIRER_ID, builderServiceTxnDetails?.procId, IsoType.LLVAR, BuilderConstants.ISO_FIELD_PROC_ID_LENGTH) // LLVAR length auto-handled
        iso.setValue(BuilderConstants.ISO_FIELD_NET_MGMT_INFO_CODE, BuilderConstants.HANDSHAKE_REQUEST, IsoType.NUMERIC, 3) // Fixed-length numeric
        msg_sec_code?.length?.let { iso.setValue(BuilderConstants.ISO_FIELD_KEY_MGMT_DATA, msg_sec_code, IsoType.ALPHA, it ) }
        iso.setBinaryHeader(false)        // Use ASCII header
        iso.setBinaryFields(false)        // Use ASCII fields
        iso.setForceStringEncoding(true)  // Ensure ASCII encoding
        iso.setIsoHeader("") // no custom header
        return iso.writeData()
    }

    @OptIn(ExperimentalStdlibApi::class)
    fun createKeyRequest(builderServiceTxnDetails: BuilderServiceTxnDetails?): ByteArray {
        val time = BuilderUtils.getCurrentDateTime(BuilderConstants.DEFAULT_ISO8583_TIME_FORMAT)
        val stan = getSTAN().toString().padStart(6, '0')
        val msg_sec_code = builderServiceTxnDetails?.procId?.drop(3)
        val workKey = builderServiceTxnDetails?.workKey ?: ""
        val iso = IsoMessage()
        iso.setType(BuilderConstants.ISO_TYPE_SIGN_ON)  // MTI 0800
        iso.setValue(BuilderConstants.ISO_FIELD_TRANSMISSION_DATE, time, IsoType.NUMERIC, 10) // Fixed-length numeric 10 digits
        iso.setValue(BuilderConstants.ISO_FIELD_STAN, stan, IsoType.NUMERIC, BuilderConstants.ISO_FIELD_STAN_LENGTH) // Fixed-length numeric 6 digits
        iso.setValue(BuilderConstants.ISO_FIELD_ACQUIRER_ID, builderServiceTxnDetails?.procId, IsoType.LLVAR, BuilderConstants.ISO_FIELD_ACQUIRER_ID_LENGTH)              // DE032 Acquirer ID // LLVAR length auto-handled
        iso.setValue(BuilderConstants.ISO_FIELD_NET_MGMT_INFO_CODE, BuilderConstants.KEY_CHANGE, IsoType.NUMERIC, BuilderConstants.ISO_FIELD_NET_MGMT_INFO_CODE_LENGTH) // Fixed-length numeric
        msg_sec_code?.length?.let { iso.setValue(BuilderConstants.ISO_FIELD_KEY_MGMT_DATA, msg_sec_code, IsoType.ALPHA, it ) }
        iso.setValue(BuilderConstants.ISO_FIELD_KEY_DATA, workKey, IsoType.LLLVAR,workKey.length)
        iso.setBinaryHeader(false)        // Use ASCII header
        iso.setBinaryFields(false)        // Use ASCII fields
        iso.setForceStringEncoding(true)  // Ensure ASCII encoding
        iso.setIsoHeader("") // no custom header
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
        iso.setBinaryHeader(false)        // Use ASCII header
        iso.setBinaryFields(false)        // Use ASCII fields
        iso.setForceStringEncoding(true)  // Ensure ASCII encoding
        iso.setIsoHeader("") // no custom header
        return iso.writeData()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun createFinancial0200Request(builderServiceTxnDetails: BuilderServiceTxnDetails?): ByteArray {
        this.builderServiceTxnDetails = builderServiceTxnDetails?: BuilderServiceTxnDetails()
        val amount = this.builderServiceTxnDetails.ttlAmount?.toDoubleOrNull()?.toCurrencyLong() ?: 0
        val posEntryMode = getIsoPosEntryMode()
        Log.d("ISO_DEBUG", "POS Entry Mode: $posEntryMode")
        val encryptedTrack2Data = getEncryptedTrack2Data()
        val cashbackAmt = cashbackAmount((this.builderServiceTxnDetails.cashback?.toDoubleOrNull()?.toCurrencyLong() ?: 0))
        val stan = getSTAN()
        val iso = IsoMessage()
        val pinBlock = getPinBlock()
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
        iso.setValue(BuilderConstants.ISO_FIELD_PAN_NO, "6104340109641151", IsoType.LLVAR, BuilderConstants.ISO_FIELD_PAN_NO_LENGTH)          // DE002 PAN
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
        if(builderServiceTxnDetails?.cardEntryMode != CardEntryMode.MANUAL.toString() ) {
            iso.setValue(
                BuilderConstants.ISO_FIELD_TRACK2_DATA,
                "6104340109641151=3102220908",
                IsoType.LLVAR,
                BuilderConstants.ISO_FIELD_TRACK2_DATA_LENGTH
            ) // DE035 Track 2
        }
        iso.setValue(BuilderConstants.ISO_FIELD_RRN, rrn, IsoType.ALPHA, BuilderConstants.ISO_FIELD_RRN_LENGTH)              // DE037 RRN
        iso.setValue(BuilderConstants.ISO_FIELD_TERMINAL_ID, builderServiceTxnDetails?.terminalId, IsoType.ALPHA, BuilderConstants.ISO_FIELD_TERMINAL_ID_LENGTH)                   // DE041 Terminal ID
        iso.setValue(BuilderConstants.ISO_FIELD_MERCHANT_ID, builderServiceTxnDetails?.merchantId, IsoType.ALPHA, BuilderConstants.ISO_FIELD_MERCHANT_ID_LENGTH)           // DE042 Merchant ID
        iso.setValue(BuilderConstants.ISO_FIELD_MERCHANT_NAME,
            builderServiceTxnDetails?.merchantNameLocation?.padEnd(BuilderConstants.ISO_FIELD_MERCHANT_NAME_LENGTH),
            IsoType.ALPHA,
            BuilderConstants.ISO_FIELD_MERCHANT_NAME_LENGTH
        )
        iso.setValue(BuilderConstants.ISO_FIELD_MERCHANT_BANK, builderServiceTxnDetails?.merchantBankName, IsoType.LLLVAR, BuilderConstants.ISO_FIELD_MERCHANT_BANK_LENGTH)         // DE048
        iso.setValue(BuilderConstants.ISO_FIELD_CURRENCY_CODE, currencyCode, IsoType.NUMERIC, BuilderConstants.ISO_FIELD_CURRENCY_CODE_LENGTH)                      // DE049 Currency
        iso.setValue(BuilderConstants.ISO_FIELD_PIN_BLOCK, pinBlock, IsoType.ALPHA, BuilderConstants.ISO_FIELD_PIN_BLOCK_LENGTH)
        if(this.builderServiceTxnDetails.txnType ==  TxnType.PURCHASE_CASHBACK.toString())
        {
            iso.setValue(BuilderConstants.ISO_FIELD_ADD_AMOUNT, cashbackAmt, IsoType.LLLVAR, cashbackAmt.length)
        }
        iso.setValue(BuilderConstants.ISO_FIELD_POS_CONDITION_CODE, posConditionCode, IsoType.LLLVAR, BuilderConstants.ISO_FIELD_POS_CONDITION_CODE_LENGTH)
        iso.setValue(BuilderConstants.ISO_FIELD_ADDITIONAL_DATA, builderServiceTxnDetails?.fnsNumber, IsoType.LLLVAR, BuilderConstants.ISO_FIELD_ADDITIONAL_DATA_LENGTH)// DE058
        iso.setValue(BuilderConstants.ISO_FIELD_ACQ_TRACE_DATA, originalData, IsoType.LLLVAR, BuilderConstants.ISO_FIELD_ACQ_TRACE_DATA_LENGTH)    // DE127
        iso.setBinaryHeader(false)
        iso.setBinaryFields(false)
        iso.setForceStringEncoding(true)
        iso.setIsoHeader("")
        val savedTxn = SavedTxnData(
            stan = stan.toString(),
            rrn = rrn,
            amount = amount.toString(),
            ttlAmount = this.builderServiceTxnDetails.ttlAmount,
            procId = this.builderServiceTxnDetails.procId,
            processingCode = processingCode,
            transmissionDateTime = dateTime,
            localTime = localTime,
            localDate = localDate,
            terminalId = builderServiceTxnDetails?.terminalId,
            merchantId = builderServiceTxnDetails?.merchantId,
            merchantName = builderServiceTxnDetails?.merchantNameLocation,
            merchantBank = builderServiceTxnDetails?.merchantBankName,
            merchantType = builderServiceTxnDetails?.merchantType,
            currencyCode = currencyCode,
            pan = maskedPan,
            track2Data = encryptedTrack2Data,
            entryMode = posEntryMode,
            posConditionCode = posConditionCode,
            acquirerId = builderServiceTxnDetails?.procId,
            additionalData = builderServiceTxnDetails?.fnsNumber,
            originalData = originalData
        )
        IsoMessageBuilder.saveTxn(savedTxn)
        // Update DB Here
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
        val pinBlock = getPinBlock()
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
        iso.setValue(BuilderConstants.ISO_FIELD_PAN_NO, "6104340109641151", IsoType.LLVAR, BuilderConstants.ISO_FIELD_PAN_NO_LENGTH)          // DE002 PAN
        iso.setValue(BuilderConstants.ISO_FIELD_PROCESSING_CODE, processingCode, IsoType.NUMERIC, BuilderConstants.ISO_FIELD_PROCESSING_CODE_LENGTH)          // DE003 Processing Code
        iso.setValue(BuilderConstants.ISO_FIELD_AMOUNT, amount, IsoType.NUMERIC, BuilderConstants.ISO_FIELD_AMOUNT_LENGTH)            // DE004 Amount
        iso.setValue(BuilderConstants.ISO_FIELD_TRANSMISSION_DATE, dateTime, IsoType.NUMERIC, BuilderConstants.ISO_FIELD_TRANSMISSION_DATE_LENGTH)              // DE007 Transmission DateTime
        iso.setValue(BuilderConstants.ISO_FIELD_STAN, stan, IsoType.NUMERIC, BuilderConstants.ISO_FIELD_STAN_LENGTH)                  // DE011 STAN
        iso.setValue(BuilderConstants.ISO_FIELD_LOC_TIME, localTime, IsoType.NUMERIC, BuilderConstants.ISO_FIELD_LOC_TIME_LENGTH)                  // DE012 Local Time
        iso.setValue(BuilderConstants.ISO_FIELD_LOC_DATE, localDate, IsoType.NUMERIC, BuilderConstants.ISO_FIELD_LOC_DATE_LENGTH)                    // DE013 Local Date
        iso.setValue(BuilderConstants.ISO_FIELD_SET_DATE, localDate, IsoType.NUMERIC, 4)                    // DE015 Settlement Date
        iso.setValue(BuilderConstants.ISO_FIELD_CAP_DATE, localDate, IsoType.NUMERIC, 4)                    // DE017 Capture Date
        iso.setValue(BuilderConstants.ISO_FIELD_MERCHANT_TYPE, builderServiceTxnDetails?.merchantType, IsoType.NUMERIC, BuilderConstants.ISO_FIELD_MERCHANT_TYPE_LENGTH)                    // DE018 Merchant Type
        iso.setValue(BuilderConstants.ISO_FIELD_ENTRY_MODE, "012", IsoType.NUMERIC, BuilderConstants.ISO_FIELD_ENTRY_MODE_LENGTH)                     // DE022 POS Entry Mode
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
        val additional_Data = builderServiceTxnDetails?.fnsNumber + "VN" + builderServiceTxnDetails?.voucherNumber?.padStart(10,'0')
        iso.setValue(BuilderConstants.ISO_FIELD_ADDITIONAL_DATA,additional_Data, IsoType.LLLVAR, BuilderConstants.ISO_FIELD_ADDITIONAL_DATA_LENGTH)// DE058
        iso.setValue(BuilderConstants.ISO_FIELD_ACQ_TRACE_DATA, originalData, IsoType.LLLVAR, BuilderConstants.ISO_FIELD_ACQ_TRACE_DATA_LENGTH)    // DE127
        iso.setBinaryHeader(false)
        iso.setBinaryFields(false)
        iso.setForceStringEncoding(true)
        iso.setIsoHeader("")

        return iso.writeData()
    }




    @RequiresApi(Build.VERSION_CODES.O)
    fun createReversal0420(builderServiceTxnDetails: BuilderServiceTxnDetails?): ByteArray {
        val lastTxn = IsoMessageBuilder.getLastTxn()
        val iso = IsoMessage()
        iso.setType(BuilderConstants.MTI_REVERSAL_REQ)
        iso.setValue(BuilderConstants.ISO_FIELD_PAN_NO, "6104340109641151", IsoType.LLVAR, BuilderConstants.ISO_FIELD_PAN_NO_LENGTH)
        iso.setValue(BuilderConstants.ISO_FIELD_PROCESSING_CODE, lastTxn?.processingCode?.padStart(6,'0'), IsoType.NUMERIC, BuilderConstants.ISO_FIELD_PROCESSING_CODE_LENGTH)
        iso.setValue(BuilderConstants.ISO_FIELD_AMOUNT, lastTxn?.amount, IsoType.NUMERIC, BuilderConstants.ISO_FIELD_AMOUNT_LENGTH)
        iso.setValue(BuilderConstants.ISO_FIELD_TRANSMISSION_DATE, lastTxn?.transmissionDateTime, IsoType.NUMERIC, BuilderConstants.ISO_FIELD_TRANSMISSION_DATE_LENGTH)
        iso.setValue(BuilderConstants.ISO_FIELD_STAN,lastTxn?.stan , IsoType.NUMERIC, BuilderConstants.ISO_FIELD_STAN_LENGTH)
        iso.setValue(BuilderConstants.ISO_FIELD_LOC_TIME, lastTxn?.localTime, IsoType.NUMERIC, BuilderConstants.ISO_FIELD_LOC_TIME_LENGTH)
        iso.setValue(BuilderConstants.ISO_FIELD_LOC_DATE, lastTxn?.localDate, IsoType.NUMERIC, BuilderConstants.ISO_FIELD_LOC_DATE_LENGTH)
        iso.setValue(BuilderConstants.ISO_FIELD_MERCHANT_TYPE, lastTxn?.merchantType, IsoType.NUMERIC, BuilderConstants.ISO_FIELD_MERCHANT_TYPE_LENGTH)
        iso.setValue(BuilderConstants.ISO_FIELD_ENTRY_MODE, "021", IsoType.NUMERIC, BuilderConstants.ISO_FIELD_ENTRY_MODE_LENGTH)
        iso.setValue(BuilderConstants.ISO_FIELD_ACQUIRER_ID, lastTxn?.procId, IsoType.LLVAR, BuilderConstants.ISO_FIELD_ACQUIRER_ID_LENGTH)
        iso.setValue(BuilderConstants.ISO_FIELD_TRACK2_DATA, "6104340109641151=3102220908", IsoType.LLVAR, BuilderConstants.ISO_FIELD_TRACK2_DATA_LENGTH)
        iso.setValue(BuilderConstants.ISO_FIELD_RRN, lastTxn?.rrn, IsoType.ALPHA, BuilderConstants.ISO_FIELD_RRN_LENGTH)      // Original RRN
        iso.setValue(BuilderConstants.ISO_FIELD_RESPONSE_CODE, "00", IsoType.ALPHA, BuilderConstants.ISO_FIELD_RESPONSE_CODE_LENGTH)
        iso.setValue(BuilderConstants.ISO_FIELD_TERMINAL_ID, lastTxn?.terminalId, IsoType.ALPHA, BuilderConstants.ISO_FIELD_TERMINAL_ID_LENGTH)
        iso.setValue(BuilderConstants.ISO_FIELD_MERCHANT_ID, lastTxn?.merchantId, IsoType.ALPHA, BuilderConstants.ISO_FIELD_MERCHANT_ID_LENGTH)
        iso.setValue(BuilderConstants.ISO_FIELD_CURRENCY_CODE, lastTxn?.currencyCode, IsoType.ALPHA, BuilderConstants.ISO_FIELD_CURRENCY_CODE_LENGTH)
        val originalData =
            "0200" +
                    lastTxn?.stan +
                    lastTxn?.transmissionDateTime +
                    lastTxn?.procId +
                    "00000000000"   // forwarding ID if not used

        iso.setValue(BuilderConstants.ISO_FIELD_ORIGINAL_DATA, originalData, IsoType.NUMERIC, BuilderConstants.ISO_FIELD_ORIGINAL_DATA_LENGTH)

        iso.setBinaryHeader(false)
        iso.setBinaryFields(false)
        iso.setForceStringEncoding(true)
        iso.setIsoHeader("")

        return iso.writeData()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun createVoidRequest(builderServiceTxnDetails: BuilderServiceTxnDetails?): ByteArray {
        val lastTxn = IsoMessageBuilder.getLastTxn()
        Log.d("LAST_TXN", lastTxn.toString())
        val iso = IsoMessage()
        iso.setType(BuilderConstants.MTI_REVERSAL_REQ)
        iso.setValue(BuilderConstants.ISO_FIELD_PAN_NO, "6104340109641151", IsoType.LLVAR, BuilderConstants.ISO_FIELD_PAN_NO_LENGTH)
        iso.setValue(BuilderConstants.ISO_FIELD_PROCESSING_CODE, lastTxn?.processingCode?.padStart(6,'0'), IsoType.NUMERIC, BuilderConstants.ISO_FIELD_PROCESSING_CODE_LENGTH)
        iso.setValue(BuilderConstants.ISO_FIELD_AMOUNT, lastTxn?.amount, IsoType.NUMERIC, BuilderConstants.ISO_FIELD_AMOUNT_LENGTH)
        iso.setValue(BuilderConstants.ISO_FIELD_TRANSMISSION_DATE, lastTxn?.transmissionDateTime, IsoType.NUMERIC, BuilderConstants.ISO_FIELD_TRANSMISSION_DATE_LENGTH)
        iso.setValue(BuilderConstants.ISO_FIELD_STAN,lastTxn?.stan , IsoType.NUMERIC, BuilderConstants.ISO_FIELD_STAN_LENGTH)
        iso.setValue(BuilderConstants.ISO_FIELD_LOC_TIME, lastTxn?.localTime, IsoType.NUMERIC, BuilderConstants.ISO_FIELD_LOC_TIME_LENGTH)
        iso.setValue(BuilderConstants.ISO_FIELD_LOC_DATE, lastTxn?.localDate, IsoType.NUMERIC, BuilderConstants.ISO_FIELD_LOC_DATE_LENGTH)
        iso.setValue(14, "4912", IsoType.NUMERIC, BuilderConstants.ISO_FIELD_LOC_DATE_LENGTH)
        iso.setValue(15, "0325", IsoType.NUMERIC, BuilderConstants.ISO_FIELD_LOC_DATE_LENGTH)
        iso.setValue(BuilderConstants.ISO_FIELD_MERCHANT_TYPE, lastTxn?.merchantType, IsoType.NUMERIC, BuilderConstants.ISO_FIELD_MERCHANT_TYPE_LENGTH)
        iso.setValue(BuilderConstants.ISO_FIELD_ENTRY_MODE, "21", IsoType.NUMERIC, BuilderConstants.ISO_FIELD_ENTRY_MODE_LENGTH)
        iso.setValue(BuilderConstants.ISO_FIELD_ACQUIRER_ID, lastTxn?.procId, IsoType.LLVAR, BuilderConstants.ISO_FIELD_ACQUIRER_ID_LENGTH)
        iso.setValue(BuilderConstants.ISO_FIELD_RRN, lastTxn?.rrn, IsoType.ALPHA, BuilderConstants.ISO_FIELD_RRN_LENGTH)      // Original RRN
        iso.setValue(BuilderConstants.ISO_FIELD_RESPONSE_CODE, "00", IsoType.ALPHA, BuilderConstants.ISO_FIELD_RESPONSE_CODE_LENGTH)
        iso.setValue(BuilderConstants.ISO_FIELD_TERMINAL_ID, lastTxn?.terminalId, IsoType.ALPHA, BuilderConstants.ISO_FIELD_TERMINAL_ID_LENGTH)
        iso.setValue(BuilderConstants.ISO_FIELD_MERCHANT_ID, lastTxn?.merchantId, IsoType.ALPHA, BuilderConstants.ISO_FIELD_MERCHANT_ID_LENGTH)
        iso.setValue(BuilderConstants.ISO_FIELD_MERCHANT_NAME,
            builderServiceTxnDetails?.merchantNameLocation?.padEnd(BuilderConstants.ISO_FIELD_MERCHANT_NAME_LENGTH),
            IsoType.ALPHA,
            BuilderConstants.ISO_FIELD_MERCHANT_NAME_LENGTH
        )
        iso.setValue(BuilderConstants.ISO_FIELD_MERCHANT_BANK, builderServiceTxnDetails?.merchantBankName, IsoType.LLLVAR, BuilderConstants.ISO_FIELD_MERCHANT_BANK_LENGTH)         // DE048
        iso.setValue(BuilderConstants.ISO_FIELD_CURRENCY_CODE, lastTxn?.currencyCode, IsoType.ALPHA, BuilderConstants.ISO_FIELD_CURRENCY_CODE_LENGTH)
        iso.setValue(BuilderConstants.ISO_FIELD_POS_CONDITION_CODE, lastTxn?.posConditionCode, IsoType.LLLVAR, BuilderConstants.ISO_FIELD_POS_CONDITION_CODE_LENGTH)
        iso.setValue(59, "39000000000000840", IsoType.LLLVAR, 17)
        iso.setValue(BuilderConstants.ISO_FIELD_RESERVED_PRIVATE, "8018", IsoType.LLLVAR, 6)
        val originalData =
            "0200" +
                    lastTxn?.stan?.padStart(6,'0') +
                    lastTxn?.transmissionDateTime +
                    lastTxn?.procId?.padStart(11, '0') +
                    "00000000000"   // forwarding ID if not used
        Log.d("DE90_DEBUG", "OriginalData (fnsNumber): ${this.builderServiceTxnDetails.fnsNumber ?: "NULL"}")
        iso.setValue(BuilderConstants.ISO_FIELD_ORIGINAL_DATA, originalData, IsoType.NUMERIC, BuilderConstants.ISO_FIELD_ORIGINAL_DATA_LENGTH)
        iso.setValue(BuilderConstants.ISO_FIELD_ADDITIONAL_DATA, builderServiceTxnDetails?.fnsNumber, IsoType.LLLVAR, BuilderConstants.ISO_FIELD_ADDITIONAL_DATA_LENGTH)// DE058
        iso.setBinaryHeader(false)
        iso.setBinaryFields(false)
        iso.setForceStringEncoding(true)
        iso.setIsoHeader("")

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

    // Financial Transaction Response Parsing 0210  // Added By Rushikesh Testing Pending
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
                // Format date/time fields safely
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

    fun parseReversalResponse(context: Context, response: ByteArray): BuilderServiceTxnDetails {
        val details = BuilderServiceTxnDetails()

        try {
            val mf = createMessageFactory(context)
            val isoMsg = mf.parseMessage(response, 0)

            Log.d("ISO", "MTI = %04d".format(isoMsg.type)) // Should be 0430

            details.apply {
                cardPan        = isoMsg.getObjectValue<String>(BuilderConstants.ISO_FIELD_PAN_NO)
                processingCode = isoMsg.getObjectValue<String>(BuilderConstants.ISO_FIELD_PROCESSING_CODE)
                authAmount     = isoMsg.getObjectValue<String>(BuilderConstants.ISO_FIELD_AMOUNT)
                dateTime       = isoMsg.getObjectValue<String>(BuilderConstants.ISO_FIELD_TRANSMISSION_DATE)
                stan           = isoMsg.getObjectValue<String>(BuilderConstants.ISO_FIELD_STAN)
                localTime      = isoMsg.getObjectValue<String>(BuilderConstants.ISO_FIELD_LOC_TIME)
                localDate      = isoMsg.getObjectValue<String>(BuilderConstants.ISO_FIELD_LOC_DATE)
                settlementDate = isoMsg.getObjectValue<String>(BuilderConstants.ISO_FIELD_SET_DATE)
                merchantType   = isoMsg.getObjectValue<String>(BuilderConstants.ISO_FIELD_MERCHANT_TYPE)
                acquirerId     = isoMsg.getObjectValue<String>(BuilderConstants.ISO_FIELD_ACQUIRER_ID)
                track2Data     = isoMsg.getObjectValue<String>(BuilderConstants.ISO_FIELD_TRACK2_DATA)
                rrn            = isoMsg.getObjectValue<String>(BuilderConstants.ISO_FIELD_RRN)
                authId         = isoMsg.getObjectValue<String>(BuilderConstants.ISO_FIELD_AUTH_ID)
                responseCode   = isoMsg.getObjectValue<String>(BuilderConstants.ISO_FIELD_RESPONSE_CODE)
                terminalId     = isoMsg.getObjectValue<String>(BuilderConstants.ISO_FIELD_TERMINAL_ID)
                merchantId     = isoMsg.getObjectValue<String>(BuilderConstants.ISO_FIELD_MERCHANT_ID)
                merchantName   = isoMsg.getObjectValue<String>(BuilderConstants.ISO_FIELD_MERCHANT_NAME)
                merchantBank   = isoMsg.getObjectValue<String>(BuilderConstants.ISO_FIELD_MERCHANT_BANK)
                currencyCode   = isoMsg.getObjectValue<String>(BuilderConstants.ISO_FIELD_CURRENCY_CODE)
                additionalAmt  = isoMsg.getObjectValue<String>(BuilderConstants.ISO_FIELD_ADD_AMOUNT)
                reservedPrivate= isoMsg.getObjectValue<String>(BuilderConstants.ISO_FIELD_RESERVED_PRIVATE)
                posCondition   = isoMsg.getObjectValue<String>(BuilderConstants.ISO_FIELD_POS_CONDITION_CODE)
                privateData    = isoMsg.getObjectValue<String>(BuilderConstants.ISO_FIELD_ADDITIONAL_DATA)
                acquirerTrace  = isoMsg.getObjectValue<String>(BuilderConstants.ISO_FIELD_RESPONSE_TEXT)
                originalData   = isoMsg.getObjectValue<String>(BuilderConstants.ISO_FIELD_ORIGINAL_DATA)

            }

        } catch (e: Exception) {
            Log.e("ISO", "Reversal Parse error", e)
        }

        return details
    }


    fun parseReconciliationResponse(context: Context,response: ByteArray): BuilderServiceTxnDetails {

        val details = BuilderServiceTxnDetails()

        try {
            val mf = createMessageFactory(context)
            val isoMsg = mf.parseMessage(response, 0)

            details.apply {
                dateTime              = isoMsg.getObjectValue<String>(BuilderConstants.ISO_FIELD_TRANSMISSION_DATE)
                stan                  = isoMsg.getObjectValue<String>(BuilderConstants.ISO_FIELD_STAN)
                settlementDate        = isoMsg.getObjectValue<String>(BuilderConstants.ISO_FIELD_SET_DATE)
                acquirerId            = isoMsg.getObjectValue<String>(BuilderConstants.ISO_FIELD_ACQUIRER_ID)
                settlementCode        = isoMsg.getObjectValue<String>(BuilderConstants.ISO_FIELD_SETTLEMENT_CODE)
                creditsNumber         = isoMsg.getObjectValue<String>(BuilderConstants.ISO_FIELD_CREDITS_NUMBER)
                creditsReversalNumber = isoMsg.getObjectValue<String>(BuilderConstants.ISO_FIELD_CREDITS_REV_NUMBER)
                debitsNumber          = isoMsg.getObjectValue<String>(BuilderConstants.ISO_FIELD_DEBITS_NUMBER)
                debitsReversalNumber  = isoMsg.getObjectValue<String>(BuilderConstants.ISO_FIELD_DEBITS_REV_NUMBER)
                inquiriesNumber       = isoMsg.getObjectValue<String>(BuilderConstants.ISO_FIELD_INQUIRIES_NUMBER)
                authorizationsNumber  = isoMsg.getObjectValue<String>(BuilderConstants.ISO_FIELD_AUTH_NUMBER)
                creditsAmount         = isoMsg.getObjectValue<String>(BuilderConstants.ISO_FIELD_CREDITS_AMOUNT)
                creditsReversalAmount = isoMsg.getObjectValue<String>(BuilderConstants.ISO_FIELD_CREDITS_REV_AMOUNT)
                debitsAmount          = isoMsg.getObjectValue<String>(BuilderConstants.ISO_FIELD_DEBITS_AMOUNT)
                debitsReversalAmount  = isoMsg.getObjectValue<String>(BuilderConstants.ISO_FIELD_DEBITS_REV_AMOUNT)
                netSettlementAmount   = isoMsg.getObjectValue<String>(BuilderConstants.ISO_FIELD_NET_SETTLEMENT)
                settlementInstitutionId = isoMsg.getObjectValue<String>(BuilderConstants.ISO_FIELD_SETTLEMENT_INST_ID)
            }

        } catch (e: Exception) {
            Log.e("ISO", "Reconciliation Parse error", e)
        }

        return details
    }



   /*fun buildDummyPurchaseResponse(): ByteArray {
        val iccData = generateDummyIccData()
        val rrn = generateDummyRRN()
        val authCode = generateDummyAuthCode()
        val respCode = generateDummyRespCode()

        /* Don't change the request message. Instead only fill up the response parameters */
        message.type = BuilderConstants.MTI_SALE_RES

        /* TPDU Header */
        message.binaryIsoHeader.apply {
            var destBytes = byteArrayOf(this[1],this[2])
            this[1] = this[3]
            this[2] = this[4]
            this[3] = destBytes[0]
            this[4] = destBytes[1]
        }

            /* Field 37, RRN, AN12, Conditional */
        message.setValue(BuilderConstants.ISO_FIELD_RRN, rrn, IsoType.ALPHA, BuilderConstants.ISO_FIELD_RRN_LENGTH)

            /* Field 38, Auth Code, AN6, Conditional */
            .setValue(BuilderConstants.ISO_FIELD_AUTH_CODE, authCode, IsoType.ALPHA, BuilderConstants.ISO_FIELD_AUTH_CODE_LENGTH)

            /* Field 39, Response Code, AN2, Mandatory */
            .setValue(BuilderConstants.ISO_FIELD_RESP_CODE, respCode , IsoType.ALPHA,BuilderConstants.ISO_FIELD_RESP_CODE_LENGTH)

            /* Field 55, ICC Related Data, B..255, Mandatory */
            .setValue(BuilderConstants.ISO_FIELD_ICC_DATA, iccData, IsoType.LLLBIN, iccData.length)

        return appendIsoLength(message.writeData())
    }*/

    /*fun buildDummyVoidResponse(): ByteArray {
        val respCode = generateDummyRespCode()

        /* Don't change the request message. Instead only fill up the response parameters */
        message.type = BuilderConstants.MTI_VOID_RES

        /* TPDU Header */
        message.binaryIsoHeader.apply {
            var destBytes = byteArrayOf(this[1],this[2])
            this[1] = this[3]
            this[2] = this[4]
            this[3] = destBytes[0]
            this[4] = destBytes[1]
        }

        /* Field 39, Response Code, AN2, Mandatory */
        message.setValue(BuilderConstants.ISO_FIELD_RESP_CODE, respCode , IsoType.ALPHA,BuilderConstants.ISO_FIELD_RESP_CODE_LENGTH)

        return appendIsoLength(message.writeData())
    }*/

    @OptIn(ExperimentalEncodingApi::class, ExperimentalStdlibApi::class)
    fun parsePurchaseResponse(response: ByteArray): BuilderServiceTxnDetails {
        return try {
            var message = messageFactory.parseMessage(
                extractIsoPayload(response),
                BuilderConstants.ISO_HEADER.size,
                true
            )

            builderServiceTxnDetails.apply {
                message.getObjectValue<String>(BuilderConstants.ISO_FIELD_RESP_CODE)
                    ?.let { hostRespCode = it }
                message.getObjectValue<String>(BuilderConstants.ISO_FIELD_AUTH_CODE)
                    ?.let { hostAuthCode = it }
                message.getObjectValue<String>(BuilderConstants.ISO_FIELD_RRN)
                    ?.let { hostTxnRef = it }
                message.getObjectValue<ByteArray>(BuilderConstants.ISO_FIELD_ICC_DATA)
                    ?.let { emvData = it.toHexString().uppercase() }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            builderServiceTxnDetails.apply {
                hostRespCode = null
            }
        }
    }



    @OptIn(ExperimentalEncodingApi::class, ExperimentalStdlibApi::class)
    fun parseVoidResponse(response: ByteArray): BuilderServiceTxnDetails {
        return try {
            var message = messageFactory.parseMessage(
                extractIsoPayload(response),
                BuilderConstants.ISO_HEADER.size,
                true
            )
            builderServiceTxnDetails.apply {
                message.getObjectValue<String>(BuilderConstants.ISO_FIELD_RESP_CODE)
                    ?.let { hostRespCode = it }
                message.getObjectValue<String>(BuilderConstants.ISO_FIELD_RRN)
                    ?.let { hostTxnRef = it }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            builderServiceTxnDetails.apply {
                hostRespCode = null
            }
        }
    }

    fun populateTxnDetails(
        txn: BuilderServiceTxnDetails,
        stan: Int,
        rrn: String,
        processingCode: String,
        amount: Long,
        dateTime: String,
        localTime: String,
        localDate: String,
        maskedPan: String,
        track2Data: String,
        posEntryMode: String,
        currencyCode: String,
        posConditionCode: String
    ): BuilderServiceTxnDetails {

        return txn.apply {

            // Core txn fields
            this.stan = stan.toString()
            this.rrn = rrn
            this.processingCode = processingCode
            this.txnAmount = amount.toString()
            this.ttlAmount = amount.toString()

            // Date/time
            this.dateTime = dateTime
            this.localTime = localTime
            this.localDate = localDate

            // Card details
            this.cardMaskedPan = maskedPan
            this.track2Data = track2Data

            // Merchant / terminal
            this.terminalId = txn.terminalId
            this.merchantId = txn.merchantId
            this.merchantNameLocation = txn.merchantNameLocation
            this.merchantBankName = txn.merchantBankName

            // Other ISO fields
            this.merchantType = txn.merchantType
            this.posEntryMode = posEntryMode
            this.procId = txn.procId
            this.currencyCode = currencyCode
            this.fnsNumber = txn.fnsNumber
            this.posConditionCode = posConditionCode

            // 🔥 IMPORTANT for VOID (DE90 base)
            this.originalData = "0200" +
                    stan.toString().padStart(6, '0') +
                    dateTime.substring(4, 10) +
                    (txn.procId?.padStart(11, '0') ?: "")
        }
    }


}