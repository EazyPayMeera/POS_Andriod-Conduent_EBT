package com.analogics.paymentservicecore.repository.paymentService


import android.content.Context
import com.analogics.builder_core.model.PaymentServiceTxnDetails
import com.analogics.paymentservicecore.listeners.requestListener.IPaymentServiceRequestListener
import com.analogics.paymentservicecore.listeners.rootListener.IOnRootAppPaymentListener
import com.analogics.paymentservicecore.model.error.PaymentServiceError
import com.analogics.paymentservicecore.models.PosConfig
import com.analogics.paymentservicecore.models.TxnType
import com.analogics.paymentservicecore.repository.paymentService.auth_capture.AuthCaptureRequestRepository
import com.analogics.paymentservicecore.repository.paymentService.login.LoginRequestRepository
import com.analogics.paymentservicecore.repository.paymentService.purchase.PurchaseRequestRepository
import com.analogics.paymentservicecore.repository.paymentService.refund.RefundRequestRepository
import com.analogics.paymentservicecore.repository.paymentService.reversal.ReversalRequestRepository
import com.analogics.tpaymentcore.handler.PaymentConfigurationHandler
import com.analogics.tpaymentcore.listener.IPaymentSDKListener
import javax.inject.Inject

class PaymentServiceRepository @Inject constructor(
    private var refundRequestRepository: RefundRequestRepository,
    private val authCaptureRequestRepository: AuthCaptureRequestRepository,
    private val loginRequestRepository: LoginRequestRepository,
    private val reversalRequestRepository: ReversalRequestRepository,
    private val voidRequestRepository: VoidRequestRepository,
    private val purchaseRequestRepository: PurchaseRequestRepository
) :
    IPaymentServiceRequestListener,
    IPaymentSDKListener {
    lateinit var iOnRootAppPaymentListener: IOnRootAppPaymentListener
    lateinit var context: Context

    override fun onTPaymentSDKInit(uiData: String) {
        /* Just for testing comparing with uiData value */
        if (uiData == "SUCCESS") {
            PosConfig.apply { isPaymentSDKInit = true }.saveToPrefs(context)
            iOnRootAppPaymentListener.onPaymentSuccess(true)
        } else {
            PosConfig.apply { isPaymentSDKInit = false }.saveToPrefs(context)
            iOnRootAppPaymentListener.onPaymentError(PaymentServiceError("Error"))
        }
    }

    override fun onTPaymentSDKHandler(uiData: String) {
        /* Just for testing comparing with uiData value */

        if (uiData == "SUCCESS")
            iOnRootAppPaymentListener.onPaymentSuccess(true)
        else
            iOnRootAppPaymentListener.onPaymentError(PaymentServiceError("Error"))
    }


    override fun initPaymentSDK(
        context: Context,
        iOnRootAppPaymentListener: IOnRootAppPaymentListener
    ) {
        this.iOnRootAppPaymentListener = iOnRootAppPaymentListener
        this.context = context
        if (PosConfig.isPaymentSDKInit != true)
            PaymentConfigurationHandler.initPaymentSDK(context, this)
    }

    override fun startPayment(
        context: Context,
        iOnRootAppPaymentListener: IOnRootAppPaymentListener
    ) {
        this.iOnRootAppPaymentListener = iOnRootAppPaymentListener
        PaymentConfigurationHandler.startPayment(context, this)
    }

    override fun getPosConfig(): PosConfig {
        return PosConfig
    }

    override suspend fun apiServiceRefund(
        paymentServiceTxnDetails: PaymentServiceTxnDetails?,
        iOnRootAppPaymentListener: IOnRootAppPaymentListener
    ) {
        refundRequestRepository.sendRefundRequest(paymentServiceTxnDetails)
    }

    override suspend fun apiServiceVoid(
        paymentServiceTxnDetails: PaymentServiceTxnDetails?,
        iOnRootAppPaymentListener: IOnRootAppPaymentListener
    ) {
        voidRequestRepository.sendVoidRequest(paymentServiceTxnDetails)
    }

    override suspend fun apiServicePurchase(
        paymentServiceTxnDetails: PaymentServiceTxnDetails?,
        iOnRootAppPaymentListener: IOnRootAppPaymentListener
    ) {
        purchaseRequestRepository.sendPurchaseRequest(paymentServiceTxnDetails)
    }

    override suspend fun apiServiceAuthCapture(
        paymentServiceTxnDetails: PaymentServiceTxnDetails?,
        iOnRootAppPaymentListener: IOnRootAppPaymentListener
    ) {
        if(TxnType.PREAUTH==TxnType.PREAUTH) {
            authCaptureRequestRepository.sendPreAuthRequest(paymentServiceTxnDetails)
        }else {
            authCaptureRequestRepository.sendAuthCaptureRequest(paymentServiceTxnDetails)
        }
    }

    override suspend fun apiServiceReversal(
        paymentServiceTxnDetails: PaymentServiceTxnDetails?,
        iOnRootAppPaymentListener: IOnRootAppPaymentListener
    ) {
        reversalRequestRepository.sendReversal(paymentServiceTxnDetails)
    }

    override suspend fun apiServiceLogin(
        paymentServiceTxnDetails: PaymentServiceTxnDetails?,
        iOnRootAppPaymentListener: IOnRootAppPaymentListener
    ) {
        loginRequestRepository.apiDeviceLogin(paymentServiceTxnDetails)
    }

    override fun onAPIServiceResponse(response: Any) {
        when (response) {
            is PaymentServiceError -> {
                iOnRootAppPaymentListener.onPaymentError(PaymentServiceError(response.toString()))
            }
            else ->
            {
                iOnRootAppPaymentListener.onPaymentSuccess(response)
            }
        }
    }
}