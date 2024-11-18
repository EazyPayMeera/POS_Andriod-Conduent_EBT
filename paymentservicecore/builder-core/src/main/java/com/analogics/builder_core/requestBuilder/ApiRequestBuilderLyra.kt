package com.analogics.builder_core.requestBuilder

import android.content.Context
import com.analogics.builder_core.constants.BuilderConstants
import com.analogics.builder_core.model.BuilderServiceTxnDetails
import com.analogics.builder_core.utils.BuilderUtils
import com.analogics.builder_core.utils.toCurrencyLong
import com.solab.iso8583.IsoMessage
import com.solab.iso8583.IsoType
import com.solab.iso8583.MessageFactory
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlin.io.encoding.ExperimentalEncodingApi

class ApiRequestBuilderLyra @Inject constructor(@ApplicationContext val context: Context) {
    val messageFactory = MessageFactory<IsoMessage>()
    var builderServiceTxnDetails = BuilderServiceTxnDetails()

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

    fun getIsoPosEntryMode(builderServiceTxnDetails: BuilderServiceTxnDetails?) : String?
    {
        return "051"
    }

    fun getIsoPosConditionCode(builderServiceTxnDetails: BuilderServiceTxnDetails?) : String?
    {
        return "00"
    }

    fun getEncryptedTrack2Data(builderServiceTxnDetails: BuilderServiceTxnDetails?) : String?
    {
        var trackData : String? =null
        builderServiceTxnDetails?.trackData?.let {
            trackData = it
        }
        return trackData
    }

    fun getIccData(builderServiceTxnDetails: BuilderServiceTxnDetails?) : String?
    {
        var iccData : String? =null
        builderServiceTxnDetails?.emvData?.let {
            iccData = it
        }
        return iccData
    }

    fun getKsnTag(builderServiceTxnDetails: BuilderServiceTxnDetails?) : String?
    {
        var ksn :String? = null
        builderServiceTxnDetails?.ksn?.padStart(BuilderConstants.ISO_FIELD_KSN_LENGTH,BuilderConstants.ISO_FIELD_KSN_PAD_CHAR)?.let {
            ksn = BuilderConstants.ISO_FIELD_KSN_TAG + it.length.toString().padStart(3,'0') + it
        }
        return ksn
    }

    fun getBatchNumber(builderServiceTxnDetails: BuilderServiceTxnDetails?) : String? {
        var batchNumber: String? =
            builderServiceTxnDetails?.batchId?.toInt()?.toString()?:"1"
        batchNumber?.padStart(BuilderConstants.ISO_FIELD_PVT_USE_BATCH_LENGTH, '0')?.let {
            batchNumber = it.length.toString()
                .padStart(BuilderConstants.ISO_FIELD_PVT_USE_BATCH_LENGTH_LENGTH, '0') + it
        }
        return batchNumber
    }

    fun getInvoiceNumber(builderServiceTxnDetails: BuilderServiceTxnDetails?) : String?
    {
        var invoiceNumber: String? =
            builderServiceTxnDetails?.invoiceNo?.toInt()?.toString()
        invoiceNumber?.padStart(BuilderConstants.ISO_FIELD_INVOICE_NUMBER_LENGTH, '0')?.let {
            invoiceNumber = it

        }
        return invoiceNumber
    }

    fun getCurrencyCode(builderServiceTxnDetails: BuilderServiceTxnDetails?) : String?
    {
        var currencyCode: String? =
            builderServiceTxnDetails?.txnCurrencyCode?.toInt()?.toString()?:"356"
        currencyCode?.padStart(BuilderConstants.ISO_FIELD_CURRENCY_CODE_LEN, '0')?.let {
            currencyCode = it
        }
        return currencyCode
    }

    fun getCardSeqNum(builderServiceTxnDetails: BuilderServiceTxnDetails?) : String?
    {
        var cardSeqNumber: String? =
            builderServiceTxnDetails?.cardSeqNum?.toInt()?.toString()?:"1"
        cardSeqNumber?.padStart(BuilderConstants.ISO_FIELD_PAN_SEQ_NO_LENGTH, '0')?.let {
            cardSeqNumber = it
        }
        return cardSeqNumber
    }

    fun getPinBlock(builderServiceTxnDetails: BuilderServiceTxnDetails?) : String?
    {
        var pinBlock: String? =
            builderServiceTxnDetails?.pinBlock?.toInt()?.toString()?:"56BA46A5F3401014"
        pinBlock?.padEnd(BuilderConstants.ISO_FIELD_PAN_SEQ_NO_LENGTH, 'F')?.let {
            pinBlock = it
        }
        return pinBlock
    }

