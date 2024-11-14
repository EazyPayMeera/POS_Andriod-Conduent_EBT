package com.analogics.builder_core.requestBuilder

import android.content.Context
import android.renderscript.Element
import com.analogics.builder_core.constants.BuilderConstants
import com.analogics.builder_core.model.BuilderServiceTxnDetails
import com.analogics.builder_core.model.auth_capture.PostAuthRequest
import com.analogics.builder_core.model.auth_capture.PreAuthRequest
import com.analogics.builder_core.model.auth_token.AuthTokenRequest
import com.analogics.builder_core.model.login.UserLoginRequest
import com.analogics.builder_core.model.purchase.PurchaseRequest
import com.analogics.builder_core.model.reund.RefundRequest
import com.analogics.builder_core.model.reversal.ReversalReqeust
import com.analogics.builder_core.model.void.VoidReqeust
import com.analogics.builder_core.utils.BuilderUtils
import com.analogics.networkservicecore.serviceutils.NetworkConstants
import com.solab.iso8583.CustomField
import com.solab.iso8583.IsoMessage
import com.solab.iso8583.IsoType
import com.solab.iso8583.MessageFactory
import com.solab.iso8583.parse.LllvarParseInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.temporal.IsoFields
import javax.inject.Inject
import kotlin.io.encoding.ExperimentalEncodingApi

class ApiRequestBuilderLyra @Inject constructor(@ApplicationContext val context: Context) {
    val messageFactory = MessageFactory<IsoMessage>()

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

    @OptIn(ExperimentalEncodingApi::class, ExperimentalStdlibApi::class)
    fun createRklRequest(builderServiceTxnDetails: BuilderServiceTxnDetails?): ByteArray {

        var message = messageFactory.newMessage(BuilderConstants.MTI_NETWORK)

        /* Field 3, Processing Code, N6, Mandatory */
            .setValue(BuilderConstants.ISO_FIELD_PROC_CODE, BuilderConstants.PROC_CODE_RKL_FULL_SN, IsoType.NUMERIC,BuilderConstants.ISO_FIELD_PROC_CODE_LENGTH)

        /* Field 11, STAN, N6, Mandatory */
            .setValue(BuilderConstants.ISO_FIELD_STAN,
            BuilderUtils.getSTAN(context), IsoType.NUMERIC,BuilderConstants.ISO_FIELD_STAN_LENGTH)

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

        return BuilderServiceTxnDetails().apply {
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
        var message = messageFactory.newMessage(BuilderConstants.MTI_NETWORK)

            /* Field 3, Processing Code, N6, Mandatory */
            .setValue(BuilderConstants.ISO_FIELD_PROC_CODE, BuilderConstants.PROC_CODE_RKL_FULL_SN, IsoType.NUMERIC,BuilderConstants.ISO_FIELD_PROC_CODE_LENGTH)

            /* Field 11, STAN, N6, Mandatory */
            .setValue(BuilderConstants.ISO_FIELD_STAN,
                BuilderUtils.getSTAN(context), IsoType.NUMERIC,BuilderConstants.ISO_FIELD_STAN_LENGTH)

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
}