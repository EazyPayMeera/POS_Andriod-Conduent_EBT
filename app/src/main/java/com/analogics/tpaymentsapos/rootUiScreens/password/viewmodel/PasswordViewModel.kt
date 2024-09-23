package com.analogics.tpaymentsapos.rootUiScreens.password.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PasswordViewModel : ViewModel() {
    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password

    fun updatePassword(newValue: String):String {
        _password.value = newValue
        return _password.value
    }

    fun navigateToInvoiceScreen(navHostController: NavHostController) {
        viewModelScope.launch {
            navHostController.navigate(AppNavigationItems.InvoiceScreen.route)
        }
    }

    fun navigateToTrainingScreen(navHostController: NavHostController) {
        viewModelScope.launch {
            navHostController.navigate(AppNavigationItems.TrainingScreen.route)
        }
    }
}
