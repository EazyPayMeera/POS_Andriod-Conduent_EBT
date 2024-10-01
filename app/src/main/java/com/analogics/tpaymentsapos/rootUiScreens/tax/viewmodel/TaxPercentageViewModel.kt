package com.analogics.tpaymentsapos.rootUiScreens.tax.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.navigation.NavHostController
import com.analogics.paymentservicecore.constants.AppConstants
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import com.analogics.tpaymentsapos.rootUiScreens.activity.SharedViewModel
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.transformToAmountDouble

class TaxPercentageViewModel : ViewModel() {
    enum class TaxType{
        SGST,CGST
    }

    var taxType by mutableStateOf(TaxType.SGST)

    var taxPercent by mutableStateOf("")
        private set

    fun onTaxChange(newValue: String) {
        taxPercent = newValue
    }

    fun onConfirm(navHostController: NavHostController, sharedViewModel: SharedViewModel) {
        navHostController.popBackStack()
        when(taxType)
        {
            TaxType.SGST -> sharedViewModel.objPosConfig?.apply { SGSTPercent = transformToAmountDouble(taxPercent) }?.saveToPrefs()
            TaxType.CGST -> sharedViewModel.objPosConfig?.apply { CGSTPercent = transformToAmountDouble(taxPercent) }?.saveToPrefs()
        }

    }

    fun onCancel(navHostController: NavHostController) {
        navHostController.navigate(AppNavigationItems.TrainingScreen.route)
    }

    fun onLoad(navHostController: NavHostController)
    {
        navHostController.previousBackStackEntry?.savedStateHandle?.get<String>(AppConstants.NAV_KEY_TAX_TYPE)?.let {
            if(it.equals(AppConstants.NAV_VAL_TAX_TYPE_SGST,true))
                taxType = TaxType.SGST
            else
                taxType = TaxType.CGST

            navHostController.previousBackStackEntry?.savedStateHandle?.remove<String>(AppConstants.NAV_KEY_TAX_TYPE)
        }
    }
}
