package com.analogics.paymentservicecore.repository.apiService.rkl

import com.analogics.builder_core.listener.responseListener.IBuilderServiceResponseListenerLyra
import com.analogics.builder_core.model.PaymentServiceTxnDetails
import com.analogics.builder_core.repository.BuilderServiceRepositoryLyra
import com.analogics.builder_core.requestBuilder.ApiRequestBuilderLyra
import com.analogics.paymentservicecore.model.error.ApiServiceError
import javax.inject.Inject

class RklRequestRepository@Inject constructor(
    var apiRequestBuilder: ApiRequestBuilderLyra,
    private var builderServiceRepository: BuilderServiceRepositoryLyra
)  {
        @OptIn(ExperimentalStdlibApi::class)
        suspend fun apiRklRequest(paymentServiceTxnDetails: PaymentServiceTxnDetails?, onAPIServiceResponse:(Any)->Unit) {

            builderServiceRepository.networkServiceRequest(
                object : IBuilderServiceResponseListenerLyra{
                    override fun onBuilderSuccess(response: ByteArray) {
                        onAPIServiceResponse(response)
                    }

                    override fun onBuilderFailure(error: Any) {
                        onAPIServiceResponse(ApiServiceError(error.toString()))
                    }
                },
                apiRequestBuilder.createRklRequest(paymentServiceTxnDetails)
            )
        }
}