    @OptIn(ExperimentalEncodingApi::class, ExperimentalStdlibApi::class)
    fun createRklRequest(builderServiceTxnDetails: BuilderServiceTxnDetails?): ByteArray {
        this.builderServiceTxnDetails = builderServiceTxnDetails?: BuilderServiceTxnDetails()
        val stan = BuilderUtils.getSTAN(context)

        var message = messageFactory.newMessage(BuilderConstants.MTI_NETWORK_REQ)

        /* TPDU Header */
        /* TPDU Header */
        message.binaryIsoHeader = BuilderConstants.ISO_HEADER.apply {
            this[1] = ((stan/256)%256).toByte()
            this[2] = (stan%256).toByte()
        }

        /* Field 3, Processing Code, N6, Mandatory */
        message.setValue(BuilderConstants.ISO_FIELD_PROC_CODE, BuilderConstants.PROC_CODE_RKL_FULL_SN, IsoType.NUMERIC,BuilderConstants.ISO_FIELD_PROC_CODE_LENGTH)

        /* Field 11, STAN, N6, Mandatory */
            .setValue(BuilderConstants.ISO_FIELD_STAN,
                stan, IsoType.NUMERIC,BuilderConstants.ISO_FIELD_STAN_LENGTH)

        /* Field 12, Time, N6, Mandatory */
            .setValue(BuilderConstants.ISO_FIELD_TIME,
            BuilderUtils.getCurrentDateTime(BuilderConstants.DEFAULT_ISO8583_TIME_FORMAT), IsoType.TIME,BuilderConstants.ISO_FIELD_TIME_LENGTH)

        /* Field 13, Date, N4, Mandatory */
            .setValue(BuilderConstants.ISO_FIELD_DATE,
            BuilderUtils.getCurrentDateTime(BuilderConstants.DEFAULT_ISO8583_DATE_FORMAT), IsoType.DATE4,BuilderConstants.ISO_FIELD_DATE_LENGTH)

        /* Field 24, NII, N3, Mandatory */
            .setValue(BuilderConstants.ISO_FIELD_NII, BuilderConstants.DEFAULT_ISO8583_NII, IsoType.NUMERIC,BuilderConstants.ISO_FIELD_NII_LENGTH)

        /* Field 41, TID, ANS8, Mandatory */
            .setValue(BuilderConstants.ISO_FIELD_TID, builderServiceTxnDetails?.terminalId, IsoType.ALPHA,BuilderConstants.ISO_FIELD_TID_LENGTH)

        /* Field 42, MID, ANS15, Mandatory */
            .setValue(BuilderConstants.ISO_FIELD_MID, builderServiceTxnDetails?.merchantId, IsoType.ALPHA,BuilderConstants.ISO_FIELD_MID_LENGTH)

        /* Field 60, Serial No, ANS...999, Mandatory */
            .setValue(BuilderConstants.ISO_FIELD_TERM_SR_NO, builderServiceTxnDetails?.deviceSN, IsoType.LLLVAR,builderServiceTxnDetails?.deviceSN?.length?:0)

        /* Field 62, Working Key, ANS...999, Mandatory */
            .setValue(BuilderConstants.ISO_FIELD_WORKING_KEY, builderServiceTxnDetails?.devicePublicKey, IsoType.LLLVAR,builderServiceTxnDetails?.devicePublicKey?.length?:0)

        return appendIsoLength(message.writeData())
    }

    @OptIn(ExperimentalEncodingApi::class, ExperimentalStdlibApi::class)
    fun parseRklResponse(response: ByteArray): BuilderServiceTxnDetails {
        var message = messageFactory.parseMessage(extractIsoPayload(response), BuilderConstants.ISO_HEADER.size,true)

        return builderServiceTxnDetails.apply {
            message.getObjectValue<String>(BuilderConstants.ISO_FIELD_RESP_CODE)?.let { hostRespCode = it }
            message.getObjectValue<String>(BuilderConstants.ISO_FIELD_ADDL_DATA_KSN)?.let {
                if(it.slice(0 until BuilderConstants.ISO_FIELD_KSN_TAG.length) == BuilderConstants.ISO_FIELD_KSN_TAG) {
                    var length = it.slice(BuilderConstants.ISO_FIELD_KSN_TAG.length until BuilderConstants.ISO_FIELD_KSN_TAG.length+3).toInt()
                    if(it.length == length+BuilderConstants.ISO_FIELD_KSN_TAG.length+3)
                        ksn = it.slice(BuilderConstants.ISO_FIELD_KSN_TAG.length+3 until it.length)
                }
            }
            message.getObjectValue<String>(BuilderConstants.ISO_FIELD_WORKING_KEY)?.let {
                if(it.length > BuilderConstants.ISO_FIELD_KCV_LENGTH) {
                    kcv = it.slice(0 until BuilderConstants.ISO_FIELD_KCV_LENGTH)
                    encryptedIpek = it.slice(BuilderConstants.ISO_FIELD_KCV_LENGTH until it.length)
                }
            }
        }
    }

