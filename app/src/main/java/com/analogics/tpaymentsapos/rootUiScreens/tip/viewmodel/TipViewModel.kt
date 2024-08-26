package com.analogics.tpaymentsapos.rootUiScreens.tip.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.navigation.NavHostController
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.TransactionState
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.formatAmount
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.getFormattedDateTime

class TipViewModel : ViewModel() {
    var amount by mutableStateOf("")
        private set

    var formattedAmount by mutableStateOf("0.00")
        private set

    val transactionDateTime: String = getFormattedDateTime()

    val isRefund: Boolean = TransactionState.isRefund
    val isVoid: Boolean = TransactionState.isVoid
    val isPreauth: Boolean = TransactionState.isPreauth
    val isAuthcap: Boolean = TransactionState.isAuthcap

    fun onTipChange(newValue: String) {
        if (newValue.all { it.isDigit() || it == '.' }) {
            amount = newValue
            formattedAmount = formatAmount(newValue)
        }
    }

    fun onConfirm(navHostController: NavHostController) {
        navHostController.navigate(AppNavigationItems.ConfirmationScreen.createRoute(formattedAmount))
    }

    fun onCancel(navHostController: NavHostController) {
        navHostController.navigate(AppNavigationItems.TrainingScreen.route)
    }
}
