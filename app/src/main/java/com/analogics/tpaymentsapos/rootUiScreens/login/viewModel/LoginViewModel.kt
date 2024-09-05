package com.analogics.tpaymentsapos.rootUiScreens.login.viewModel

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateOf

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController

import com.analogics.paymentservicecore.listeners.rootListener.IOnRootAppPaymentListener
import com.analogics.paymentservicecore.model.error.PaymentServiceError
import com.analogics.paymentservicecore.repository.paymentService.PaymentServiceRepository
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import com.example.example.ObjEmployeeResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
@HiltViewModel
class LoginViewModel @Inject constructor(private var paymentServiceRepository: PaymentServiceRepository): ViewModel(),
IOnRootAppPaymentListener {
    var emailCredentials = mutableStateOf("")
    var pwdCredentials = mutableStateOf("")
    var userApiSuccessHolder = MutableStateFlow(ObjEmployeeResponse())
    var userApiErrorHolder = MutableStateFlow(PaymentServiceError())
    lateinit var navHostController:NavHostController
    val isFormValid: Boolean
        get() = emailCredentials.value.isNotBlank() && pwdCredentials.value.isNotBlank()

    fun onEmailChange(newEmail: String) {
        emailCredentials.value = newEmail
    }

    fun onPasswordChange(newPassword: String) {
        pwdCredentials.value = newPassword
    }

    fun onLoginClick(navHost: NavHostController?, context: Context) {
        this.navHostController=navHost!!
        viewModelScope.launch {
            try {
                    //paymentServiceRepository.apiEmpDetails(iOnRootAppPaymentListener = this@LoginViewModel)
                navHostController?.navigate(AppNavigationItems.TrainingScreen.route)
                paymentServiceRepository.getPosConfig().apply {isLoggedIn = true}.saveToPrefs(context)
            } catch (e: Exception) {
               e.printStackTrace()
            }
        }
    }


    override fun onPaymentSuccess(response: Any) {
        userApiSuccessHolder.value=response as ObjEmployeeResponse
        if (isFormValid) {
            navHostController?.navigate(AppNavigationItems.TrainingScreen.route)
        }
        Log.e("API Response",response.toString())
    }

    override fun onPaymentError(paymentError: PaymentServiceError) {
        Log.e("API Response",paymentError.errorMessage)
        userApiErrorHolder.value= paymentError
    }
}
