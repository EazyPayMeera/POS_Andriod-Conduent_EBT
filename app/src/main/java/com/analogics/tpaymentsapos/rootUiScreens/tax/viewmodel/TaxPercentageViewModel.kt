package com.analogics.tpaymentsapos.rootUiScreens.tax.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.navigation.NavHostController
import com.analogics.paymentservicecore.constants.AppConstants
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import com.analogics.tpaymentsapos.rootUiScreens.activity.SharedViewModel
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.navigateAndClean
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.toPercentFormat
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.transformToAmountDouble

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
