package com.analogics.paymentservicecore.domain.repository.apiService.reversal

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.analogics.builder_core.data.constants.BuilderConstants
//import com.eazypaytech.builder_core.listener.responseListener.IBuilderServiceResponseListener
import com.analogics.builder_core.domain.listener.responseListener.IBuilderServiceResponseListenerLyra
import com.analogics.builder_core.data.model.BuilderServiceTxnDetails
//import com.eazypaytech.builder_core.repository.BuilderServiceRepository
import com.analogics.builder_core.domain.repository.BuilderServiceRepositoryLyra
//import com.eazypaytech.builder_core.requestBuilder.ApiRequestBuilder
import com.analogics.paymentservicecore.data.model.PaymentServiceTxnDetails
import com.analogics.paymentservicecore.data.model.error.ApiServiceError
import com.analogics.paymentservicecore.data.model.TxnStatus
import com.analogics.builder_core.builder.ApiRequestBuilderLyra
import com.analogics.paymentservicecore.utils.PaymentServiceUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class ReversalRequestRepository @Inject constructor(
    @ApplicationContext val context: Context,
    var apiRequestBuilder: ApiRequestBuilderLyra,
    private var builderServiceRepository: BuilderServiceRepositoryLyra
) {

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun sendReversal(paymentServiceTxnDetails: PaymentServiceTxnDetails?, onAPIServiceResponse:(Any)->Unit) {
        Log.d("REVERSAL_TXN", "paymentServiceTxnDetails: $paymentServiceTxnDetails")
        builderServiceRepository.networkServiceFinancialRequest(
            object : IBuilderServiceResponseListenerLyra {
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
            apiRequestBuilder.createVoidRequest(PaymentServiceUtils.transformObject<BuilderServiceTxnDetails>(paymentServiceTxnDetails))
        )
    }

}