package com.analogics.paymentservicecore.repository.apiService.purchase

import android.util.Log
import com.analogics.builder_core.listener.responseListener.IBuilderServiceResponseListener
import com.analogics.builder_core.model.BuilderServiceTxnDetails
import com.analogics.builder_core.repository.BuilderServiceRepository
import com.analogics.builder_core.requestBuilder.ApiRequestBuilder
import com.analogics.builder_core.utils.BuilderUtils
import com.analogics.paymentservicecore.model.PaymentServiceTxnDetails
import com.analogics.paymentservicecore.model.error.ApiServiceError
import com.analogics.paymentservicecore.utils.PaymentServiceUtils
import com.analogics.securityframework.database.dbRepository.TxnDBRepository
import com.analogics.securityframework.database.entity.TxnEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class PurchaseRequestRepository @Inject constructor(
    var apiRequestBuilder: ApiRequestBuilder,
    private var builderServiceRepository: BuilderServiceRepository,
    var dbRepository: TxnDBRepository
) {
    lateinit var paymentServiceTxnDetails1:PaymentServiceTxnDetails
    suspend fun sendPurchaseRequest(paymentServiceTxnDetails: PaymentServiceTxnDetails?,onAPIServiceResponse:(Any)->Unit) {
        val requestDetails =
            PaymentServiceUtils.objectToJsonString(paymentServiceTxnDetails)
        PaymentServiceUtils.jsonStringToObject<TxnEntity>(requestDetails)?.let {
            dbRepository.insertTxn(
                it
            )
        }
        paymentServiceTxnDetails1= PaymentServiceUtils.jsonStringToObject<PaymentServiceTxnDetails>(requestDetails)!!
        Log.d("record insert", requestDetails)
        builderServiceRepository.apiPurchase(
            object :IBuilderServiceResponseListener{
                override fun onBuilderSuccess(response: String) {
                    onAPIServiceResponse(response)
                    Log.d("record insert", "onApiSuccessRes")

                }

                override fun onBuilderFailure(error: Any) {
                    Log.d("record insert", "onApiFailureRes")
                    CoroutineScope(Dispatchers.IO).launch {
                        val requestDetails =
                            PaymentServiceUtils.objectToJsonString(paymentServiceTxnDetails1)
                        PaymentServiceUtils.jsonStringToObject<TxnEntity>(requestDetails)?.let {
                            dbRepository.updateTxn(
                                it
                            )
                            Log.d("record update", requestDetails)
                        }
                    }
                    onAPIServiceResponse(ApiServiceError(error.toString()))
                    paymentServiceTxnDetails?.let { onAPIServiceResponse(it) }

                }
            },
            BuilderUtils.prepareApiRequestBody(
                apiRequestBuilder.createPurchaseRequest(PaymentServiceUtils.transformObject<BuilderServiceTxnDetails>(paymentServiceTxnDetails))
            )
        )
    }
}