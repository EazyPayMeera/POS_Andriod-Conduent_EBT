package com.analogics.paymentservicecore.domain.repository.apiService.purchase

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.analogics.builder_core.data.constants.BuilderConstants
import com.analogics.builder_core.domain.listener.responseListener.IBuilderServiceResponseListenerLyra
import com.analogics.builder_core.data.model.BuilderServiceTxnDetails
//import com.eazypaytech.builder_core.repository.BuilderServiceRepository
import com.analogics.builder_core.domain.repository.BuilderServiceRepositoryLyra
import com.eazypaytech.paymentservicecore.constants.EmvConstants
import com.analogics.paymentservicecore.data.model.PaymentServiceTxnDetails
import com.analogics.paymentservicecore.data.model.error.ApiServiceError
import com.analogics.paymentservicecore.data.model.error.ApiServiceTimeout
import com.analogics.paymentservicecore.data.model.TxnStatus
import com.analogics.builder_core.builder.ApiRequestBuilderLyra
import com.analogics.paymentservicecore.utils.PaymentServiceUtils
import com.eazypaytech.hardwarecore.utils.TlvUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import java.net.SocketTimeoutException
import javax.inject.Inject


class PurchaseRequestRepository @Inject constructor(
    @ApplicationContext val context: Context,
    var apiRequestBuilder: ApiRequestBuilderLyra,
    //private var builderServiceRepository: BuilderServiceRepository,
    var apiRequestBuilderLyra: ApiRequestBuilderLyra,
    private var builderServiceRepositoryLyra: BuilderServiceRepositoryLyra
) {

    @OptIn(ExperimentalStdlibApi::class)
    fun parseIsoRespMessage123(paymentServiceTxnDetails : PaymentServiceTxnDetails, response: ByteArray) : PaymentServiceTxnDetails {
        if (response.isEmpty()) {
            Log.e("ISO", "Empty response received from host")

            return paymentServiceTxnDetails.apply {
                txnStatus = TxnStatus.DECLINED.toString()
                hostAuthResult = TxnStatus.DECLINED.toString()
                hostResMessage = "No response from host"
            }
        }

        if (response.size < 20) {
            Log.e("ISO", "Invalid response size: ${response.size}")
            return paymentServiceTxnDetails.apply {
                txnStatus = TxnStatus.DECLINED.toString()
                hostAuthResult = TxnStatus.DECLINED.toString()
                hostResMessage = "Invalid response"
            }
        }
            apiRequestBuilderLyra.parsePurchaseResponse123(context,response).let {
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
                paymentServiceTxnDetails.dateTime = it.dateTime

                var tlv = TlvUtils(it.emvData)
                /* Extract tag 8A from ISO field if required */
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


    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun purchaseRequest(paymentServiceTxnDetails: PaymentServiceTxnDetails?, onAPIServiceResponse:(Any)->Unit) {
        builderServiceRepositoryLyra.networkServiceFinancialRequest(
            object : IBuilderServiceResponseListenerLyra{
                @SuppressLint("NewApi")
                override fun onBuilderSuccess(response: ByteArray) {
                    paymentServiceTxnDetails?.let { details ->

                        onAPIServiceResponse(parseIsoRespMessage123(details, response))
                    } ?: run {
                        onAPIServiceResponse(ApiServiceError("paymentServiceTxnDetails is null"))
                    }


                }

                override fun onBuilderFailure(error: Any) {

                    if (error is SocketTimeoutException ||
                        error.toString().contains("timed out", true)
                    ) {

                        Log.d("API_FLOW", "Timeout mapped correctly")
                        onAPIServiceResponse(   // ✅ THIS WAS MISSING
                            ApiServiceTimeout("Transaction Timed Out")
                        )

                    } else {

                        onAPIServiceResponse(   // ✅ THIS WAS MISSING
                            ApiServiceError(error.toString())
                        )
                    }
                }

            },
            apiRequestBuilderLyra.createFinancial0200Request(PaymentServiceUtils.transformObject<BuilderServiceTxnDetails>(paymentServiceTxnDetails))
        )
    }

}





