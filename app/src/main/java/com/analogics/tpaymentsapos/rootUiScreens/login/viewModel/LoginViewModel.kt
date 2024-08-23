package com.analogics.tpaymentsapos.rootUiScreens.login.viewModel

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.navigation.NavHostController
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.showToast

class LoginViewModel : ViewModel() {
    var emailCredentials = mutableStateOf("")
    var pwdCredentials = mutableStateOf("")

    val isFormValid: Boolean
        get() = emailCredentials.value.isNotBlank() && pwdCredentials.value.isNotBlank()

    fun onEmailChange(newEmail: String) {
        emailCredentials.value = newEmail
    }

    fun onPasswordChange(newPassword: String) {
        pwdCredentials.value = newPassword
    }

    fun onLoginClick(navHostController: NavHostController?,context: Context) {
        if (isFormValid) {
            navHostController?.navigate(AppNavigationItems.TrainingScreen.route)
        }
    }
}
