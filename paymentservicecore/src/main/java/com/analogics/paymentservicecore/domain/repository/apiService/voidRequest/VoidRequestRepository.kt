package com.analogics.paymentservicecore.domain.repository.apiService.voidRequest

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.analogics.builder_core.data.constants.BuilderConstants
import com.analogics.builder_core.domain.listener.responseListener.IBuilderServiceResponseListener
import com.analogics.builder_core.data.model.BuilderServiceTxnDetails
import com.analogics.builder_core.domain.repository.BuilderServiceRepository
import com.eazypaytech.paymentservicecore.constants.EmvConstants
import com.analogics.paymentservicecore.data.model.PaymentServiceTxnDetails
import com.analogics.paymentservicecore.data.model.error.ApiServiceError
import com.analogics.paymentservicecore.data.model.TxnStatus
import com.analogics.builder_core.builder.ApiRequestBuilder
import com.analogics.paymentservicecore.utils.PaymentServiceUtils
import com.eazypaytech.hardwarecore.utils.TlvUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class VoidRequestRepository @Inject constructor (
    @ApplicationContext val context: Context,
    var apiRequestBuilder: ApiRequestBuilder,
    private var builderServiceRepository: BuilderServiceRepository
) {

    /**
     * Parses ISO 8583 VOID response and maps it to PaymentServiceTxnDetails.
     *
     * Responsibilities:
     * - Extract ISO fields (STAN, RRN, Response Code, etc.)
     * - Decode EMV TLV data
     * - Determine transaction status (APPROVED / DECLINED)
     */
    @OptIn(ExperimentalStdlibApi::class)
    fun parseVoidResponse(paymentServiceTxnDetails : PaymentServiceTxnDetails, response: ByteArray) : PaymentServiceTxnDetails {
        apiRequestBuilder.parseISOMessage(context,response).let {
            paymentServiceTxnDetails.stan = it.stan
            paymentServiceTxnDetails.hostRespCode = it.hostRespCode
            paymentServiceTxnDetails.hostAuthCode = it.hostAuthCode
            paymentServiceTxnDetails.hostTxnRef = it.hostTxnRef
            paymentServiceTxnDetails.hostResMessage = it.hostResMessage
            var tlv = TlvUtils(it.emvData)
            if(tlv.tlvMap.containsKey(EmvConstants.EMV_TAG_RESP_CODE)==false) {
                it.hostRespCode?.encodeToByteArray()?.toHexString()?.let {
                    tlv.tlvMap[EmvConstants.EMV_TAG_RESP_CODE] = it
                }
            }
            paymentServiceTxnDetails.emvData = tlv.toTlvString()
            if (it.hostRespCode == BuilderConstants.ISO_RESP_CODE_APPROVED) {
                paymentServiceTxnDetails.hostAuthResult = TxnStatus.APPROVED.toString()
                paymentServiceTxnDetails.txnStatus = TxnStatus.APPROVED.toString()
            }
            else {
                paymentServiceTxnDetails.hostAuthResult = TxnStatus.DECLINED.toString()
                paymentServiceTxnDetails.txnStatus = TxnStatus.DECLINED.toString()
            }
        }

        return paymentServiceTxnDetails
    }

    /**
     * Sends VOID request to host system and handles async response.
     *
     * Steps:
     * - Build VOID ISO request
     * - Send via BuilderServiceRepository
     * - Parse response and return mapped result
     */
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun voidRequest(paymentServiceTxnDetails: PaymentServiceTxnDetails?, onAPIServiceResponse:(Any)->Unit) {
        builderServiceRepository.networkServiceFinancialRequest(
            object : IBuilderServiceResponseListener{
                @SuppressLint("NewApi")
                override fun onBuilderSuccess(response: ByteArray) {
                    paymentServiceTxnDetails?.let { details ->
                        onAPIServiceResponse(parseVoidResponse(details, response))
                    } ?: run {
                        onAPIServiceResponse(ApiServiceError("paymentServiceTxnDetails is null"))
                    }
                }

                override fun onBuilderFailure(error: Any) {
                    onAPIServiceResponse(ApiServiceError(error.toString()))
                }
            },
            apiRequestBuilder.createVoidRequest(PaymentServiceUtils.transformObject<BuilderServiceTxnDetails>(paymentServiceTxnDetails))
        )
    }
}