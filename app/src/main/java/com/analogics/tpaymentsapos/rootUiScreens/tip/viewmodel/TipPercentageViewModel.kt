package com.analogics.tpaymentsapos.rootUiScreens.tax.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.navigation.NavHostController
import com.analogics.paymentservicecore.constants.AppConstants
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import com.analogics.tpaymentsapos.rootUiScreens.activity.SharedViewModel
import com.analogics.tpaymentsapos.rootUiScreens.settings.config.TipPercentage
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.toAmountFormat
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.toPercentFormat
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.transformToAmountDouble

class TipPercentageViewModel : ViewModel() {

    var tipOption by mutableStateOf(TipPercentage.OPTION1)

    var tipPercent by mutableStateOf("")
        private set

    fun onTipChange(newValue: String) {
        tipPercent = newValue
    }

    fun onConfirm(navHostController: NavHostController, sharedViewModel: SharedViewModel) {
        navHostController.popBackStack()
        when(tipOption)
        {
            TipPercentage.OPTION1 -> sharedViewModel.objPosConfig?.apply { tipPercent1 = transformToAmountDouble(tipPercent, decimalPlaces = 0) }?.saveToPrefs()
            TipPercentage.OPTION2 -> sharedViewModel.objPosConfig?.apply { tipPercent2 = transformToAmountDouble(tipPercent, decimalPlaces = 0) }?.saveToPrefs()
            TipPercentage.OPTION3 -> sharedViewModel.objPosConfig?.apply { tipPercent3 = transformToAmountDouble(tipPercent, decimalPlaces = 0) }?.saveToPrefs()
        }
    }

    fun onCancel(navHostController: NavHostController) {
        navHostController.navigate(AppNavigationItems.TrainingScreen.route)
    }

    fun onLoad(navHostController: NavHostController, sharedViewModel: SharedViewModel)
    {
        navHostController.previousBackStackEntry?.savedStateHandle?.get<Int>(AppConstants.NAV_KEY_TIP_PERCENT_INDEX)?.let {
            tipOption = when(it) {
                TipPercentage.OPTION1.value -> TipPercentage.OPTION1
                TipPercentage.OPTION2.value -> TipPercentage.OPTION2
                else -> TipPercentage.OPTION3
            }

            tipPercent = when(it)
            {
                TipPercentage.OPTION1.value -> sharedViewModel.objPosConfig?.tipPercent1.toPercentFormat(decimalPlaces = 0)
                TipPercentage.OPTION2.value -> sharedViewModel.objPosConfig?.tipPercent2.toPercentFormat(decimalPlaces = 0)
                else -> sharedViewModel.objPosConfig?.tipPercent3.toPercentFormat(decimalPlaces = 0)
            }

            navHostController.previousBackStackEntry?.savedStateHandle?.remove<Int>(AppConstants.NAV_KEY_TIP_PERCENT_INDEX)
        }
    }
}
