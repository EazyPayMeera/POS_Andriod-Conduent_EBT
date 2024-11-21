package com.analogics.paymentservicecore.repository.apiService.purchase

import android.util.Log
import com.analogics.builder_core.listener.responseListener.IBuilderServiceResponseListener
import com.analogics.builder_core.listener.responseListener.IBuilderServiceResponseListenerLyra
import com.analogics.builder_core.model.BuilderServiceTxnDetails
import com.analogics.builder_core.repository.BuilderServiceRepository
import com.analogics.builder_core.repository.BuilderServiceRepositoryLyra
import com.analogics.builder_core.requestBuilder.ApiRequestBuilder
import com.analogics.builder_core.requestBuilder.ApiRequestBuilderLyra
import com.analogics.builder_core.utils.BuilderUtils
import com.analogics.paymentservicecore.constants.AppConstants
import com.analogics.paymentservicecore.model.PaymentServiceTxnDetails
import com.analogics.paymentservicecore.model.error.ApiServiceError
import com.analogics.paymentservicecore.utils.PaymentServiceUtils
import com.analogics.securityframework.database.dbRepository.TxnDBRepository
import com.analogics.securityframework.database.entity.TxnEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

class PurchaseRequestRepository @Inject constructor(
    var apiRequestBuilder: ApiRequestBuilder,
    private var builderServiceRepository: BuilderServiceRepository,
    var apiRequestBuilderLyra: ApiRequestBuilderLyra,
    private var builderServiceRepositoryLyra: BuilderServiceRepositoryLyra,
    var dbRepository: TxnDBRepository
) {
    lateinit var paymentServiceTxnDetails1:PaymentServiceTxnDetails
    suspend fun sendPurchaseRequest(paymentServiceTxnDetails: PaymentServiceTxnDetails?,onAPIServiceResponse:(Any)->Unit) {

        /* Insert entry into DB & update later */
        PaymentServiceUtils.transformObject<TxnEntity>(paymentServiceTxnDetails)?.let {
            dbRepository.insertTxn(
                it
            )
        }

        if(paymentServiceTxnDetails?.acquirerName == AppConstants.ACQUIRER_LYRA)
        {
            if(paymentServiceTxnDetails.isDemoMode==true)
            {
                //apiRequestBuilderLyra.createPurchaseRequest(PaymentServiceUtils.transformObject<BuilderServiceTxnDetails>(paymentServiceTxnDetails))
                delay(AppConstants.DEMO_MODE_PROMPTS_DELAY_MS)
                onAPIServiceResponse(paymentServiceTxnDetails)
            }
            else {
                builderServiceRepositoryLyra.networkServiceRequest(object :
                    IBuilderServiceResponseListenerLyra {
                    override fun onBuilderSuccess(response: ByteArray) {
                        onAPIServiceResponse(paymentServiceTxnDetails)
                    }

                    override fun onBuilderFailure(error: Any) {
                        onAPIServiceResponse(ApiServiceError(error.toString()))
                    }

                },
                    apiRequestBuilderLyra.createPurchaseRequest(
                        PaymentServiceUtils.transformObject<BuilderServiceTxnDetails>(
                            paymentServiceTxnDetails
                        )
                    )
                )
            }
        }
        else {
            builderServiceRepository.apiPurchase(
                object : IBuilderServiceResponseListener {
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
                    apiRequestBuilder.createPurchaseRequest(
                        PaymentServiceUtils.transformObject<BuilderServiceTxnDetails>(
                            paymentServiceTxnDetails
                        )
                    )
                )

            )
        }
    }
}