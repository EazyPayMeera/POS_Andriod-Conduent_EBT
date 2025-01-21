package com.eazypaytech.posafrica.rootUiScreens.serviceCharge.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.navigation.NavHostController
import com.eazypaytech.paymentservicecore.constants.AppConstants
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.transformToAmountDouble

class ServiceChargeViewModel : ViewModel() {
    var serviceCharge by mutableStateOf("")
        private set

    fun onTipChange(newValue: String) {
        serviceCharge = newValue
    }

    fun onConfirm(navHostController: NavHostController) {
        navHostController.previousBackStackEntry?.savedStateHandle?.set(AppConstants.NAV_KEY_CUSTOM_SERVICE_CHARGE,transformToAmountDouble(serviceCharge))
        navHostController.popBackStack()
    }

    fun onCancel(navHostController: NavHostController) {
        navHostController.popBackStack()
    }
}
