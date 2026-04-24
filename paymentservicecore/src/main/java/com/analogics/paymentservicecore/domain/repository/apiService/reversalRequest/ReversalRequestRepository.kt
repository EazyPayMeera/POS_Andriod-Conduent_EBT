package com.analogics.paymentservicecore.domain.repository.apiService.reversalRequest

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.analogics.builder_core.data.constants.BuilderConstants
//import com.eazypaytech.builder_core.listener.responseListener.IBuilderServiceResponseListener
import com.analogics.builder_core.domain.listener.responseListener.IBuilderServiceResponseListener
import com.analogics.builder_core.data.model.BuilderServiceTxnDetails
//import com.eazypaytech.builder_core.repository.BuilderServiceRepository
import com.analogics.builder_core.domain.repository.BuilderServiceRepository
//import com.eazypaytech.builder_core.requestBuilder.ApiRequestBuilder
import com.analogics.paymentservicecore.data.model.PaymentServiceTxnDetails
import com.analogics.paymentservicecore.data.model.error.ApiServiceError
import com.analogics.paymentservicecore.data.model.TxnStatus
import com.analogics.builder_core.builder.ApiRequestBuilder
import com.analogics.paymentservicecore.utils.PaymentServiceUtils
import com.eazypaytech.hardwarecore.utils.TlvUtils
import com.eazypaytech.paymentservicecore.constants.EmvConstants
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Handles REVERSAL / VOID transaction flow.
 *
 * Responsibilities:
 * - Build reversal request
 * - Send to host via BuilderService
 * - Parse ISO reversal response
 * - Map status into PaymentServiceTxnDetails
 */
class ReversalRequestRepository @Inject constructor(
    @ApplicationContext val context: Context,
    var apiRequestBuilder: ApiRequestBuilder,
    private var builderServiceRepository: BuilderServiceRepository
) {

    /**
     * Parses reversal ISO response into domain model.
     *
     * Handles:
     * - ISO fields extraction
     * - EMV TLV injection
     * - Transaction status mapping
     */
    @OptIn(ExperimentalStdlibApi::class)
    fun parseReversalResponse(paymentServiceTxnDetails : PaymentServiceTxnDetails, response: ByteArray) : PaymentServiceTxnDetails {
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
     * Sends reversal request to host.
     *
     * Flow:
     * 1. Build VOID/Reversal ISO request
     * 2. Send via BuilderService
     * 3. Handle success/failure callbacks
     */
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun sendReversal(paymentServiceTxnDetails: PaymentServiceTxnDetails?, onAPIServiceResponse:(Any)->Unit) {
        builderServiceRepository.networkServiceFinancialRequest(
            object : IBuilderServiceResponseListener{
                @SuppressLint("NewApi")
                override fun onBuilderSuccess(response: ByteArray) {
                    paymentServiceTxnDetails?.let { details ->
                        onAPIServiceResponse(parseReversalResponse(details, response))
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