package com.analogics.paymentservicecore.repository.paymentService.login

import com.analogics.builder_core.listener.responseListener.IApiServiceResponseListener
import com.analogics.builder_core.model.PaymentServiceTxnDetails
import com.analogics.builder_core.repository.BuildApiRepository
import com.analogics.builder_core.utils.APIServiceRequestBuilder
import com.analogics.builder_core.utils.BuilderUtils
import com.analogics.paymentservicecore.model.error.PaymentServiceError
import javax.inject.Inject

class AccessTokenRequestRepository @Inject constructor(
    var apiServiceRequestBuilder: APIServiceRequestBuilder,
    private var buildApiRepository: BuildApiRepository
) {
    suspend fun apiGetAccessToken(paymentServiceTxnDetails: PaymentServiceTxnDetails?,onAPIServiceResponse:(Any)->Unit) {

        buildApiRepository.apiGetAccessToken(
            object :IApiServiceResponseListener{
                override fun onApiSuccessRes(response: String) {
                    onAPIServiceResponse(response)
                }

                override fun onApiFailureRes(error: Any) {
                    onAPIServiceResponse(PaymentServiceError(error.toString()))
                }
            },
            BuilderUtils.prepareAPIRequestBody(
                apiServiceRequestBuilder.createAccessTokenRequest(paymentServiceTxnDetails)
            )
        )
    }

}