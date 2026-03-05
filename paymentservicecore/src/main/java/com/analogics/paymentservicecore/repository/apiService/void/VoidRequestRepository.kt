package com.eazypaytech.paymentservicecore.repository.apiService.preauth

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.eazypaytech.builder_core.constants.BuilderConstants
import com.eazypaytech.builder_core.listener.responseListener.IBuilderServiceResponseListener
import com.eazypaytech.builder_core.listener.responseListener.IBuilderServiceResponseListenerLyra
import com.eazypaytech.builder_core.model.BuilderServiceTxnDetails
import com.eazypaytech.builder_core.repository.BuilderServiceRepository
import com.eazypaytech.builder_core.repository.BuilderServiceRepositoryLyra
import com.eazypaytech.builder_core.requestBuilder.ApiRequestBuilder
import com.eazypaytech.builder_core.requestBuilder.ApiRequestBuilderLyra
import com.eazypaytech.builder_core.utils.BuilderUtils
import com.eazypaytech.paymentservicecore.constants.AppConstants
import com.eazypaytech.paymentservicecore.constants.EmvConstants
import com.eazypaytech.paymentservicecore.model.PaymentServiceTxnDetails
import com.eazypaytech.paymentservicecore.model.error.ApiServiceError
import com.eazypaytech.paymentservicecore.models.TxnStatus
import com.eazypaytech.paymentservicecore.utils.PaymentServiceUtils
import com.eazypaytech.tpaymentcore.utils.TlvUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class VoidRequestRepository @Inject constructor (
    @ApplicationContext val context: Context,
    var apiRequestBuilder: ApiRequestBuilder,
    private var builderServiceRepository: BuilderServiceRepository,
    var apiRequestBuilderLyra: ApiRequestBuilderLyra,
    private var builderServiceRepositoryLyra: BuilderServiceRepositoryLyra
) {

    @OptIn(ExperimentalStdlibApi::class)
    fun parseIsoRespMessage123(paymentServiceTxnDetails : PaymentServiceTxnDetails, response: ByteArray) : PaymentServiceTxnDetails {
        apiRequestBuilderLyra.parsePurchaseResponse123(context,response).let {
            paymentServiceTxnDetails.stan = it.stan
            paymentServiceTxnDetails.hostRespCode = it.hostRespCode
            paymentServiceTxnDetails.hostAuthCode = it.hostAuthCode
            paymentServiceTxnDetails.hostTxnRef = it.hostTxnRef
            var tlv = TlvUtils(it.emvData)
            /* Extract tag 8A from ISO field if required */
            if(tlv.tlvMap.containsKey(EmvConstants.EMV_TAG_RESP_CODE)==false) {
                it.hostRespCode?.encodeToByteArray()?.toHexString()?.let {
                    tlv.tlvMap[EmvConstants.EMV_TAG_RESP_CODE] = it
                }
            }
            paymentServiceTxnDetails.emvData = tlv.toTlvString()
            if (it.hostRespCode == BuilderConstants.ISO_RESP_CODE_FORMAT_ERROR) {
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


    suspend fun voidRequest(paymentServiceTxnDetails: PaymentServiceTxnDetails?, onAPIServiceResponse:(Any)->Unit) {
        builderServiceRepositoryLyra.networkServiceRequest(
            object : IBuilderServiceResponseListenerLyra{
                @SuppressLint("NewApi")
                override fun onBuilderSuccess(response: ByteArray) {
                    paymentServiceTxnDetails?.let { details ->
                        onAPIServiceResponse(parseIsoRespMessage123(details, response))
                    } ?: run {
                        onAPIServiceResponse(ApiServiceError("paymentServiceTxnDetails is null"))
                    }


                }

                override fun onBuilderFailure(error: Any) {
                    onAPIServiceResponse(ApiServiceError(error.toString()))
                }
            },
            apiRequestBuilderLyra.createReversal0420(PaymentServiceUtils.transformObject<BuilderServiceTxnDetails>(paymentServiceTxnDetails))
        )
    }
}