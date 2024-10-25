package com.analogics.tpaymentsapos.rootUiScreens.activationScreen.viewModel

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.analogics.securityframework.database.dbRepository.TxnDBRepository
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import com.analogics.tpaymentsapos.rootUiScreens.activity.SharedViewModel
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.convertObjRootToUserManagementEntity
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.navigateAndClean
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ClerkLoginViewModel @Inject constructor(
    private var dbRepository: TxnDBRepository
) :
    ViewModel()
{


    var userCredentials = mutableStateOf("")
    var pwdCredentials = mutableStateOf("")
    var cnfPwdCredentials = mutableStateOf("")
    val isLoginEnabled = mutableStateOf(true)
    lateinit var navHostController: NavHostController
    var sharedViewModel: SharedViewModel? = null
    val isFormValid: Boolean
        get() = userCredentials.value.isNotBlank() && pwdCredentials.value.isNotBlank() && cnfPwdCredentials.value.isNotBlank() && (pwdCredentials.value == cnfPwdCredentials.value)


    fun onEmailChange(newEmail: String) {
        userCredentials.value = newEmail
    }

    fun onPasswordChange(newPassword: String) {
        pwdCredentials.value = newPassword
    }

    fun onCnfPasswordChange(newPassword: String) {
        cnfPwdCredentials.value = newPassword
    }

   private fun setLoginButtonState(enabled: Boolean) {
        isLoginEnabled.value = enabled
    }

    fun onLoginClick(navHost: NavHostController?, sharedViewModel: SharedViewModel) {
        this.navHostController = navHost!!
        this.sharedViewModel = sharedViewModel

        viewModelScope.launch {
            try {
                Log.d("logggg",sharedViewModel.objRootAppPaymentDetail.objUserDetails.toString())
                val userEntity = convertObjRootToUserManagementEntity(sharedViewModel.objRootAppPaymentDetail)

                if (isFormValid) {
                    dbRepository.insertUser(userEntity)
                    setLoginButtonState(true)
                    sharedViewModel.objPosConfig?.apply { isLoggedIn = true }?.saveToPrefs()
                    navHostController.navigateAndClean(AppNavigationItems.DashBoardScreen.route)
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


}