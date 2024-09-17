package com.analogics.paymentservicecore.repository.paymentService.auth_capture

import com.analogics.builder_core.listener.responseListener.IApiServiceResponseListener
import com.analogics.builder_core.model.PaymentServiceTxnDetails
import com.analogics.builder_core.repository.BuildApiRepository
import com.analogics.builder_core.utils.APIServiceRequestBuilder
import com.analogics.builder_core.utils.BuilderUtils
import com.analogics.paymentservicecore.model.error.PaymentServiceError
import com.analogics.paymentservicecore.repository.paymentService.PaymentServiceRepository
import javax.inject.Inject

class AuthCaptureRequestRepository @Inject constructor(
    private var paymentServiceRepository: PaymentServiceRepository,
    var apiServiceRequestBuilder: APIServiceRequestBuilder,
    private var buildApiRepository: BuildApiRepository,
    private var builderUtils: BuilderUtils
) :
    IApiServiceResponseListener {

    suspend fun sendPreAuthRequest(paymentServiceTxnDetails: PaymentServiceTxnDetails) {

        buildApiRepository.apiPreAuth(
            this,
            builderUtils.prepareAPIRequestBody(
                apiServiceRequestBuilder.createPre_AuthRequest(paymentServiceTxnDetails)
            )
        )
    }

    suspend fun sendAuthCaptureRequest(paymentServiceTxnDetails: PaymentServiceTxnDetails) {

        buildApiRepository.apiPostAuth(
            this,
            builderUtils.prepareAPIRequestBody(
                apiServiceRequestBuilder.createAuthCaptureRequest(paymentServiceTxnDetails)
            )
        )
    }

    override fun onApiSuccessRes(response: String) {
        paymentServiceRepository.onAPIServiceResponse(response)
    }

    override fun onApiFailureRes(error: Any) {
        paymentServiceRepository.onAPIServiceResponse(PaymentServiceError(error.toString()))
    }


}