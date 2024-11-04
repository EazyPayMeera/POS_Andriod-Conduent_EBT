package com.analogics.tpaymentsapos.rootUiScreens.changepassword.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.analogics.paymentservicecore.constants.AppConstants
import com.analogics.securityframework.database.dbRepository.TxnDBRepository
import com.analogics.securityframework.database.entity.UserManagementEntity
import com.analogics.tpaymentsapos.rootUiScreens.activity.SharedViewModel
import com.analogics.tpaymentsapos.rootUiScreens.dialogs.CustomDialogBuilder
import com.analogics.tpaymentsapos.R
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.navigateAndClean
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.text.isNotBlank

@HiltViewModel
class ChangePasswordViewModel @Inject constructor(
    private var dbRepository: TxnDBRepository
) :
    ViewModel()
{
    var currentPassword = mutableStateOf("")
    var newPassword = mutableStateOf("")
    var confirmPassword = mutableStateOf("")
    lateinit var navHostController: NavHostController
    var sharedViewModel: SharedViewModel? = null
    val isFormValid: Boolean
        get() = currentPassword.value.isNotBlank() && newPassword.value.isNotBlank() && newPassword.value.length>=AppConstants.MIN_LENGTH_PASSWORD && confirmPassword.value.isNotBlank() && (newPassword.value == confirmPassword.value)

    fun onCurrentChange(password: String) {
        currentPassword.value = password
    }

    fun onNewChange(password: String) {
        newPassword.value = password
    }

    fun onConfirmChange(password: String) {
        confirmPassword.value = password
    }

    fun clearFields() {
        currentPassword.value = ""
        newPassword.value = ""
        confirmPassword.value = ""
    }

    fun onChangePasswordClick(navHost: NavHostController?, sharedViewModel: SharedViewModel) {
        this.navHostController = navHost!!
        this.sharedViewModel = sharedViewModel

        viewModelScope.launch {
            try {
                if (isFormValid) {
                    dbRepository.getUserDetails(sharedViewModel.objPosConfig?.loginId?:"")?.let {
                        if(it.password != currentPassword.value)
                            CustomDialogBuilder.composeAlertDialog(title = navHost.context.resources.getString(R.string.change_password), subtitle = navHost.context.resources.getString(R.string.invalid_current_password))
                        else {
                            it.password = newPassword.value
                            dbRepository.updateUser(it).let {
                                CustomDialogBuilder.composeAlertDialog(title = navHost.context.resources.getString(R.string.change_password), subtitle = navHost.context.resources.getString(R.string.password_change_success))
                                navHostController.popBackStack()
                            }
                        }
                    }?:let {
                        CustomDialogBuilder.composeAlertDialog(title = navHost.context.resources.getString(R.string.change_password), subtitle = navHost.context.resources.getString(R.string.password_change_failed))
                    }
                }
                else{
                    if(currentPassword.value.isBlank())
                        CustomDialogBuilder.composeAlertDialog(title = navHost.context.resources.getString(R.string.change_password), subtitle = navHost.context.resources.getString(R.string.current_password_empty))
                    else if(newPassword.value != confirmPassword.value)
                        CustomDialogBuilder.composeAlertDialog(title = navHost.context.resources.getString(R.string.change_password), subtitle = navHost.context.resources.getString(R.string.clerk_password_mismatch))
                    else
                        CustomDialogBuilder.composeAlertDialog(title = navHost.context.resources.getString(R.string.change_password), subtitle = navHost.context.resources.getString(R.string.clerk_min_password_length))
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}