package com.analogics.paymentservicecore.domain.repository.apiService.voidreq

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.analogics.builder_core.data.constants.BuilderConstants
import com.analogics.builder_core.domain.listener.responseListener.IBuilderServiceResponseListenerLyra
import com.analogics.builder_core.data.model.BuilderServiceTxnDetails
import com.analogics.builder_core.domain.repository.BuilderServiceRepositoryLyra
import com.eazypaytech.paymentservicecore.constants.EmvConstants
import com.analogics.paymentservicecore.data.model.PaymentServiceTxnDetails
import com.analogics.paymentservicecore.data.model.error.ApiServiceError
import com.analogics.paymentservicecore.data.model.TxnStatus
import com.analogics.builder_core.builder.ApiRequestBuilderLyra
import com.analogics.paymentservicecore.utils.PaymentServiceUtils
import com.eazypaytech.hardwarecore.utils.TlvUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class VoidRequestRepository @Inject constructor (
    @ApplicationContext val context: Context,
    //var apiRequestBuilder: ApiRequestBuilder,
    //private var builderServiceRepository: BuilderServiceRepository,
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
            paymentServiceTxnDetails.hostResMessage = it.hostResMessage
            var tlv = TlvUtils(it.emvData)
            /* Extract tag 8A from ISO field if required */
            if(tlv.tlvMap.containsKey(EmvConstants.EMV_TAG_RESP_CODE)==false) {
                it.hostRespCode?.encodeToByteArray()?.toHexString()?.let {
                    tlv.tlvMap[EmvConstants.EMV_TAG_RESP_CODE] = it
                }
            }
            paymentServiceTxnDetails.emvData = tlv.toTlvString()
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


    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun voidRequest(paymentServiceTxnDetails: PaymentServiceTxnDetails?, onAPIServiceResponse:(Any)->Unit) {
        builderServiceRepositoryLyra.networkServiceFinancialRequest(
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
            apiRequestBuilderLyra.createVoidRequest(PaymentServiceUtils.transformObject<BuilderServiceTxnDetails>(paymentServiceTxnDetails))
        )
    }
}