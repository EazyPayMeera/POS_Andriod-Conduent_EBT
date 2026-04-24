package com.analogics.paymentservicecore.domain.repository.apiService.signOnRequest

import android.content.Context
import com.analogics.builder_core.data.constants.BuilderConstants
import com.analogics.builder_core.domain.listener.responseListener.IBuilderServiceResponseListener
import com.analogics.builder_core.data.model.BuilderServiceTxnDetails
import com.analogics.builder_core.domain.repository.BuilderServiceRepository
import com.analogics.builder_core.utils.BuilderUtils
import com.analogics.paymentservicecore.data.model.PaymentServiceTxnDetails
import com.analogics.paymentservicecore.data.model.error.ApiServiceError
import com.analogics.paymentservicecore.data.model.TxnStatus
import com.analogics.builder_core.builder.ApiRequestBuilder
import com.analogics.paymentservicecore.utils.PaymentServiceUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Handles terminal security lifecycle:
 *
 * - Sign On
 * - Key Exchange
 * - Key Change
 * - Handshake
 * - Sign Off
 *
 * This module is responsible for secure communication with host for:
 * - Master key injection
 * - Work key setup
 * - Terminal activation/deactivation
 */
class SignOnRequestRepository@Inject constructor(
    @ApplicationContext val context: Context,
    var apiRequestBuilder: ApiRequestBuilder,
    private var builderServiceRepository: BuilderServiceRepository
)  {
    /**
     * SIGN ON REQUEST
     * ----------------
     * Establishes terminal session and fetches keys (work keys / config).
     */
    suspend fun signOnRequest(
        paymentServiceTxnDetails: PaymentServiceTxnDetails?,
        onAPIServiceResponse: (Any) -> Unit
    ) {
        val masterKey = paymentServiceTxnDetails?.masterKey

        builderServiceRepository.networkServiceRequest(
            object : IBuilderServiceResponseListener {
                override fun onBuilderSuccess(response: ByteArray) {
                    val isoStr = String(response, Charsets.US_ASCII)
                    val mti = isoStr.take(4)

                    CoroutineScope(Dispatchers.Default).launch {
                        val resPaymentServiceTxnDetails = try {
                            when (mti) {
                                "0810" -> apiRequestBuilder.parseNetworkManResponse(context, response)
                                "0800" -> apiRequestBuilder.parseEcoResponse(context, response)
                                else -> BuilderServiceTxnDetails() // Unknown MTI
                            }
                        } catch (e: Exception) {
                            BuilderServiceTxnDetails()
                        }

                        paymentServiceTxnDetails?.let {
                            if (mti == "0810") {
                                it.hostRespCode = resPaymentServiceTxnDetails.hostRespCode
                                it.txnStatus = if (it.hostRespCode == BuilderConstants.ISO_RESP_CODE_APPROVED)
                                    TxnStatus.APPROVED.toString()
                                else
                                    TxnStatus.DECLINED.toString()
                            }

                            if (mti == "0800") {
                                it.ternNameLoc = resPaymentServiceTxnDetails.terminalId
                                it.deviceSN   = resPaymentServiceTxnDetails.deviceSN
                                it.workKey = resPaymentServiceTxnDetails.workKey
                            }
                            val kcv = BuilderUtils.calculateKCV(paymentServiceTxnDetails.masterKey)
                            if (masterKey != null) {
                                resPaymentServiceTxnDetails.workKey?.let { it1 ->
                                    PaymentServiceUtils.injectKeys(masterKey,
                                        it1,kcv,context)
                                }
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

    /**
     * KEY EXCHANGE REQUEST
     * --------------------
     * Exchanges terminal keys with host.
     */
    suspend fun keyExchangeRequest(paymentServiceTxnDetails: PaymentServiceTxnDetails?, onAPIServiceResponse:(Any)->Unit) {
        builderServiceRepository.networkServiceRequest(
            object : IBuilderServiceResponseListener{
                override fun onBuilderSuccess(response: ByteArray) {
                    CoroutineScope(Dispatchers.Default).launch {
                        var resPaymentServiceTxnDetails = apiRequestBuilder.parseNetworkManResponse(context,response)
                        resPaymentServiceTxnDetails?.let {
                            var keyInjectResult = false
                            val workKey = it.workKey
                            it.hostRespCode = resPaymentServiceTxnDetails.hostRespCode
                            if (it.hostRespCode == BuilderConstants.ISO_RESP_CODE_APPROVED) {
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

    fun keyChangeRequest(
        paymentServiceTxnDetails: PaymentServiceTxnDetails?,
        onAPIServiceResponse: (Any) -> Unit
    ) {
        builderServiceRepository.networkServiceResponse(
            object : IBuilderServiceResponseListener {
                override fun onBuilderSuccess(response: ByteArray) {

                }

                override fun onBuilderFailure(error: Any) {

                }
            },
            apiRequestBuilder.createKeyRequest(
                PaymentServiceUtils.transformObject<BuilderServiceTxnDetails>(paymentServiceTxnDetails)
            )
        )

        val dummyResponse = PaymentServiceTxnDetails().apply {
            txnStatus = TxnStatus.APPROVED.toString()
            hostRespCode = "00"
        }
        onAPIServiceResponse(dummyResponse)
    }

    /**
     * HANDSHAKE REQUEST
     * ------------------
     * Used to validate terminal connectivity with host.
     */
    suspend fun handShakeRequest(paymentServiceTxnDetails: PaymentServiceTxnDetails?, onAPIServiceResponse:(Any)->Unit) {
        builderServiceRepository.handShakeRequest(
            object : IBuilderServiceResponseListener{
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

    /**
     * SIGN OFF REQUEST
     * ----------------
     * Ends terminal session securely.
     */
    suspend fun signOff(paymentServiceTxnDetails: PaymentServiceTxnDetails?, onAPIServiceResponse:(Any)->Unit) {
        builderServiceRepository.networkServiceRequest(
            object : IBuilderServiceResponseListener{
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
            apiRequestBuilder.createSignOffRequest(PaymentServiceUtils.transformObject<BuilderServiceTxnDetails>(paymentServiceTxnDetails))
        )
    }
}