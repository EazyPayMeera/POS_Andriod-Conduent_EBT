package com.analogics.paymentservicecore.repository.apiService.rkl

import com.analogics.builder_core.listener.responseListener.IBuilderServiceResponseListenerLyra
import com.analogics.builder_core.model.PaymentServiceTxnDetails
import com.analogics.builder_core.repository.BuilderServiceRepositoryLyra
import com.analogics.builder_core.requestBuilder.ApiRequestBuilderLyra
import com.analogics.paymentservicecore.model.error.ApiServiceError
import com.analogics.paymentservicecore.models.PosConfig
import com.analogics.paymentservicecore.utils.PaymentServiceUtils
import javax.inject.Inject
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

class RklRequestRepository@Inject constructor(
    var apiRequestBuilder: ApiRequestBuilderLyra,
    private var builderServiceRepository: BuilderServiceRepositoryLyra
)  {
        @OptIn(ExperimentalStdlibApi::class, ExperimentalEncodingApi::class)
        suspend fun apiRklRequest(paymentServiceTxnDetails: PaymentServiceTxnDetails?, onAPIServiceResponse:(Any)->Unit) {

            /* Override Security Parameters from Payment Service. Don't expose in App Module */
            var activationKey = PaymentServiceUtils.generateRsaKey()
            paymentServiceTxnDetails?.devicePublicKey = Base64.encode(activationKey.public.encoded)
            paymentServiceTxnDetails?.devicePrivateKey = Base64.encode(activationKey.private.encoded)
            paymentServiceTxnDetails?.deviceSN = PaymentServiceUtils.getDeviceSN()

            builderServiceRepository.networkServiceRequest(
                object : IBuilderServiceResponseListenerLyra{
                    override fun onBuilderSuccess(response: ByteArray) {
                        /* Parse Response Packet to Object */
                        var resPaymentServiceTxnDetails = apiRequestBuilder.parseRklResponse(response)

                        paymentServiceTxnDetails?.let {
                            it.hostRespCode = resPaymentServiceTxnDetails.hostRespCode
                            onAPIServiceResponse(it)
                        }
                    }

                    override fun onBuilderFailure(error: Any) {
                        onAPIServiceResponse(ApiServiceError(error.toString()))
                    }
                },
                apiRequestBuilder.createRklRequest(paymentServiceTxnDetails)
            )
        }
}