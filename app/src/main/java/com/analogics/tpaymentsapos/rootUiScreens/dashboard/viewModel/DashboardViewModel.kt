package com.analogics.tpaymentsapos.rootUiScreens.dashboard.viewModel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.navigation.NavHostController
import androidx.compose.runtime.State



class DashboardViewModel : ViewModel() {
    private val _selectedButton = mutableStateOf<String?>(null)
    val selectedButton: State<String?> get() = _selectedButton

    fun onButtonClick(text: String, onClick: () -> Unit) {
        _selectedButton.value = text
        onClick()
    }

    fun navigateTo(navHostController: NavHostController, route: String) {
        navHostController.navigate(route)
    }

    fun resetSelection() {
        _selectedButton.value = null
    }
}
