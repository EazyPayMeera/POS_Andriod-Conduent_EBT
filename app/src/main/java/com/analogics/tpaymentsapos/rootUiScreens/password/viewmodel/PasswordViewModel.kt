package com.analogics.tpaymentsapos.rootUiScreens.password.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import androidx.navigation.NavHostController
import com.analogics.tpaymentsapos.navigation.AppNavigationItems

class PasswordViewModel : ViewModel() {
    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password

    fun updatePassword(newValue: String) {
        _password.value = newValue
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
