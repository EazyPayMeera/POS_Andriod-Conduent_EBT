package com.analogics.tpaymentsapos.rootUiScreens.pin.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import kotlinx.coroutines.launch

class PinViewModel : ViewModel() {
    var invoiceno by mutableStateOf("")

    fun onPinChange(newPin: String) {
        invoiceno = newPin
    }

    fun onDoneAction(navHostController: NavHostController) {
        viewModelScope.launch {
            navHostController.navigate(AppNavigationItems.PleaseWaitScreen.route)
        }
    }

    fun onCancelAction(navHostController: NavHostController) {
        viewModelScope.launch {
            navHostController.navigate(AppNavigationItems.TrainingScreen.route)
        }
    }
}
