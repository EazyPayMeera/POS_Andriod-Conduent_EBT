package com.analogics.paymentservicecore.repository.paymentService.purchase

import android.util.Log
import com.analogics.builder_core.listener.responseListener.IApiServiceResponseListener
import com.analogics.builder_core.model.PaymentServiceTxnDetails
import com.analogics.builder_core.repository.BuildApiRepository
import com.analogics.builder_core.utils.APIServiceRequestBuilder
import com.analogics.builder_core.utils.BuilderUtils
import com.analogics.paymentservicecore.model.error.ApiServiceError
import com.analogics.paymentservicecore.model.error.PaymentServiceError
import com.analogics.paymentservicecore.repository.paymentService.PaymentServiceRepository
import com.analogics.paymentservicecore.utils.PaymentServiceUtils
import com.analogics.securityframework.database.dbRepository.TxnDBRepository
import com.analogics.securityframework.database.entity.TxnEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class PurchaseRequestRepository @Inject constructor(
    var apiServiceRequestBuilder: APIServiceRequestBuilder,
    private var buildApiRepository: BuildApiRepository,
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
        buildApiRepository.apiPurchase(
            object :IApiServiceResponseListener{
                override fun onApiSuccess(response: String) {
                    onAPIServiceResponse(response)
                    Log.d("record insert", "onApiSuccessRes")

                }

                override fun onApiFailure(error: Any) {
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
                apiServiceRequestBuilder.createPurchaseRequest(paymentServiceTxnDetails)
            )
        )
    }


}