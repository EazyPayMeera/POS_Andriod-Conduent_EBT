package com.eazypaytech.builder_core.requestBuilder

import android.content.Context
import android.util.Log
import com.eazypaytech.builder_core.constants.BuilderConstants
import com.eazypaytech.builder_core.model.BuilderServiceTxnDetails
import com.eazypaytech.builder_core.model.CardEntryMode
import com.eazypaytech.builder_core.model.PosConditionCode
import com.eazypaytech.builder_core.utils.BuilderUtils
import com.eazypaytech.builder_core.utils.toBcd
import com.eazypaytech.builder_core.utils.toCurrencyLong
import com.solab.iso8583.IsoMessage
import com.solab.iso8583.IsoType
import com.solab.iso8583.MessageFactory
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.random.Random

class ApiRequestBuilderLyra @Inject constructor(@ApplicationContext val context: Context) {
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

    fun getIsoPosEntryMode() : String?
    {
        return when(builderServiceTxnDetails.cardEntryMode)
        {
            CardEntryMode.MANUAL.toString()->"0110"
            CardEntryMode.MAGSTRIPE.toString()->"0210"
            CardEntryMode.CONTACT.toString()->"0510"
            CardEntryMode.CONTACLESS.toString()->"0710"
            CardEntryMode.FALLBACK_MAGSTRIPE.toString()->"8010"
            CardEntryMode.CONTACLESS_MAGSTRIPE.toString()->"9110"
            else -> "0010"
        }
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

    fun getBatchNumber() : String? {
        var batchNumber: String? =
            builderServiceTxnDetails.batchId?.toInt()?.toString()?:"1"
        batchNumber?.padStart(BuilderConstants.ISO_FIELD_PVT_USE_BATCH_LENGTH, '0')?.let {
            batchNumber = it.length.toString()
                .padStart(BuilderConstants.ISO_FIELD_PVT_USE_BATCH_LENGTH_LENGTH, '0') + it
        }
        return batchNumber
    }

    fun getOrigAmount() : String?
    {
        Log.d("OriginalAmountLog", "originalTtlAmount: ${builderServiceTxnDetails.originalTtlAmount}")
        var amount: String? =
            (builderServiceTxnDetails.originalTtlAmount?.trim('[')?.trim(']')?.toDoubleOrNull()?.toCurrencyLong()?:0).toString()
        amount?.padStart(BuilderConstants.ISO_FIELD_AMOUNT_LENGTH, '0')?.let {
            amount = it
        }
        return amount
    }


    fun getInvoiceNumber() : String?
    {
        var invoiceNumber: String? =
            builderServiceTxnDetails.invoiceNo?.toInt()?.toString()
        invoiceNumber?.padStart(BuilderConstants.ISO_FIELD_INVOICE_NUMBER_LENGTH, '0')?.let {
            invoiceNumber = it
        }
        return invoiceNumber
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

    /* Dummy Response Functions */
    fun generateDummyRRN() : String
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


    /* Request Builders */
    @OptIn(ExperimentalEncodingApi::class, ExperimentalStdlibApi::class)
//    fun createRklRequest(): ByteArray {
//        this.builderServiceTxnDetails = builderServiceTxnDetails?: BuilderServiceTxnDetails()
//        val stan = getSTAN()
//
//        message = messageFactory.newMessage(BuilderConstants.MTI_NETWORK_REQ)
//
//        /* TPDU Header */
//        message.binaryIsoHeader = BuilderConstants.ISO_HEADER.apply {
//            this[3] = ((stan/100)%100).toInt().toBcd()
//            this[4] = (stan%100).toInt().toBcd()
//        }
//
//        /* Field 3, Processing Code, N6, Mandatory */
//        message.setValue(BuilderConstants.ISO_FIELD_PROC_CODE, BuilderConstants.PROC_CODE_RKL_FULL_SN, IsoType.NUMERIC,BuilderConstants.ISO_FIELD_PROC_CODE_LENGTH)
//
//        /* Field 11, STAN, N6, Mandatory */
//            .setValue(BuilderConstants.ISO_FIELD_STAN,
//                stan, IsoType.NUMERIC,BuilderConstants.ISO_FIELD_STAN_LENGTH)
//
//        /* Field 12, Time, N6, Mandatory */
//            .setValue(BuilderConstants.ISO_FIELD_TIME,
//            BuilderUtils.getCurrentDateTime(BuilderConstants.DEFAULT_ISO8583_TIME_FORMAT), IsoType.TIME,BuilderConstants.ISO_FIELD_TIME_LENGTH)
//
//        /* Field 13, Date, N4, Mandatory */
//            .setValue(BuilderConstants.ISO_FIELD_DATE,
//            BuilderUtils.getCurrentDateTime(BuilderConstants.DEFAULT_ISO8583_DATE_FORMAT), IsoType.DATE4,BuilderConstants.ISO_FIELD_DATE_LENGTH)
//
//        /* Field 24, NII, N3, Mandatory */
//            .setValue(BuilderConstants.ISO_FIELD_NII, BuilderConstants.DEFAULT_ISO8583_NII, IsoType.NUMERIC,BuilderConstants.ISO_FIELD_NII_LENGTH)
//
//        /* Field 41, TID, ANS8, Mandatory */
//            .setValue(BuilderConstants.ISO_FIELD_TID, builderServiceTxnDetails?.terminalId, IsoType.ALPHA,BuilderConstants.ISO_FIELD_TID_LENGTH)
//
//        /* Field 42, MID, ANS15, Mandatory */
//            .setValue(BuilderConstants.ISO_FIELD_MID, builderServiceTxnDetails?.merchantId, IsoType.ALPHA,BuilderConstants.ISO_FIELD_MID_LENGTH)
//
//        /* Field 60, Serial No, ANS...999, Mandatory */
//            .setValue(BuilderConstants.ISO_FIELD_TERM_SR_NO, builderServiceTxnDetails?.deviceSN, IsoType.LLLVAR,builderServiceTxnDetails?.deviceSN?.length?:0)
//
//        /* Field 62, Working Key, ANS...999, Mandatory */
//            .setValue(BuilderConstants.ISO_FIELD_WORKING_KEY, builderServiceTxnDetails?.devicePublicKey, IsoType.LLLVAR,builderServiceTxnDetails?.devicePublicKey?.length?:0)
//
//        return appendIsoLength(message.writeData())
//    }

    fun createSignOnRequest(builderServiceTxnDetails: BuilderServiceTxnDetails?): ByteArray {
        val iso = IsoMessage()
        iso.setType(BuilderConstants.ISO_TYPE_SIGN_ON)  // MTI 0800
        val time = BuilderUtils.getCurrentDateTime(BuilderConstants.ISO_DATE_FORMAT)
        iso.setValue(BuilderConstants.ISO_FIELD_TRANSMISSION_DATE, time, IsoType.NUMERIC, 10) // Fixed-length numeric 10 digits
        val stan = getSTAN().toString().padStart(6, '0')
        iso.setValue(BuilderConstants.STAN, stan, IsoType.NUMERIC, 6) // Fixed-length numeric 6 digits
        iso.setValue(BuilderConstants.ACQUIRER_ID, builderServiceTxnDetails?.procId, IsoType.LLVAR, BuilderConstants.ISO_FIELD_PROC_ID_LENGTH) // LLVAR length auto-handled
        iso.setValue(BuilderConstants.ISO_FIELD_NET_MGMT_INFO_CODE, BuilderConstants.SIGN_ON_REQUEST, IsoType.NUMERIC, 3) // Fixed-length numeric
        val de096 = "04000007"
        iso.setValue(BuilderConstants.ISO_FIELD_KEY_MGMT_DATA, de096, IsoType.LLLVAR, de096.length) // LLLVAR length auto-handled
        iso.setBinaryHeader(false)        // Use ASCII header
        iso.setBinaryFields(false)        // Use ASCII fields
        iso.setForceStringEncoding(true)  // Ensure ASCII encoding
        iso.setIsoHeader("") // no custom header
        return iso.writeData()
    }

    fun createSignOffRequest(builderServiceTxnDetails: BuilderServiceTxnDetails?): ByteArray {
        val iso = IsoMessage()
        iso.setType(BuilderConstants.ISO_TYPE_SIGN_ON)  // MTI 0800
        val time = BuilderUtils.getCurrentDateTime(BuilderConstants.ISO_DATE_FORMAT)
        iso.setValue(BuilderConstants.ISO_FIELD_TRANSMISSION_DATE, time, IsoType.NUMERIC, 10) // Fixed-length numeric 10 digits
        val stan = getSTAN().toString().padStart(6, '0')
        iso.setValue(BuilderConstants.STAN, stan, IsoType.NUMERIC, 6) // Fixed-length numeric 6 digits
        iso.setValue(BuilderConstants.ACQUIRER_ID, builderServiceTxnDetails?.procId, IsoType.LLVAR, BuilderConstants.ISO_FIELD_PROC_ID_LENGTH) // LLVAR length auto-handled
        iso.setValue(BuilderConstants.ISO_FIELD_NET_MGMT_INFO_CODE, BuilderConstants.SIGN_OFF_REQUEST, IsoType.NUMERIC, 3) // Fixed-length numeric
        val de096 = "04000007"
        iso.setValue(BuilderConstants.ISO_FIELD_KEY_MGMT_DATA, de096, IsoType.LLLVAR, de096.length) // LLLVAR length auto-handled
        iso.setBinaryHeader(false)        // Use ASCII header
        iso.setBinaryFields(false)        // Use ASCII fields
        iso.setForceStringEncoding(true)  // Ensure ASCII encoding
        iso.setIsoHeader("") // no custom header
        return iso.writeData()
    }

    fun createHandShakeRequest(builderServiceTxnDetails: BuilderServiceTxnDetails?): ByteArray {
        val iso = IsoMessage()
        iso.setType(BuilderConstants.ISO_TYPE_SIGN_ON)  // MTI 0800
        val time = BuilderUtils.getCurrentDateTime(BuilderConstants.ISO_DATE_FORMAT)
        iso.setValue(BuilderConstants.ISO_FIELD_TRANSMISSION_DATE, time, IsoType.NUMERIC, 10) // Fixed-length numeric 10 digits
        val stan = getSTAN().toString().padStart(6, '0')
        iso.setValue(BuilderConstants.STAN, stan, IsoType.NUMERIC, 6) // Fixed-length numeric 6 digits
        iso.setValue(BuilderConstants.ACQUIRER_ID, builderServiceTxnDetails?.procId, IsoType.LLVAR, BuilderConstants.ISO_FIELD_PROC_ID_LENGTH) // LLVAR length auto-handled
        iso.setValue(BuilderConstants.ISO_FIELD_NET_MGMT_INFO_CODE, BuilderConstants.HANDSHAKE_REQUEST, IsoType.NUMERIC, 3) // Fixed-length numeric
        val de096 = "04000007"
        iso.setValue(BuilderConstants.ISO_FIELD_KEY_MGMT_DATA, de096, IsoType.LLLVAR, de096.length) // LLLVAR length auto-handled
        iso.setBinaryHeader(false)        // Use ASCII header
        iso.setBinaryFields(false)        // Use ASCII fields
        iso.setForceStringEncoding(true)  // Ensure ASCII encoding
        iso.setIsoHeader("") // no custom header
        return iso.writeData()
    }

    fun createKeyRequest(builderServiceTxnDetails: BuilderServiceTxnDetails?): ByteArray {
        val iso = IsoMessage()
        iso.setType(BuilderConstants.ISO_TYPE_SIGN_ON)  // MTI 0800
        val time = BuilderUtils.getCurrentDateTime(BuilderConstants.ISO_DATE_FORMAT)
        iso.setValue(BuilderConstants.ISO_FIELD_TRANSMISSION_DATE, time, IsoType.NUMERIC, 10) // Fixed-length numeric 10 digits
        val stan = getSTAN().toString().padStart(6, '0')
        iso.setValue(BuilderConstants.STAN, stan, IsoType.NUMERIC, 6) // Fixed-length numeric 6 digits
        iso.setValue(BuilderConstants.ACQUIRER_ID, builderServiceTxnDetails?.procId, IsoType.LLVAR, BuilderConstants.ISO_FIELD_PROC_ID_LENGTH) // LLVAR length auto-handled
        iso.setValue(BuilderConstants.ISO_FIELD_NET_MGMT_INFO_CODE, BuilderConstants.KEY_CHANGE, IsoType.NUMERIC, 3) // Fixed-length numeric
        val de096 = "04000007"
        iso.setValue(BuilderConstants.ISO_FIELD_KEY_MGMT_DATA, de096, IsoType.LLLVAR, de096.length) // LLLVAR length auto-handled
        iso.setBinaryHeader(false)        // Use ASCII header
        iso.setBinaryFields(false)        // Use ASCII fields
        iso.setForceStringEncoding(true)  // Ensure ASCII encoding
        iso.setIsoHeader("") // no custom header
        return iso.writeData()
    }

    fun createKeyChangeRequest(builderServiceTxnDetails: BuilderServiceTxnDetails?): ByteArray {
        val iso = IsoMessage()
        iso.setType(BuilderConstants.ISO_TYPE_SIGN_ON)  // MTI 0800
        val time = BuilderUtils.getCurrentDateTime(BuilderConstants.ISO_DATE_FORMAT)
        iso.setValue(BuilderConstants.ISO_FIELD_TRANSMISSION_DATE, time, IsoType.NUMERIC, 10) // Fixed-length numeric 10 digits
        val stan = getSTAN().toString().padStart(6, '0')
        iso.setValue(BuilderConstants.STAN, stan, IsoType.NUMERIC, 6) // Fixed-length numeric 6 digits
        iso.setValue(BuilderConstants.ACQUIRER_ID, builderServiceTxnDetails?.procId, IsoType.LLVAR, BuilderConstants.ISO_FIELD_PROC_ID_LENGTH) // LLVAR length auto-handled
        iso.setValue(BuilderConstants.ISO_FIELD_NET_MGMT_INFO_CODE, BuilderConstants.KEY_CHANGE_REQUEST, IsoType.NUMERIC, 3) // Fixed-length numeric
        val de096 = "04000007"
        iso.setValue(BuilderConstants.ISO_FIELD_KEY_MGMT_DATA, de096, IsoType.LLLVAR, de096.length) // LLLVAR length auto-handled
        iso.setBinaryHeader(false)        // Use ASCII header
        iso.setBinaryFields(false)        // Use ASCII fields
        iso.setForceStringEncoding(true)  // Ensure ASCII encoding
        iso.setIsoHeader("") // no custom header
        return iso.writeData()
    }

    fun createFinancial0200Request(txn: BuilderServiceTxnDetails): ByteArray {

        val iso = IsoMessage()

        // ---------------- MTI ----------------
        iso.setType(0x0200)   // Financial transaction request

        // ---------------- Fixed Fields ----------------
        iso.setValue(2, "6005281234560300", IsoType.LLVAR, 19)          // DE002 PAN
        iso.setValue(3, "009800", IsoType.NUMERIC, 6)                   // DE003 Processing Code
        iso.setValue(4, "000000000298", IsoType.NUMERIC, 12)            // DE004 Amount
        iso.setValue(7, "1024214918", IsoType.NUMERIC, 10)              // DE007 Transmission DateTime
        iso.setValue(11, "570554", IsoType.NUMERIC, 6)                  // DE011 STAN
        iso.setValue(12, "154918", IsoType.NUMERIC, 6)                  // DE012 Local Time
        iso.setValue(13, "1024", IsoType.NUMERIC, 4)                    // DE013 Local Date
        iso.setValue(15, "1024", IsoType.NUMERIC, 4)                    // DE015 Settlement Date
        iso.setValue(17, "1024", IsoType.NUMERIC, 4)                    // DE017 Capture Date
        iso.setValue(18, "5499", IsoType.NUMERIC, 4)                    // DE018 Merchant Type
        iso.setValue(22, "021", IsoType.NUMERIC, 3)                     // DE022 POS Entry Mode

        // ---------------- LLVAR Fields ----------------
        iso.setValue(32, "00004000002", IsoType.LLVAR, 11)              // DE032 Acquirer ID
        iso.setValue(35, "6005281234560300=4912120782", IsoType.LLVAR, 37) // DE035 Track 2

        // ---------------- Fixed Alphanumeric ----------------
        iso.setValue(37, "529700038968", IsoType.ALPHA, 12)              // DE037 RRN
        iso.setValue(41, "86887658", IsoType.ALPHA, 8)                   // DE041 Terminal ID
        iso.setValue(42, "000008042086887", IsoType.ALPHA, 15)           // DE042 Merchant ID
        iso.setValue(43,
            "1388A TAYLOR AVE   US  PARKVILLE    MDUS".padEnd(40),
            IsoType.ALPHA,
            40
        )                                                                 // DE043 Name/Location

        // ---------------- LLLVAR Fields ----------------
        iso.setValue(48, "APPLE 1 MINI MAR", IsoType.LLLVAR, 16)         // DE048
        iso.setValue(49, "840", IsoType.NUMERIC, 3)                      // DE049 Currency

        // PIN block (hex but sent as ASCII if your lib expects ASCII)
        iso.setValue(52, "096D6CBA5BB6411D", IsoType.ALPHA, 16)          // DE052

        iso.setValue(58, "0000000001", IsoType.LLLVAR, 10)               // DE058
        iso.setValue(111, "EB0070901664", IsoType.LLLVAR, 12)            // DE111
        iso.setValue(127, "10242149180000000000", IsoType.LLLVAR, 20)    // DE127

        // ---------------- Encoding Settings ----------------
        iso.setBinaryHeader(false)
        iso.setBinaryFields(false)
        iso.setForceStringEncoding(true)
        iso.setIsoHeader("")

        return iso.writeData()
    }

//    fun createReversal0420(txn: BuilderServiceTxnDetails): ByteArray {
//
//        val iso = IsoMessage()
//
//        // MTI 0420 = Reversal Advice
//        iso.setType(0x0420)
//
//        iso.setValue(3, txn.processingCode, IsoType.NUMERIC, 6)
//        iso.setValue(4, txn.amount, IsoType.NUMERIC, 12)
//        iso.setValue(7, txn.transmissionDateTime, IsoType.NUMERIC, 10)
//        iso.setValue(11, txn.stan, IsoType.NUMERIC, 6)
//        iso.setValue(12, txn.localTime, IsoType.NUMERIC, 6)
//        iso.setValue(13, txn.localDate, IsoType.NUMERIC, 4)
//
//        iso.setValue(37, txn.rrn, IsoType.ALPHA, 12)      // Original RRN
//        iso.setValue(41, txn.terminalId, IsoType.ALPHA, 8)
//        iso.setValue(42, txn.merchantId, IsoType.ALPHA, 15)
//
//        // DE90 – Original Data Elements (MANDATORY for reversal)
//        // Format: MTI + STAN + TransmissionDT + AcquirerID + ForwardingID
//        val originalData =
//            "0200" +
//                    txn.stan +
//                    txn.transmissionDateTime +
//                    txn.acquirerId.padStart(11, '0') +
//                    "00000000000"   // forwarding ID if not used
//
//        iso.setValue(90, originalData, IsoType.NUMERIC, 42)
//
//        iso.setBinaryHeader(false)
//        iso.setBinaryFields(false)
//        iso.setForceStringEncoding(true)
//        iso.setIsoHeader("")
//
//        return iso.writeData()
//    }

//    fun createReconciliation0500(txn: BuilderServiceTxnDetails, isRepeat: Boolean
//    ): ByteArray {
//
//        val iso = IsoMessage()
//
//        // MTI 0500 = Reconciliation
//        // MTI 0501 = Repeat Reconciliation
//        iso.setType(if (isRepeat) 0x0501 else 0x0500)
//
//        val transmissionDateTime =
//            BuilderUtils.getCurrentDateTime("MMddHHmmss")
//
//        val stan = getSTAN().toString().padStart(6, '0')
//
//        // ---------------- Mandatory Fields ----------------
//        iso.setValue(7, transmissionDateTime, IsoType.NUMERIC, 10)
//        iso.setValue(11, stan, IsoType.NUMERIC, 6)
//        iso.setValue(15, txn.settlementDate, IsoType.NUMERIC, 4)
//
//        iso.setValue(32, txn.acquirerId, IsoType.LLVAR, 11)
//
//        // ---------------- Counters ----------------
//        iso.setValue(74, txn.creditsCount.padStart(10, '0'), IsoType.NUMERIC, 10)
//        iso.setValue(75, txn.creditsReversalCount.padStart(10, '0'), IsoType.NUMERIC, 10)
//        iso.setValue(76, txn.debitsCount.padStart(10, '0'), IsoType.NUMERIC, 10)
//        iso.setValue(77, txn.debitsReversalCount.padStart(10, '0'), IsoType.NUMERIC, 10)
//        iso.setValue(80, txn.inquiriesCount.padStart(10, '0'), IsoType.NUMERIC, 10)
//        iso.setValue(81, txn.authorizationsCount.padStart(10, '0'), IsoType.NUMERIC, 10)
//
//        // ---------------- Amount Totals ----------------
//        iso.setValue(86, txn.creditsAmount.padStart(16, '0'), IsoType.NUMERIC, 16)
//        iso.setValue(87, txn.creditsReversalAmount.padStart(16, '0'), IsoType.NUMERIC, 16)
//        iso.setValue(88, txn.debitsAmount.padStart(16, '0'), IsoType.NUMERIC, 16)
//        iso.setValue(89, txn.debitsReversalAmount.padStart(16, '0'), IsoType.NUMERIC, 16)
//
//        // ---------------- Net Settlement ----------------
//        // Format: C/D + 16 digit amount (total 17)
//        val de97 = txn.netSettlementSign + txn.netSettlementAmount.padStart(16, '0')
//        iso.setValue(97, de97, IsoType.ALPHA, 17)
//
//        // ---------------- Settlement Institution ----------------
//        iso.setValue(99, txn.settlementInstitutionId, IsoType.LLVAR, 11)
//
//        // ---------------- Encoding ----------------
//        iso.setBinaryHeader(false)
//        iso.setBinaryFields(false)
//        iso.setForceStringEncoding(true)
//        iso.setIsoHeader("")
//
//        return iso.writeData()
//    }



    fun parseRklResponse(response: ByteArray): BuilderServiceTxnDetails {
        Log.d("Log", "Inside parseRklResponse")

        val details = BuilderServiceTxnDetails()

        try {
            Log.d("RKLResponse", "Raw bytes length = ${response.size}")
            Log.d(
                "RKLResponse",
                "Raw ASCII = ${response.toString(Charsets.US_ASCII)}"
            )

            // ✅ Parse ISO message (ASCII bitmap, secondary bitmap supported)
            val isoMsg = messageFactory.parseMessage(response, 0)

            fun fieldString(de: Int): String? =
                isoMsg.getField<Any>(de)?.value?.toString()

            // ---- Standard fields ----
            val de7  = fieldString(7)     // Transmission Date & Time
            val de11 = fieldString(11)    // STAN
            val de32 = fieldString(32)    // Acquirer ID
            val de39 = fieldString(39)    // Response Code
            val de70 = fieldString(70)    // Network Management Info Code

            // ---- Text response ("Approved") usually here ----
            val de62 = fieldString(62) ?: fieldString(63)

            // ---- Binary field example (IPEK / key block) ----
            val de96Bytes = isoMsg.getField<Any>(96)?.value as? ByteArray
            val de96Hex = de96Bytes?.joinToString("") { "%02X".format(it) }

            // ---- Logs ----
            Log.d("RKLResponse", "MTI              = ${isoMsg.type}")
            Log.d("RKLResponse", "DE07 (DateTime)  = $de7")
            Log.d("RKLResponse", "DE11 (STAN)      = $de11")
            Log.d("RKLResponse", "DE32 (Acquirer)  = $de32")
            Log.d("RKLResponse", "DE39 (RespCode)  = $de39")
            Log.d("RKLResponse", "DE70 (NMIC)      = $de70")
            Log.d("RKLResponse", "DE62/63 (Text)   = $de62")
            Log.d("RKLResponse", "DE96 (HEX)       = $de96Hex")

            // ---- Populate response object ----
            details.apply {
                dateTime = de7
                stan = de11
                merchantId = de32
                hostRespCode = de39
                hostTxnRef = de70
                cardBrand = de62      // ← "Approved"
                encryptedIpek = de96Hex
            }

        } catch (e: Exception) {
            Log.e("RKLResponse", "ISO parse failed", e)
        }

        return details
    }









    fun createPurchaseRequest(builderServiceTxnDetails: BuilderServiceTxnDetails?): ByteArray {
        this.builderServiceTxnDetails = builderServiceTxnDetails?: BuilderServiceTxnDetails()
        val amount = builderServiceTxnDetails?.ttlAmount?.toDoubleOrNull()?.toCurrencyLong()?:0
        val posEntryMode = getIsoPosEntryMode()
        val posConditionCode = getIsoPosConditionCode()
        val encryptedTrack2Data = getEncryptedTrack2Data()
        val iccData = getIccData()
        val ksn = getKsnTag()
        val batchNumber = getBatchNumber()
        val invoiceNumber = getInvoiceNumber()
        val currencyCode = getCurrencyCode()
        val cardSeqNumber = getCardSeqNum()
        val pinBlock = getPinBlock()
        val stan = getSTAN()

        message = messageFactory.newMessage(BuilderConstants.MTI_SALE_REQ)

        /* TPDU Header */
        message.binaryIsoHeader = BuilderConstants.ISO_HEADER.apply {
            this[3] = ((stan/100)%100).toInt().toBcd()
            this[4] = (stan%100).toInt().toBcd()
        }

            /* Field 3, Processing Code, N6, Mandatory */
        message.setValue(BuilderConstants.ISO_FIELD_PROC_CODE, BuilderConstants.PROC_CODE_SALE, IsoType.NUMERIC,BuilderConstants.ISO_FIELD_PROC_CODE_LENGTH)

            /* Field 4, Amount, N12, Mandatory */
            .setValue(BuilderConstants.ISO_FIELD_AMOUNT, amount, IsoType.NUMERIC,BuilderConstants.ISO_FIELD_AMOUNT_LENGTH)

            /* Field 11, STAN, N6, Mandatory */
            .setValue(BuilderConstants.ISO_FIELD_STAN,
                stan, IsoType.NUMERIC,BuilderConstants.ISO_FIELD_STAN_LENGTH)

            /* Field 12, Time, N6, Mandatory */
            .setValue(BuilderConstants.ISO_FIELD_TIME,
                BuilderUtils.getCurrentDateTime(BuilderConstants.DEFAULT_ISO8583_TIME_FORMAT), IsoType.TIME,BuilderConstants.ISO_FIELD_TIME_LENGTH)

            /* Field 13, Date, N4, Mandatory */
            .setValue(BuilderConstants.ISO_FIELD_DATE,
                BuilderUtils.getCurrentDateTime(BuilderConstants.DEFAULT_ISO8583_DATE_FORMAT), IsoType.DATE4,BuilderConstants.ISO_FIELD_DATE_LENGTH)

            /* Field 22, POS Entry Mode, N3, Mandatory */
            .setValue(BuilderConstants.ISO_FIELD_POS_ENTRY_MODE,
                posEntryMode, IsoType.NUMERIC,posEntryMode?.length?:0)

            /* Field 23, PAN Seq Number, N3, Conditional */
            .setValue(BuilderConstants.ISO_FIELD_PAN_SEQ_NO,
                cardSeqNumber, IsoType.NUMERIC,cardSeqNumber?.length?:0)

            /* Field 24, NII, N3, Mandatory */
            .setValue(BuilderConstants.ISO_FIELD_NII, BuilderConstants.DEFAULT_ISO8583_NII, IsoType.NUMERIC,BuilderConstants.ISO_FIELD_NII_LENGTH)

            /* Field 25, POS Condition Code, N2, Mandatory */
            .setValue(BuilderConstants.ISO_FIELD_POS_CONDITION_CODE, posConditionCode, IsoType.NUMERIC,BuilderConstants.ISO_FIELD_POS_CONDITION_CODE_LENGTH)

            /* Field 35, Track2 Data, ANS..37, Mandatory */
            .setValue(BuilderConstants.ISO_FIELD_TRACK2_DATA, encryptedTrack2Data, IsoType.LLBIN,encryptedTrack2Data?.length?:0)

            /* Field 41, TID, ANS8, Mandatory */
            //.setValue(BuilderConstants.ISO_FIELD_TID, builderServiceTxnDetails?.terminalId, IsoType.ALPHA,BuilderConstants.ISO_FIELD_TID_LENGTH)

            /* Field 42, MID, ANS15, Mandatory */
            .setValue(BuilderConstants.ISO_FIELD_MID, builderServiceTxnDetails?.merchantId, IsoType.ALPHA,BuilderConstants.ISO_FIELD_MID_LENGTH)

            /* Field 48, Additional Data KSN, ANS...999, Mandatory */
            .setValue(BuilderConstants.ISO_FIELD_ADDL_DATA_KSN, ksn, IsoType.LLLVAR,ksn?.length?:0)

            /* Field 49, Currency Code Transaction, N3, Mandatory */
            .setValue(BuilderConstants.ISO_FIELD_CURRENCY_CODE_TXN, currencyCode, IsoType.ALPHA,currencyCode?.length?:0)

            /* Field 52, Pin Block, Binary 64, Mandatory */
            .setValue(BuilderConstants.ISO_FIELD_PIN_BLOCK, pinBlock, IsoType.BINARY,BuilderConstants.ISO_FIELD_PIN_BLOCK_LENGTH)

            /* Field 55, ICC Related Data, B..255, Mandatory */
            .setValue(BuilderConstants.ISO_FIELD_ICC_DATA, iccData, IsoType.LLLBIN,iccData?.length?:0)

            /* Field 60, Batch Number, ANS...999, Mandatory */
            .setValue(BuilderConstants.ISO_FIELD_PVT_USE_BATCH, batchNumber, IsoType.LLLVAR, batchNumber?.length?:0)

            /* Field 62, Invoice Number, N6, Mandatory */
            .setValue(BuilderConstants.ISO_FIELD_INVOICE_NUMBER, invoiceNumber, IsoType.LLLVAR,invoiceNumber?.length?:0)

        return appendIsoLength(message.writeData())
    }

    fun createPreAuthRequest(builderServiceTxnDetails: BuilderServiceTxnDetails?): ByteArray {
        this.builderServiceTxnDetails = builderServiceTxnDetails?: BuilderServiceTxnDetails()
        val amount = builderServiceTxnDetails?.ttlAmount?.toDoubleOrNull()?.toCurrencyLong()?:0
        val posEntryMode = getIsoPosEntryMode()
        val posConditionCode = getIsoPosConditionCode()
        val encryptedTrack2Data = getEncryptedTrack2Data()
        val iccData = getIccData()
        val ksn = getKsnTag()
        val batchNumber = getBatchNumber()
        val invoiceNumber = getInvoiceNumber()
        val currencyCode = getCurrencyCode()
        val cardSeqNumber = getCardSeqNum()
        val pinBlock = getPinBlock()
        val stan = getSTAN()

        message = messageFactory.newMessage(BuilderConstants.MTI_AUTH_REQ)

        /* TPDU Header */
        message.binaryIsoHeader = BuilderConstants.ISO_HEADER.apply {
            this[3] = ((stan/100)%100).toInt().toBcd()
            this[4] = (stan%100).toInt().toBcd()
        }

        /* Field 3, Processing Code, N6, Mandatory */
        message.setValue(BuilderConstants.ISO_FIELD_PROC_CODE, BuilderConstants.PROC_CODE_SALE, IsoType.NUMERIC,BuilderConstants.ISO_FIELD_PROC_CODE_LENGTH)

            /* Field 4, Amount, N12, Mandatory */
            .setValue(BuilderConstants.ISO_FIELD_AMOUNT, amount, IsoType.NUMERIC,BuilderConstants.ISO_FIELD_AMOUNT_LENGTH)

            /* Field 11, STAN, N6, Mandatory */
            .setValue(BuilderConstants.ISO_FIELD_STAN,
                stan, IsoType.NUMERIC,BuilderConstants.ISO_FIELD_STAN_LENGTH)

            /* Field 12, Time, N6, Mandatory */
            .setValue(BuilderConstants.ISO_FIELD_TIME,
                BuilderUtils.getCurrentDateTime(BuilderConstants.DEFAULT_ISO8583_TIME_FORMAT), IsoType.TIME,BuilderConstants.ISO_FIELD_TIME_LENGTH)

            /* Field 13, Date, N4, Mandatory */
            .setValue(BuilderConstants.ISO_FIELD_DATE,
                BuilderUtils.getCurrentDateTime(BuilderConstants.DEFAULT_ISO8583_DATE_FORMAT), IsoType.DATE4,BuilderConstants.ISO_FIELD_DATE_LENGTH)

            /* Field 22, POS Entry Mode, N3, Mandatory */
            .setValue(BuilderConstants.ISO_FIELD_POS_ENTRY_MODE,
                posEntryMode, IsoType.NUMERIC,posEntryMode?.length?:0)

            /* Field 23, PAN Seq Number, N3, Conditional */
            .setValue(BuilderConstants.ISO_FIELD_PAN_SEQ_NO,
                cardSeqNumber, IsoType.NUMERIC,cardSeqNumber?.length?:0)

            /* Field 24, NII, N3, Mandatory */
            .setValue(BuilderConstants.ISO_FIELD_NII, BuilderConstants.DEFAULT_ISO8583_NII, IsoType.NUMERIC,BuilderConstants.ISO_FIELD_NII_LENGTH)

            /* Field 25, POS Condition Code, N2, Mandatory */
            .setValue(BuilderConstants.ISO_FIELD_POS_CONDITION_CODE, posConditionCode, IsoType.NUMERIC,BuilderConstants.ISO_FIELD_POS_CONDITION_CODE_LENGTH)

            /* Field 35, Track2 Data, ANS..37, Mandatory */
            .setValue(BuilderConstants.ISO_FIELD_TRACK2_DATA, encryptedTrack2Data, IsoType.LLBIN,encryptedTrack2Data?.length?:0)

            /* Field 41, TID, ANS8, Mandatory */
            //.setValue(BuilderConstants.ISO_FIELD_TID, builderServiceTxnDetails?.terminalId, IsoType.ALPHA,BuilderConstants.ISO_FIELD_TID_LENGTH)

            /* Field 42, MID, ANS15, Mandatory */
            .setValue(BuilderConstants.ISO_FIELD_MID, builderServiceTxnDetails?.merchantId, IsoType.ALPHA,BuilderConstants.ISO_FIELD_MID_LENGTH)

            /* Field 48, Additional Data KSN, ANS...999, Mandatory */
            .setValue(BuilderConstants.ISO_FIELD_ADDL_DATA_KSN, ksn, IsoType.LLLVAR,ksn?.length?:0)

            /* Field 49, Currency Code Transaction, N3, Mandatory */
            .setValue(BuilderConstants.ISO_FIELD_CURRENCY_CODE_TXN, currencyCode, IsoType.ALPHA,currencyCode?.length?:0)

            /* Field 52, Pin Block, Binary 64, Mandatory */
            .setValue(BuilderConstants.ISO_FIELD_PIN_BLOCK, pinBlock, IsoType.BINARY,BuilderConstants.ISO_FIELD_PIN_BLOCK_LENGTH)

            /* Field 55, ICC Related Data, B..255, Mandatory */
            .setValue(BuilderConstants.ISO_FIELD_ICC_DATA, iccData, IsoType.LLLBIN,iccData?.length?:0)

            /* Field 60, Batch Number, ANS...999, Mandatory */
            .setValue(BuilderConstants.ISO_FIELD_PVT_USE_BATCH, batchNumber, IsoType.LLLVAR, batchNumber?.length?:0)

            /* Field 62, Invoice Number, N6, Mandatory */
            .setValue(BuilderConstants.ISO_FIELD_INVOICE_NUMBER, invoiceNumber, IsoType.LLLVAR,invoiceNumber?.length?:0)

        return appendIsoLength(message.writeData())
    }

    fun CreateRefundRequest(builderServiceTxnDetails: BuilderServiceTxnDetails?): ByteArray {
        Log.d("TransactionTypeDebug", "Original Transaction Type: ${builderServiceTxnDetails?.originalTxnType}")

        Log.d("Request_date","CreateRefundRequest")
        this.builderServiceTxnDetails = builderServiceTxnDetails?: BuilderServiceTxnDetails()
        val amount = builderServiceTxnDetails?.ttlAmount?.toDoubleOrNull()?.toCurrencyLong()?:0
        val posEntryMode = getIsoPosEntryMode()
        val posConditionCode = getIsoPosConditionCode()
        val encryptedTrack2Data = getEncryptedTrack2Data()
        val iccData = getIccData()
        val ksn = getKsnTag()
        val batchNumber = getBatchNumber()
        val invoiceNumber = getInvoiceNumber()
        val currencyCode = getCurrencyCode()
        val cardSeqNumber = getCardSeqNum()
        val pinBlock = getPinBlock()
        val stan = getSTAN()

        message = messageFactory.newMessage(BuilderConstants.MTI_SALE_REQ)

         //TPDU Header
        message.binaryIsoHeader = BuilderConstants.ISO_HEADER.apply {
            this[3] = ((stan/100)%100).toInt().toBcd()
            this[4] = (stan%100).toInt().toBcd()
        }

        /* Field 3, Processing Code, N6, Mandatory */
        message.setValue(BuilderConstants.ISO_FIELD_PROC_CODE, BuilderConstants.PROC_CODE_REFUND, IsoType.NUMERIC,BuilderConstants.ISO_FIELD_PROC_CODE_LENGTH)

            /* Field 4, Amount, N12, Mandatory */
            .setValue(BuilderConstants.ISO_FIELD_AMOUNT, amount, IsoType.NUMERIC,BuilderConstants.ISO_FIELD_AMOUNT_LENGTH)

            /* Field 11, STAN, N6, Mandatory */
            .setValue(BuilderConstants.ISO_FIELD_STAN,
                stan, IsoType.NUMERIC,BuilderConstants.ISO_FIELD_STAN_LENGTH)

            /* Field 12, Time, N6, Mandatory */
            .setValue(BuilderConstants.ISO_FIELD_TIME,
                BuilderUtils.getCurrentDateTime(BuilderConstants.DEFAULT_ISO8583_TIME_FORMAT), IsoType.TIME,BuilderConstants.ISO_FIELD_TIME_LENGTH)

            /* Field 13, Date, N4, Mandatory */
            .setValue(BuilderConstants.ISO_FIELD_DATE,
                BuilderUtils.getCurrentDateTime(BuilderConstants.DEFAULT_ISO8583_DATE_FORMAT), IsoType.DATE4,BuilderConstants.ISO_FIELD_DATE_LENGTH)

            /* Field 22, POS Entry Mode, N3, Mandatory */
            .setValue(BuilderConstants.ISO_FIELD_POS_ENTRY_MODE,
                posEntryMode, IsoType.NUMERIC,posEntryMode?.length?:0)

            /* Field 23, PAN Seq Number, N3, Conditional */
            .setValue(BuilderConstants.ISO_FIELD_PAN_SEQ_NO,
                cardSeqNumber, IsoType.NUMERIC,cardSeqNumber?.length?:0)

            /* Field 24, NII, N3, Mandatory */
            .setValue(BuilderConstants.ISO_FIELD_NII, BuilderConstants.DEFAULT_ISO8583_NII, IsoType.NUMERIC,BuilderConstants.ISO_FIELD_NII_LENGTH)

            /* Field 25, POS Condition Code, N2, Mandatory */
            .setValue(BuilderConstants.ISO_FIELD_POS_CONDITION_CODE, posConditionCode, IsoType.NUMERIC,BuilderConstants.ISO_FIELD_POS_CONDITION_CODE_LENGTH)

            /* Field 35, Track2 Data, ANS..37, Mandatory */
            .setValue(BuilderConstants.ISO_FIELD_TRACK2_DATA, encryptedTrack2Data, IsoType.LLBIN,encryptedTrack2Data?.length?:0)

            /* Field 41, TID, ANS8, Mandatory */
            //.setValue(BuilderConstants.ISO_FIELD_TID, builderServiceTxnDetails?.terminalId, IsoType.ALPHA,BuilderConstants.ISO_FIELD_TID_LENGTH)

            /* Field 42, MID, ANS15, Mandatory */
            .setValue(BuilderConstants.ISO_FIELD_MID, builderServiceTxnDetails?.merchantId, IsoType.ALPHA,BuilderConstants.ISO_FIELD_MID_LENGTH)

            /* Field 48, Additional Data KSN, ANS...999, Mandatory */
            .setValue(BuilderConstants.ISO_FIELD_ADDL_DATA_KSN, ksn, IsoType.LLLVAR,ksn?.length?:0)

            /* Field 49, Currency Code Transaction, N3, Mandatory */
            .setValue(BuilderConstants.ISO_FIELD_CURRENCY_CODE_TXN, currencyCode, IsoType.ALPHA,currencyCode?.length?:0)

            /* Field 52, Pin Block, Binary 64, Mandatory */
            .setValue(BuilderConstants.ISO_FIELD_PIN_BLOCK, pinBlock, IsoType.BINARY,BuilderConstants.ISO_FIELD_PIN_BLOCK_LENGTH)

            /* Field 55, ICC Related Data, B..255, Mandatory */
            .setValue(BuilderConstants.ISO_FIELD_ICC_DATA, iccData, IsoType.LLLBIN,iccData?.length?:0)

            /* Field 60, Batch Number, ANS...999, Mandatory */
            .setValue(BuilderConstants.ISO_FIELD_PVT_USE_BATCH, batchNumber, IsoType.LLLVAR, batchNumber?.length?:0)

            /* Field 62, Invoice Number, N6, Mandatory */
            .setValue(BuilderConstants.ISO_FIELD_INVOICE_NUMBER, invoiceNumber, IsoType.LLLVAR,invoiceNumber?.length?:0)

        return appendIsoLength(message.writeData())
    }

    fun CreateVoidRequest(builderServiceTxnDetails: BuilderServiceTxnDetails?): ByteArray {
        Log.d("TransactionTypeDebug", "Original Transaction Type: ${builderServiceTxnDetails?.originalTxnType}")

        Log.d("Request_date","CreateVoidRequest")
        this.builderServiceTxnDetails = builderServiceTxnDetails?: BuilderServiceTxnDetails()
        val amount = 0
        val originalAmt = getOrigAmount()
        Log.d("AmountDebug", "After fetching amount")
        val posEntryMode = getIsoPosEntryMode()
        val posConditionCode = getIsoPosConditionCode()
        val encryptedPan = getEncryptedPAN()
        val iccData = getIccData()
        val ksn = getKsnTag()
        val batchNumber = getBatchNumber()
        val invoiceNumber = getInvoiceNumber()
        val currencyCode = getCurrencyCode()
        val cardSeqNumber = getCardSeqNum()
        val pinBlock = getPinBlock()
        val stan = getSTAN()
        val rrn = builderServiceTxnDetails?.originalHostTxnRef?.trim('[')?.trim(']')
        val authCode = getAuthCode()
        val procCode = getProcessingCodeForVoid()
        Log.d("Void Request", "Original Amount: $originalAmt")
        Log.d("Void Request", "Original Host Transaction Reference: ${builderServiceTxnDetails?.ttlAmount}")

        message = messageFactory.newMessage(BuilderConstants.MTI_VOID_REQ)

        //TPDU Header
        message.binaryIsoHeader = BuilderConstants.ISO_HEADER.apply {
            this[3] = ((stan/100)%100).toInt().toBcd()
            this[4] = (stan%100).toInt().toBcd()
        }

            /* Field 2, PAN, N..19, Mandatory */
             message.setValue(BuilderConstants.ISO_FIELD_PAN, encryptedPan, IsoType.LLBIN,encryptedPan?.length?:0)

            /* Field 3, Processing Code, N6, Mandatory */
            .setValue(BuilderConstants.ISO_FIELD_PROC_CODE, procCode, IsoType.NUMERIC,BuilderConstants.ISO_FIELD_PROC_CODE_LENGTH)

            /* Field 4, Amount, N12, Mandatory */
            .setValue(BuilderConstants.ISO_FIELD_AMOUNT, amount, IsoType.NUMERIC,BuilderConstants.ISO_FIELD_AMOUNT_LENGTH)

            /* Field 11, STAN, N6, Mandatory */
            .setValue(BuilderConstants.ISO_FIELD_STAN,
                stan, IsoType.NUMERIC,BuilderConstants.ISO_FIELD_STAN_LENGTH)

            /* Field 12, Time, N6, Mandatory */
            .setValue(BuilderConstants.ISO_FIELD_TIME,
                BuilderUtils.getCurrentDateTime(BuilderConstants.DEFAULT_ISO8583_TIME_FORMAT), IsoType.TIME,BuilderConstants.ISO_FIELD_TIME_LENGTH)

            /* Field 13, Date, N4, Mandatory */
            .setValue(BuilderConstants.ISO_FIELD_DATE,
                BuilderUtils.getCurrentDateTime(BuilderConstants.DEFAULT_ISO8583_DATE_FORMAT), IsoType.DATE4,BuilderConstants.ISO_FIELD_DATE_LENGTH)

            /* Field 22, POS Entry Mode, N3, Mandatory */
            .setValue(BuilderConstants.ISO_FIELD_POS_ENTRY_MODE,
                posEntryMode, IsoType.NUMERIC,posEntryMode?.length?:0)

            /* Field 23, PAN Seq Number, N3, Conditional */
            .setValue(BuilderConstants.ISO_FIELD_PAN_SEQ_NO,
                cardSeqNumber, IsoType.NUMERIC,cardSeqNumber?.length?:0)

            /* Field 24, NII, N3, Mandatory */
            .setValue(BuilderConstants.ISO_FIELD_NII, BuilderConstants.DEFAULT_ISO8583_NII, IsoType.NUMERIC,BuilderConstants.ISO_FIELD_NII_LENGTH)

            /* Field 25, POS Condition Code, N2, Mandatory */
            .setValue(BuilderConstants.ISO_FIELD_POS_CONDITION_CODE, posConditionCode, IsoType.NUMERIC,BuilderConstants.ISO_FIELD_POS_CONDITION_CODE_LENGTH)

            /* Field 37, RRN, ANS..37, Mandatory */
            .setValue(BuilderConstants.ISO_FIELD_RRN, rrn, IsoType.ALPHA,BuilderConstants.ISO_FIELD_RRN_LENGTH)

            /* Field 38, Auth Code, AN6, Mandatory */
            .setValue(BuilderConstants.ISO_FIELD_AUTH_CODE, authCode, IsoType.ALPHA,BuilderConstants.ISO_FIELD_AUTH_CODE_LENGTH)

            /* Field 39, Response Code, AN2, Mandatory */
            .setValue(BuilderConstants.ISO_FIELD_RESP_CODE, builderServiceTxnDetails?.hostRespCode, IsoType.ALPHA,BuilderConstants.ISO_FIELD_RESP_CODE_LENGTH)

            /* Field 41, TID, ANS8, Mandatory */
            //.setValue(BuilderConstants.ISO_FIELD_TID, builderServiceTxnDetails?.terminalId, IsoType.ALPHA,BuilderConstants.ISO_FIELD_TID_LENGTH)

            /* Field 42, MID, ANS15, Mandatory */
            .setValue(BuilderConstants.ISO_FIELD_MID, builderServiceTxnDetails?.merchantId, IsoType.ALPHA,BuilderConstants.ISO_FIELD_MID_LENGTH)

            /* Field 48, Additional Data KSN, ANS...999, Mandatory */
            .setValue(BuilderConstants.ISO_FIELD_ADDL_DATA_KSN, ksn, IsoType.LLLVAR,ksn?.length?:0)

            /* Field 49, Currency Code Transaction, N3, Mandatory */
            .setValue(BuilderConstants.ISO_FIELD_CURRENCY_CODE_TXN, currencyCode, IsoType.ALPHA,currencyCode?.length?:0)

            /* Field 52, Pin Block, Binary 64, Mandatory */
            .setValue(BuilderConstants.ISO_FIELD_PIN_BLOCK, pinBlock, IsoType.BINARY,BuilderConstants.ISO_FIELD_PIN_BLOCK_LENGTH)

            /* Field 55, ICC Related Data, B..255, Mandatory */
            .setValue(BuilderConstants.ISO_FIELD_ICC_DATA, iccData, IsoType.LLLBIN,iccData?.length?:0)

            /* Field 60, Batch Number, ANS...999, Mandatory */
            .setValue(BuilderConstants.ISO_FIELD_PVT_USE_BATCH, batchNumber + originalAmt, IsoType.LLLVAR, batchNumber?.length?:0)

            /* Field 62, Invoice Number, N6, Mandatory */
            .setValue(BuilderConstants.ISO_FIELD_INVOICE_NUMBER, invoiceNumber, IsoType.LLLVAR,invoiceNumber?.length?:0)

        return appendIsoLength(message.writeData())
    }

    fun createAuthCapRequest(builderServiceTxnDetails: BuilderServiceTxnDetails?): ByteArray {
        this.builderServiceTxnDetails = builderServiceTxnDetails?: BuilderServiceTxnDetails()
        val amount = builderServiceTxnDetails?.ttlAmount?.toDoubleOrNull()?.toCurrencyLong()?:0
        val posEntryMode = getIsoPosEntryMode()
        val posConditionCode = getIsoPosConditionCode()
        val encryptedTrack2Data = getEncryptedTrack2Data()
        val encryptedPan = getEncryptedPAN()
        val iccData = getIccData()
        val ksn = getKsnTag()
        val batchNumber = getBatchNumber()
        val invoiceNumber = getInvoiceNumber()
        val currencyCode = getCurrencyCode()
        val cardSeqNumber = getCardSeqNum()
        val pinBlock = getPinBlock()
        val stan = getSTAN()
        val rrn = builderServiceTxnDetails?.originalHostTxnRef?.trim('[')?.trim(']')

        message = messageFactory.newMessage(BuilderConstants.MTI_VOID_REQ)

        /* TPDU Header */
        message.binaryIsoHeader = BuilderConstants.ISO_HEADER.apply {
            this[3] = ((stan/100)%100).toInt().toBcd()
            this[4] = (stan%100).toInt().toBcd()
        }

        /* Field 2, PAN, N..19, Mandatory */
        message.setValue(BuilderConstants.ISO_FIELD_PAN, encryptedPan, IsoType.LLBIN,encryptedPan?.length?:0)


        /* Field 3, Processing Code, N6, Mandatory */
        message.setValue(BuilderConstants.ISO_FIELD_PROC_CODE, BuilderConstants.PROC_CODE_SALE, IsoType.NUMERIC,BuilderConstants.ISO_FIELD_PROC_CODE_LENGTH)

            /* Field 4, Amount, N12, Mandatory */
            .setValue(BuilderConstants.ISO_FIELD_AMOUNT, amount, IsoType.NUMERIC,BuilderConstants.ISO_FIELD_AMOUNT_LENGTH)

            /* Field 11, STAN, N6, Mandatory */
            .setValue(BuilderConstants.ISO_FIELD_STAN,
                stan, IsoType.NUMERIC,BuilderConstants.ISO_FIELD_STAN_LENGTH)

            /* Field 12, Time, N6, Mandatory */
            .setValue(BuilderConstants.ISO_FIELD_TIME,
                BuilderUtils.getCurrentDateTime(BuilderConstants.DEFAULT_ISO8583_TIME_FORMAT), IsoType.TIME,BuilderConstants.ISO_FIELD_TIME_LENGTH)

            /* Field 13, Date, N4, Mandatory */
            .setValue(BuilderConstants.ISO_FIELD_DATE,
                BuilderUtils.getCurrentDateTime(BuilderConstants.DEFAULT_ISO8583_DATE_FORMAT), IsoType.DATE4,BuilderConstants.ISO_FIELD_DATE_LENGTH)

            /* Field 22, POS Entry Mode, N3, Mandatory */
            .setValue(BuilderConstants.ISO_FIELD_POS_ENTRY_MODE,
                posEntryMode, IsoType.NUMERIC,posEntryMode?.length?:0)

            /* Field 23, PAN Seq Number, N3, Conditional */
            .setValue(BuilderConstants.ISO_FIELD_PAN_SEQ_NO,
                cardSeqNumber, IsoType.NUMERIC,cardSeqNumber?.length?:0)

            /* Field 24, NII, N3, Mandatory */
            .setValue(BuilderConstants.ISO_FIELD_NII, BuilderConstants.DEFAULT_ISO8583_NII, IsoType.NUMERIC,BuilderConstants.ISO_FIELD_NII_LENGTH)

            /* Field 25, POS Condition Code, N2, Mandatory */
            .setValue(BuilderConstants.ISO_FIELD_POS_CONDITION_CODE, posConditionCode, IsoType.NUMERIC,BuilderConstants.ISO_FIELD_POS_CONDITION_CODE_LENGTH)

            /* Field 35, Track2 Data, ANS..37, Mandatory */
            .setValue(BuilderConstants.ISO_FIELD_TRACK2_DATA, encryptedTrack2Data, IsoType.LLBIN,encryptedTrack2Data?.length?:0)

            /* Field 37, RRN, ANS..37, Mandatory */
            .setValue(BuilderConstants.ISO_FIELD_RRN, rrn, IsoType.ALPHA,BuilderConstants.ISO_FIELD_RRN_LENGTH)

            /* Field 41, TID, ANS8, Mandatory */
            //.setValue(BuilderConstants.ISO_FIELD_TID, builderServiceTxnDetails?.terminalId, IsoType.ALPHA,BuilderConstants.ISO_FIELD_TID_LENGTH)

            /* Field 42, MID, ANS15, Mandatory */
            .setValue(BuilderConstants.ISO_FIELD_MID, builderServiceTxnDetails?.merchantId, IsoType.ALPHA,BuilderConstants.ISO_FIELD_MID_LENGTH)

            /* Field 48, Additional Data KSN, ANS...999, Mandatory */
            .setValue(BuilderConstants.ISO_FIELD_ADDL_DATA_KSN, ksn, IsoType.LLLVAR,ksn?.length?:0)

            /* Field 49, Currency Code Transaction, N3, Mandatory */
            .setValue(BuilderConstants.ISO_FIELD_CURRENCY_CODE_TXN, currencyCode, IsoType.ALPHA,currencyCode?.length?:0)

            /* Field 52, Pin Block, Binary 64, Mandatory */
            .setValue(BuilderConstants.ISO_FIELD_PIN_BLOCK, pinBlock, IsoType.BINARY,BuilderConstants.ISO_FIELD_PIN_BLOCK_LENGTH)

            /* Field 55, ICC Related Data, B..255, Mandatory */
            .setValue(BuilderConstants.ISO_FIELD_ICC_DATA, iccData, IsoType.LLLBIN,iccData?.length?:0)

            /* Field 60, Batch Number, ANS...999, Mandatory */
            .setValue(BuilderConstants.ISO_FIELD_PVT_USE_BATCH, batchNumber, IsoType.LLLVAR, batchNumber?.length?:0)

            /* Field 62, Invoice Number, N6, Mandatory */
            .setValue(BuilderConstants.ISO_FIELD_INVOICE_NUMBER, invoiceNumber, IsoType.LLLVAR,invoiceNumber?.length?:0)

        return appendIsoLength(message.writeData())
    }

    fun buildDummyPurchaseResponse(): ByteArray {
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
    }

    fun buildDummyVoidResponse(): ByteArray {
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
    }

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
}