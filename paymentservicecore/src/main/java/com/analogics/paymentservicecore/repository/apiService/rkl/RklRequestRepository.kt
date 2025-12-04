package com.eazypaytech.paymentservicecore.repository.apiService.rkl

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.eazypaytech.builder_core.constants.BuilderConstants
import com.eazypaytech.builder_core.listener.responseListener.IBuilderServiceResponseListenerLyra
import com.eazypaytech.builder_core.model.BuilderServiceTxnDetails
import com.eazypaytech.builder_core.repository.BuilderServiceRepositoryLyra
import com.eazypaytech.builder_core.requestBuilder.ApiRequestBuilderLyra
import com.eazypaytech.networkservicecore.nComponent.NetworkCallProvider.safeApiCall
import com.eazypaytech.networkservicecore.nComponent.ResultProvider
import com.eazypaytech.networkservicecore.nComponent.ResultProviderString
import com.eazypaytech.paymentservicecore.model.PaymentServiceTxnDetails
import com.eazypaytech.paymentservicecore.model.error.ApiServiceError
import com.eazypaytech.paymentservicecore.models.TxnStatus
import com.eazypaytech.paymentservicecore.utils.PaymentServiceUtils
import com.eazypaytech.securityframework.handler.SecureKeyHandler
import dagger.hilt.android.internal.Contexts
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

