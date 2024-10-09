package com.analogics.paymentservicecore.repository.paymentService.login

import com.analogics.builder_core.listener.responseListener.IBuilderServiceResponseListener
import com.analogics.builder_core.model.PaymentServiceTxnDetails
import com.analogics.builder_core.repository.BuildApiRepository
import com.analogics.builder_core.utils.APIServiceRequestBuilder
import com.analogics.builder_core.utils.BuilderUtils
import com.analogics.paymentservicecore.model.error.ApiServiceError
import javax.inject.Inject

class LoginRequestRepository @Inject constructor(
    var apiServiceRequestBuilder: APIServiceRequestBuilder,
    private var buildApiRepository: BuildApiRepository
) {
    suspend fun apiDeviceLogin(paymentServiceTxnDetails: PaymentServiceTxnDetails?,onAPIServiceResponse:(Any)->Unit) {

        buildApiRepository.apiDeviceLogin(
            object :IBuilderServiceResponseListener{
                override fun onBuilderSuccess(response: String) {
                    onAPIServiceResponse(response)

                }

                override fun onBuilderFailure(error: Any) {
                    onAPIServiceResponse(ApiServiceError(error.toString()))
                }
            },
            BuilderUtils.prepareApiRequestBody(
                apiServiceRequestBuilder.createLoginRequest(paymentServiceTxnDetails)
            )
        )
    }

}