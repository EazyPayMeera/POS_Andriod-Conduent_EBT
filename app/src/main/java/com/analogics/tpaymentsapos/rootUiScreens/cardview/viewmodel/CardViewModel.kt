package com.analogics.tpaymentsapos.rootUiScreens.cardview.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.analogics.paymentservicecore.constants.AppConstants
import com.analogics.paymentservicecore.constants.EmvConstants
import com.analogics.paymentservicecore.listeners.responseListener.IApiServiceResponseListener
import com.analogics.paymentservicecore.listeners.responseListener.IEmvServiceResponseListener
import com.analogics.paymentservicecore.model.PaymentServiceTxnDetails
import com.analogics.paymentservicecore.model.emv.CardCheckMode
import com.analogics.paymentservicecore.model.emv.EmvServiceResult
import com.analogics.paymentservicecore.model.emv.TransConfig
import com.analogics.paymentservicecore.model.error.ApiServiceError
import com.analogics.paymentservicecore.models.TxnStatus
import com.analogics.paymentservicecore.models.toEmvTransType
import com.analogics.paymentservicecore.repository.apiService.ApiServiceRepository
import com.analogics.paymentservicecore.repository.emvService.EmvServiceRepository
import com.analogics.paymentservicecore.utils.PaymentServiceUtils
import com.analogics.securityframework.database.dbRepository.TxnDBRepository
import com.analogics.securityframework.database.entity.TxnEntity
import com.analogics.tpaymentcore.utils.TlvUtils
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import com.analogics.tpaymentsapos.rootModel.ObjRootAppPaymentDetails
import com.analogics.tpaymentsapos.rootUiScreens.activity.SharedViewModel
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.emvCardCheckStatusToMsgId
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.emvStatusToTransStatus
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.getCurrentDateTime
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.navigateAndClean
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.toDecimalFormat
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CardViewModel @Inject constructor(private  var emvServiceRepository: EmvServiceRepository, var dbRepository: TxnDBRepository, var apiServiceRepository: ApiServiceRepository) : ViewModel() {

    var emvInProgress = mutableStateOf(false)
    var showProgressVar = mutableStateOf(true)
    var displayInfoMsgId = mutableStateOf(EmvServiceResult.DisplayMsgId.NONE)

    private val _isBatchPresent = MutableStateFlow<List<String>>(emptyList())
    val isBatchPresent: StateFlow<List<String>> = _isBatchPresent

    private val _openBatch = MutableStateFlow<String?>(null)
    val openBatch: StateFlow<String?> = _openBatch

    private val _lastBatch = MutableStateFlow<String?>(null)
    val lastBatch: StateFlow<String?> = _lastBatch


    fun navigateToApprovalScreen(navHostController: NavHostController) {
        viewModelScope.launch {
            navHostController.navigate(AppNavigationItems.ApprovedScreen.route) // Navigate to the desired screen
        }
    }

    fun navigateToDeclinedScreen(navHostController: NavHostController) {
        viewModelScope.launch {
            navHostController.navigate(AppNavigationItems.DeclineScreen.route) // Navigate to the desired screen
        }
    }

    fun onUpiClick(navHostController: NavHostController) {
        navHostController.navigate(AppNavigationItems. BarcodeScreen.route)
        viewModelScope.launch {
            emvServiceRepository.abortPayment()
        }
    }

    fun onCancelClick(navHostController: NavHostController) {
        navHostController.navigateAndClean(AppNavigationItems.DashBoardScreen.route)
        viewModelScope.launch {
            emvServiceRepository.abortPayment()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun updateTransResult(objRootAppPaymentDetails: ObjRootAppPaymentDetails)
    {
        viewModelScope.launch {
            dbRepository.insertOrUpdateTxn(PaymentServiceUtils.transformObject<TxnEntity>(objRootAppPaymentDetails))
        }
    }

    fun getTransConfig(objRootAppPaymentDetails: ObjRootAppPaymentDetails) : TransConfig
    {
        return TransConfig(
            amount = objRootAppPaymentDetails.ttlAmount.toDecimalFormat(),
            cashbackAmount = objRootAppPaymentDetails.cashback.toDecimalFormat(),
            currencyCode = objRootAppPaymentDetails.txnCurrencyCode?: AppConstants.DEFAULT_CURRENCY_CODE,
            transactionType = objRootAppPaymentDetails.txnType?.toEmvTransType().toString(),
            cardCheckMode = CardCheckMode.SWIPE_OR_INSERT_OR_TAP,
            cardCheckTimeout = AppConstants.CARD_CHECK_TIMEOUT_S.toString(),
            supportDRL = false
        )
    }

    fun startPayment(
        context: Context,
        sharedViewModel: SharedViewModel,
        navHostController: NavHostController
    ) {
        Log.d("Start Payment","Inside DashBoard ViewModel")
        viewModelScope.launch {
            sharedViewModel.objRootAppPaymentDetail.dateTime = getCurrentDateTime()
            emvServiceRepository.startPayment(
                context = context,
                transConfig = getTransConfig(sharedViewModel.objRootAppPaymentDetail),
                iEmvServiceResponseListener = object :
                    IEmvServiceResponseListener {
                    @RequiresApi(Build.VERSION_CODES.O)
                    @SuppressLint("DefaultLocale")
                    override fun onEmvServiceResponse(response: Any) {
                        when (response) {
                            is EmvServiceResult.TransResult -> {
                                Log.d("EMVServiceResponse", "Transaction Status: ${response.status}")
                                /* Update Transaction Result & Store in DB */
                                sharedViewModel.objRootAppPaymentDetail.txnStatus = emvStatusToTransStatus(response.status)
                                updateTransResult(sharedViewModel.objRootAppPaymentDetail)
                                navigateToApprovalScreen(navHostController)
                                /*if ((response.status == EmvServiceResult.TransStatus.APPROVED_ONLINE ||
                                            response.status == EmvServiceResult.TransStatus.APPROVED_OFFLINE)
                                ) {
                                    navigateToApprovalScreen(navHostController)
                                } else {
                                    navigateToDeclinedScreen(navHostController)
                                }*/
                            }

                            is EmvServiceResult.CardCheckResult -> {
                                when (response.status) {
                                    EmvServiceResult.CardCheckStatus.CARD_INSERTED,
                                    EmvServiceResult.CardCheckStatus.CARD_SWIPED,
                                    EmvServiceResult.CardCheckStatus.CARD_TAPPED -> {
                                        emvInProgress.value = true
                                        showProgressVar.value = true
                                        displayInfoMsgId.value =
                                            emvCardCheckStatusToMsgId(response.status as EmvServiceResult.CardCheckStatus)
                                    }

                                    else -> {
                                        emvInProgress.value = false
                                        showProgressVar.value = false
                                        displayInfoMsgId.value = EmvServiceResult.DisplayMsgId.NONE
                                    }
                                }
                            }
                        }
                    }

                    override fun onEmvServiceDisplayMessage(
                        displayMsgId: EmvServiceResult.DisplayMsgId
                    ) {

                    }

                    @RequiresApi(Build.VERSION_CODES.O)
                    override fun onEmvServiceRequestOnline(
                        emvTags: HashMap<String, String>,
                        onResponse: (HashMap<String, String>) -> Unit
                    ) {
                        var responseEmvTags =
                            hashMapOf(EmvConstants.EMV_TAG_RESP_CODE to EmvConstants.EMV_TAG_VAL_UNABLE_TO_GO_ONLINE_DECLINE)  // Unable to go online, Decline

                        val tlvData = TlvUtils(emvTags)
                        if (tlvData.tlvMap.containsKey(EmvConstants.EMV_TAG_ENC_TRACK)) {
                            sharedViewModel.objRootAppPaymentDetail.trackData =
                                tlvData.tlvMap[EmvConstants.EMV_TAG_ENC_TRACK]
                            tlvData.tlvMap.remove(EmvConstants.EMV_TAG_ENC_TRACK)
                        }
                        if (tlvData.tlvMap.containsKey(EmvConstants.EMV_TAG_ENC_KSN)) {
                            sharedViewModel.objRootAppPaymentDetail.ksn =
                                tlvData.tlvMap[EmvConstants.EMV_TAG_ENC_KSN]
                            tlvData.tlvMap.remove(EmvConstants.EMV_TAG_ENC_KSN)
                        }
                        if (tlvData.tlvMap.containsKey(EmvConstants.EMV_TAG_ENC_PIN_BLOCK)) {
                            sharedViewModel.objRootAppPaymentDetail.pinBlock =
                                tlvData.tlvMap[EmvConstants.EMV_TAG_ENC_PIN_BLOCK]
                            tlvData.tlvMap.remove(EmvConstants.EMV_TAG_ENC_PIN_BLOCK)
                        }
                        if (tlvData.tlvMap.containsKey(EmvConstants.EMV_TAG_PAN)) {
                            tlvData.tlvMap.remove(EmvConstants.EMV_TAG_PAN)
                        }

                        sharedViewModel.objRootAppPaymentDetail.emvData = tlvData.toTlvString()

                        viewModelScope.launch {
                            displayInfoMsgId.value = EmvServiceResult.DisplayMsgId.PROCESSING_ONLINE
                            apiServiceRepository.apiServiceRequestOnlineAuth(
                                PaymentServiceUtils.transformObject<PaymentServiceTxnDetails>(
                                    sharedViewModel.objRootAppPaymentDetail
                                ), object : IApiServiceResponseListener {

                                    override fun onApiServiceSuccess(paymentServiceTxnDetails: PaymentServiceTxnDetails) {
                                        responseEmvTags =
                                            TlvUtils(paymentServiceTxnDetails.emvData).tlvMap
                                        onResponse(responseEmvTags)
                                    }

                                    override fun onApiServiceError(apiServiceError: ApiServiceError) {
                                        onResponse(responseEmvTags)
                                    }
                                })
                        }
                    }
                })
        }
    }
}