package com.analogics.paymentservicecore.domain.repository.apiService


    import android.os.Build
    import android.util.Log
    import androidx.annotation.RequiresApi
    import com.eazypaytech.paymentservicecore.constants.AppConstants
    import com.analogics.paymentservicecore.data.listeners.requestListener.IApiServiceRequestListener
    import com.analogics.paymentservicecore.data.listeners.responseListener.IApiServiceResponseListener
    import com.analogics.paymentservicecore.data.model.PaymentServiceTxnDetails
    import com.analogics.paymentservicecore.data.model.emv.EmvServiceResult.TransStatus
    import com.analogics.paymentservicecore.data.model.error.ApiServiceError
    import com.analogics.paymentservicecore.data.model.error.ApiServiceTimeout
    import com.analogics.paymentservicecore.data.model.PosConfig
    import com.analogics.paymentservicecore.data.model.TxnType
    //import com.eazypaytech.paymentservicecore.repository.apiService.access_token.AccessTokenRequestRepository
    //import com.eazypaytech.paymentservicecore.repository.apiService.auth_capture.AuthCaptureRequestRepository
    //import com.eazypaytech.paymentservicecore.repository.apiService.batch.BatchRequestRepository
    import com.analogics.paymentservicecore.domain.repository.apiService.login.LoginRequestRepository
    //import com.eazypaytech.paymentservicecore.repository.apiService.preauth.PreAuthRequestRepository
    import com.analogics.paymentservicecore.domain.repository.apiService.voidreq.VoidRequestRepository
    import com.analogics.paymentservicecore.domain.repository.apiService.purchase.PurchaseRequestRepository
    import com.analogics.paymentservicecore.domain.repository.apiService.voucherSettlement.VoucherSettlementRequestRepository
    //import com.eazypaytech.paymentservicecore.repository.apiService.refund.RefundRequestRepository
    import com.analogics.paymentservicecore.domain.repository.apiService.reversal.ReversalRequestRepository
    import com.analogics.paymentservicecore.domain.repository.apiService.rkl.RklRequestRepository
    import com.analogics.paymentservicecore.utils.PaymentServiceUtils
    import com.analogics.securityframework.data.repository.TxnDBRepository
    import com.analogics.securityframework.database.entity.TxnEntity
    import kotlinx.coroutines.CoroutineScope
    import kotlinx.coroutines.Dispatchers
    import kotlinx.coroutines.delay
    import kotlinx.coroutines.launch
    import javax.inject.Inject

    class ApiServiceRepository @Inject constructor(
        //private val accessTokenRequestRepository: AccessTokenRequestRepository,
        private val voucherSettlementRequestRepository: VoucherSettlementRequestRepository,
        private val loginRequestRepository: LoginRequestRepository,
        private val reversalRequestRepository: ReversalRequestRepository,
        private val voidRequestRepository: VoidRequestRepository,
        private val purchaseRequestRepository: PurchaseRequestRepository,
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
                TxnType.FOOD_PURCHASE.toString() -> apiServicePurchase(paymentServiceTxnDetails,iApiServiceResponseListener)
                TxnType.CASH_WITHDRAWAL.toString() -> apiServicePurchase(paymentServiceTxnDetails,iApiServiceResponseListener)
                TxnType.CASH_PURCHASE.toString() -> apiServicePurchase(paymentServiceTxnDetails,iApiServiceResponseListener)
                TxnType.FOODSTAMP_RETURN.toString() -> apiServicePurchase(paymentServiceTxnDetails,iApiServiceResponseListener)
                TxnType.PURCHASE_CASHBACK.toString() -> apiServicePurchase(paymentServiceTxnDetails,iApiServiceResponseListener)
                TxnType.E_VOUCHER.toString() -> voucherSettlement(paymentServiceTxnDetails,iApiServiceResponseListener)
                TxnType.BALANCE_ENQUIRY_CASH.toString(),
                TxnType.BALANCE_ENQUIRY_SNAP.toString() ->
                    apiServicePurchase(paymentServiceTxnDetails, iApiServiceResponseListener)
                TxnType.VOID_LAST.toString() -> apiServiceVoid(paymentServiceTxnDetails,iApiServiceResponseListener)
                else -> iApiServiceResponseListener.onApiServiceError(ApiServiceError(errorMessage = "Transaction Not Supported"))
            }
         }

        override suspend fun apiServiceRefund(
            paymentServiceTxnDetails: PaymentServiceTxnDetails?,
            iApiServiceResponseListener: IApiServiceResponseListener
        ) {
            Log.d("Request_date","apiServiceRefund")
            this.iApiServiceResponseListener = iApiServiceResponseListener
    //        refundRequestRepository.sendRefundRequest(paymentServiceTxnDetails){
    //            onApiServiceResponse(it)
    //        }
        }

         @RequiresApi(Build.VERSION_CODES.O)
         override suspend fun apiServicePreAuth(
             paymentServiceTxnDetails: PaymentServiceTxnDetails?,
             iApiServiceResponseListener: IApiServiceResponseListener
         ) {
             Log.d("Request_date","apiServiceRefund")
             this.iApiServiceResponseListener = iApiServiceResponseListener
    //         preAuthRequestRepository.sendPreAuthRequest(paymentServiceTxnDetails){
    //             onApiServiceResponse(it)
    //         }
         }

        @RequiresApi(Build.VERSION_CODES.O)
        override suspend fun apiServiceVoid(
            paymentServiceTxnDetails: PaymentServiceTxnDetails?,
            iApiServiceResponseListener: IApiServiceResponseListener
        ) {
            this.iApiServiceResponseListener = iApiServiceResponseListener
            this.iApiServiceResponseListener.onApiServiceDisplayProgress(true)
            voidRequestRepository.voidRequest(paymentServiceTxnDetails){
                onApiServiceResponse(it)
            }
        }

         @RequiresApi(Build.VERSION_CODES.O)
         override suspend fun apiServicePurchase(
             paymentServiceTxnDetails: PaymentServiceTxnDetails?,
             iApiServiceResponseListener: IApiServiceResponseListener
         ) {
             this.iApiServiceResponseListener = iApiServiceResponseListener
             this.iApiServiceResponseListener.onApiServiceDisplayProgress(true)
             purchaseRequestRepository.purchaseRequest(paymentServiceTxnDetails){
                 onApiServiceResponse(it)
             }
         }

         @RequiresApi(Build.VERSION_CODES.O)
         override suspend fun voucherSettlement(
             paymentServiceTxnDetails: PaymentServiceTxnDetails?,
             iApiServiceResponseListener: IApiServiceResponseListener
         ) {
             this.iApiServiceResponseListener = iApiServiceResponseListener
             this.iApiServiceResponseListener.onApiServiceDisplayProgress(true)
             voucherSettlementRequestRepository.voucherSettlementRequest(paymentServiceTxnDetails){
                 onApiServiceResponse(it)
             }
         }



        @RequiresApi(Build.VERSION_CODES.O)
        override suspend fun apiServiceAuthCapture(
            paymentServiceTxnDetails: PaymentServiceTxnDetails?,
            iApiServiceResponseListener: IApiServiceResponseListener
        ) {
            this.iApiServiceResponseListener = iApiServiceResponseListener
    //        authCaptureRequestRepository.sendAuthCapRequest(paymentServiceTxnDetails){
    //                onApiServiceResponse(it)
    //        }
        }

        @RequiresApi(Build.VERSION_CODES.O)
        override suspend fun apiServiceReversal(
            paymentServiceTxnDetails: PaymentServiceTxnDetails?,
            iApiServiceResponseListener: IApiServiceResponseListener
        ) {
            this.iApiServiceResponseListener = iApiServiceResponseListener
            this.iApiServiceResponseListener.onApiServiceDisplayProgress(true)
            reversalRequestRepository.sendReversal(paymentServiceTxnDetails){
                onApiServiceResponse(it)
            }
        }

        override suspend fun apiServiceLogin(
            paymentServiceTxnDetails: PaymentServiceTxnDetails?,
            iApiServiceResponseListener: IApiServiceResponseListener
        ) {
            this.iApiServiceResponseListener = iApiServiceResponseListener
            this.iApiServiceResponseListener.onApiServiceDisplayProgress(true)
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
            /*accessTokenRequestRepository.apiGetAccessToken(paymentServiceTxnDetails){
                onApiServiceResponse(it)
            }*/
        }


         override suspend fun signOnRequest(
             paymentServiceTxnDetails: PaymentServiceTxnDetails?,
             iApiServiceResponseListener: IApiServiceResponseListener
         ) {
             this.iApiServiceResponseListener = iApiServiceResponseListener
             this.iApiServiceResponseListener.onApiServiceDisplayProgress(true)
             rklRequestRepository.signOnRequest(paymentServiceTxnDetails){
                 onApiServiceResponse(it)
             }

         }

         override suspend fun signOnOff(
             paymentServiceTxnDetails: PaymentServiceTxnDetails?,
             iApiServiceResponseListener: IApiServiceResponseListener
         ) {
             this.iApiServiceResponseListener = iApiServiceResponseListener
             this.iApiServiceResponseListener.onApiServiceDisplayProgress(true)
             rklRequestRepository.signOff(paymentServiceTxnDetails){
                 onApiServiceResponse(it)
             }

         }

         override suspend fun handShakeRequest(
             paymentServiceTxnDetails: PaymentServiceTxnDetails?,
             iApiServiceResponseListener: IApiServiceResponseListener
         ) {
             this.iApiServiceResponseListener = iApiServiceResponseListener

             rklRequestRepository.handShakeRequest(paymentServiceTxnDetails) { result ->

                 CoroutineScope(Dispatchers.IO).launch {
                     onApiServiceResponse(result)  // ✅ now safe
                 }
             }
         }


         override suspend fun keyExchange(
            paymentServiceTxnDetails: PaymentServiceTxnDetails?,
            iApiServiceResponseListener: IApiServiceResponseListener
        ) {
            this.iApiServiceResponseListener = iApiServiceResponseListener
             this.iApiServiceResponseListener.onApiServiceDisplayProgress(true)
             rklRequestRepository.keyExchangeRequest(paymentServiceTxnDetails){
                onApiServiceResponse(it)
            }
        }

         override suspend fun keyChange(
             paymentServiceTxnDetails: PaymentServiceTxnDetails?,
             iApiServiceResponseListener: IApiServiceResponseListener
         ) {
             this.iApiServiceResponseListener = iApiServiceResponseListener
             this.iApiServiceResponseListener.onApiServiceDisplayProgress(true)
             rklRequestRepository.keyChangeRequest(paymentServiceTxnDetails) {
                 onApiServiceResponse(it)
             }
         }

         override fun onApiServiceResponse(response: Any) {

             iApiServiceResponseListener.onApiServiceDisplayProgress(false)

             when (response) {

                 is ApiServiceTimeout -> {
                     iApiServiceResponseListener.onApiServiceTimeout(response)
                 }

                 is ApiServiceError -> {
                     iApiServiceResponseListener.onApiServiceError(
                         ApiServiceError(response.errorMessage)   // ✅ fix this too
                     )
                 }

                 is PaymentServiceTxnDetails -> {
                     try {
                         CoroutineScope(Dispatchers.IO).launch {
                             PaymentServiceUtils.transformObject<TxnEntity>(response)?.let {
                                 dbRepository.updateTxn(it)
                             }
                         }

                         iApiServiceResponseListener.onApiServiceSuccess(response)

                     } catch (e: Exception) {
                         e.printStackTrace()
                         iApiServiceResponseListener.onApiServiceError(
                             ApiServiceError(e.message.toString())
                         )
                     }
                 }

                 else -> {
                     iApiServiceResponseListener.onApiServiceError(
                         ApiServiceError("Unknown response type")
                     )
                 }
             }
         }
    }