package com.eazypaytech.posafrica.rootUiScreens.tax.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.navigation.NavHostController
import com.eazypaytech.paymentservicecore.constants.AppConstants
import com.eazypaytech.posafrica.navigation.AppNavigationItems
import com.eazypaytech.posafrica.rootUiScreens.activity.SharedViewModel
import com.eazypaytech.posafrica.rootUiScreens.settings.config.PercentButton
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.navigateAndClean
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.toPercentFormat
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.transformToAmountDouble

class TipPercentageViewModel : ViewModel() {

    var tipButton by mutableStateOf(PercentButton.NONE)

    var tipPercent by mutableStateOf("")
        private set

    fun onTipChange(newValue: String) {
        tipPercent = newValue
    }

    fun onConfirm(navHostController: NavHostController, sharedViewModel: SharedViewModel) {
        navHostController.popBackStack()
        when(tipButton)
        {
            PercentButton.PERCENT1 -> sharedViewModel.objPosConfig?.apply { tipPercent1 = transformToAmountDouble(tipPercent, decimalPlaces = 0) }?.saveToPrefs()
            PercentButton.PERCENT2 -> sharedViewModel.objPosConfig?.apply { tipPercent2 = transformToAmountDouble(tipPercent, decimalPlaces = 0) }?.saveToPrefs()
            PercentButton.PERCENT3 -> sharedViewModel.objPosConfig?.apply { tipPercent3 = transformToAmountDouble(tipPercent, decimalPlaces = 0) }?.saveToPrefs()
            else -> 0.00
        }
    }

    fun onCancel(navHostController: NavHostController) {
        navHostController.navigateAndClean(AppNavigationItems.DashBoardScreen.route)
    }

    fun onLoad(navHostController: NavHostController, sharedViewModel: SharedViewModel)
    {
        navHostController.previousBackStackEntry?.savedStateHandle?.get<Int>(AppConstants.NAV_KEY_TIP_PERCENT_INDEX)?.let {
            tipButton = when(it) {
                PercentButton.PERCENT1.value -> PercentButton.PERCENT1
                PercentButton.PERCENT2.value -> PercentButton.PERCENT2
                PercentButton.PERCENT3.value -> PercentButton.PERCENT3
                else -> PercentButton.NONE
            }

            tipPercent = when(it)
            {
                PercentButton.PERCENT1.value -> sharedViewModel.objPosConfig?.tipPercent1.toPercentFormat(decimalPlaces = 0)
                PercentButton.PERCENT2.value -> sharedViewModel.objPosConfig?.tipPercent2.toPercentFormat(decimalPlaces = 0)
                PercentButton.PERCENT3.value -> sharedViewModel.objPosConfig?.tipPercent3.toPercentFormat(decimalPlaces = 0)
                else -> 0.00.toPercentFormat(decimalPlaces = 0)
            }

            navHostController.previousBackStackEntry?.savedStateHandle?.remove<Int>(AppConstants.NAV_KEY_TIP_PERCENT_INDEX)
        }
    }
}
