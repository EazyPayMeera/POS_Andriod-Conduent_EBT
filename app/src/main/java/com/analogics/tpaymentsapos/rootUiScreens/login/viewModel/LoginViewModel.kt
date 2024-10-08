package com.analogics.tpaymentsapos.rootUiScreens.login.viewModel

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.analogics.builder_core.model.PaymentServiceTxnDetails
import com.analogics.paymentservicecore.listeners.rootListener.IOnRootAppPaymentListener
import com.analogics.paymentservicecore.logger.AppLogger
import com.analogics.paymentservicecore.model.error.PaymentServiceError
import com.analogics.paymentservicecore.repository.paymentService.PaymentServiceRepository
import com.analogics.paymentservicecore.utils.PaymentServiceUtils
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import com.analogics.tpaymentsapos.rootModel.ObjRootAppPaymentDetails
import com.analogics.tpaymentsapos.rootUiScreens.activity.SharedViewModel
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.navigateAndClean
import com.example.example.ObjEmployeeResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(private var paymentServiceRepository: PaymentServiceRepository) :
    ViewModel(),
    IOnRootAppPaymentListener {
    var emailCredentials = mutableStateOf("")
    var pwdCredentials = mutableStateOf("")
    val isLoginEnabled = mutableStateOf(true)
    var userApiSuccessHolder = MutableStateFlow(ObjEmployeeResponse())
    var useRootAppPaymentDetails = MutableStateFlow(ObjRootAppPaymentDetails())
    var userApiErrorHolder = MutableStateFlow(PaymentServiceError())
    lateinit var navHostController: NavHostController
    val isFormValid: Boolean
        get() = emailCredentials.value.isNotBlank() && pwdCredentials.value.isNotBlank()

    fun onEmailChange(newEmail: String) {
        emailCredentials.value = newEmail
    }

    fun onPasswordChange(newPassword: String) {
        pwdCredentials.value = newPassword
    }

    fun setLoginButtonState(enabled: Boolean)  { isLoginEnabled.value = enabled }

    fun onLoginClick(navHost: NavHostController?, sharedViewModel : SharedViewModel) {
        this.navHostController = navHost!!

        viewModelScope.launch {
            try {

                setLoginButtonState(false)
                getAccessToken()
                navHostController.navigateAndClean(AppNavigationItems.DashBoardScreen.route)
                //sharedViewModel.objPosConfig?.apply { isLoggedIn = true}?.saveToPrefs()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /*
       * Call any API like bellow and it will return the dummy data in ObjRootAppPaymnet Details
       *
       * */
    fun onApiDeviceLogin() {
        viewModelScope.launch {
            try {
                val requestDetails =
                    PaymentServiceUtils.objectToJsonString(useRootAppPaymentDetails.value)
                paymentServiceRepository.apiServiceLogin(
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
                paymentServiceRepository.apiServiceAccessToken(
                    PaymentServiceUtils.jsonStringToObject<PaymentServiceTxnDetails>(requestDetails), this@LoginViewModel)
            } catch (e: Exception) {
                AppLogger.d(AppLogger.MODULE.APP_UI, e.message ?: "")
            }
        }
    }

    override fun onPaymentSuccess(response: Any) {
        when (response) {
            is ObjRootAppPaymentDetails -> {
                useRootAppPaymentDetails.value = response
            }

            else -> {
                //userApiSuccessHolder.value = response as ObjEmployeeResponse
                if (isFormValid) {
                    navHostController.navigateAndClean(AppNavigationItems.DashBoardScreen.route)
                }
                Log.e("API Response", response.toString())
            }
        }

    }

    override fun onPaymentError(paymentError: PaymentServiceError) {
        Log.e("API Response", paymentError.errorMessage)
        userApiErrorHolder.value = paymentError
        setLoginButtonState(true)
    }
}
