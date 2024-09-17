package com.analogics.paymentservicecore.repository.paymentService.reversal

import com.analogics.builder_core.listener.responseListener.IApiServiceResponseListener
import com.analogics.builder_core.model.PaymentServiceTxnDetails
import com.analogics.builder_core.repository.BuildApiRepository
import com.analogics.builder_core.utils.APIServiceRequestBuilder
import com.analogics.builder_core.utils.BuilderUtils
import com.analogics.paymentservicecore.model.error.PaymentServiceError
import com.analogics.paymentservicecore.repository.paymentService.PaymentServiceRepository
import javax.inject.Inject

class ReversalRequestRepository @Inject constructor(
    private var paymentServiceRepository: PaymentServiceRepository,
    var apiServiceRequestBuilder: APIServiceRequestBuilder,
    private var buildApiRepository: BuildApiRepository,
    private var builderUtils: BuilderUtils
) :
    IApiServiceResponseListener {

    suspend fun sendReversal(paymentServiceTxnDetails: PaymentServiceTxnDetails) {

        buildApiRepository.apiReversal(
            this,
            builderUtils.prepareAPIRequestBody(apiServiceRequestBuilder.createReversalRequest(paymentServiceTxnDetails)
        ))
    }

    override fun onApiSuccessRes(response: String) {
        paymentServiceRepository.onAPIServiceResponse(response)
    }

    override fun onApiFailureRes(error: Any) {
        paymentServiceRepository.onAPIServiceResponse(PaymentServiceError(error.toString()))
    }


}