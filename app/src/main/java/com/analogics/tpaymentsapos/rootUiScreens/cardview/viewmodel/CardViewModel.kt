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
import com.analogics.paymentservicecore.models.toEmvTransType
import com.analogics.paymentservicecore.repository.apiService.ApiServiceRepository
import com.analogics.paymentservicecore.repository.emvService.EmvServiceRepository
import com.analogics.paymentservicecore.utils.PaymentServiceUtils
import com.analogics.securityframework.database.dbRepository.TxnDBRepository
import com.analogics.securityframework.database.entity.BatchEntity
import com.analogics.securityframework.database.entity.TxnEntity
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import com.analogics.tpaymentsapos.rootModel.ObjRootAppPaymentDetails
import com.analogics.tpaymentsapos.rootUiScreens.activity.SharedViewModel
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.BaseConstant
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.emvCardCheckStatusToMsgId
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.navigateAndClean
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.toDecimalFormat
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
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
        objRootAppPaymentDetails: ObjRootAppPaymentDetails,
        sharedViewModel: SharedViewModel,
        navHostController: NavHostController
    ) {
        viewModelScope.launch {
            emvServiceRepository.startPayment(
                context = context,
                transConfig = getTransConfig(objRootAppPaymentDetails),
                iEmvServiceResponseListener = object :
                IEmvServiceResponseListener {
                    @SuppressLint("DefaultLocale")
                    override fun onEmvServiceResponse(response: Any) {
                        when (response) {
                            is EmvServiceResult.TransResult -> {
                                if ((response.status == EmvServiceResult.TransStatus.APPROVED_ONLINE ||
                                            response.status == EmvServiceResult.TransStatus.APPROVED_OFFLINE)
                                ) {
                                    if (isBatchPresent.value.isEmpty()) {
                                        Log.d("Batch Id", "Initial Batch Id Inserted")
                                        sharedViewModel.batchEntity.batchId = "000001"
                                        sharedViewModel.objRootAppPaymentDetail.batchId =
                                            sharedViewModel.batchEntity.batchId
                                        sharedViewModel.batchEntity.batchStatus = "open"
                                        insertBatchData(sharedViewModel.batchEntity)
                                    } else {
                                        Log.d("Batch Id", "No Open Batch Found: $_lastBatch")
                                        if (_openBatch.value.isNullOrEmpty()) {
                                            val newBatchId = _lastBatch.value?.toIntOrNull()
                                                ?.let { String.format("%06d", it + 1) }
                                            Log.d("Batch Id", "Last batch ID Present: $_lastBatch")
                                            Log.d("Batch Id", "Generated new batch ID: $newBatchId")
                                            sharedViewModel.batchEntity.batchId = newBatchId
                                            sharedViewModel.objRootAppPaymentDetail.batchId =
                                                sharedViewModel.batchEntity.batchId
                                            sharedViewModel.batchEntity.batchStatus = "open"
                                            insertBatchData(sharedViewModel.batchEntity)
                                        } else {
                                            Log.d(
                                                "Batch Id",
                                                "Open Batch Id Found and set same Batch Id"
                                            )
                                            sharedViewModel.objRootAppPaymentDetail.batchId =
                                                _openBatch.value
                                        }
                                    }
                                    navigateToApprovalScreen(navHostController)
                                } else {
                                    if (isBatchPresent.value.isEmpty()) {
                                        Log.d("Batch Id", "Initial Batch Id Inserted")
                                        sharedViewModel.batchEntity.batchId = "000001"
                                        sharedViewModel.objRootAppPaymentDetail.batchId =
                                            sharedViewModel.batchEntity.batchId
                                        sharedViewModel.batchEntity.batchStatus = "open"
                                        insertBatchData(sharedViewModel.batchEntity)
                                    } else {
                                        Log.d("Batch Id", "No Open Batch Found: $_lastBatch")
                                        if (_openBatch.value.isNullOrEmpty()) {
                                            val newBatchId = _lastBatch.value?.toIntOrNull()
                                                ?.let { String.format("%06d", it + 1) }
                                            Log.d("Batch Id", "Last batch ID Present: $_lastBatch")
                                            Log.d("Batch Id", "Generated new batch ID: $newBatchId")
                                            sharedViewModel.batchEntity.batchId = newBatchId
                                            sharedViewModel.objRootAppPaymentDetail.batchId =
                                                sharedViewModel.batchEntity.batchId
                                            sharedViewModel.batchEntity.batchStatus = "open"
                                            insertBatchData(sharedViewModel.batchEntity)
                                        } else {
                                            Log.d(
                                                "Batch Id",
                                                "Open Batch Id Found and set same Batch Id"
                                            )
                                            sharedViewModel.objRootAppPaymentDetail.batchId =
                                                _openBatch.value
                                        }
                                    }
                                    navigateToDeclinedScreen(navHostController)
                                }
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

                    override fun onEmvServiceRequestOnline(
                        emvTags: HashMap<String, String>,
                        onResponse: (HashMap<String, String>) -> Unit
                    ) {
                        var responseEmvTags =
                            hashMapOf(EmvConstants.EMV_TAG_RESP_CODE to EmvConstants.EMV_TAG_VAL_UNABLE_TO_GO_ONLINE_DECLINE)  // Unable to go online, Decline

                        viewModelScope.launch {
                            displayInfoMsgId.value = EmvServiceResult.DisplayMsgId.PROCESSING_ONLINE
                            if(sharedViewModel.objPosConfig?.isDemoMode==true) {
                                delay(AppConstants.DEMO_MODE_PROMPTS_DELAY_MS)
                                onResponse(hashMapOf(EmvConstants.EMV_TAG_RESP_CODE to EmvConstants.EMV_TAG_VAL_APPROVED_ONLINE))
                            }
                            else {
                                apiServiceRepository.apiServiceRequestOnlineAuth(
                                PaymentServiceUtils.transformObject<PaymentServiceTxnDetails>(
                                    sharedViewModel.objRootAppPaymentDetail
                                ), object : IApiServiceResponseListener {

                                    override fun onApiServiceSuccess(paymentServiceTxnDetails: PaymentServiceTxnDetails) {
                                        paymentServiceTxnDetails.txnStatus
                                    }

                                    override fun onApiServiceError(apiServiceError: ApiServiceError) {
                                        onResponse(responseEmvTags)
                                    }
                                })
                                onResponse(responseEmvTags)
                            }
                        }
                    }
                })
        }
    }

    fun insertTxnData(objRootAppPaymentDetails: ObjRootAppPaymentDetails)=viewModelScope.launch{
        val json = Gson().toJson(objRootAppPaymentDetails) // Convert ObjRootAppPaymentDetails to JSON

        dbRepository.insertTxn(Gson().fromJson(json, TxnEntity::class.java))
        Log.d("password " +
                "record insert suc", Gson().fromJson(json, TxnEntity::class.java).toString())

    }

    /*@RequiresApi(Build.VERSION_CODES.O)
    fun fetchOpenBatches() {
        viewModelScope.launch {
            try {
                // Fetch the list of BatchEntity
                val batches: List<BatchEntity> = dbRepository.getOpenBatchId()

                // Map BatchEntity to String (using batchId for this example)
                val batchIds: List<String> = batches.mapNotNull { it.batchId } // Ensure no nulls if batchId can be null

                // Assign the transformed list to _openBatchId
                _openBatchId.value = batchIds
                Log.d("BatchViewModel", "Fetched Open Batches: $batchIds")
            } catch (e: Exception) {
                Log.e("BatchViewModel", "Error fetching open batches", e)
            }
        }
    }*/

    @RequiresApi(Build.VERSION_CODES.O)
    fun isBatchPresent() {
        viewModelScope.launch {
            try {
                // Fetch the list of BatchEntity
                val batches: List<String> = dbRepository.isBatchPresent()
                Log.d("Batch Id", "Fetched batches: $batches")
                // Assign the transformed list to _openBatchId
                _isBatchPresent.value = batches
            } catch (e: Exception) {
                Log.e("BatchViewModel", "Error fetching open batches", e)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun openBatchPresent() {
        viewModelScope.launch {
            try {
                // Fetch the open batch ID (or null if no open batch exists)
                val batchId: String? = dbRepository.openBatchId()
                Log.d("Batch Id", "Open batch ID: $batchId")

                // Update the value of _openBatch
                _openBatch.value = batchId
            } catch (e: Exception) {
                Log.e("BatchViewModel", "Error fetching open batch ID", e)
            }
        }
    }

    fun insertBatchData(batchEntity: BatchEntity)=viewModelScope.launch{
        val json = Gson().toJson(batchEntity) // Convert ObjRootAppPaymentDetails to JSON

        dbRepository.insertBatch(Gson().fromJson(json, batchEntity::class.java))
        Log.d("password " +
                "record insert suc", Gson().fromJson(json, batchEntity::class.java).toString())

    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getLastBatchId() {
        viewModelScope.launch {
            try {
                // Fetch the list of BatchEntity
                val lastBatch: String? = dbRepository.getLastBatch()
                Log.d("Batch Id", "Fetched batches: $lastBatch")
                // Assign the transformed list to _openBatchId
                _lastBatch.value = lastBatch
            } catch (e: Exception) {
                Log.e("BatchViewModel", "Error fetching open batches", e)
            }
        }
    }

}