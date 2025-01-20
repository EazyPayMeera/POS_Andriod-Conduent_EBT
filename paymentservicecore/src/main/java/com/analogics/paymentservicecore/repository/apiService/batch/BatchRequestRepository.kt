package com.eazypaytech.paymentservicecore.repository.apiService.batch

import com.eazypaytech.builder_core.listener.responseListener.IBuilderServiceResponseListener
import com.eazypaytech.builder_core.model.BuilderServiceTxnDetails
import com.eazypaytech.builder_core.repository.BuilderServiceRepository
import com.eazypaytech.builder_core.requestBuilder.ApiRequestBuilder
import com.eazypaytech.builder_core.utils.BuilderUtils
import com.eazypaytech.paymentservicecore.model.PaymentServiceTxnDetails
import com.eazypaytech.paymentservicecore.model.error.ApiServiceError
import com.eazypaytech.paymentservicecore.utils.PaymentServiceUtils
import javax.inject.Inject

class BatchRequestRepository @Inject constructor(
    var apiRequestBuilder: ApiRequestBuilder,
    private var builderServiceRepository: BuilderServiceRepository
) {

    suspend fun sendBatchRequest(paymentServiceTxnDetails: PaymentServiceTxnDetails?, onApiServiceResponse:(Any)->Unit) {

        builderServiceRepository.apiRefund(
            object :IBuilderServiceResponseListener{
                override fun onBuilderSuccess(response: String) {
                    onApiServiceResponse(response)
                        //call db repo insert query for batch with batch id into batch table

                }

                override fun onBuilderFailure(error: Any) {
                    onApiServiceResponse(ApiServiceError(error.toString()))
                }
            },
            BuilderUtils.prepareApiRequestBody(
                apiRequestBuilder.createRefundRequest(PaymentServiceUtils.transformObject<BuilderServiceTxnDetails>(paymentServiceTxnDetails))
            )
        )
    }

}