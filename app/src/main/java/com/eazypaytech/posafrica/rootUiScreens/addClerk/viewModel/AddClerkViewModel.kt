package com.eazypaytech.posafrica.rootUiScreens.addClerk.viewModel

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.focus.FocusRequester
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.eazypaytech.paymentservicecore.constants.AppConstants
import com.eazypaytech.securityframework.database.dbRepository.TxnDBRepository
import com.eazypaytech.securityframework.database.entity.UserManagementEntity
import com.eazypaytech.posafrica.navigation.AppNavigationItems
import com.eazypaytech.posafrica.rootUiScreens.activity.SharedViewModel
import com.eazypaytech.posafrica.rootUiScreens.dialogs.CustomDialogBuilder
import com.eazypaytech.posafrica.R
import com.eazypaytech.posafrica.rootModel.UserType
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.navigateAndClean
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.text.isNotBlank

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
    val isDoneBtnEnabled = mutableStateOf(false)
    lateinit var navHostController: NavHostController
    var sharedViewModel: SharedViewModel? = null
    val isFormValid: Boolean
        get() = userCredentials.value.isNotBlank() && pwdCredentials.value.isNotBlank() && pwdCredentials.value.length>=AppConstants.MIN_LENGTH_PASSWORD && cnfPwdCredentials.value.isNotBlank() && (pwdCredentials.value == cnfPwdCredentials.value)
    var userType = mutableStateOf(UserType.CLERK)
    val allowClerks = mutableStateOf(false)
    lateinit var focusRequester : FocusRequester

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

    private fun setDoneBtnState(enabled: Boolean) {
        isDoneBtnEnabled.value = enabled
    }

    fun onDoneClick(navHost: NavHostController?, sharedViewModel: SharedViewModel)
    {
        if(sharedViewModel.objPosConfig?.isLoggedIn == true)
            navHost?.navigateAndClean(AppNavigationItems.DashBoardScreen.route)
        else
            navHost?.navigateAndClean(AppNavigationItems.LoginScreen.route)
    }

    fun onRegisterClick(navHost: NavHostController?, sharedViewModel: SharedViewModel) {
        this.navHostController = navHost!!
        this.sharedViewModel = sharedViewModel

        viewModelScope.launch {
            try {
                val userEntity = UserManagementEntity(
                    userId = userCredentials.value,
                    password = pwdCredentials.value,
                    userType = userType.value.toString()
                )

                if (isFormValid) {
                    dbRepository.getUserDetails(userCredentials.value)?.let {
                        CustomDialogBuilder.composeAlertDialog(title = navHost.context.resources.getString(R.string.clerk_register_title), subtitle = navHost.context.resources.getString(R.string.clerk_already_exists))
                    }?:dbRepository.insertUser(userEntity).let {
                        CustomDialogBuilder.composeAlertDialog(title = navHost.context.resources.getString(R.string.clerk_register_title), subtitle = navHost.context.resources.getString(R.string.clerk_registration_success))
                        onLoad(focusRequester)
                    }
                }
                else{
                    if(userCredentials.value.isBlank())
                        CustomDialogBuilder.composeAlertDialog(title = navHost.context.resources.getString(R.string.clerk_register_title), subtitle = navHost.context.resources.getString(R.string.clerk_username_empty))
                    else if(pwdCredentials.value != cnfPwdCredentials.value)
                        CustomDialogBuilder.composeAlertDialog(title = navHost.context.resources.getString(R.string.clerk_register_title), subtitle = navHost.context.resources.getString(R.string.clerk_password_mismatch))
                    else
                        CustomDialogBuilder.composeAlertDialog(title = navHost.context.resources.getString(R.string.clerk_register_title), subtitle = navHost.context.resources.getString(R.string.clerk_min_password_length))
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun onLoad(focusRequester: FocusRequester)
    {
        this.focusRequester = focusRequester
        viewModelScope.launch{
            dbRepository.getUserCount().takeIf { it>0 }?.let {
                userType.value = UserType.CLERK
                allowClerks.value = true
                setDoneBtnState(true)
            }?:let {
                userType.value = UserType.ADMIN
                allowClerks.value = false
                setDoneBtnState(false)
            }
            pwdCredentials.value = ""
            cnfPwdCredentials.value = ""
            userCredentials.value = ""
            focusRequester.requestFocus()
        }
    }
}