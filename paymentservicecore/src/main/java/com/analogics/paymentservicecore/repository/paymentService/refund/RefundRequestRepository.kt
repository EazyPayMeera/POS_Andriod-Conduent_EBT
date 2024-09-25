package com.analogics.paymentservicecore.repository.paymentService.refund

import com.analogics.builder_core.listener.responseListener.IApiServiceResponseListener
import com.analogics.builder_core.model.PaymentServiceTxnDetails
import com.analogics.builder_core.repository.BuildApiRepository
import com.analogics.builder_core.utils.APIServiceRequestBuilder
import com.analogics.builder_core.utils.BuilderUtils
import com.analogics.paymentservicecore.model.error.PaymentServiceError
import com.analogics.paymentservicecore.repository.paymentService.PaymentServiceRepository
import javax.inject.Inject

class RefundRequestRepository @Inject constructor(
    private var paymentServiceRepository: Lazy<PaymentServiceRepository>,
    var apiServiceRequestBuilder: APIServiceRequestBuilder,
    private var buildApiRepository: BuildApiRepository
) : IApiServiceResponseListener {

    suspend fun sendRefundRequest(paymentServiceTxnDetails: PaymentServiceTxnDetails?) {

        buildApiRepository.apiRefund(
            this,
            BuilderUtils.prepareAPIRequestBody(
                apiServiceRequestBuilder.createRefundRequest(paymentServiceTxnDetails)
            )
        )
    }

    override fun onApiSuccessRes(response: String) {
        paymentServiceRepository.value.onAPIServiceResponse(response)
    }

    override fun onApiFailureRes(error: Any) {
        paymentServiceRepository.value.onAPIServiceResponse(PaymentServiceError(error.toString()))
    }


}