package com.analogics.tpaymentsapos.rootUiScreens.login.viewModel

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.analogics.builder_core.model.PaymentServiceTxnDetails
import com.analogics.paymentservicecore.listeners.responseListener.IApiServiceResponseListener
import com.analogics.paymentservicecore.logger.AppLogger
import com.analogics.paymentservicecore.model.error.ApiServiceError
import com.analogics.paymentservicecore.repository.apiService.ApiServiceRepository
import com.analogics.paymentservicecore.utils.PaymentServiceUtils
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import com.analogics.tpaymentsapos.rootModel.ObjRootAppPaymentDetails
import com.analogics.tpaymentsapos.rootUiScreens.activity.SharedViewModel
import com.analogics.tpaymentsapos.rootUiScreens.dialogs.CustomDialogBuilder
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.navigateAndClean
import com.example.example.ObjEmployeeResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(private var apiServiceRepository: ApiServiceRepository) :
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

        viewModelScope.launch {
            try {

                setLoginButtonState(false)
                getAccessToken()
                //navHostController.navigateAndClean(AppNavigationItems.DashBoardScreen.route)
                //sharedViewModel.objPosConfig?.apply { isLoggedIn = true}?.saveToPrefs()
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

    override fun onApiSuccess(response: Any) {
        when (response) {
            is ObjRootAppPaymentDetails -> {
                useRootAppPaymentDetails.value = response
            }

            else -> {
                //userApiSuccessHolder.value = response as ObjEmployeeResponse
                if (isFormValid) {
                    sharedViewModel?.objPosConfig?.apply { isLoggedIn = true}?.saveToPrefs()
                    navHostController.navigateAndClean(AppNavigationItems.DashBoardScreen.route)
                }
                Log.e("API Response", response.toString())
            }
        }

    }

    override fun onApiError(paymentError: ApiServiceError) {
        Log.e("API Response", paymentError.errorMessage)
        userApiServiceErrorHolder.value = paymentError
        setLoginButtonState(true)
    }

    override fun onDisplayProgress(
        show: Boolean,
        title: String?,
        subTitle: String?,
        message: String?
    ) {
        CustomDialogBuilder.composeProgressDialog(show = show,title = title, subtitle = subTitle, message = message)
    }
}
