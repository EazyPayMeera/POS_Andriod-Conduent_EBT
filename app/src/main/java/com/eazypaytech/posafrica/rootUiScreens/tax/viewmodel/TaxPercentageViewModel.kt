package com.eazypaytech.posafrica.rootUiScreens.tax.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.navigation.NavHostController
import com.eazypaytech.paymentservicecore.constants.AppConstants
import com.eazypaytech.posafrica.navigation.AppNavigationItems
import com.eazypaytech.posafrica.rootUiScreens.activity.SharedViewModel
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.navigateAndClean
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.toPercentFormat
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.transformToAmountDouble

class TaxPercentageViewModel : ViewModel() {
    enum class TaxType{
        VAT
    }

    var taxType by mutableStateOf(TaxType.VAT)

    var taxPercent by mutableStateOf("")
        private set

    fun onTaxChange(newValue: String) {
        taxPercent = newValue
    }

    fun onConfirm(navHostController: NavHostController, sharedViewModel: SharedViewModel) {
        navHostController.popBackStack()
        when(taxType)
        {
            TaxType.VAT -> sharedViewModel.objPosConfig?.apply { vatPercent = transformToAmountDouble(taxPercent) }?.saveToPrefs()
        }
    }

    fun onCancel(navHostController: NavHostController) {
        navHostController.navigateAndClean(AppNavigationItems.DashBoardScreen.route)
    }

    fun onLoad(navHostController: NavHostController, sharedViewModel: SharedViewModel)
    {
        navHostController.previousBackStackEntry?.savedStateHandle?.get<String>(AppConstants.NAV_KEY_TAX_TYPE)?.let {
                taxType = TaxType.VAT
                taxPercent = sharedViewModel.objPosConfig?.vatPercent.toPercentFormat()
            navHostController.previousBackStackEntry?.savedStateHandle?.remove<String>(AppConstants.NAV_KEY_TAX_TYPE)
        }
    }
}
