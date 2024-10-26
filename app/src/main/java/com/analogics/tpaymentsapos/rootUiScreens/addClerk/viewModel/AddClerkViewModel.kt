package com.analogics.tpaymentsapos.rootUiScreens.addClerk.viewModel

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.analogics.securityframework.database.dbRepository.TxnDBRepository
import com.analogics.securityframework.database.entity.UserManagementEntity
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import com.analogics.tpaymentsapos.rootUiScreens.activity.SharedViewModel
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.convertObjRootToUserManagementEntity
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.navigateAndClean
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.apply
import kotlin.text.isNotBlank
import kotlin.toString

@HiltViewModel
class AddClerkViewModel @Inject constructor(
    private var dbRepository: TxnDBRepository
) :
    ViewModel()
{
    var userCredentials = mutableStateOf("")
    var pwdCredentials = mutableStateOf("")
    var cnfPwdCredentials = mutableStateOf("")
    val isRegisterBtnEnabled = mutableStateOf(true)
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

   private fun setRegisterBtnState(enabled: Boolean) {
        isRegisterBtnEnabled.value = enabled
    }

    fun onRegisterClick(navHost: NavHostController?, sharedViewModel: SharedViewModel) {
        this.navHostController = navHost!!
        this.sharedViewModel = sharedViewModel

        viewModelScope.launch {
            try {
                val userEntity = UserManagementEntity(
                    userId = userCredentials.value,
                    password = pwdCredentials.value
                )

                if (isFormValid) {
                    dbRepository.insertUser(userEntity).let {
                        if(sharedViewModel.objPosConfig?.isLoggedIn == true)
                            navHostController.navigateAndClean(AppNavigationItems.DashBoardScreen.route)
                        else
                            navHostController.navigateAndClean(AppNavigationItems.LoginScreen.route)
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


}