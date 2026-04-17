package com.eazypaytech.posafrica.features.manualentry.ui

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.analogics.builder_core.data.constants.BuilderConstants

import com.eazypaytech.paymentservicecore.constants.AppConstants
import com.analogics.paymentservicecore.data.listeners.responseListener.IApiServiceResponseListener
import com.analogics.paymentservicecore.data.model.EBTBalance
import com.analogics.paymentservicecore.data.model.PaymentServiceTxnDetails
import com.analogics.paymentservicecore.data.model.emv.CardEntryMode
import com.analogics.paymentservicecore.data.model.error.ApiServiceError
import com.analogics.paymentservicecore.data.model.error.ApiServiceTimeout
import com.analogics.paymentservicecore.data.model.TxnStatus
import com.analogics.paymentservicecore.data.model.TxnType
import com.analogics.paymentservicecore.domain.repository.apiService.ApiServiceRepository
import com.analogics.paymentservicecore.domain.repository.emvService.EmvServiceRepository
import com.analogics.paymentservicecore.utils.PaymentServiceUtils
import com.eazypaytech.posafrica.R
import com.eazypaytech.posafrica.core.navigation.routes.AppNavigationItems
import com.eazypaytech.posafrica.features.activity.ui.SharedViewModel
import com.eazypaytech.posafrica.features.dialogs.ui.CustomDialogBuilder
import com.eazypaytech.posafrica.core.utils.navigateAndClean
import com.eazypaytech.posafrica.core.utils.miscellaneous.NetworkUtils
import com.analogics.securityframework.data.repository.TxnDBRepository
import com.eazypaytech.posafrica.core.utils.emvStatusToTransStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ManualCardViewModel @Inject constructor(
    private  var apiServiceRepository: ApiServiceRepository,
    private val emvServiceRepository: EmvServiceRepository,
    private val dbRepository: TxnDBRepository
) : ViewModel() {
    lateinit var context: Context
    lateinit var sharedViewModel: SharedViewModel
    lateinit var navHostController : NavHostController

    var cardNumber by mutableStateOf("")
        private set
    private val _cardExists = MutableStateFlow<Boolean?>(null)
    val cardExists: StateFlow<Boolean?> = _cardExists

    val isFormValid: Boolean
        get() = cardNumber.isNotBlank() &&
                cardNumber.length == AppConstants.MAX_LENGTH_CARD_NO

    fun onCardNoChange(newValue: String) {
        cardNumber = newValue
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun onConfirm(
        navHostController: NavHostController,
        sharedViewModel: SharedViewModel
    ) {
        sharedViewModel.objRootAppPaymentDetail.cardMaskedPan = cardNumber
        sharedViewModel.objRootAppPaymentDetail.cardEntryMode =
            CardEntryMode.MANUAL
        when {
            cardNumber.isEmpty() -> {
                showError(
                    navHostController,
                    "Please Enter Card Details"
                )
            }

            sharedViewModel.objRootAppPaymentDetail.txnType == TxnType.E_VOUCHER -> {
                navHostController.navigate(AppNavigationItems.VoucherScreen.route)
            }

            else -> {
                generatePinAndProceed(navHostController,sharedViewModel)
            }
        }
    }

    fun onCancel(navHostController: NavHostController) {
        navHostController.navigateAndClean(AppNavigationItems.DashBoardScreen.route)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun generatePinAndProceed(
        navHostController: NavHostController,
        sharedViewModel: SharedViewModel
    ) {
        sharedViewModel.objRootAppPaymentDetail.ttlAmount.toString().let {
            emvServiceRepository.pinGeneration(
                pan = cardNumber,
                amount = it
            ) { pinBlock ->

                if (pinBlock != null) {
                    val pinBlockHex = pinBlock.toHexString().lowercase()
                    sharedViewModel.objRootAppPaymentDetail.pinBlock = pinBlockHex
                    Log.d("TXN_TYPE", sharedViewModel.objRootAppPaymentDetail.txnType.toString())
                    if(sharedViewModel.objRootAppPaymentDetail.txnType == TxnType.CASH_WITHDRAWAL)
                    {
                        viewModelScope.launch(Dispatchers.Main) {
                            navHostController.navigate(AppNavigationItems.LoginScreen.route)
                        }

                    }
                    else {
                        authenticateTransaction(sharedViewModel, navHostController)
                    }

                } else {
                    Log.e("PIN_FLOW", "PIN entry cancelled or failed")
                }
            }
        }
    }


    fun isCardExists(context: Context): Boolean {
        return emvServiceRepository.isCardExists(context)
    }


    fun checkNetwork(context: Context)
    {
        if(NetworkUtils().checkForInternet(context)!=true)
        {
            CustomDialogBuilder.composeAlertDialog(
                title = context.getString(R.string.default_alert_title_error),
                message = context.getString(R.string.err_no_internet_connection),
            )
        }
    }


    private fun showError(
        navHostController: NavHostController,
        message: String
    ) {
        CustomDialogBuilder.composeAlertDialog(
            title = navHostController.context.getString(R.string.default_alert_title_error),
            message = message
        )
    }

    fun onInvalidFormData(context: Context) {
        var message = if(cardNumber.length != AppConstants.MAX_LENGTH_CARD_NO)
            context.resources.getString(R.string.max_card_length_err)
        else
            context.resources.getString(R.string.act_empty_card_cvv)

        CustomDialogBuilder.composeAlertDialog(title = context.resources.getString(R.string.default_alert_title_error),
            message = message
        )
    }

    private fun ByteArray.toHexString(): String {
        return joinToString("") { "%02X".format(it) }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun authenticateTransaction(sharedViewModel: SharedViewModel, navHostController: NavHostController) {
        viewModelScope.launch(Dispatchers.IO) {  // ← IO thread for network call
            try {
                Log.d("AuthTransaction", "Going For Authenticate the transaction")

                // Show progress on Main thread
                withContext(Dispatchers.Main) {
                    CustomDialogBuilder.composeProgressDialog(true)
                }

                apiServiceRepository.apiServiceRequestOnlineAuth(
                    paymentServiceTxnDetails = PaymentServiceUtils.transformObject<PaymentServiceTxnDetails>(
                        sharedViewModel.objRootAppPaymentDetail
                    ),
                    object : IApiServiceResponseListener {

                        override fun onApiServiceSuccess(response: PaymentServiceTxnDetails) {
                            viewModelScope.launch(Dispatchers.Main) {  // ← navigate on Main
                                CustomDialogBuilder.composeProgressDialog(false)
                                sharedViewModel.objRootAppPaymentDetail.hostResMessage = BuilderConstants.getIsoResponseMessage(response.hostRespCode.toString())
                                sharedViewModel.objRootAppPaymentDetail.hostAuthCode = response.hostAuthCode
                                sharedViewModel.objRootAppPaymentDetail.settlementDate = response.settlementDate
                                sharedViewModel.objRootAppPaymentDetail.expiryDate = response.expiryDate
                                sharedViewModel.objRootAppPaymentDetail.rrn = response.rrn
                                sharedViewModel.objRootAppPaymentDetail.currencyCode = response.currencyCode
                                sharedViewModel.objRootAppPaymentDetail.originalDateTime = response.dateTime
                                Log.d("AuthTransaction", "hostRespCode: ${response.hostRespCode}")
                                Log.d("AuthTransaction", "id: ${sharedViewModel.objRootAppPaymentDetail.id}")
                                Log.d("AuthTransaction", "mapped status: ${emvStatusToTransStatus(response.hostRespCode)}")
                                Log.d(
                                    "EBT",
                                    "originalDateTime: ${response.dateTime}"
                                )
                                sharedViewModel.objRootAppPaymentDetail.hostAuthCode = response.hostAuthCode
                                sharedViewModel.objRootAppPaymentDetail.posCondition = response.posCondition
                                updateTransResult(sharedViewModel, emvStatusToTransStatus(response.hostRespCode),
                                    sharedViewModel.objRootAppPaymentDetail.dateTime.toString(),sharedViewModel.objRootAppPaymentDetail.hostAuthCode.toString(),sharedViewModel.objRootAppPaymentDetail.posCondition.toString())
                                val rawAdditionalAmt = response.additionalAmt

                                if (!rawAdditionalAmt.isNullOrBlank() && rawAdditionalAmt != "null") {
                                    try {
                                        val balance = parseEBTBalances(rawAdditionalAmt)
                                        sharedViewModel.objRootAppPaymentDetail.snapEndBalance = balance.snap
                                        sharedViewModel.objRootAppPaymentDetail.cashEndBalance = balance.cash
                                        sharedViewModel.objRootAppPaymentDetail.cashEndBalance = balance.cash
                                        updateBalance(sharedViewModel)
                                    } catch (e: Exception) {
                                        sharedViewModel.objRootAppPaymentDetail.additionalAmt = "0.0"
                                    }
                                } else {
                                    sharedViewModel.objRootAppPaymentDetail.additionalAmt = "0.0"
                                }
                                sharedViewModel.objRootAppPaymentDetail.txnStatus =
                                    if (response.txnStatus == TxnStatus.APPROVED.toString())
                                        TxnStatus.APPROVED
                                    else
                                        TxnStatus.DECLINED
                                navHostController.navigate(AppNavigationItems.ApprovedScreen.route)
                            }
                        }

                        override fun onApiServiceError(error: ApiServiceError) {
                            viewModelScope.launch(Dispatchers.Main) {  // ← navigate on Main
                                CustomDialogBuilder.composeProgressDialog(false)
                                Log.e("AuthTransaction", "API Error: ${error}")
                                navHostController.navigate(AppNavigationItems.ApprovedScreen.route)
                            }
                        }

                        override fun onApiServiceTimeout(apiServiceTimeout: ApiServiceTimeout) {
                            viewModelScope.launch(Dispatchers.Main) {  // ← dialog on Main
                                CustomDialogBuilder.composeProgressDialog(false)
                                CustomDialogBuilder.composeAlertDialog(
                                    title = navHostController.context.resources?.getString(R.string.default_alert_title_error),
                                    message = apiServiceTimeout.message
                                )
                            }
                        }
                    }
                )
            } catch (e: Exception) {
                Log.e("AuthTransaction", "Exception: ${e.message}", e)  // ← you had empty catch!
                withContext(Dispatchers.Main) {
                    CustomDialogBuilder.composeProgressDialog(false)
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun updateTransResult(
        sharedViewModel: SharedViewModel,
        txnStatus: TxnStatus?,
        originalDateTime: String,
        AuthCode: String,
        posCondition: String
    ) {
        val TAG = "UPDATE_TXN_DEBUG"

        sharedViewModel.objRootAppPaymentDetail.txnStatus = txnStatus

        val txnId = sharedViewModel.objRootAppPaymentDetail.id

        dbRepository.fetchTxnById(txnId)?.let { txn ->
            txn.txnStatus = txnStatus?.toString() ?: ""
            txn.originalDateTime = sharedViewModel.objRootAppPaymentDetail.originalDateTime
            txn.hostAuthCode = AuthCode
            txn.posConditionCode = posCondition

            dbRepository.updateTxn(txn)

            Log.d(TAG, "---- DB Update Completed for txnId: $txnId ----")
        } ?: run {
            Log.e(TAG, "Transaction NOT FOUND for txnId: $txnId")
        }
    }

    fun parseEBTBalances(hexString: String): EBTBalance {

        val blocks = hexString.chunked(20)

        var snapBalance = 0.0
        var cashBalance = 0.0

        blocks.forEach { block ->

            val bytes = block.chunked(2).map { it.toInt(16) }

            val accountType = bytes[0] // first byte

            val bcdBytes = bytes.subList(4, 10)
            val digits = bcdBytes.joinToString("") { byte ->
                val high = (byte shr 4) and 0x0F
                val low  = byte and 0x0F
                "$high$low"
            }

            val amount = digits.toLong() / 100.0

            when (accountType) {
                0x96 -> {
                    cashBalance = amount
                    Log.d("EBT", "CASH Balance: $cashBalance")
                }
                0x98 -> {
                    snapBalance = amount
                    Log.d("EBT", "SNAP Balance: $snapBalance")
                }
            }
        }

        return EBTBalance(
            snap = snapBalance,
            cash = cashBalance
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun updateBalance(sharedViewModel: SharedViewModel) {
        viewModelScope.launch {

            val cash = sharedViewModel.objRootAppPaymentDetail.cashEndBalance
            val snap = sharedViewModel.objRootAppPaymentDetail.snapEndBalance

            // ✅ Print values
            Log.d("EBT", "Updating Balance → SNAP: $snap | CASH: $cash")

            dbRepository.fetchTxnById(sharedViewModel.objRootAppPaymentDetail.id)?.let { txn ->

                txn.cashEndBalance = cash.toString()
                txn.snapEndBalance = snap.toString()

                // ✅ Log before saving
                Log.d("EBT", "Saving to DB → SNAP: ${txn.snapEndBalance}, CASH: ${txn.cashEndBalance}")

                dbRepository.updateTxn(txn)
            }
        }
    }
}