    fun createPurchaseRequest(builderServiceTxnDetails: BuilderServiceTxnDetails?): ByteArray {
        val amount = builderServiceTxnDetails?.ttlAmount?.toDoubleOrNull()?.toCurrencyLong()?:0
        val posEntryMode = getIsoPosEntryMode(builderServiceTxnDetails)
        val posConditionCode = getIsoPosConditionCode(builderServiceTxnDetails)
        val encryptedTrack2Data = getEncryptedTrack2Data(builderServiceTxnDetails)
        val iccData = getIccData(builderServiceTxnDetails)
        val ksn = getKsnTag(builderServiceTxnDetails)
        val batchNumber = getBatchNumber(builderServiceTxnDetails)
        val invoiceNumber = getInvoiceNumber(builderServiceTxnDetails)
        val currencyCode = getCurrencyCode(builderServiceTxnDetails)
        val cardSeqNumber = getCardSeqNum(builderServiceTxnDetails)
        val pinBlock = getPinBlock(builderServiceTxnDetails)
        val stan = BuilderUtils.getSTAN(context)

        var message = messageFactory.newMessage(BuilderConstants.MTI_SALE_REQ)

        /* TPDU Header */
        message.binaryIsoHeader = BuilderConstants.ISO_HEADER.apply {
            this[3] = ((stan/256)%256).toByte()
            this[4] = (stan%256).toByte()
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
                posEntryMode, IsoType.NUMERIC,BuilderConstants.ISO_FIELD_POS_ENTRY_MODE_LENGTH)

            /* Field 23, PAN Seq Number, N3, Conditional */
            .setValue(BuilderConstants.ISO_FIELD_PAN_SEQ_NO,
                cardSeqNumber, IsoType.NUMERIC,BuilderConstants.ISO_FIELD_PAN_SEQ_NO_LENGTH)

            /* Field 24, NII, N3, Mandatory */
            .setValue(BuilderConstants.ISO_FIELD_NII, BuilderConstants.DEFAULT_ISO8583_NII, IsoType.NUMERIC,BuilderConstants.ISO_FIELD_NII_LENGTH)

            /* Field 25, POS Condition Code, N2, Mandatory */
            .setValue(BuilderConstants.ISO_FIELD_POS_CONDITION_CODE, posConditionCode, IsoType.NUMERIC,BuilderConstants.ISO_FIELD_POS_CONDITION_CODE_LENGTH)

            /* Field 35, Track2 Data, ANS..37, Mandatory */
            .setValue(BuilderConstants.ISO_FIELD_TRACK2_DATA, encryptedTrack2Data, IsoType.LLLBIN,encryptedTrack2Data?.length?:0)

            /* Field 41, TID, ANS8, Mandatory */
            .setValue(BuilderConstants.ISO_FIELD_TID, builderServiceTxnDetails?.terminalId, IsoType.ALPHA,BuilderConstants.ISO_FIELD_TID_LENGTH)

            /* Field 42, MID, ANS15, Mandatory */
            .setValue(BuilderConstants.ISO_FIELD_MID, builderServiceTxnDetails?.merchantId, IsoType.ALPHA,BuilderConstants.ISO_FIELD_MID_LENGTH)

            /* Field 48, Additional Data KSN, ANS...999, Mandatory */
            .setValue(BuilderConstants.ISO_FIELD_ADDL_DATA_KSN, ksn, IsoType.LLLVAR,ksn?.length?:0)

            /* Field 49, Currency Code Transaction, N3, Mandatory */
            .setValue(BuilderConstants.ISO_FIELD_CURRENCY_CODE_TXN, currencyCode, IsoType.NUMERIC,currencyCode?.length?:0)

            /* Field 52, Pin Block, Binary 64, Mandatory */
            .setValue(BuilderConstants.ISO_FIELD_PIN_BLOCK, pinBlock, IsoType.BINARY,BuilderConstants.ISO_FIELD_PIN_BLOCK_LENGTH)

            /* Field 55, ICC Related Data, B..255, Mandatory */
            .setValue(BuilderConstants.ISO_FIELD_ICC_DATA, iccData, IsoType.LLLBIN,iccData?.length?:0)

            /* Field 60, Batch Number, ANS...999, Mandatory */
            .setValue(BuilderConstants.ISO_FIELD_PVT_USE_BATCH, batchNumber, IsoType.LLLVAR, batchNumber?.length?:0)

            /* Field 62, Invoice Number, N6, Mandatory */
            .setValue(BuilderConstants.ISO_FIELD_INVOICE_NUMBER, invoiceNumber, IsoType.NUMERIC,BuilderConstants.ISO_FIELD_INVOICE_NUMBER_LENGTH)

        return appendIsoLength(message.writeData())
    }

    @OptIn(ExperimentalEncodingApi::class, ExperimentalStdlibApi::class)
    fun parsePurchaseResponse(response: ByteArray): BuilderServiceTxnDetails {
        var message = messageFactory.parseMessage(extractIsoPayload(response), BuilderConstants.ISO_HEADER.size,true)

        return builderServiceTxnDetails.apply {
            message.getObjectValue<String>(BuilderConstants.ISO_FIELD_RESP_CODE)?.let { hostRespCode = it }
        }
    }
}