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
import dagger.hilt.android.lifecycle.HiltViewModel
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

    fun onEmailChange(newEmail: String) {
        emailCredentials.value = newEmail
    }

    fun onPasswordChange(newPassword: String) {
        pwdCredentials.value = newPassword
    }

    fun setLoginButtonState(enabled: Boolean)  { isLoginEnabled.value = enabled }

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




    override fun onApiServiceSuccess(paymentServiceTxnDetails: PaymentServiceTxnDetails) {
        sharedViewModel?.objPosConfig?.apply { isLoggedIn = true }?.saveToPrefs()
        navHostController.navigateAndClean(AppNavigationItems.DashBoardScreen.route)
    }

    override fun onApiServiceError(apiServiceError: ApiServiceError) {
        Log.e("API Response", apiServiceError.errorMessage)
        userApiServiceErrorHolder.value = apiServiceError
        setLoginButtonState(true)
    }

    override fun onApiServiceTimeout(apiServiceTimeout: ApiServiceTimeout) {
        CustomDialogBuilder.Companion.composeAlertDialog(title = navHostController.context.resources?.getString(
            R.string.default_alert_title_error),message = apiServiceTimeout.message)
    }

    override fun onApiServiceDisplayProgress(
        show: Boolean,
        title: String?,
        subTitle: String?,
        message: String?
    ) {
        CustomDialogBuilder.Companion.composeProgressDialog(show = show,title = title, subtitle = subTitle, message = message)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun authenticateTransaction(sharedViewModel: SharedViewModel, navHostController: NavHostController) {
        viewModelScope.launch {
            try {
                Log.d("AuthTransaction","Going For Authenticate the transaction")
                CustomDialogBuilder.Companion.composeProgressDialog(true)
                apiServiceRepository.apiServiceRequestOnlineAuth(paymentServiceTxnDetails = PaymentServiceUtils.transformObject<PaymentServiceTxnDetails>(sharedViewModel.objRootAppPaymentDetail), object :
                    IApiServiceResponseListener {

                    override fun onApiServiceSuccess(response: PaymentServiceTxnDetails) {
                        CustomDialogBuilder.Companion.composeProgressDialog(false)
                        sharedViewModel.objRootAppPaymentDetail.hostResMessage = BuilderConstants.getIsoResponseMessage(response.hostRespCode.toString())
                        sharedViewModel.objRootAppPaymentDetail.txnStatus = if(response.txnStatus == TxnStatus.APPROVED.toString()) TxnStatus.APPROVED else TxnStatus.DECLINED
                        navHostController.navigate(AppNavigationItems.ApprovedScreen.route)
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
}