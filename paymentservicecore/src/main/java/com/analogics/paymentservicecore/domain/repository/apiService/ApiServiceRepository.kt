package com.analogics.paymentservicecore.domain.repository.apiService


    import android.os.Build
    import androidx.annotation.RequiresApi
    import com.analogics.paymentservicecore.data.listeners.requestListener.IApiServiceRequestListener
    import com.analogics.paymentservicecore.data.listeners.responseListener.IApiServiceResponseListener
    import com.analogics.paymentservicecore.data.model.PaymentServiceTxnDetails
    import com.analogics.paymentservicecore.data.model.emv.EmvServiceResult.TransStatus
    import com.analogics.paymentservicecore.data.model.error.ApiServiceError
    import com.analogics.paymentservicecore.data.model.error.ApiServiceTimeout
    import com.analogics.paymentservicecore.data.model.PosConfig
    import com.analogics.paymentservicecore.data.model.TxnType
    import com.analogics.paymentservicecore.domain.repository.apiService.purchaseRequest.PurchaseRequestRepository
    import com.analogics.paymentservicecore.domain.repository.apiService.voucherSettlementRequest.VoucherSettlementRequestRepository
    import com.analogics.paymentservicecore.domain.repository.apiService.reversalRequest.ReversalRequestRepository
    import com.analogics.paymentservicecore.domain.repository.apiService.signOnRequest.SignOnRequestRepository
    import com.analogics.paymentservicecore.domain.repository.apiService.voidRequest.VoidRequestRepository
    import com.analogics.paymentservicecore.utils.PaymentServiceUtils
    import com.analogics.securityframework.data.repository.TxnDBRepository
    import com.analogics.securityframework.database.entity.TxnEntity
    import kotlinx.coroutines.CoroutineScope
    import kotlinx.coroutines.Dispatchers
    import kotlinx.coroutines.launch
    import javax.inject.Inject

    class ApiServiceRepository @Inject constructor(
        private val voucherSettlementRequestRepository: VoucherSettlementRequestRepository,
        private val reversalRequestRepository: ReversalRequestRepository,
        private val voidRequestRepository: VoidRequestRepository,
        private val purchaseRequestRepository: PurchaseRequestRepository,
        private val rklRequestRepository: SignOnRequestRepository,
        private val dbRepository: TxnDBRepository,
        private val posConfig: PosConfig
    ) : IApiServiceRequestListener
     {
        lateinit var iApiServiceResponseListener: IApiServiceResponseListener
         /**
          * Returns POS config from persisted storage
          */
        override fun getPosConfig(): PosConfig {
            return posConfig.loadFromPrefs()
        }

         /**
          * Entry point for online financial authorization request.
          *
          * Flow:
          * 1. Mark txn as INITIATED
          * 2. Save to DB
          * 3. Route based on TxnType
          */
         @RequiresApi(Build.VERSION_CODES.O)
         override suspend fun apiServiceRequestOnlineAuth(
             paymentServiceTxnDetails: PaymentServiceTxnDetails?,
             iApiServiceResponseListener: IApiServiceResponseListener
         )
         {
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

         /**
          * VOID transaction
          */
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

         /**
          * PURCHASE transaction
          */
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

         /**
          * VOUCHER settlement transaction
          */
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

         /**
          * REVERSAL transaction
          */
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

         /**
          * SIGN ON request
          */
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

         /**
          * SIGN OFF request
          */
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

         /**
          * HANDSHAKE request
          */
         override suspend fun handShakeRequest(
             paymentServiceTxnDetails: PaymentServiceTxnDetails?,
             iApiServiceResponseListener: IApiServiceResponseListener
         ) {
             this.iApiServiceResponseListener = iApiServiceResponseListener

             rklRequestRepository.handShakeRequest(paymentServiceTxnDetails) { result ->

                 CoroutineScope(Dispatchers.IO).launch {
                     onApiServiceResponse(result)
                 }
             }
         }

         /**
          * KEY EXCHANGE
          */
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

         /**
          * KEY CHANGE
          */
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

         /**
          * CENTRAL RESPONSE HANDLER
          *
          * Responsibilities:
          * - Hide progress
          * - Route success/error/timeout
          * - Update DB on success
          */
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