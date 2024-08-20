package com.analogics.tpaymentsapos.rootUiScreens.login

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.TransactionState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class InvoiceViewModel : ViewModel() {
    private val _invoiceno = MutableStateFlow("")
    val invoiceno: StateFlow<String> = _invoiceno

    val isRefund: Boolean = TransactionState.isRefund
    val isVoid: Boolean = TransactionState.isVoid
    val isPreauth: Boolean = TransactionState.isPreauth
    val isAuthcap: Boolean = TransactionState.isAuthcap

    fun updateInvoiceNo(newValue: String) {
        _invoiceno.value = newValue
    }

    fun navigateToAmountScreen(navHostController: NavHostController) {
        viewModelScope.launch {
            if(isAuthcap)
            {
                navHostController.navigate(AppNavigationItems.InfoConfirmScreen.route)
            }
            else {
                navHostController.navigate(AppNavigationItems.AmountScreen.route)
            }
        }
    }

    fun navigateToTrainingScreen(navHostController: NavHostController) {
        viewModelScope.launch {
            navHostController.navigate(AppNavigationItems.TrainingScreen.route)
        }
    }
}
