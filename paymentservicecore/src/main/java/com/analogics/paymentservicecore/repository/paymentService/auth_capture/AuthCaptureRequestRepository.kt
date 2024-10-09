package com.analogics.paymentservicecore.repository.paymentService.auth_capture

import com.analogics.builder_core.listener.responseListener.IApiServiceResponseListener
import com.analogics.builder_core.model.PaymentServiceTxnDetails
import com.analogics.builder_core.repository.BuildApiRepository
import com.analogics.builder_core.utils.APIServiceRequestBuilder
import com.analogics.builder_core.utils.BuilderUtils
import com.analogics.paymentservicecore.model.error.ApiServiceError
import javax.inject.Inject

class AuthCaptureRequestRepository @Inject constructor(
    var apiServiceRequestBuilder: APIServiceRequestBuilder,
    private var buildApiRepository: BuildApiRepository
) {


    suspend fun sendAuthCaptureRequest(paymentServiceTxnDetails: PaymentServiceTxnDetails?,onAPIServiceResponse:(Any)->Unit) {

        buildApiRepository.apiRefund(
            object :IApiServiceResponseListener{
                override fun onApiSuccess(response: String) {
                    onAPIServiceResponse(response)

                }

                override fun onApiFailure(error: Any) {
                    onAPIServiceResponse(ApiServiceError(error.toString()))
                }
            },
            BuilderUtils.prepareApiRequestBody(
                apiServiceRequestBuilder.createAuthCaptureRequest(paymentServiceTxnDetails)
            )
        )
    }
    suspend fun sendPreAuthRequest(paymentServiceTxnDetails: PaymentServiceTxnDetails?,onAPIServiceResponse:(Any)->Unit) {

        buildApiRepository.apiRefund(
            object :IApiServiceResponseListener{
                override fun onApiSuccess(response: String) {
                    onAPIServiceResponse(response)
                }
                override fun onApiFailure(error: Any) {
                    onAPIServiceResponse(ApiServiceError(error.toString()))
                }
            },
            BuilderUtils.prepareApiRequestBody(
                apiServiceRequestBuilder.createPre_AuthRequest(paymentServiceTxnDetails)
            )
        )
    }

}