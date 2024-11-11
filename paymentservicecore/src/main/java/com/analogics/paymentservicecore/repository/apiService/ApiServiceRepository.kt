package com.analogics.paymentservicecore.repository.apiService


import com.analogics.builder_core.model.PaymentServiceTxnDetails
import com.analogics.paymentservicecore.listeners.requestListener.IApiServiceRequestListener
import com.analogics.paymentservicecore.listeners.responseListener.IApiServiceResponseListener
import com.analogics.paymentservicecore.model.error.ApiServiceError
import com.analogics.paymentservicecore.models.PosConfig
import com.analogics.paymentservicecore.models.TxnType
import com.analogics.paymentservicecore.repository.apiService.access_token.AccessTokenRequestRepository
import com.analogics.paymentservicecore.repository.apiService.auth_capture.AuthCaptureRequestRepository
import com.analogics.paymentservicecore.repository.apiService.batch.BatchRequestRepository
import com.analogics.paymentservicecore.repository.apiService.login.LoginRequestRepository
import com.analogics.paymentservicecore.repository.apiService.purchase.PurchaseRequestRepository
import com.analogics.paymentservicecore.repository.apiService.refund.RefundRequestRepository
import com.analogics.paymentservicecore.repository.apiService.reversal.ReversalRequestRepository
import com.analogics.paymentservicecore.repository.apiService.rkl.RklRequestRepository
import com.analogics.paymentservicecore.utils.PaymentServiceUtils
import com.analogics.securityframework.database.dbRepository.TxnDBRepository
import javax.inject.Inject
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

class ApiServiceRepository @Inject constructor(
    private val accessTokenRequestRepository: AccessTokenRequestRepository,
    private var refundRequestRepository: RefundRequestRepository,
    private val authCaptureRequestRepository: AuthCaptureRequestRepository,
    private val loginRequestRepository: LoginRequestRepository,
    private val reversalRequestRepository: ReversalRequestRepository,
    private val voidRequestRepository: VoidRequestRepository,
    private val purchaseRequestRepository: PurchaseRequestRepository,
    private val batchRequestRepository: BatchRequestRepository,
    private val rklRequestRepository: RklRequestRepository,
    private val dbRepository: TxnDBRepository,
    private val posConfig: PosConfig
) : IApiServiceRequestListener
 {
    lateinit var iApiServiceResponseListener: IApiServiceResponseListener

    override fun getPosConfig(): PosConfig {
        return posConfig.loadFromPrefs()
    }

    override suspend fun apiServiceRefund(
        paymentServiceTxnDetails: PaymentServiceTxnDetails?,
        iApiServiceResponseListener: IApiServiceResponseListener
    ) {
        this.iApiServiceResponseListener = iApiServiceResponseListener
        refundRequestRepository.sendRefundRequest(paymentServiceTxnDetails){
            onApiServiceResponse(it)
        }
    }

    override suspend fun apiServiceVoid(
        paymentServiceTxnDetails: PaymentServiceTxnDetails?,
        iApiServiceResponseListener: IApiServiceResponseListener
    ) {
        this.iApiServiceResponseListener = iApiServiceResponseListener
        voidRequestRepository.sendVoidRequest(paymentServiceTxnDetails){
            onApiServiceResponse(it)
        }
    }

    override suspend fun apiServicePurchase(
        paymentServiceTxnDetails: PaymentServiceTxnDetails?,
        iApiServiceResponseListener: IApiServiceResponseListener
    ) {
        this.iApiServiceResponseListener = iApiServiceResponseListener
        purchaseRequestRepository.sendPurchaseRequest(paymentServiceTxnDetails){
            onApiServiceResponse(it)
        }
    }

    override suspend fun apiServiceAuthCapture(
        paymentServiceTxnDetails: PaymentServiceTxnDetails?,
        iApiServiceResponseListener: IApiServiceResponseListener
    ) {
        this.iApiServiceResponseListener = iApiServiceResponseListener
        if(TxnType.PREAUTH==TxnType.PREAUTH) {
            authCaptureRequestRepository.sendPreAuthRequest(paymentServiceTxnDetails){
                onApiServiceResponse(it)
            }
        }else {
            authCaptureRequestRepository.sendAuthCaptureRequest(paymentServiceTxnDetails){
                onApiServiceResponse(it)
            }
        }
    }

    override suspend fun apiServiceReversal(
        paymentServiceTxnDetails: PaymentServiceTxnDetails?,
        iApiServiceResponseListener: IApiServiceResponseListener
    ) {
        this.iApiServiceResponseListener = iApiServiceResponseListener
        reversalRequestRepository.sendReversal(paymentServiceTxnDetails){
            onApiServiceResponse(it)
        }
    }

    override suspend fun apiServiceLogin(
        paymentServiceTxnDetails: PaymentServiceTxnDetails?,
        iApiServiceResponseListener: IApiServiceResponseListener
    ) {
        this.iApiServiceResponseListener = iApiServiceResponseListener
        loginRequestRepository.apiDeviceLogin(paymentServiceTxnDetails){
            onApiServiceResponse(it)
        }
    }

    override suspend fun apiServiceAccessToken(
        paymentServiceTxnDetails: PaymentServiceTxnDetails?,
        iApiServiceResponseListener: IApiServiceResponseListener
    ) {
        this.iApiServiceResponseListener = iApiServiceResponseListener
        this.iApiServiceResponseListener.onDisplayProgress(true)
        accessTokenRequestRepository.apiGetAccessToken(paymentServiceTxnDetails){
            onApiServiceResponse(it)
        }
    }

     @OptIn(ExperimentalEncodingApi::class)
     override suspend fun apiServiceRklRequest(
         paymentServiceTxnDetails: PaymentServiceTxnDetails?,
         iApiServiceResponseListener: IApiServiceResponseListener
     ) {
         this.iApiServiceResponseListener = iApiServiceResponseListener
         this.iApiServiceResponseListener.onDisplayProgress(true)

         rklRequestRepository.apiRklRequest(paymentServiceTxnDetails){
             onApiServiceResponse(it)
         }
     }

    override suspend fun apiServiceBatch(
        paymentServiceTxnDetails: PaymentServiceTxnDetails?,
        iApiServiceResponseListener: IApiServiceResponseListener
    ) {
        this.iApiServiceResponseListener = iApiServiceResponseListener
        batchRequestRepository.sendBatchRequest(paymentServiceTxnDetails){
            onApiServiceResponse(it)
        }
    }

    override fun onApiServiceResponse(response: Any) {
        iApiServiceResponseListener.onDisplayProgress(false)
        when (response) {
            is ApiServiceError -> {
                iApiServiceResponseListener.onApiError(ApiServiceError(response.toString()))
            }
            else ->
            {
                iApiServiceResponseListener.onApiSuccess(response)
            }
        }
    }


}