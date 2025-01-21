package com.eazypaytech.posafrica.rootUiScreens.serviceCharge.viewmodel

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

class ServiceChargePercentageViewModel : ViewModel() {

    var serviceChargeButton by mutableStateOf(PercentButton.NONE)

    var serviceChargePercent by mutableStateOf("")
        private set

    fun onServiceChargeChange(newValue: String) {
        serviceChargePercent = newValue
    }

    fun onConfirm(navHostController: NavHostController, sharedViewModel: SharedViewModel) {
        navHostController.popBackStack()
        when(serviceChargeButton)
        {
            PercentButton.PERCENT1 -> sharedViewModel.objPosConfig?.apply { serviceChargePercent1 = transformToAmountDouble(serviceChargePercent, decimalPlaces = 2) }?.saveToPrefs()
            PercentButton.PERCENT2 -> sharedViewModel.objPosConfig?.apply { serviceChargePercent2 = transformToAmountDouble(serviceChargePercent, decimalPlaces = 2) }?.saveToPrefs()
            PercentButton.PERCENT3 -> sharedViewModel.objPosConfig?.apply { serviceChargePercent3 = transformToAmountDouble(serviceChargePercent, decimalPlaces = 2) }?.saveToPrefs()
            else -> 0.00
        }
    }

    fun onCancel(navHostController: NavHostController) {
        navHostController.navigateAndClean(AppNavigationItems.DashBoardScreen.route)
    }

    fun onLoad(navHostController: NavHostController, sharedViewModel: SharedViewModel)
    {
        navHostController.previousBackStackEntry?.savedStateHandle?.get<Int>(AppConstants.NAV_KEY_SERVICE_CHARGE_PERCENT_INDEX)?.let {
            serviceChargeButton = when(it) {
                PercentButton.PERCENT1.value -> PercentButton.PERCENT1
                PercentButton.PERCENT2.value -> PercentButton.PERCENT2
                PercentButton.PERCENT3.value -> PercentButton.PERCENT3
                else -> PercentButton.NONE
            }

            serviceChargePercent = when(it)
            {
                PercentButton.PERCENT1.value -> sharedViewModel.objPosConfig?.serviceChargePercent1.toPercentFormat(decimalPlaces = 2)
                PercentButton.PERCENT2.value -> sharedViewModel.objPosConfig?.serviceChargePercent2.toPercentFormat(decimalPlaces = 2)
                PercentButton.PERCENT3.value -> sharedViewModel.objPosConfig?.serviceChargePercent3.toPercentFormat(decimalPlaces = 2)
                else -> 0.00.toPercentFormat(decimalPlaces = 2)
            }

            navHostController.previousBackStackEntry?.savedStateHandle?.remove<Int>(AppConstants.NAV_KEY_SERVICE_CHARGE_PERCENT_INDEX)
        }
    }
}
