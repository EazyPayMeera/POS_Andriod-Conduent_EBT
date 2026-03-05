package com.eazypaytech.paymentservicecore.repository.apiService.rkl

import android.content.Context
import android.util.Log
import com.eazypaytech.builder_core.constants.BuilderConstants
import com.eazypaytech.builder_core.listener.responseListener.IBuilderServiceResponseListenerLyra
import com.eazypaytech.builder_core.model.BuilderServiceTxnDetails
import com.eazypaytech.builder_core.repository.BuilderServiceRepositoryLyra
import com.eazypaytech.builder_core.requestBuilder.ApiRequestBuilderLyra
import com.eazypaytech.paymentservicecore.model.PaymentServiceTxnDetails
import com.eazypaytech.paymentservicecore.model.error.ApiServiceError
import com.eazypaytech.paymentservicecore.models.TxnStatus
import com.eazypaytech.paymentservicecore.utils.PaymentServiceUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject


class RklRequestRepository@Inject constructor(
    @ApplicationContext val context: Context,
    var apiRequestBuilder: ApiRequestBuilderLyra,
    private var builderServiceRepository: BuilderServiceRepositoryLyra
)  {
    suspend fun signOnRequest(
        paymentServiceTxnDetails: PaymentServiceTxnDetails?,
        onAPIServiceResponse: (Any) -> Unit
    ) {
        builderServiceRepository.networkServiceRequest(
            object : IBuilderServiceResponseListenerLyra {
                override fun onBuilderSuccess(response: ByteArray) {
                    val isoStr = String(response, Charsets.US_ASCII)
                    val mti = isoStr.take(4)
                    Log.d("Conduent", "Received MTI in listener: $isoStr")

                    CoroutineScope(Dispatchers.Default).launch {
                        val resPaymentServiceTxnDetails = try {
                            when (mti) {
                                "0810" -> apiRequestBuilder.parseNetworkManResponse(context, response)
                                "0800" -> apiRequestBuilder.parseEcoResponse(context, response)
                                else -> BuilderServiceTxnDetails() // Unknown MTI
                            }
                        } catch (e: Exception) {
                            Log.e("ISO_PARSE", "Failed to parse MTI $mti", e)
                            BuilderServiceTxnDetails()
                        }

                        paymentServiceTxnDetails?.let {
                            // Update host response and txn status only for Sign-On (0810)
                            if (mti == "0810") {
                                it.hostRespCode = resPaymentServiceTxnDetails.hostRespCode
                                it.txnStatus = if (it.hostRespCode == BuilderConstants.ISO_RESP_CODE_APPROVED)
                                    TxnStatus.APPROVED.toString()
                                else
                                    TxnStatus.DECLINED.toString()
                            }

                            // You can also update Echo-specific fields if needed
                            if (mti == "0800") {
                                it.ternNameLoc = resPaymentServiceTxnDetails.terminalId
                                it.deviceSN   = resPaymentServiceTxnDetails.deviceSN
                            }

                            onAPIServiceResponse(it)
                        }
                    }
                }

                override fun onBuilderFailure(error: Any) {
                    onAPIServiceResponse(ApiServiceError(error.toString()))
                }
            },
            apiRequestBuilder.createSignOnRequest(
                PaymentServiceUtils.transformObject<BuilderServiceTxnDetails>(paymentServiceTxnDetails)
            )
        )
    }

    suspend fun keyExchangeRequest(paymentServiceTxnDetails: PaymentServiceTxnDetails?, onAPIServiceResponse:(Any)->Unit) {
        builderServiceRepository.networkServiceRequest(
            object : IBuilderServiceResponseListenerLyra{
                override fun onBuilderSuccess(response: ByteArray) {
                    CoroutineScope(Dispatchers.Default).launch {
                        var resPaymentServiceTxnDetails = apiRequestBuilder.parseNetworkManResponse(context,response)
                        resPaymentServiceTxnDetails?.let {
                            var keyInjectResult = false
                            val workKey = it.workKey
                            Log.d("KEY_DEBUG", "Work Key: $workKey")
                            if (!workKey.isNullOrEmpty()) {
                                val encryptedPinKey = workKey.substring(0, 32)
                                val checkValue = workKey.substring(32)

                                Log.d("KEY_DEBUG", "Encrypted Key: $encryptedPinKey")
                                Log.d("KEY_DEBUG", "KCV From Host: $checkValue")

                                keyInjectResult =
                                    PaymentServiceUtils.injectWorkingPinKey(encryptedPinKey, context)

                                Log.d("KEY_DEBUG", "Injection Result: $keyInjectResult")
                            }
                            it.hostRespCode = resPaymentServiceTxnDetails.hostRespCode
                            if (it.hostRespCode == BuilderConstants.ISO_RESP_CODE_APPROVED) {
                                Log.d("Response", "Aprroved and Key Injection pass")
                                it.txnStatus = TxnStatus.APPROVED.toString()
                            }
                            else
                                it.txnStatus = TxnStatus.DECLINED.toString()

                            onAPIServiceResponse(it)
                        }
                    }
                }

                override fun onBuilderFailure(error: Any) {
                    onAPIServiceResponse(ApiServiceError(error.toString()))
                }
            },
            apiRequestBuilder.createKeyChangeRequest(PaymentServiceUtils.transformObject<BuilderServiceTxnDetails>(paymentServiceTxnDetails))
        )
    }

    suspend fun keyChangeRequest(paymentServiceTxnDetails: PaymentServiceTxnDetails?, onAPIServiceResponse:(Any)->Unit) {
        builderServiceRepository.networkServiceRequest(
            object : IBuilderServiceResponseListenerLyra{
                override fun onBuilderSuccess(response: ByteArray) {
                    CoroutineScope(Dispatchers.Default).launch {
                        var resPaymentServiceTxnDetails = apiRequestBuilder.parseNetworkManResponse(context,response)
                        paymentServiceTxnDetails?.let {
                            it.hostRespCode = resPaymentServiceTxnDetails.hostRespCode
                            if (it.hostRespCode == BuilderConstants.ISO_RESP_CODE_APPROVED /*&& keyInjectResult == true*/)
                                it.txnStatus = TxnStatus.APPROVED.toString()
                            else
                                it.txnStatus = TxnStatus.DECLINED.toString()

                            onAPIServiceResponse(it)
                        }
                    }
                }

                override fun onBuilderFailure(error: Any) {
                    onAPIServiceResponse(ApiServiceError(error.toString()))
                }
            },
            apiRequestBuilder.createKeyRequest(PaymentServiceUtils.transformObject<BuilderServiceTxnDetails>(paymentServiceTxnDetails))
        )
    }

    suspend fun handShakeRequest(paymentServiceTxnDetails: PaymentServiceTxnDetails?, onAPIServiceResponse:(Any)->Unit) {
        builderServiceRepository.networkServiceRequest(
            object : IBuilderServiceResponseListenerLyra{
                override fun onBuilderSuccess(response: ByteArray) {
                    CoroutineScope(Dispatchers.Default).launch {
                        var resPaymentServiceTxnDetails = apiRequestBuilder.parseNetworkManResponse(context,response)
                        paymentServiceTxnDetails?.let {
                            it.hostRespCode = resPaymentServiceTxnDetails.hostRespCode
                            if (it.hostRespCode == BuilderConstants.ISO_RESP_CODE_APPROVED /*&& keyInjectResult == true*/)
                                it.txnStatus = TxnStatus.APPROVED.toString()
                            else
                                it.txnStatus = TxnStatus.DECLINED.toString()

                            onAPIServiceResponse(it)
                        }
                    }
                }

                override fun onBuilderFailure(error: Any) {
                    onAPIServiceResponse(ApiServiceError(error.toString()))
                }
            },
            apiRequestBuilder.createHandShakeRequest(PaymentServiceUtils.transformObject<BuilderServiceTxnDetails>(paymentServiceTxnDetails))
        )
    }
}