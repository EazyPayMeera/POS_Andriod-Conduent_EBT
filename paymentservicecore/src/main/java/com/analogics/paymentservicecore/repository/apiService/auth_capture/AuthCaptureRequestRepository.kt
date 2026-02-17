package com.eazypaytech.paymentservicecore.repository.apiService.auth_capture

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.eazypaytech.builder_core.constants.BuilderConstants
import com.eazypaytech.builder_core.listener.responseListener.IBuilderServiceResponseListener
import com.eazypaytech.builder_core.listener.responseListener.IBuilderServiceResponseListenerLyra
import com.eazypaytech.builder_core.model.BuilderServiceTxnDetails
import com.eazypaytech.builder_core.repository.BuilderServiceRepository
import com.eazypaytech.builder_core.repository.BuilderServiceRepositoryLyra
import com.eazypaytech.builder_core.requestBuilder.ApiRequestBuilder
import com.eazypaytech.builder_core.requestBuilder.ApiRequestBuilderLyra
import com.eazypaytech.builder_core.utils.BuilderUtils
import com.eazypaytech.paymentservicecore.constants.AppConstants
import com.eazypaytech.paymentservicecore.constants.EmvConstants
import com.eazypaytech.paymentservicecore.model.PaymentServiceTxnDetails
import com.eazypaytech.paymentservicecore.model.error.ApiServiceError
import com.eazypaytech.paymentservicecore.models.TxnStatus
import com.eazypaytech.paymentservicecore.utils.PaymentServiceUtils
import com.eazypaytech.tpaymentcore.utils.TlvUtils
import javax.inject.Inject

class AuthCaptureRequestRepository @Inject constructor(
    var apiRequestBuilder: ApiRequestBuilder,
    private var builderServiceRepository: BuilderServiceRepository,
    var apiRequestBuilderLyra: ApiRequestBuilderLyra,
    private var builderServiceRepositoryLyra: BuilderServiceRepositoryLyra
) {


    //lateinit var paymentServiceTxnDetails:PaymentServiceTxnDetails
    @RequiresApi(Build.VERSION_CODES.O)
//    suspend fun sendAuthCapRequest(paymentServiceTxnDetails: PaymentServiceTxnDetails?, onAPIServiceResponse:(Any)->Unit) {
//
//        if(paymentServiceTxnDetails?.acquirerName == AppConstants.ACQUIRER_LYRA) {
//            var request = apiRequestBuilderLyra.createAuthCapRequest(
//                PaymentServiceUtils.transformObject<BuilderServiceTxnDetails>(
//                    paymentServiceTxnDetails
//                )
//            )
//
//            if(paymentServiceTxnDetails.isDemoMode == true)
//            {
//                onAPIServiceResponse(parseIsoRespMessage(paymentServiceTxnDetails,
//                    apiRequestBuilderLyra.buildDummyPurchaseResponse()))
//            }
//            else {
//                builderServiceRepositoryLyra.networkServiceRequest(
//                    object :
//                        IBuilderServiceResponseListenerLyra {
//                        override fun onBuilderSuccess(response: ByteArray) {
//                            onAPIServiceResponse(parseIsoRespMessage(paymentServiceTxnDetails,response))
//                        }
//
//                        override fun onBuilderFailure(error: Any) {
//                            onAPIServiceResponse(ApiServiceError(error.toString()))
//                        }
//                    },
//                    request
//                )
//            }
//        }
//        else {
//            builderServiceRepository.apiPurchase(
//                object : IBuilderServiceResponseListener {
//                    override fun onBuilderSuccess(response: String) {
//                        onAPIServiceResponse(response)
//                        Log.d("record insert", "onApiSuccessRes")
//
//                    }
//
//                    override fun onBuilderFailure(error: Any) {
//                        Log.d("record insert", "onApiFailureRes")
//                        onAPIServiceResponse(ApiServiceError(error.toString()))
//                        paymentServiceTxnDetails?.let { onAPIServiceResponse(it) }
//                    }
//                },
//                BuilderUtils.prepareApiRequestBody(
//                    apiRequestBuilder.createAuthCaptureRequest(
//                        PaymentServiceUtils.transformObject<BuilderServiceTxnDetails>(
//                            paymentServiceTxnDetails
//                        )
//                    )
//                )
//
//            )
//        }
//    }

    @OptIn(ExperimentalStdlibApi::class)
    fun parseIsoRespMessage(paymentServiceTxnDetails : PaymentServiceTxnDetails, response: ByteArray) : PaymentServiceTxnDetails {
        apiRequestBuilderLyra.parsePurchaseResponse(response).let {
            paymentServiceTxnDetails.stan = it.stan
            paymentServiceTxnDetails.hostRespCode = it.hostRespCode
            paymentServiceTxnDetails.hostAuthCode = it.hostAuthCode
            paymentServiceTxnDetails.hostTxnRef = it.hostTxnRef
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
            }
            else {
                paymentServiceTxnDetails.hostAuthResult = TxnStatus.DECLINED.toString()
                paymentServiceTxnDetails.txnStatus = TxnStatus.DECLINED.toString()
            }
        }
        return paymentServiceTxnDetails
    }

}