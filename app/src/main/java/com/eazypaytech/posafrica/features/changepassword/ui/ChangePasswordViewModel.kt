package com.eazypaytech.posafrica.features.changepassword.ui

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.eazypaytech.paymentservicecore.constants.AppConstants
import com.eazypaytech.posafrica.R
import com.eazypaytech.posafrica.features.activity.ui.SharedViewModel
import com.eazypaytech.posafrica.features.dialogs.ui.CustomDialogBuilder
import com.eazypaytech.posafrica.core.utils.generateMasterPassword
import com.analogics.securityframework.data.repository.TxnDBRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

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
        get() = currentPassword.value.isNotBlank() && newPassword.value.isNotBlank() && newPassword.value.length>= AppConstants.MIN_LENGTH_PASSWORD && confirmPassword.value.isNotBlank() && (newPassword.value == confirmPassword.value)

    fun onCurrentChange(password: String) {
        currentPassword.value = password
    }

    fun onNewChange(password: String) {
        newPassword.value = password
    }

    fun onConfirmChange(password: String) {
        confirmPassword.value = password
    }

    fun onChangePasswordClick(navHost: NavHostController?, sharedViewModel: SharedViewModel) {
        this.navHostController = navHost!!
        this.sharedViewModel = sharedViewModel

        viewModelScope.launch {
            try {
                if (isFormValid) {
                    dbRepository.getUserDetails(sharedViewModel.objPosConfig?.loginId?:"")?.let {
                        if(it.password != currentPassword.value && currentPassword.value != generateMasterPassword(
                                sharedViewModel.objPosConfig?.loginId,
                                sharedViewModel
                            )
                        )
                            CustomDialogBuilder.Companion.composeAlertDialog(title = navHost.context.resources.getString(
                                R.string.change_password), subtitle = navHost.context.resources.getString(
                                R.string.invalid_current_password))
                        else {
                            it.password = newPassword.value
                            dbRepository.updateUser(it).let {
                                CustomDialogBuilder.Companion.composeAlertDialog(title = navHost.context.resources.getString(
                                    R.string.change_password), subtitle = navHost.context.resources.getString(
                                    R.string.password_change_success))
                                navHostController.popBackStack()
                            }
                        }
                    }?:let {
                        CustomDialogBuilder.Companion.composeAlertDialog(title = navHost.context.resources.getString(
                            R.string.change_password), subtitle = navHost.context.resources.getString(
                            R.string.password_change_failed))
                    }
                }
                else{
                    if(currentPassword.value.isBlank())
                        CustomDialogBuilder.Companion.composeAlertDialog(title = navHost.context.resources.getString(
                            R.string.change_password), subtitle = navHost.context.resources.getString(
                            R.string.current_password_empty))
                    else if(newPassword.value != confirmPassword.value)
                        CustomDialogBuilder.Companion.composeAlertDialog(title = navHost.context.resources.getString(
                            R.string.change_password), subtitle = navHost.context.resources.getString(
                            R.string.clerk_password_mismatch))
                    else
                        CustomDialogBuilder.Companion.composeAlertDialog(title = navHost.context.resources.getString(
                            R.string.change_password), subtitle = navHost.context.resources.getString(
                            R.string.clerk_min_password_length))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}