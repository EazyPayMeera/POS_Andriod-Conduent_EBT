package com.analogics.tpaymentsapos.rootUiScreens.tax.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.navigation.NavHostController
import com.analogics.paymentservicecore.constants.AppConstants
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import com.analogics.tpaymentsapos.rootUiScreens.activity.SharedViewModel
import com.analogics.tpaymentsapos.rootUiScreens.settings.config.TipButton
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.toPercentFormat
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.transformToAmountDouble

class TipPercentageViewModel : ViewModel() {

    var tipButton by mutableStateOf(TipButton.NONE)

    var tipPercent by mutableStateOf("")
        private set

    fun onTipChange(newValue: String) {
        tipPercent = newValue
    }

    fun onConfirm(navHostController: NavHostController, sharedViewModel: SharedViewModel) {
        navHostController.popBackStack()
        when(tipButton)
        {
            TipButton.PERCENT1 -> sharedViewModel.objPosConfig?.apply { tipPercent1 = transformToAmountDouble(tipPercent, decimalPlaces = 0) }?.saveToPrefs()
            TipButton.PERCENT2 -> sharedViewModel.objPosConfig?.apply { tipPercent2 = transformToAmountDouble(tipPercent, decimalPlaces = 0) }?.saveToPrefs()
            TipButton.PERCENT3 -> sharedViewModel.objPosConfig?.apply { tipPercent3 = transformToAmountDouble(tipPercent, decimalPlaces = 0) }?.saveToPrefs()
            else -> 0.00
        }
    }

    fun onCancel(navHostController: NavHostController) {
        navHostController.navigate(AppNavigationItems.TrainingScreen.route)
    }

    fun onLoad(navHostController: NavHostController, sharedViewModel: SharedViewModel)
    {
        navHostController.previousBackStackEntry?.savedStateHandle?.get<Int>(AppConstants.NAV_KEY_TIP_PERCENT_INDEX)?.let {
            tipButton = when(it) {
                TipButton.PERCENT1.value -> TipButton.PERCENT1
                TipButton.PERCENT2.value -> TipButton.PERCENT2
                TipButton.PERCENT3.value -> TipButton.PERCENT3
                else -> TipButton.NONE
            }

            tipPercent = when(it)
            {
                TipButton.PERCENT1.value -> sharedViewModel.objPosConfig?.tipPercent1.toPercentFormat(decimalPlaces = 0)
                TipButton.PERCENT2.value -> sharedViewModel.objPosConfig?.tipPercent2.toPercentFormat(decimalPlaces = 0)
                TipButton.PERCENT3.value -> sharedViewModel.objPosConfig?.tipPercent3.toPercentFormat(decimalPlaces = 0)
                else -> 0.00.toPercentFormat(decimalPlaces = 0)
            }

            navHostController.previousBackStackEntry?.savedStateHandle?.remove<Int>(AppConstants.NAV_KEY_TIP_PERCENT_INDEX)
        }
    }
}
