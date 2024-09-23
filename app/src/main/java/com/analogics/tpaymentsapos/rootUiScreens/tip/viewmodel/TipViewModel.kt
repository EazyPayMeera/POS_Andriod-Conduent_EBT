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

/*    var formattedtipAmount by mutableStateOf("0.00")
        private set*/


    fun setTipAmount(tip: Double) {
        updated_tip = tip // Update the global variable as well
        Log.d("Formattedtip", "Set Tip: $updated_tip")
    }


    fun onTipChange(newValue: String) {
        tipamount = formatAmount(newValue, withSymbol = false)
        Log.d("TipChange", "tipamount: $tipamount")
        Log.d("TipChange", "Before conversion: '${tipamount.trim()}'")
        val formattedTipAsDouble = tipamount.toDoubleOrNull() ?: 0.00
        setTipAmount(formattedTipAsDouble)
        Log.d("TipChange", "Updated tip amount: $formattedTipAsDouble")
    }

    fun onConfirm(navHostController: NavHostController) {
        navHostController.navigate(AppNavigationItems.ConfirmationScreen.createRoute(tipamount))
    }

    fun onCancel(navHostController: NavHostController) {
        navHostController.navigate(AppNavigationItems.TrainingScreen.route)
    }
}
