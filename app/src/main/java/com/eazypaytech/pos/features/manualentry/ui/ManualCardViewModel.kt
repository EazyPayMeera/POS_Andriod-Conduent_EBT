package com.eazypaytech.pos.features.manualentry.ui

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
import com.eazypaytech.pos.navigation.AppNavigationItems
import com.eazypaytech.pos.features.activity.ui.SharedViewModel
import com.eazypaytech.pos.features.dialogs.ui.CustomDialogBuilder
import com.eazypaytech.pos.core.utils.navigateAndClean
import com.eazypaytech.pos.core.utils.miscellaneous.NetworkUtils
import com.analogics.securityframework.data.repository.TxnDBRepository
import com.eazypaytech.pos.core.utils.emvStatusToTransStatus
import com.eazypaytech.pos.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
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

    /**
     * Updates card number when user inputs or modifies it.
     *
     * @param newValue Entered card number
     */
    fun onCardNoChange(newValue: String) {
        cardNumber = newValue
    }

    /**
     * Handles confirm action for manual card entry.
     *
     * Behavior:
     * - Sets card details into transaction object
     * - Validates card input
     * - Navigates based on transaction type
     * - Initiates PIN generation if required
     *
     * @param context Application context
     * @param navHostController Navigation controller
     * @param sharedViewModel Shared ViewModel containing transaction data
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun onConfirm(
        context: Context,
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
                    context.getString(R.string.plz_enter_card),
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

    /**
     * Handles cancel action and navigates back to dashboard.
     *
     * @param navHostController Navigation controller
     */
    fun onCancel(navHostController: NavHostController) {
        navHostController.navigateAndClean(AppNavigationItems.DashBoardScreen.route)
    }

    /**
     * Generates PIN block and proceeds with transaction.
     *
     * Behavior:
     * - Calls EMV service for PIN generation
     * - Stores PIN block in transaction
     * - Navigates based on transaction type
     *
     * @param navHostController Navigation controller
     * @param sharedViewModel Shared ViewModel containing transaction data
     */
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

    /**
     * Checks whether a card is physically present in the device.
     *
     * @param context Application context
     * @return true if card exists, false otherwise
     */
    fun isCardExists(context: Context): Boolean {
        return emvServiceRepository.isCardExists(context)
    }

    /**
     * Displays an error dialog with provided message.
     *
     * @param navHostController Navigation controller
     * @param message Error message to display
     */
    private fun showError(
        navHostController: NavHostController,
        message: String
    ) {
        CustomDialogBuilder.composeAlertDialog(
            title = navHostController.context.getString(R.string.default_alert_title_error),
            message = message
        )
    }

    /**
     * Handles invalid form data scenarios and shows appropriate error message.
     *
     * @param context Application context
     */
    fun onInvalidFormData(context: Context) {
        var message = if(cardNumber.length != AppConstants.MAX_LENGTH_CARD_NO)
            context.resources.getString(R.string.max_card_length_err)
        else
            context.resources.getString(R.string.act_empty_card_cvv)

        CustomDialogBuilder.composeAlertDialog(title = context.resources.getString(R.string.default_alert_title_error),
            message = message
        )
    }

    /**
     * Converts ByteArray  to hexadecimal string.
     *
     * @return Hex string representation
     */
    private fun ByteArray.toHexString(): String {
        return joinToString("") { "%02X".format(it) }
    }

    /**
     * Performs online transaction authentication via API.
     *
     * Behavior:
     * - Calls authentication API
     * - Updates transaction details from response
     * - Handles success, error, and timeout cases
     * - Navigates to result screen
     *
     * @param sharedViewModel Shared ViewModel containing transaction data
     * @param navHostController Navigation controller
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun authenticateTransaction(sharedViewModel: SharedViewModel, navHostController: NavHostController) {
        viewModelScope.launch(Dispatchers.IO) {  // ← IO thread for network call
            try {
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
                                sharedViewModel.objRootAppPaymentDetail.hostRespCode = response.hostRespCode
                                sharedViewModel.objRootAppPaymentDetail.hostAuthCode = response.hostAuthCode
                                sharedViewModel.objRootAppPaymentDetail.settlementDate = response.settlementDate
                                sharedViewModel.objRootAppPaymentDetail.expiryDate = response.expiryDate
                                sharedViewModel.objRootAppPaymentDetail.rrn = response.rrn
                                sharedViewModel.objRootAppPaymentDetail.currencyCode = response.currencyCode
                                sharedViewModel.objRootAppPaymentDetail.originalDateTime = response.originalDateTime
                                sharedViewModel.objRootAppPaymentDetail.hostAuthCode = response.hostAuthCode
                                sharedViewModel.objRootAppPaymentDetail.posCondition = response.posCondition
                                updateTransResult(sharedViewModel, emvStatusToTransStatus(response.hostRespCode),
                                    sharedViewModel.objRootAppPaymentDetail.originalDateTime.toString(),sharedViewModel.objRootAppPaymentDetail.hostAuthCode.toString(),sharedViewModel.objRootAppPaymentDetail.posCondition.toString())
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

    /**
     * Updates transaction result in local database.
     *
     * @param sharedViewModel Shared ViewModel
     * @param txnStatus Transaction status
     * @param originalDateTime Original transaction timestamp
     * @param AuthCode Authorization code
     * @param posCondition POS condition code
     */
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun updateTransResult(
        sharedViewModel: SharedViewModel,
        txnStatus: TxnStatus?,
        originalDateTime: String,
        AuthCode: String,
        posCondition: String
    ) {
        sharedViewModel.objRootAppPaymentDetail.txnStatus = txnStatus
        val txnId = sharedViewModel.objRootAppPaymentDetail.id
        dbRepository.fetchTxnById(txnId)?.let { txn ->
            txn.txnStatus = txnStatus?.toString() ?: ""
            txn.originalDateTime = sharedViewModel.objRootAppPaymentDetail.originalDateTime
            txn.hostAuthCode = AuthCode
            txn.posConditionCode = posCondition
            txn.hostResMessage = sharedViewModel.objRootAppPaymentDetail.hostResMessage
            txn.cashEndBalance = sharedViewModel.objRootAppPaymentDetail.cashEndBalance.toString()
            txn.snapEndBalance = sharedViewModel.objRootAppPaymentDetail.snapEndBalance.toString()
            Log.d("DATABASE","Txn Update ManualCardViewModel")
            dbRepository.updateTxn(txn)

        }
    }

    /**
     * Parses EBT balance data from hex string.
     *
     * Behavior:
     * - Extracts SNAP and Cash balances
     * - Processes only "Available Balance" entries
     *
     * @param hexString Raw hex string containing balance data
     * @return Parsed EBT balance object
     */
    fun parseEBTBalances(hexString: String): EBTBalance {
        val blocks = hexString.chunked(20)
        var snapBalance = 0.0
        var cashBalance = 0.0

        blocks.forEach { block ->
            val bytes = block.chunked(2).map { it.toInt(16) }
            val accountType = bytes[0]  // positions 1-2: account type
            val amountType = bytes[1]   // positions 3-4: amount type

            // Only process Available Balance (0x02)
            if (amountType != 0x02) return@forEach

            val bcdBytes = bytes.subList(4, 10)
            val digits = bcdBytes.joinToString("") { byte ->
                val high = (byte shr 4) and 0x0F
                val low  = byte and 0x0F
                "$high$low"
            }
            val amount = digits.toLong() / 100.0

            when (accountType) {
                0x96 -> cashBalance = amount   // Cash Benefit
                0x98 -> snapBalance = amount   // Food Stamp Benefit
            }
        }

        return EBTBalance(snap = snapBalance, cash = cashBalance)
    }

    /**
     * Updates EBT balance values in local database.
     *
     * @param sharedViewModel Shared ViewModel containing updated balances
     */
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun updateBalance(sharedViewModel: SharedViewModel) {
        try {
            val id = sharedViewModel.objRootAppPaymentDetail.id ?: run {
                Log.e("UPDATE_BALANCE", "❌ id is null")
                return
            }
            val newCash = sharedViewModel.objRootAppPaymentDetail.cashEndBalance ?: 0.0
            val newSnap = sharedViewModel.objRootAppPaymentDetail.snapEndBalance ?: 0.0

            Log.d("UPDATE_BALANCE", "▶ START — id=$id, cash=$newCash, snap=$newSnap")

            if (newCash == 0.0 && newSnap == 0.0) {
                Log.e("UPDATE_BALANCE", "❌ ABORTED — both balances are 0.0, skipping update")
                return
            }

            dbRepository.updateBalancesOnly(id, newCash, newSnap)

            Log.d("UPDATE_BALANCE", "✅ SUCCESS — cash=$newCash, snap=$newSnap")

        } catch (e: Exception) {
            Log.e("UPDATE_BALANCE", "❌ Error updating balance: ${e.message}")
        }
    }
}