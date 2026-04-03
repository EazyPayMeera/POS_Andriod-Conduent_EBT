package com.eazypaytech.paymentservicecore.repository.apiService.login

//import com.eazypaytech.builder_core.listener.responseListener.IBuilderServiceResponseListener
//import com.eazypaytech.builder_core.repository.BuilderServiceRepository
//import com.eazypaytech.builder_core.requestBuilder.ApiRequestBuilder
import com.eazypaytech.paymentservicecore.model.PaymentServiceTxnDetails
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