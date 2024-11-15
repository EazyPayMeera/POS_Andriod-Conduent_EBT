package com.analogics.tpaymentsapos.rootUiScreens.password.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.analogics.securityframework.database.dbRepository.TxnDBRepository
import com.analogics.tpaymentsapos.R
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import com.analogics.tpaymentsapos.rootUiScreens.activity.SharedViewModel
import com.analogics.tpaymentsapos.rootUiScreens.dialogs.CustomDialogBuilder
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.navigateAndClean
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PasswordViewModel @Inject constructor(private val dbRepository: TxnDBRepository) : ViewModel() {
    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password

    private val _savedPassword = MutableStateFlow("")
    val savedPassword: StateFlow<String> = _savedPassword

    fun updatePassword(newValue: String):String {
        _password.value = newValue
        return _password.value
    }

    fun navigateToInvoiceScreen(navHostController: NavHostController) {
        viewModelScope.launch {
            navHostController.navigate(AppNavigationItems.InvoiceScreen.route)
        }
    }

    fun navigateToAmountScreen(navHostController: NavHostController) {
        viewModelScope.launch {
            navHostController.navigate(AppNavigationItems.AmountScreen.route)
        }
    }

    fun navigateToTrainingScreen(navHostController: NavHostController) {
        viewModelScope.launch {
            navHostController.navigateAndClean(AppNavigationItems.DashBoardScreen.route)
        }
    }

    fun checkPassword(sharedViewModel: SharedViewModel, context: Context, loggedUser:String, enteredPassword:String,navHostController: NavHostController)
    {
        viewModelScope.launch {
            try {
                val Password = dbRepository.fetchPassword(loggedUser)
                if(Password != enteredPassword)
                {
                    CustomDialogBuilder.composeAlertDialog(
                        title = context.resources.getString(
                            R.string.default_alert_title_error
                        ),
                        subtitle = context.resources.getString(R.string.default_alert_subtitle_wrong_password)
                    )
                    _password.value = ""
                }
                else
                {
                    navigateToInvoiceScreen(navHostController)
                }
            } catch (e: Exception) {
                Log.e("FetchStartDates", "Error fetching start dates: ${e.message}")
            }
        }
    }
}
