package com.eazypaytech.paymentservicecore.repository.apiService


import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.eazypaytech.paymentservicecore.constants.AppConstants
import com.eazypaytech.paymentservicecore.listeners.requestListener.IApiServiceRequestListener
import com.eazypaytech.paymentservicecore.listeners.responseListener.IApiServiceResponseListener
import com.eazypaytech.paymentservicecore.model.PaymentServiceTxnDetails
import com.eazypaytech.paymentservicecore.model.emv.EmvServiceResult.TransStatus
import com.eazypaytech.paymentservicecore.model.error.ApiServiceError
import com.eazypaytech.paymentservicecore.models.PosConfig
import com.eazypaytech.paymentservicecore.models.TxnType
import com.eazypaytech.paymentservicecore.repository.apiService.access_token.AccessTokenRequestRepository
import com.eazypaytech.paymentservicecore.repository.apiService.auth_capture.AuthCaptureRequestRepository
import com.eazypaytech.paymentservicecore.repository.apiService.batch.BatchRequestRepository
import com.eazypaytech.paymentservicecore.repository.apiService.login.LoginRequestRepository
import com.eazypaytech.paymentservicecore.repository.apiService.preauth.PreAuthRequestRepository
import com.eazypaytech.paymentservicecore.repository.apiService.preauth.VoidRequestRepository
import com.eazypaytech.paymentservicecore.repository.apiService.purchase.PurchaseRequestRepository
import com.eazypaytech.paymentservicecore.repository.apiService.refund.RefundRequestRepository
import com.eazypaytech.paymentservicecore.repository.apiService.reversal.ReversalRequestRepository
import com.eazypaytech.paymentservicecore.repository.apiService.rkl.RklRequestRepository
import com.eazypaytech.paymentservicecore.utils.PaymentServiceUtils
import com.eazypaytech.securityframework.database.dbRepository.TxnDBRepository
import com.eazypaytech.securityframework.database.entity.TxnEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.io.encoding.ExperimentalEncodingApi

class ApiServiceRepository @Inject constructor(
    private val accessTokenRequestRepository: AccessTokenRequestRepository,
    private var refundRequestRepository: RefundRequestRepository,
    private val authCaptureRequestRepository: AuthCaptureRequestRepository,
    private var preAuthRequestRepository: PreAuthRequestRepository,
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

     @RequiresApi(Build.VERSION_CODES.O)
     override suspend fun apiServiceRequestOnlineAuth(
         paymentServiceTxnDetails: PaymentServiceTxnDetails?,
         iApiServiceResponseListener: IApiServiceResponseListener
     )
     {
         Log.d("Request_date","apiServiceRequestOnlineAuth")
         /* Delay to show processing screen in demo mode */
         if(paymentServiceTxnDetails?.isDemoMode == true)
             delay(AppConstants.DEMO_MODE_PROMPTS_DELAY_MS)

         /* Set Transaction Status as Initiated & Insert entry into DB. Update later on response */
         paymentServiceTxnDetails?.txnStatus = TransStatus.INITIATED.toString()
         PaymentServiceUtils.transformObject<TxnEntity>(paymentServiceTxnDetails)?.let {
             dbRepository.insertOrUpdateTxn(
                 it
             )
         }

        when(paymentServiceTxnDetails?.txnType.toString())
        {
            TxnType.PURCHASE.toString() -> apiServicePurchase(paymentServiceTxnDetails,iApiServiceResponseListener)
            TxnType.REFUND.toString() -> apiServiceRefund(paymentServiceTxnDetails,iApiServiceResponseListener)
            TxnType.PREAUTH.toString() -> apiServicePreAuth(paymentServiceTxnDetails,iApiServiceResponseListener)
            TxnType.VOID.toString() -> apiServiceVoid(paymentServiceTxnDetails,iApiServiceResponseListener)
            TxnType.AUTHCAP.toString() -> apiServiceAuthCapture(paymentServiceTxnDetails,iApiServiceResponseListener)
            else -> iApiServiceResponseListener.onApiServiceError(ApiServiceError(errorMessage = "Transaction Not Supported"))
        }
     }

    override suspend fun apiServiceRefund(
        paymentServiceTxnDetails: PaymentServiceTxnDetails?,
        iApiServiceResponseListener: IApiServiceResponseListener
    ) {
        Log.d("Request_date","apiServiceRefund")
        this.iApiServiceResponseListener = iApiServiceResponseListener
        refundRequestRepository.sendRefundRequest(paymentServiceTxnDetails){
            onApiServiceResponse(it)
        }
    }

     @RequiresApi(Build.VERSION_CODES.O)
     override suspend fun apiServicePreAuth(
         paymentServiceTxnDetails: PaymentServiceTxnDetails?,
         iApiServiceResponseListener: IApiServiceResponseListener
     ) {
         Log.d("Request_date","apiServiceRefund")
         this.iApiServiceResponseListener = iApiServiceResponseListener
         preAuthRequestRepository.sendPreAuthRequest(paymentServiceTxnDetails){
             onApiServiceResponse(it)
         }
     }

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun apiServiceVoid(
        paymentServiceTxnDetails: PaymentServiceTxnDetails?,
        iApiServiceResponseListener: IApiServiceResponseListener
    ) {
        this.iApiServiceResponseListener = iApiServiceResponseListener
        voidRequestRepository.sendVoidRequest(paymentServiceTxnDetails){
            onApiServiceResponse(it)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun apiServicePurchase(
        paymentServiceTxnDetails: PaymentServiceTxnDetails?,
        iApiServiceResponseListener: IApiServiceResponseListener
    ) {
        this.iApiServiceResponseListener = iApiServiceResponseListener
        purchaseRequestRepository.sendPurchaseRequest(paymentServiceTxnDetails){
            onApiServiceResponse(it)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun apiServiceAuthCapture(
        paymentServiceTxnDetails: PaymentServiceTxnDetails?,
        iApiServiceResponseListener: IApiServiceResponseListener
    ) {
        this.iApiServiceResponseListener = iApiServiceResponseListener
        authCaptureRequestRepository.sendAuthCapRequest(paymentServiceTxnDetails){
                onApiServiceResponse(it)
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
        this.iApiServiceResponseListener.onApiServiceDisplayProgress(true)
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
         this.iApiServiceResponseListener.onApiServiceDisplayProgress(true)
         Log.d("Conduent","apiServiceRklRequest")
         // Call the Echo Test request
         rklRequestRepository.apiRklRequest { response ->
             // Forward the response to listener
             //iApiServiceResponseListener.onApiServiceResponse(response)
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
        iApiServiceResponseListener.onApiServiceDisplayProgress(false)
        when (response) {
            is ApiServiceError -> {
                iApiServiceResponseListener.onApiServiceError(ApiServiceError(response.toString()))
            }
            else -> {
                try {
                    CoroutineScope(Dispatchers.IO).launch {
                        PaymentServiceUtils.transformObject<TxnEntity>(response)?.let {
                            dbRepository.updateTxn(
                                it
                            )
                        }
                    }
                    iApiServiceResponseListener.onApiServiceSuccess(
                        PaymentServiceUtils.transformObject<PaymentServiceTxnDetails>(
                            response
                        ) ?: PaymentServiceTxnDetails()
                    )
                }catch (e : Exception)
                {
                    e.printStackTrace()
                    iApiServiceResponseListener.onApiServiceError(ApiServiceError(e.message.toString()))
                }
            }
        }
    }
}