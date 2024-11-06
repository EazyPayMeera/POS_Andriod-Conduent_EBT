package com.analogics.paymentservicecore.repository.apiService.login

import com.analogics.builder_core.listener.responseListener.IBuilderServiceResponseListener
import com.analogics.builder_core.model.PaymentServiceTxnDetails
import com.analogics.builder_core.repository.BuilderServiceRepository
import com.analogics.builder_core.utils.APIServiceRequestBuilder
import com.analogics.builder_core.utils.BuilderUtils
import com.analogics.paymentservicecore.model.error.ApiServiceError
import javax.inject.Inject

class AccessTokenRequestRepository @Inject constructor(
    var apiServiceRequestBuilder: APIServiceRequestBuilder,
    private var builderServiceRepository: BuilderServiceRepository
) {
    @OptIn(ExperimentalStdlibApi::class)
    suspend fun apiGetAccessToken(paymentServiceTxnDetails: PaymentServiceTxnDetails?, onAPIServiceResponse:(Any)->Unit) {

        builderServiceRepository.apiGetAccessToken(
            object :IBuilderServiceResponseListener{
                override fun onBuilderSuccess(response: String) {
                    onAPIServiceResponse(response)
                }

                override fun onBuilderFailure(error: Any) {
                    onAPIServiceResponse(ApiServiceError(error.toString()))
                }
            },
            BuilderUtils.prepareApiRequestBody(
                //apiServiceRequestBuilder.createAccessTokenRequest(paymentServiceTxnDetails)
                apiServiceRequestBuilder.createRklRequest(paymentServiceTxnDetails)?.toHexString()?:""
            )
        )
    }

}