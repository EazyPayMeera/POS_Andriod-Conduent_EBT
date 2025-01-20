package com.eazypaytech.paymentservicecore.repository.apiService.access_token

import com.eazypaytech.builder_core.listener.responseListener.IBuilderServiceResponseListener
import com.eazypaytech.builder_core.model.BuilderServiceTxnDetails
import com.eazypaytech.builder_core.repository.BuilderServiceRepository
import com.eazypaytech.builder_core.requestBuilder.ApiRequestBuilder
import com.eazypaytech.builder_core.utils.BuilderUtils
import com.eazypaytech.paymentservicecore.model.PaymentServiceTxnDetails
import com.eazypaytech.paymentservicecore.model.error.ApiServiceError
import com.eazypaytech.paymentservicecore.utils.PaymentServiceUtils
import javax.inject.Inject

class AccessTokenRequestRepository @Inject constructor(
    var apiRequestBuilder: ApiRequestBuilder,
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
                apiRequestBuilder.createRklRequest(PaymentServiceUtils.transformObject<BuilderServiceTxnDetails>(paymentServiceTxnDetails))?.toHexString()?:""
            )
        )
    }

}