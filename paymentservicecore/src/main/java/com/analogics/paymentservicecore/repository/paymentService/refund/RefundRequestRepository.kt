package com.analogics.paymentservicecore.repository.paymentService.refund

import com.analogics.builder_core.listener.responseListener.IApiServiceResponseListener
import com.analogics.builder_core.model.PaymentServiceTxnDetails
import com.analogics.builder_core.repository.BuildApiRepository
import com.analogics.builder_core.utils.APIServiceRequestBuilder
import com.analogics.builder_core.utils.BuilderUtils
import com.analogics.paymentservicecore.model.error.ApiServiceError
import javax.inject.Inject

class RefundRequestRepository @Inject constructor(
    var apiServiceRequestBuilder: APIServiceRequestBuilder,
    private var buildApiRepository: BuildApiRepository
) {

    suspend fun sendRefundRequest(paymentServiceTxnDetails: PaymentServiceTxnDetails?, onApiServiceResponse:(Any)->Unit) {

        buildApiRepository.apiRefund(
            object :IApiServiceResponseListener{
                override fun onApiSuccess(response: String) {
                    onApiServiceResponse(response)

                }

                override fun onApiFailure(error: Any) {
                    onApiServiceResponse(ApiServiceError(error.toString()))
                }
            },
            BuilderUtils.prepareApiRequestBody(
                apiServiceRequestBuilder.createRefundRequest(paymentServiceTxnDetails)
            )
        )
    }

}