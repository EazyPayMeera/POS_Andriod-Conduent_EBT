package com.analogics.tpaymentsapos.rootUiScreens.tip.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.navigation.NavHostController
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.formatAmount

/*var updated_tip = ""*/
var updated_tip: Double = 0.0

class TipViewModel : ViewModel() {
    var tipamount by mutableStateOf("")
        private set

    var formattedtipAmount by mutableStateOf("0.00")
        private set

    fun setTipAmount(tip: Double) {
        updated_tip = tip // Update the global variable as well
    }

    fun onTipChange(newValue: String) {
        if (newValue.all { it.isDigit() || it == '.' }) {
            tipamount = newValue
            formattedtipAmount = formatAmount(newValue)

            // Convert formatted tip amount back to Double
            val formattedTipAsDouble = formattedtipAmount.toDoubleOrNull() ?: 0.0
            setTipAmount(formattedTipAsDouble)

            // Print the result
            Log.d("TipChange", "Updated tip amount: $formattedTipAsDouble")
            Log.d("Formattedtip", "Formatted tip amount: $formattedTipAsDouble")
        }
    }

/*    fun onTipChange(newValue: String) {
        if (newValue.all { it.isDigit() || it == '.' }) {
            tipamount = newValue
            formattedtipAmount = formatAmount(newValue)
            setTipAmount(formattedtipAmount)

            // Print the result returned from setTipAmount
            Log.d("TipChange", "Updated tip amount: $updated_tip")
            Log.d("Formattedtip", "Formattedtip amount: $formattedtipAmount")

        }
    }*/

    fun onConfirm(navHostController: NavHostController) {
        navHostController.navigate(AppNavigationItems.ConfirmationScreen.createRoute(tipamount))
    }

    fun onCancel(navHostController: NavHostController) {
        navHostController.navigate(AppNavigationItems.TrainingScreen.route)
    }
}
