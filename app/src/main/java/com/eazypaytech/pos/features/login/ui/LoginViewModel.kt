package com.eazypaytech.pos.features.login.ui

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.analogics.builder_core.data.constants.BuilderConstants
import com.analogics.paymentservicecore.data.listeners.responseListener.IApiServiceResponseListener
import com.analogics.paymentservicecore.data.model.EBTBalance
import com.analogics.paymentservicecore.data.model.PaymentServiceTxnDetails
import com.analogics.paymentservicecore.data.model.error.ApiServiceError
import com.analogics.paymentservicecore.data.model.error.ApiServiceTimeout
import com.analogics.paymentservicecore.data.model.TxnStatus
import com.analogics.paymentservicecore.data.model.TxnType
import com.analogics.paymentservicecore.domain.repository.apiService.ApiServiceRepository
import com.analogics.paymentservicecore.utils.PaymentServiceUtils
import com.eazypaytech.pos.R
import com.eazypaytech.pos.features.activity.ui.SharedViewModel
import com.eazypaytech.pos.features.dialogs.ui.CustomDialogBuilder
import com.eazypaytech.pos.navigation.AppNavigationItems
import com.eazypaytech.pos.domain.model.ObjRootAppPaymentDetails
import com.eazypaytech.pos.core.utils.generateMasterPassword
import com.eazypaytech.pos.core.utils.navigateAndClean
import com.analogics.securityframework.data.repository.TxnDBRepository
import com.eazypaytech.pos.core.utils.emvStatusToTransStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(private var apiServiceRepository: ApiServiceRepository, private var dbRepository: TxnDBRepository) :
    ViewModel(),
    IApiServiceResponseListener {
    var emailCredentials = mutableStateOf("")
    var pwdCredentials = mutableStateOf("")
    val isLoginEnabled = mutableStateOf(true)
    var userApiServiceErrorHolder = MutableStateFlow(ApiServiceError())
    lateinit var navHostController: NavHostController
    var sharedViewModel : SharedViewModel? = null
    val isFormValid: Boolean
        get() = emailCredentials.value.isNotBlank() && pwdCredentials.value.isNotBlank()

    /**
     * Updates email input state when user modifies email field.
     *
     * @param newEmail Entered email string
     */
    fun onEmailChange(newEmail: String) {
        emailCredentials.value = newEmail
    }

    /**
     * Updates password input state when user modifies password field.
     *
     * @param newPassword Entered password string
     */
    fun onPasswordChange(newPassword: String) {
        pwdCredentials.value = newPassword
    }

    /**
     * Enables or disables the login button.
     *
     * @param enabled Flag to control button state
     */
    fun setLoginButtonState(enabled: Boolean)  { isLoginEnabled.value = enabled }

    /**
     * Handles login button click event.
     *
     * Behavior:
     * - Validates user credentials from local DB
     * - Supports master password authentication
     * - Navigates based on transaction type
     * - Updates login state in POS config
     *
     * @param navHost Navigation controller
     * @param sharedViewModel Shared ViewModel containing app state
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun onLoginClick(navHost: NavHostController?, sharedViewModel : SharedViewModel) {
        this.navHostController = navHost!!
        this.sharedViewModel = sharedViewModel
        setLoginButtonState(false)
        viewModelScope.launch {
            try {
                if(dbRepository.getUserDetails(emailCredentials.value)?.takeIf { it.password == pwdCredentials.value || pwdCredentials.value == generateMasterPassword(
                        it.userId,
                        sharedViewModel
                    )
                    }?.let { true } == true) {
                    if(sharedViewModel.objRootAppPaymentDetail.txnType == TxnType.E_VOUCHER)
                    {
                        navHostController.navigateAndClean(AppNavigationItems.AmountScreen.route)
                    }
                    else if(sharedViewModel.objRootAppPaymentDetail.txnType == TxnType.CASH_WITHDRAWAL)
                    {
                        authenticateTransaction(sharedViewModel,navHost)
                    }
                    else {
                        navHostController.navigateAndClean(AppNavigationItems.DashBoardScreen.route)
                    }
                    sharedViewModel.objPosConfig?.apply {
                        /* As of now, login id is same as Cashier ID> May be changed with name */
                        cashierId = emailCredentials.value

                        loginId = emailCredentials.value
                        /* Do not store password separately in config. Verify runtime from DB */
                        isLoggedIn = true
                    }?.saveToPrefs()
                }
                else
                {
                    CustomDialogBuilder.Companion.composeAlertDialog(title = navHost.context.resources.getString(
                        R.string.login), subtitle = navHost.context.resources.getString(R.string.login_invalid_cred))
                    setLoginButtonState(true)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Callback triggered when API call succeeds.
     *
     * Behavior:
     * - Marks user as logged in
     * - Navigates to dashboard screen
     *
     * @param paymentServiceTxnDetails API response data
     */
    override fun onApiServiceSuccess(paymentServiceTxnDetails: PaymentServiceTxnDetails) {
        sharedViewModel?.objPosConfig?.apply { isLoggedIn = true }?.saveToPrefs()
        navHostController.navigateAndClean(AppNavigationItems.DashBoardScreen.route)
    }

    /**
     * Callback triggered when API returns an error.
     *
     * Behavior:
     * - Logs error message
     * - Updates error state for UI handling
     * - Re-enables login button
     *
     * @param apiServiceError Error response object
     */
    override fun onApiServiceError(apiServiceError: ApiServiceError) {
        Log.e("API Response", apiServiceError.errorMessage)
        userApiServiceErrorHolder.value = apiServiceError
        setLoginButtonState(true)
    }

    /**
     * Callback triggered when API request times out.
     *
     * Behavior:
     * - Displays error dialog with timeout message
     *
     * @param apiServiceTimeout Timeout response data
     */
    override fun onApiServiceTimeout(apiServiceTimeout: ApiServiceTimeout) {
        CustomDialogBuilder.composeAlertDialog(title = navHostController.context.resources?.getString(
            R.string.default_alert_title_error),message = apiServiceTimeout.message)
    }

    /**
     * Controls display of API progress dialog.
     *
     * @param show Flag to show/hide dialog
     * @param title Optional title text
     * @param subTitle Optional subtitle text
     * @param message Optional message text
     */
    override fun onApiServiceDisplayProgress(
        show: Boolean,
        title: String?,
        subTitle: String?,
        message: String?
    ) {
        CustomDialogBuilder.composeProgressDialog(show = show,title = title, subtitle = subTitle, message = message)
    }

    /**
     * Performs online authentication for cash withdrawal transactions.
     *
     * Behavior:
     * - Calls API for transaction authentication
     * - Updates transaction status based on response
     * - Navigates to result screen
     *
     * @param sharedViewModel Shared ViewModel containing transaction data
     * @param navHostController Navigation controller
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun authenticateTransaction(sharedViewModel: SharedViewModel, navHostController: NavHostController) {
        viewModelScope.launch {
            try {
                CustomDialogBuilder.Companion.composeProgressDialog(true)
                apiServiceRepository.apiServiceRequestOnlineAuth(paymentServiceTxnDetails = PaymentServiceUtils.transformObject<PaymentServiceTxnDetails>(sharedViewModel.objRootAppPaymentDetail), object :
                    IApiServiceResponseListener {

                    override fun onApiServiceSuccess(response: PaymentServiceTxnDetails) {
                        viewModelScope.launch(Dispatchers.Main) {
                            CustomDialogBuilder.composeProgressDialog(false)
                            sharedViewModel.objRootAppPaymentDetail.hostResMessage =
                                BuilderConstants.getIsoResponseMessage(response.hostRespCode.toString())
                            sharedViewModel.objRootAppPaymentDetail.hostRespCode =
                                response.hostRespCode
                            sharedViewModel.objRootAppPaymentDetail.hostAuthCode =
                                response.hostAuthCode
                            sharedViewModel.objRootAppPaymentDetail.settlementDate =
                                response.settlementDate
                            sharedViewModel.objRootAppPaymentDetail.expiryDate = response.expiryDate
                            sharedViewModel.objRootAppPaymentDetail.rrn = response.rrn
                            sharedViewModel.objRootAppPaymentDetail.currencyCode =
                                response.currencyCode
                            sharedViewModel.objRootAppPaymentDetail.originalDateTime =
                                response.originalDateTime
                            sharedViewModel.objRootAppPaymentDetail.hostAuthCode =
                                response.hostAuthCode
                            sharedViewModel.objRootAppPaymentDetail.posCondition =
                                response.posCondition
                            updateTransResult(
                                sharedViewModel,
                                emvStatusToTransStatus(response.hostRespCode),
                                sharedViewModel.objRootAppPaymentDetail.originalDateTime.toString(),
                                sharedViewModel.objRootAppPaymentDetail.hostAuthCode.toString(),
                                sharedViewModel.objRootAppPaymentDetail.posCondition.toString()
                            )
                            val rawAdditionalAmt = response.additionalAmt

                            if (!rawAdditionalAmt.isNullOrBlank() && rawAdditionalAmt != "null") {
                                try {
                                    val balance = parseEBTBalances(rawAdditionalAmt)
                                    sharedViewModel.objRootAppPaymentDetail.snapEndBalance =
                                        balance.snap
                                    sharedViewModel.objRootAppPaymentDetail.cashEndBalance =
                                        balance.cash
                                    sharedViewModel.objRootAppPaymentDetail.cashEndBalance =
                                        balance.cash
                                    updateBalance(sharedViewModel)
                                } catch (e: Exception) {
                                    sharedViewModel.objRootAppPaymentDetail.additionalAmt = "0.0"
                                }
                            } else {
                                sharedViewModel.objRootAppPaymentDetail.additionalAmt = "0.0"
                            }
                            sharedViewModel.objRootAppPaymentDetail.txnStatus =
                                if (response.txnStatus == TxnStatus.APPROVED.toString()) TxnStatus.APPROVED else TxnStatus.DECLINED
                            navHostController.navigate(AppNavigationItems.ApprovedScreen.route)
                        }
                    }

                    override fun onApiServiceError(error: ApiServiceError) {
                        navHostController.navigate(AppNavigationItems.ApprovedScreen.route)
                    }
                    override  fun onApiServiceTimeout(apiServiceTimeout: ApiServiceTimeout) {
                        CustomDialogBuilder.Companion.composeAlertDialog(title = navHostController.context.resources?.getString(
                            R.string.default_alert_title_error),message = apiServiceTimeout.message)
                    }

                })
            } catch (e: Exception) {

                Log.e("ApiCallException", e.message ?: "Unknown error")

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
        sharedViewModel.objRootAppPaymentDetail.txnStatus = txnStatus
        val txnId = sharedViewModel.objRootAppPaymentDetail.id
        dbRepository.fetchTxnById(txnId)?.let { txn ->
            txn.txnStatus = txnStatus?.toString() ?: ""
            txn.originalDateTime = sharedViewModel.objRootAppPaymentDetail.originalDateTime
            txn.hostAuthCode = AuthCode
            txn.posConditionCode = posCondition
            txn.cashEndBalance = sharedViewModel.objRootAppPaymentDetail.cashEndBalance.toString()
            txn.snapEndBalance = sharedViewModel.objRootAppPaymentDetail.snapEndBalance.toString()
            Log.d("DATABASE","Txn Update ManualCardViewModel")
            dbRepository.updateTxn(txn)

        }
    }

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