package com.analogics.paymentservicecore.repository.apiService.purchase

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.analogics.builder_core.constants.BuilderConstants
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
import com.analogics.paymentservicecore.models.TxnStatus
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
    var apiRequestBuilderLyra: ApiRequestBuilderLyra,
    private var builderServiceRepositoryLyra: BuilderServiceRepositoryLyra,
    var dbRepository: TxnDBRepository
) {
    //lateinit var paymentServiceTxnDetails:PaymentServiceTxnDetails
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun sendPurchaseRequest(paymentServiceTxnDetails: PaymentServiceTxnDetails?, onAPIServiceResponse:(Any)->Unit) {

        /* Insert entry into DB & update later */
        PaymentServiceUtils.transformObject<TxnEntity>(paymentServiceTxnDetails)?.let {
            dbRepository.insertOrUpdateTxn(
                it
            )
        }

        if(paymentServiceTxnDetails?.acquirerName == AppConstants.ACQUIRER_LYRA) {
            var request = apiRequestBuilderLyra.createPurchaseRequest(
                PaymentServiceUtils.transformObject<BuilderServiceTxnDetails>(
                    paymentServiceTxnDetails
                )
            )

            if(paymentServiceTxnDetails.isDemoMode == true)
            {
                onAPIServiceResponse(parseIsoRespMessage(paymentServiceTxnDetails,
                    apiRequestBuilderLyra.buildDummyPurchaseResponse()))
            }
            else {
                builderServiceRepositoryLyra.networkServiceRequest(
                    object :
                        IBuilderServiceResponseListenerLyra {
                        override fun onBuilderSuccess(response: ByteArray) {
                            onAPIServiceResponse(parseIsoRespMessage(paymentServiceTxnDetails,response))
                        }

                        override fun onBuilderFailure(error: Any) {
                            onAPIServiceResponse(ApiServiceError(error.toString()))
                        }
                    },
                    request
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
                                PaymentServiceUtils.objectToJsonString(paymentServiceTxnDetails)
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

    fun parseIsoRespMessage(paymentServiceTxnDetails : PaymentServiceTxnDetails, response: ByteArray) : PaymentServiceTxnDetails {
            apiRequestBuilderLyra.parsePurchaseResponse(response).let {
                paymentServiceTxnDetails.hostRespCode = it.hostRespCode
                paymentServiceTxnDetails.hostAuthCode = it.hostAuthCode
                paymentServiceTxnDetails.hostTxnRef = it.hostTxnRef
                paymentServiceTxnDetails.emvData = it.emvData
                if (it.hostRespCode == BuilderConstants.ISO_RESP_CODE_APPROVED) {
                    paymentServiceTxnDetails.hostAuthResult = TxnStatus.APPROVED.toString()
                    paymentServiceTxnDetails.txnStatus = TxnStatus.APPROVED.toString()
                }
                else {
                    paymentServiceTxnDetails.hostAuthResult = TxnStatus.DECLINED.toString()
                    paymentServiceTxnDetails.txnStatus = TxnStatus.DECLINED.toString()
                }
            }
        return paymentServiceTxnDetails
    }
}