package com.analogics.paymentservicecore.repository.paymentService.purchase

import com.analogics.builder_core.listener.responseListener.IApiServiceResponseListener
import com.analogics.builder_core.model.PaymentServiceTxnDetails
import com.analogics.builder_core.repository.BuildApiRepository
import com.analogics.builder_core.utils.APIServiceRequestBuilder
import com.analogics.builder_core.utils.BuilderUtils
import javax.inject.Inject

class PurchaseRequestRepository @Inject constructor(
    var apiServiceRequestBuilder: APIServiceRequestBuilder,
    private var buildApiRepository: BuildApiRepository
) {
    suspend fun sendPurchaseRequest(paymentServiceTxnDetails: PaymentServiceTxnDetails?,onAPIServiceResponse:(Any)->Unit) {

        buildApiRepository.apiPurchase(
            object :IApiServiceResponseListener{
                override fun onApiSuccess(response: String) {
                    onAPIServiceResponse(response)

                }

                override fun onApiFailure(error: Any) {
                   // onAPIServiceResponse(PaymentServiceError(error.toString()))

                    paymentServiceTxnDetails?.let { onAPIServiceResponse(it) }

                }
            },
            BuilderUtils.prepareApiRequestBody(
                apiServiceRequestBuilder.createPurchaseRequest(paymentServiceTxnDetails)
            )
        )
    }


}