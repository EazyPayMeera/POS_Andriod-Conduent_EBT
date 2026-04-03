package com.analogics.paymentservicecore.domain.repository.apiService.login

import com.analogics.paymentservicecore.data.model.PaymentServiceTxnDetails
import javax.inject.Inject

class LoginRequestRepository @Inject constructor(
    //var apiRequestBuilder: ApiRequestBuilder,
    //private var builderServiceRepository: BuilderServiceRepository
) {
    suspend fun apiDeviceLogin(paymentServiceTxnDetails: PaymentServiceTxnDetails?,onAPIServiceResponse:(Any)->Unit) {

        /*builderServiceRepository.apiDeviceLogin(
            object :IBuilderServiceResponseListener{
                override fun onBuilderSuccess(response: String) {
                    onAPIServiceResponse(response)

                }

                override fun onBuilderFailure(error: Any) {
                    onAPIServiceResponse(ApiServiceError(error.toString()))
                }
            },
            BuilderUtils.prepareApiRequestBody(
                apiRequestBuilder.createLoginRequest(PaymentServiceUtils.transformObject<BuilderServiceTxnDetails>(paymentServiceTxnDetails))
            )
        )*/
    }

}