package com.analogics.tpaymentsapos.rootUiScreens.tax.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.navigation.NavHostController
import com.analogics.tpaymentsapos.navigation.AppNavigationItems

class TaxPercentageViewModel : ViewModel() {
    // State for raw input and formatted tax percentage
    var rawInput by mutableStateOf("")
        private set

    var taxpercentage by mutableStateOf("0.00")
        private set

    // Function to handle changes to the raw input
    fun onRawInputChange(newValue: String) {
        if (newValue.all { char -> char.isDigit() || char == '.' }) {
            rawInput = newValue
            taxpercentage = formatAmount(newValue)
        }
    }

    // Function to handle the Done action
    fun onDoneAction(navHostController: NavHostController) {
        navHostController.navigate(AppNavigationItems.ConfirmationScreen.createRoute(taxpercentage))
    }

    // Utility function to format amount (if not already defined elsewhere)
    private fun formatAmount(amount: String): String {
        // Implement the formatting logic or use an existing method
        // Example: "1234" -> "1,234.00"
        return amount
    }
}
