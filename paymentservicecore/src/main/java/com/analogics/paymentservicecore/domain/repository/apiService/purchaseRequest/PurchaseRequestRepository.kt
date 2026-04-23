package com.analogics.paymentservicecore.domain.repository.apiService.purchaseRequest

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
import com.analogics.paymentservicecore.data.model.error.ApiServiceTimeout
import com.analogics.paymentservicecore.data.model.TxnStatus
import com.analogics.builder_core.builder.ApiRequestBuilder
import com.analogics.paymentservicecore.utils.PaymentServiceUtils
import com.eazypaytech.hardwarecore.utils.TlvUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import java.net.SocketTimeoutException
import javax.inject.Inject


class PurchaseRequestRepository @Inject constructor(
    @ApplicationContext val context: Context,
    //var apiRequestBuilder: ApiRequestBuilder,
    var apiRequestBuilder: ApiRequestBuilder,
    private var builderServiceRepository: BuilderServiceRepository
) {

    @OptIn(ExperimentalStdlibApi::class)
    fun parseFinancialMessage(paymentServiceTxnDetails: PaymentServiceTxnDetails, response: ByteArray): PaymentServiceTxnDetails {

        if (response.isEmpty()) {
            return paymentServiceTxnDetails.apply {
                txnStatus = TxnStatus.DECLINED.toString()
                hostAuthResult = TxnStatus.DECLINED.toString()
                hostResMessage = "No response from host"
            }
        }
        if (response.size < 20) {
            return paymentServiceTxnDetails.apply {
                txnStatus = TxnStatus.DECLINED.toString()
                hostAuthResult = TxnStatus.DECLINED.toString()
                hostResMessage = "Invalid response"
            }
        }
        apiRequestBuilder.parseISOMessage(context, response).let { it ->
            paymentServiceTxnDetails.stan = it.stan
            paymentServiceTxnDetails.hostRespCode = it.hostRespCode
            paymentServiceTxnDetails.hostAuthCode = it.hostAuthCode
            paymentServiceTxnDetails.hostTxnRef = it.hostTxnRef
            paymentServiceTxnDetails.additionalAmt = it.additionalAmt
            paymentServiceTxnDetails.settlementDate = it.settlementDate
            paymentServiceTxnDetails.rrn = it.rrn
            paymentServiceTxnDetails.processingCode = it.processingCode
            paymentServiceTxnDetails.localDate = it.localDate
            paymentServiceTxnDetails.localTime = it.localTime
            paymentServiceTxnDetails.expiryDate = it.expiryDate
            paymentServiceTxnDetails.posEntryMode = it.posEntryMode
            paymentServiceTxnDetails.posCondition = it.posCondition
            paymentServiceTxnDetails.currencyCode = it.currencyCode
            paymentServiceTxnDetails.originalDateTime = it.dateTime
            var tlv = TlvUtils(it.emvData)
            if (tlv.tlvMap.containsKey(EmvConstants.EMV_TAG_RESP_CODE) == false) {
                it.hostRespCode
                    ?.encodeToByteArray()
                    ?.toHexString()
                    ?.let { hexValue ->
                        tlv.tlvMap[EmvConstants.EMV_TAG_RESP_CODE] = hexValue
                    }
            }
            paymentServiceTxnDetails.emvData = tlv.toTlvString()
            if (it.hostRespCode == BuilderConstants.ISO_RESP_CODE_APPROVED) {
                paymentServiceTxnDetails.hostAuthResult = TxnStatus.APPROVED.toString()
                paymentServiceTxnDetails.txnStatus = TxnStatus.APPROVED.toString()
            } else {
                paymentServiceTxnDetails.hostAuthResult = TxnStatus.DECLINED.toString()
                paymentServiceTxnDetails.txnStatus = TxnStatus.DECLINED.toString()
            }

            paymentServiceTxnDetails.hostResMessage =
                BuilderConstants.getIsoResponseMessage(it.hostRespCode!!)
        }
        return paymentServiceTxnDetails
    }


    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun purchaseRequest(paymentServiceTxnDetails: PaymentServiceTxnDetails?, onAPIServiceResponse:(Any)->Unit) {
        builderServiceRepository.networkServiceFinancialRequest(
            object : IBuilderServiceResponseListener{
                @SuppressLint("NewApi")
                override fun onBuilderSuccess(response: ByteArray) {
                    paymentServiceTxnDetails?.let { details ->
                        onAPIServiceResponse(parseFinancialMessage(details, response))
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
            apiRequestBuilder.createFinancialRequest(PaymentServiceUtils.transformObject<BuilderServiceTxnDetails>(paymentServiceTxnDetails))
        )
    }

}





