package com.analogics.tpaymentsapos.rootUiScreens.amount.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.navigation.NavHostController
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.TransactionState
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.formatAmount
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.getFormattedDateTime

var Trans_Amt = ""

class AmountViewModel : ViewModel() {

    var rawInput by mutableStateOf("")
        private set

    var formattedAmount by mutableStateOf("0.00")
        private set

    val transactionDateTime: String = getFormattedDateTime()

    val isRefund: Boolean = TransactionState.isRefund
    val isVoid: Boolean = TransactionState.isVoid
    val isPreauth: Boolean = TransactionState.isPreauth
    val isAuthcap: Boolean = TransactionState.isAuthcap


    // Function to set the total amount and update the global variable
    fun setTransAmount(transAmt: String) {
        Trans_Amt = transAmt // Update the global variable as well
    }

    fun onAmountChange(newValue: String) {
        if (newValue.all { it.isDigit() || it == '.' }) {
            rawInput = newValue
            formattedAmount = formatAmount(newValue)
            setTransAmount(formattedAmount)
        }
    }



    fun onConfirm(navHostController: NavHostController) {
        when {
            isRefund || isPreauth -> {
                navHostController.navigate(AppNavigationItems.CardScreen.createRoute(formattedAmount))
            }
            isVoid || isAuthcap -> {
                navHostController.navigate(AppNavigationItems.PleaseWaitScreen.route)
            }
            else -> {
                navHostController.navigate(AppNavigationItems.ConfirmationScreen.createRoute(formattedAmount))
            }
        }
    }

    fun onCancel(navHostController: NavHostController) {
        navHostController.navigate(AppNavigationItems.TrainingScreen.route)
    }
}