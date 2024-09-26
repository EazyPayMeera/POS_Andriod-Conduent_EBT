package com.analogics.tpaymentsapos.rootUiScreens.tax.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.navigation.NavHostController
import com.analogics.tpaymentsapos.navigation.AppNavigationItems

var updated_tax: Double = 0.00

class TaxPercentageViewModel : ViewModel() {
    var taxPercent by mutableStateOf("")
        private set

    fun setTaxAmount(tip: Double) {
        updated_tax = tip // Update the global variable as well
    }

    fun onTaxChange(newValue: String) {
        taxPercent = newValue
    }

    fun onConfirm(navHostController: NavHostController) {
        navHostController.navigate(AppNavigationItems.ConfigurationScreen.route)
    }

    fun onCancel(navHostController: NavHostController) {
        navHostController.navigate(AppNavigationItems.TrainingScreen.route)
    }
}
