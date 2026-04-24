package com.analogics.paymentservicecore.domain.repository.apiService.voucherSettlementRequest

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.analogics.builder_core.data.constants.BuilderConstants
import com.analogics.builder_core.domain.listener.responseListener.IBuilderServiceResponseListener
import com.analogics.builder_core.data.model.BuilderServiceTxnDetails
//import com.eazypaytech.builder_core.repository.BuilderServiceRepository
import com.analogics.builder_core.domain.repository.BuilderServiceRepository
import com.eazypaytech.paymentservicecore.constants.EmvConstants
import com.analogics.paymentservicecore.data.model.PaymentServiceTxnDetails
import com.analogics.paymentservicecore.data.model.error.ApiServiceError
import com.analogics.paymentservicecore.data.model.error.ApiServiceTimeout
import com.analogics.paymentservicecore.data.model.TxnStatus
import com.analogics.builder_core.builder.ApiRequestBuilder
import com.analogics.paymentservicecore.utils.PaymentServiceUtils
import com.eazypaytech.hardwarecore.utils.TlvUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import java.net.SocketTimeoutException
import javax.inject.Inject


class VoucherSettlementRequestRepository @Inject constructor(
    @ApplicationContext val context: Context,
    var apiRequestBuilder: ApiRequestBuilder,
    private var builderServiceRepository: BuilderServiceRepository
) {

    /**
     * Parses settlement ISO response and maps it into PaymentServiceTxnDetails.
     *
     * Responsibilities:
     * - Extract ISO fields (STAN, RRN, Settlement Date, etc.)
     * - Decode EMV TLV data
     * - Set transaction status (APPROVED / DECLINED)
     * - Attach host response message
     */
    @OptIn(ExperimentalStdlibApi::class)
    fun parseSettlementResponse(paymentServiceTxnDetails : PaymentServiceTxnDetails, response: ByteArray) : PaymentServiceTxnDetails {
        apiRequestBuilder.parseISOMessage(context,response).let {
            paymentServiceTxnDetails.stan = it.stan
            paymentServiceTxnDetails.hostRespCode = it.hostRespCode
            paymentServiceTxnDetails.hostAuthCode = it.hostAuthCode
            paymentServiceTxnDetails.hostTxnRef = it.hostTxnRef
            paymentServiceTxnDetails.settlementDate = it.settlementDate
            paymentServiceTxnDetails.rrn = it.rrn
            paymentServiceTxnDetails.hostAuthCode = it.hostAuthCode
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
                paymentServiceTxnDetails.hostResMessage = BuilderConstants.getIsoResponseMessage(
                    it.hostRespCode!!
                )
            }
            else {
                paymentServiceTxnDetails.hostAuthResult = TxnStatus.DECLINED.toString()
                paymentServiceTxnDetails.txnStatus = TxnStatus.DECLINED.toString()
                paymentServiceTxnDetails.hostResMessage = BuilderConstants.getIsoResponseMessage(
                    it.hostRespCode!!
                )

            }
        }
        return paymentServiceTxnDetails
    }

    /**
     * Sends voucher settlement request to host system.
     *
     * Handles:
     * - Success response → parse & return mapped object
     * - Failure → timeout or generic error mapping
     */
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun voucherSettlementRequest(paymentServiceTxnDetails: PaymentServiceTxnDetails?, onAPIServiceResponse:(Any)->Unit) {
        builderServiceRepository.networkServiceFinancialRequest(
            object : IBuilderServiceResponseListener{
                @SuppressLint("NewApi")
                override fun onBuilderSuccess(response: ByteArray) {
                    paymentServiceTxnDetails?.let { details ->
                        onAPIServiceResponse(parseSettlementResponse(details, response))
                    } ?: run {
                        onAPIServiceResponse(ApiServiceError("paymentServiceTxnDetails is null"))
                    }
                }

                override fun onBuilderFailure(error: Any) {
                    if (error is SocketTimeoutException ||
                        error.toString().contains("timed out", true)
                    ) {

                        onAPIServiceResponse(
                            ApiServiceTimeout("Transaction Timed Out")
                        )

                    } else {

                        onAPIServiceResponse(
                            ApiServiceError(error.toString())
                        )
                    }
                }

            },
            apiRequestBuilder.voucherSettlement(PaymentServiceUtils.transformObject<BuilderServiceTxnDetails>(paymentServiceTxnDetails))
        )
    }
}





