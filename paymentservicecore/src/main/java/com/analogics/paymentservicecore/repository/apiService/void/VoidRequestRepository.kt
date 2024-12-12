package com.analogics.paymentservicecore.repository.apiService.preauth

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.analogics.builder_core.constants.BuilderConstants
import com.analogics.builder_core.listener.responseListener.IBuilderServiceResponseListener
import com.analogics.builder_core.listener.responseListener.IBuilderServiceResponseListenerLyra
import com.analogics.builder_core.model.BuilderServiceTxnDetails
import com.analogics.builder_core.repository.BuilderServiceRepository
import com.analogics.builder_core.repository.BuilderServiceRepositoryLyra
import com.analogics.builder_core.requestBuilder.ApiRequestBuilder
import com.analogics.builder_core.requestBuilder.ApiRequestBuilderLyra
import com.analogics.builder_core.utils.BuilderUtils
import com.analogics.paymentservicecore.constants.AppConstants
import com.analogics.paymentservicecore.constants.EmvConstants
import com.analogics.paymentservicecore.model.PaymentServiceTxnDetails
import com.analogics.paymentservicecore.model.error.ApiServiceError
import com.analogics.paymentservicecore.models.TxnStatus
import com.analogics.paymentservicecore.utils.PaymentServiceUtils
import com.analogics.tpaymentcore.utils.TlvUtils
import javax.inject.Inject

class VoidRequestRepository @Inject constructor (
    var apiRequestBuilder: ApiRequestBuilder,
    private var builderServiceRepository: BuilderServiceRepository,
    var apiRequestBuilderLyra: ApiRequestBuilderLyra,
    private var builderServiceRepositoryLyra: BuilderServiceRepositoryLyra
) {

    //lateinit var paymentServiceTxnDetails:PaymentServiceTxnDetails
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun sendVoidRequest(paymentServiceTxnDetails: PaymentServiceTxnDetails?, onAPIServiceResponse:(Any)->Unit) {

        if(paymentServiceTxnDetails?.acquirerName == AppConstants.ACQUIRER_LYRA) {
            var request = apiRequestBuilderLyra.CreateVoidRequest(
                PaymentServiceUtils.transformObject<BuilderServiceTxnDetails>(
                    paymentServiceTxnDetails
                )
            )

            if(paymentServiceTxnDetails.isDemoMode == true)
            {
                onAPIServiceResponse(parseIsoRespMessage(paymentServiceTxnDetails,
                    apiRequestBuilderLyra.buildDummyVoidResponse()))
            }
            else {
                builderServiceRepositoryLyra.networkServiceRequest(
                    object :
                        IBuilderServiceResponseListenerLyra {
                        override fun onBuilderSuccess(response: ByteArray) {
                            onAPIServiceResponse(parseIsoRespMessage(paymentServiceTxnDetails,response))
                        }

                        override fun onBuilderFailure(error: Any) {
                            onAPIServiceResponse(ApiServiceError(error.toString()))
                        }
                    },
                    request
                )
            }
        }
        else {
            builderServiceRepository.apiPurchase(
                object : IBuilderServiceResponseListener {
                    override fun onBuilderSuccess(response: String) {
                        onAPIServiceResponse(response)
                        Log.d("record insert", "onApiSuccessRes")

                    }

                    override fun onBuilderFailure(error: Any) {
                        Log.d("record insert", "onApiFailureRes")
                        onAPIServiceResponse(ApiServiceError(error.toString()))
                        paymentServiceTxnDetails?.let { onAPIServiceResponse(it) }
                    }
                },
                BuilderUtils.prepareApiRequestBody(
                    apiRequestBuilder.createVoidRequest(
                        PaymentServiceUtils.transformObject<BuilderServiceTxnDetails>(
                            paymentServiceTxnDetails
                        )
                    )
                )

            )
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    fun parseIsoRespMessage(paymentServiceTxnDetails : PaymentServiceTxnDetails, response: ByteArray) : PaymentServiceTxnDetails {
        apiRequestBuilderLyra.parseVoidResponse(response).let {
            paymentServiceTxnDetails.hostRespCode = it.hostRespCode
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