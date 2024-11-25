package com.analogics.tpaymentsapos.rootUiScreens.login.viewModel

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.analogics.paymentservicecore.listeners.responseListener.IApiServiceResponseListener
import com.analogics.paymentservicecore.logger.AppLogger
import com.analogics.paymentservicecore.model.PaymentServiceTxnDetails
import com.analogics.paymentservicecore.model.error.ApiServiceError
import com.analogics.paymentservicecore.repository.apiService.ApiServiceRepository
import com.analogics.paymentservicecore.utils.PaymentServiceUtils
import com.analogics.securityframework.database.dbRepository.TxnDBRepository
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import com.analogics.tpaymentsapos.rootModel.ObjRootAppPaymentDetails
import com.analogics.tpaymentsapos.rootUiScreens.activity.SharedViewModel
import com.analogics.tpaymentsapos.rootUiScreens.dialogs.CustomDialogBuilder
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.navigateAndClean
import com.analogics.tpaymentsapos.R
import com.example.example.ObjEmployeeResponse
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
    var userApiSuccessHolder = MutableStateFlow(ObjEmployeeResponse())
    var useRootAppPaymentDetails = MutableStateFlow(ObjRootAppPaymentDetails())
    var userApiServiceErrorHolder = MutableStateFlow(ApiServiceError())
    lateinit var navHostController: NavHostController
    var sharedViewModel : SharedViewModel? = null
    val isFormValid: Boolean
        get() = emailCredentials.value.isNotBlank() && pwdCredentials.value.isNotBlank()
    var showProgress = mutableStateOf(false)

    fun onEmailChange(newEmail: String) {
        emailCredentials.value = newEmail
    }

    fun onPasswordChange(newPassword: String) {
        pwdCredentials.value = newPassword
    }

    fun setLoginButtonState(enabled: Boolean)  { isLoginEnabled.value = enabled }

    fun onLoginClick(navHost: NavHostController?, sharedViewModel : SharedViewModel) {
        this.navHostController = navHost!!
        this.sharedViewModel = sharedViewModel
        setLoginButtonState(false)
        viewModelScope.launch {
            try {
                if(dbRepository.getUserDetails(emailCredentials.value).takeIf { it?.password==pwdCredentials.value }?.let { true } == true) {
                    navHostController.navigateAndClean(AppNavigationItems.DashBoardScreen.route)
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
                    CustomDialogBuilder.composeAlertDialog(title = navHost.context.resources.getString(R.string.login), subtitle = navHost.context.resources.getString(R.string.login_invalid_cred))
                    setLoginButtonState(true)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun onApiDeviceLogin() {
        viewModelScope.launch {
            try {
                val requestDetails =
                    PaymentServiceUtils.objectToJsonString(useRootAppPaymentDetails.value)
                apiServiceRepository.apiServiceLogin(
                    PaymentServiceUtils.jsonStringToObject<PaymentServiceTxnDetails>(requestDetails), this@LoginViewModel)
            } catch (e: Exception) {
                AppLogger.d(AppLogger.MODULE.APP_UI, e.message ?: "")
            }
        }
    }

    fun getAccessToken() {
        viewModelScope.launch {
            try {
                val requestDetails =
                    PaymentServiceUtils.objectToJsonString(useRootAppPaymentDetails.value)
                apiServiceRepository.apiServiceAccessToken(
                    PaymentServiceUtils.jsonStringToObject<PaymentServiceTxnDetails>(requestDetails), this@LoginViewModel)
            } catch (e: Exception) {
                AppLogger.d(AppLogger.MODULE.APP_UI, e.message ?: "")
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

    override fun onApiServiceDisplayProgress(
        show: Boolean,
        title: String?,
        subTitle: String?,
        message: String?
    ) {
        CustomDialogBuilder.composeProgressDialog(show = show,title = title, subtitle = subTitle, message = message)
    }
}
