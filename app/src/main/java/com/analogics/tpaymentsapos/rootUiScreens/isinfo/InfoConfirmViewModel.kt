// InfoConfirmViewModel.kt
package com.analogics.tpaymentsapos.rootUiScreens.isinfo

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.navigation.NavHostController
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.formatAmount
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.getFormattedDateTime

class InfoConfirmViewModel : ViewModel() {
    var rawInput by mutableStateOf("")
        private set

    var formattedAmount by mutableStateOf("0.00")
        private set

    val transactionDateTime: String = getFormattedDateTime()


    fun onAmountChange(newValue: String) {
        if (newValue.all { it.isDigit() || it == '.' }) {
            rawInput = newValue
            formattedAmount = formatAmount(newValue)
        }
    }

    fun onConfirm(navHostController: NavHostController) {
        navHostController.navigate(AppNavigationItems.AmountScreen.route)
    }

    fun onCancel(navHostController: NavHostController) {
        navHostController.navigate(AppNavigationItems.TrainingScreen.route)
    }
}
