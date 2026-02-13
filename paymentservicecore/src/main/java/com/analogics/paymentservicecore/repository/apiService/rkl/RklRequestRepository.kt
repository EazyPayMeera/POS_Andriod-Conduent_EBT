package com.eazypaytech.paymentservicecore.repository.apiService.rkl

import android.content.Context
import com.eazypaytech.builder_core.constants.BuilderConstants
import com.eazypaytech.builder_core.listener.responseListener.IBuilderServiceResponseListenerLyra
import com.eazypaytech.builder_core.model.BuilderServiceTxnDetails
import com.eazypaytech.builder_core.repository.BuilderServiceRepositoryLyra
import com.eazypaytech.builder_core.requestBuilder.ApiRequestBuilderLyra
import com.eazypaytech.paymentservicecore.model.PaymentServiceTxnDetails
import com.eazypaytech.paymentservicecore.model.error.ApiServiceError
import com.eazypaytech.paymentservicecore.models.TxnStatus
import com.eazypaytech.paymentservicecore.utils.PaymentServiceUtils
import com.eazypaytech.securityframework.handler.SecureKeyHandler
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
        @OptIn(ExperimentalStdlibApi::class, ExperimentalEncodingApi::class)
        suspend fun apiRklRequest(paymentServiceTxnDetails: PaymentServiceTxnDetails?, onAPIServiceResponse:(Any)->Unit) {
            /* Override Security Parameters from Payment Service. Don't expose in App Module */
            var activationKey = PaymentServiceUtils.generateRsaKey()
            paymentServiceTxnDetails?.devicePublicKey = Base64.encode(activationKey.public.encoded)
            paymentServiceTxnDetails?.devicePrivateKey = Base64.encode(activationKey.private.encoded)
            paymentServiceTxnDetails?.deviceSN = PaymentServiceUtils.getDeviceSN()

            builderServiceRepository.networkServiceRequest(
                object : IBuilderServiceResponseListenerLyra{
                    override fun onBuilderSuccess(response: ByteArray) {
                        CoroutineScope(Dispatchers.Default).launch {
                            /* Parse Response Packet to Object */
                            var keyInjectResult = false
                            var resPaymentServiceTxnDetails =
                                apiRequestBuilder.parseNetworkManResponse(context,response)
                            resPaymentServiceTxnDetails.let {
                                if (it.encryptedIpek?.isNotEmpty() == true && it.ksn?.isNotEmpty() == true && it.kcv?.isNotEmpty() == true) {
                                    var ipek = SecureKeyHandler.decryptRSA(
                                        it.encryptedIpek,
                                        paymentServiceTxnDetails?.devicePrivateKey
                                    )
                                    keyInjectResult =
                                        PaymentServiceUtils.injectKeys(ipek, it.ksn, it.kcv, context)

                                }
                            }

                            paymentServiceTxnDetails?.let {
                                it.hostRespCode = resPaymentServiceTxnDetails.hostRespCode
                                if (it.hostRespCode == BuilderConstants.ISO_RESP_CODE_APPROVED && keyInjectResult == true)
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
                apiRequestBuilder.createSignOnRequest(PaymentServiceUtils.transformObject<BuilderServiceTxnDetails>(paymentServiceTxnDetails))
            )
        }

}