class RklRequestRepository@Inject constructor(
    @ApplicationContext val context: Context,
    var apiRequestBuilder: ApiRequestBuilderLyra,
    private var builderServiceRepository: BuilderServiceRepositoryLyra
)  {
//        @OptIn(ExperimentalStdlibApi::class, ExperimentalEncodingApi::class)
//        suspend fun apiRklRequest(paymentServiceTxnDetails: PaymentServiceTxnDetails?, onAPIServiceResponse:(Any)->Unit) {
//
//            /* Override Security Parameters from Payment Service. Don't expose in App Module */
//            var activationKey = PaymentServiceUtils.generateRsaKey()
//            paymentServiceTxnDetails?.devicePublicKey = Base64.encode(activationKey.public.encoded)
//            paymentServiceTxnDetails?.devicePrivateKey = Base64.encode(activationKey.private.encoded)
//            paymentServiceTxnDetails?.deviceSN = PaymentServiceUtils.getDeviceSN()
//
//            builderServiceRepository.networkServiceRequest(
//                object : IBuilderServiceResponseListenerLyra{
//                    override fun onBuilderSuccess(response: ByteArray) {
//                        CoroutineScope(Dispatchers.Default).launch {
//                            /* Parse Response Packet to Object */
//                            var keyInjectResult = false
//                            var resPaymentServiceTxnDetails =
//                                apiRequestBuilder.parseRklResponse(response)
//                            resPaymentServiceTxnDetails.let {
//                                if (it.encryptedIpek?.isNotEmpty() == true && it.ksn?.isNotEmpty() == true && it.kcv?.isNotEmpty() == true) {
//                                    var ipek = SecureKeyHandler.decryptRSA(
//                                        it.encryptedIpek,
//                                        paymentServiceTxnDetails?.devicePrivateKey
//                                    )
//                                    keyInjectResult =
//                                        PaymentServiceUtils.injectKeys(ipek, it.ksn, it.kcv, context)
//
//                                    Log.d(
//                                        "RKL",
//                                        "Private Key    :${paymentServiceTxnDetails?.devicePrivateKey}"
//                                    )
//                                    Log.d(
//                                        "RKL",
//                                        "Public Key     :${paymentServiceTxnDetails?.devicePublicKey}"
//                                    )
//                                    Log.d("RKL", "Encrypted IPEK :${it.encryptedIpek}")
//                                    Log.d("RKL", "Decrypted IPEK :$ipek")
//                                    Log.d("RKL", "KSN            :${it.ksn}")
//                                    Log.d("RKL", "KCV            :${it.kcv}")
//                                    Log.d("RKL", "RKL Success    :${keyInjectResult}")
//                                }
//                            }
//
//                            paymentServiceTxnDetails?.let {
//                                it.hostRespCode = resPaymentServiceTxnDetails.hostRespCode
//
//                                if (it.hostRespCode == BuilderConstants.ISO_RESP_CODE_APPROVED && keyInjectResult == true)
//                                    it.txnStatus = TxnStatus.APPROVED.toString()
//                                else
//                                    it.txnStatus = TxnStatus.DECLINED.toString()
//
//                                onAPIServiceResponse(it)
//                            }
//                        }
//                    }
//
//                    override fun onBuilderFailure(error: Any) {
//                        onAPIServiceResponse(ApiServiceError(error.toString()))
//                    }
//                },
//                apiRequestBuilder.createRklRequest(PaymentServiceUtils.transformObject<BuilderServiceTxnDetails>(paymentServiceTxnDetails))
//            )
//        }

    @OptIn(ExperimentalStdlibApi::class, ExperimentalEncodingApi::class)
    suspend fun apiRklRequest(onAPIServiceResponse: (Any) -> Unit) {
        try {
            Log.d("Conduent", "apiRklRequest started")

            // Create 0800 Echo Test ISO message
            val requestBytes = apiRequestBuilder.createRklRequest()
            Log.d("Conduent", "Request created: ${requestBytes.joinToString(" ") { "%02X".format(it) }}")
            Log.d("Conduent", "Request ASCII: ${requestBytes.map { if (it in 32..126) it.toChar() else '.' }.joinToString("")}")

            // Send message via TCP/TLS and get response
            Log.d("Conduent", "Calling safeApiCall...")
            val result = safeApiCall(requestBytes)
            Log.d("Conduent", "safeApiCall returned")

            when (result) {
                is ResultProvider.Success -> {
                    val response = result.data
                    Log.d("Conduent", "Raw response received: ${response.joinToString(" ") { "%02X".format(it) }}")
                    Log.d("Conduent", "Response ASCII: ${response.map { if (it in 32..126) it.toChar() else '.' }.joinToString("")}")
                    Log.d("Conduent", "Response size: ${response.size}")
                    val success = PaymentServiceUtils.injectWorkingPinKey("625118864C4A99CBF0E2D95B635046621D7DF008")
                    if (success) {
                        Log.d("Sucess","Working key is loaded")
                    } else {
                        Log.d("Sucess","Working key is not loaded")
                    }
                    // Parse response
                    val parsedResponse = try {
                        apiRequestBuilder.parseRklResponse(response)
                    } catch (e: Exception) {
                        Log.e("EchoTest", "Parsing error: ${e.message}")
                        null
                    }

                    if (parsedResponse != null) {
                        Log.d("Conduent", "Parsed Response: hostRespCode=${parsedResponse.hostRespCode}")
                        val txnStatus = if (parsedResponse.hostRespCode == BuilderConstants.ISO_RESP_CODE_APPROVED)
                            TxnStatus.APPROVED.toString()
                        else
                            TxnStatus.DECLINED.toString()
                        Log.d("Conduent", "Transaction status determined: $txnStatus")
                        onAPIServiceResponse(parsedResponse.copy(txnStatus = txnStatus))
                    } else {
                        Log.e("Conduent", "Parsed response is null")
                        onAPIServiceResponse(ApiServiceError("Failed to parse response"))
                    }
                }

                is ResultProvider.Error -> {
                    Log.e("Conduent", "No response / error: ${result.exception.message}")
                    onAPIServiceResponse(ApiServiceError(result.exception.message ?: "Unknown error"))
                }

                is ResultProviderString.Error -> {
                    Log.e("EchoTest", "ResultProviderString.Error received")
                    onAPIServiceResponse(ApiServiceError("ResultProviderString.Error"))
                }

                ResultProvider.Loading -> Log.d("EchoTest", "ResultProvider.Loading received")
                ResultProviderString.Loading -> Log.d("EchoTest", "ResultProviderString.Loading received")
                is ResultProviderString.Success -> Log.d("EchoTest", "ResultProviderString.Success received")
            }

        } catch (e: Exception) {
            Log.e("EchoTest", "Exception in apiRklRequest: ${e.message}")
            onAPIServiceResponse(ApiServiceError(e.message ?: "Unknown exception"))
        }
    }


}