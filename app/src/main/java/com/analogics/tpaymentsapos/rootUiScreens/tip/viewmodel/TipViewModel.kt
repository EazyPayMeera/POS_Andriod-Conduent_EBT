package com.analogics.tpaymentsapos.rootUiScreens.tip.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.navigation.NavHostController
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.formatAmount

var updated_tip = ""

class TipViewModel : ViewModel() {
    var tipamount by mutableStateOf("")
        private set

    var formattedtipAmount by mutableStateOf("0.00")
        private set

    // Function to set the total amount and update the global variable
    fun setTipAmount(tip: String) {
        updated_tip = tip // Update the global variable as well
    }

    fun onTipChange(newValue: String) {
        if (newValue.all { it.isDigit() || it == '.' }) {
            tipamount = newValue
            formattedtipAmount = formatAmount(newValue)
            setTipAmount(formattedtipAmount)

            // Print the result returned from setTipAmount
            Log.d("TipChange", "Updated tip amount: $updated_tip")
            Log.d("Formattedtip", "Formattedtip amount: $formattedtipAmount")

        }
    }

    fun onConfirm(navHostController: NavHostController) {
        navHostController.navigate(AppNavigationItems.ConfirmationScreen.createRoute(tipamount))
    }

    fun onCancel(navHostController: NavHostController) {
        navHostController.navigate(AppNavigationItems.TrainingScreen.route)
    }
}
