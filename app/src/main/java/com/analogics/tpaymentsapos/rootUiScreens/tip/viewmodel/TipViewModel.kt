package com.analogics.tpaymentsapos.rootUiScreens.tip.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.navigation.NavHostController
import com.analogics.paymentservicecore.constants.AppConstants
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.transformToAmountDouble

class TipViewModel : ViewModel() {
    var tipAmount by mutableStateOf("")
        private set

    fun onTipChange(newValue: String) {
        tipAmount = newValue
    }

    fun onConfirm(navHostController: NavHostController) {
        navHostController.previousBackStackEntry?.savedStateHandle?.set(AppConstants.NAV_KEY_CUSTOM_TIP_AMOUNT,transformToAmountDouble(tipAmount))
        navHostController.popBackStack()
    }

    fun onCancel(navHostController: NavHostController) {
        navHostController.popBackStack()
    }
}
