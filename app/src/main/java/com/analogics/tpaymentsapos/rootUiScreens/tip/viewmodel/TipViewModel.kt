package com.analogics.tpaymentsapos.rootUiScreens.tip.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.navigation.NavHostController
import com.analogics.paymentservicecore.constants.AppConstants
import com.analogics.paymentservicecore.models.TxnInfo
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.formatAmount
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.formatAmountToDouble

class TipViewModel : ViewModel() {
    var tipamount by mutableStateOf("")
        private set

    fun onTipChange(newValue: String) {
        tipamount = newValue
    }

    fun onConfirm(navHostController: NavHostController) {
        navHostController.previousBackStackEntry?.savedStateHandle?.set(AppConstants.KEY_CUSTOM_TIP_AMOUNT,formatAmountToDouble(tipamount))
        navHostController.popBackStack()
    }

    fun onCancel(navHostController: NavHostController) {
        navHostController.popBackStack()
    }
}
