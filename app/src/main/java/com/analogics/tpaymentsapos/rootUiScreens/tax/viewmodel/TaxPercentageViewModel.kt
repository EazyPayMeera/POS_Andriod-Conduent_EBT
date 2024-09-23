package com.analogics.tpaymentsapos.rootUiScreens.tax.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.navigation.NavHostController
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.formatAmount
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.getFormattedDateTime

var updated_tax: Double = 0.00

class TaxPercentageViewModel : ViewModel() {
    var transAmount by mutableStateOf("")
        private set

    val transactionDateTime: String = getFormattedDateTime()

    fun setTaxAmount(tip: Double) {
        updated_tax = tip // Update the global variable as well
    }

    fun onAmountChange(newValue: String) {
        transAmount = formatAmount(newValue, withSymbol = false)
        val formattedTipAsDouble = transAmount.toDoubleOrNull() ?: 0.00
        setTaxAmount(formattedTipAsDouble)
    }

    fun onConfirm(navHostController: NavHostController) {
        navHostController.navigate(AppNavigationItems.ConfigurationScreen.route)
    }

    fun onCancel(navHostController: NavHostController) {
        navHostController.navigate(AppNavigationItems.TrainingScreen.route)
    }
}
