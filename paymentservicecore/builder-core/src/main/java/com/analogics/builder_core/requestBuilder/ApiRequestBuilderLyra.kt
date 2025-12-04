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

    fun createRklRequest(): ByteArray {
        // ---------- Dynamic Fields ----------
        val de007 = BuilderUtils.getCurrentDateTime("MMddHHmmss") // Transmission date/time MMDDhhmmss
        val stan = getSTAN()                                      // STAN (6 digits)
        val de011 = stan.toString().padStart(6, '0')             // Ensure 6-digit STAN
        val de032 = "00004000002"                                 // Acquirer ID
        val de032Prefixed = de032.length.toString().padStart(2, '0') + de032 // LLVAR
        val de070 = "180".padStart(3, '0')                       // Network Management Code
        val de096 = "04000007"                                    // Key Management Data
        val de096Prefixed = de096.length.toString().padStart(3, '0') + de096 // LLLVAR
        val bitmapPrimary = "8220000100000000"
        val bitmapSecondary = "0400000100000000"
        val bitmap = bitmapPrimary + bitmapSecondary
        val mti = "0800"
        val messageStr = mti + bitmap + de007 + de011 + de032Prefixed + de070 + de096Prefixed
        val bodyBytes = messageStr.toByteArray(Charsets.US_ASCII)
        return bodyBytes
    }






    @OptIn(ExperimentalEncodingApi::class, ExperimentalStdlibApi::class)
    fun parseRklResponse(response: ByteArray): BuilderServiceTxnDetails {
        return try {
            message = messageFactory.parseMessage(
                extractIsoPayload(response),
                BuilderConstants.ISO_HEADER.size,
                true
            )

            builderServiceTxnDetails.apply {
                message.getObjectValue<String>(BuilderConstants.ISO_FIELD_RESP_CODE)
                    ?.let { hostRespCode = it }
                message.getObjectValue<String>(BuilderConstants.ISO_FIELD_ADDL_DATA_KSN)?.let {
                    if (it.slice(0 until BuilderConstants.ISO_FIELD_KSN_TAG.length) == BuilderConstants.ISO_FIELD_KSN_TAG) {
                        var length =
                            it.slice(BuilderConstants.ISO_FIELD_KSN_TAG.length until BuilderConstants.ISO_FIELD_KSN_TAG.length + 3)
                                .toInt()
                        if (it.length == length + BuilderConstants.ISO_FIELD_KSN_TAG.length + 3)
                            ksn =
                                it.slice(BuilderConstants.ISO_FIELD_KSN_TAG.length + 3 until it.length)
                    }
                }
                message.getObjectValue<String>(BuilderConstants.ISO_FIELD_WORKING_KEY)?.let {
                    if (it.length > BuilderConstants.ISO_FIELD_KCV_LENGTH) {
                        kcv = it.slice(0 until BuilderConstants.ISO_FIELD_KCV_LENGTH)
                        encryptedIpek =
                            it.slice(BuilderConstants.ISO_FIELD_KCV_LENGTH until it.length)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            builderServiceTxnDetails.apply {
                hostRespCode = null
            }
        }
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
            .setValue(BuilderConstants.ISO_FIELD_TID, builderServiceTxnDetails?.terminalId, IsoType.ALPHA,BuilderConstants.ISO_FIELD_TID_LENGTH)

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
            .setValue(BuilderConstants.ISO_FIELD_TID, builderServiceTxnDetails?.terminalId, IsoType.ALPHA,BuilderConstants.ISO_FIELD_TID_LENGTH)

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
            .setValue(BuilderConstants.ISO_FIELD_TID, builderServiceTxnDetails?.terminalId, IsoType.ALPHA,BuilderConstants.ISO_FIELD_TID_LENGTH)

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
            .setValue(BuilderConstants.ISO_FIELD_TID, builderServiceTxnDetails?.terminalId, IsoType.ALPHA,BuilderConstants.ISO_FIELD_TID_LENGTH)

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
            .setValue(BuilderConstants.ISO_FIELD_TID, builderServiceTxnDetails?.terminalId, IsoType.ALPHA,BuilderConstants.ISO_FIELD_TID_LENGTH)

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