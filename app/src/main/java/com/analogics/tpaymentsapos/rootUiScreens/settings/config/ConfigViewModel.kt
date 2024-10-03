package com.analogics.tpaymentsapos.rootUiScreens.settings.config

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.navigation.NavHostController
import com.analogics.paymentservicecore.constants.AppConstants
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import com.analogics.tpaymentsapos.rootModel.Symbol
import com.analogics.tpaymentsapos.rootUiScreens.activity.SharedViewModel
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.formatAmount
import javax.inject.Inject

class ConfigViewModel @Inject constructor() : ViewModel(){

    var isTrainingMode = mutableStateOf(false)
    var isAutoPrintReport = mutableStateOf(false)
    var isPromptInvoiceNumber = mutableStateOf(false)
    var isAutoPrintMerchant = mutableStateOf(false)
    var isTippingEnabled = mutableStateOf(false)
    var isTaxEnabled = mutableStateOf(false)

    private fun loadPreferences(sharedViewModel: SharedViewModel)
    {
        isTrainingMode.value = sharedViewModel.objPosConfig?.isDemoMode == true
        isAutoPrintReport.value = sharedViewModel.objPosConfig?.isAutoPrintReport == true
        isPromptInvoiceNumber.value = sharedViewModel.objPosConfig?.isPromptInvoiceNo == true
        isAutoPrintMerchant.value = sharedViewModel.objPosConfig?.isAutoPrintMerchant == true
        isTippingEnabled.value = sharedViewModel.objPosConfig?.isTipEnabled == true
        isTaxEnabled.value = sharedViewModel.objPosConfig?.isTaxEnabled == true
    }

    private fun getTipPercent(button: TipButton, sharedViewModel: SharedViewModel) : Double
    {
        return when(button){
            TipButton.PERCENT1 -> sharedViewModel.objPosConfig?.tipPercent1?:0.00
            TipButton.PERCENT2 -> sharedViewModel.objPosConfig?.tipPercent2?:0.00
            TipButton.PERCENT3 -> sharedViewModel.objPosConfig?.tipPercent3?:0.00
            else -> 0.00
        }
    }

    fun getTipPercentLabel(button: TipButton, sharedViewModel: SharedViewModel) : String
    {
        return formatAmount(getTipPercent(button, sharedViewModel), symbol = Symbol(type = Symbol.Type.PERCENT, position = Symbol.Position.END, noSpace = true), decimalPlaces = 0)
    }

    fun onDemoModeChange(value: Boolean, sharedViewModel: SharedViewModel) {
        isTrainingMode.value = value
        sharedViewModel.objPosConfig?.apply { this.isDemoMode = value }?.saveToPrefs()
    }

    fun onAutoPrintReportChange(value: Boolean, sharedViewModel: SharedViewModel) {
        isAutoPrintReport.value = value
        sharedViewModel.objPosConfig?.apply { this.isAutoPrintReport = value }?.saveToPrefs()
    }

    fun onPromptInvoiceNumberChange(value: Boolean, sharedViewModel: SharedViewModel) {
        isPromptInvoiceNumber.value = value
        sharedViewModel.objPosConfig?.apply { this.isPromptInvoiceNo = value }?.saveToPrefs()
    }

    fun onAutoPrintMerchantChange(value: Boolean, sharedViewModel: SharedViewModel) {
        isAutoPrintMerchant.value = value
        sharedViewModel.objPosConfig?.apply { this.isAutoPrintMerchant = value }?.saveToPrefs()
    }

    fun onTippingEnabledChange(value: Boolean, sharedViewModel: SharedViewModel) {
        isTippingEnabled.value = value
        sharedViewModel.objPosConfig?.apply { this.isTipEnabled = value }?.saveToPrefs()
    }

    fun onTaxEnabledChange(value: Boolean, sharedViewModel: SharedViewModel) {
        isTaxEnabled.value = value
        sharedViewModel.objPosConfig?.apply { this.isTaxEnabled = value }?.saveToPrefs()
    }

    fun onTaxPercentChange(index: Int, navHostController: NavHostController) {
        navHostController.currentBackStackEntry?.savedStateHandle?.set<String>(AppConstants.NAV_KEY_TAX_TYPE, if (index == 0) AppConstants.NAV_VAL_TAX_TYPE_SGST else AppConstants.NAV_VAL_TAX_TYPE_CGST)
        navHostController.navigate(AppNavigationItems.TaxPercentageScreen.route)
    }

    fun onTipPercentChange(button: TipButton, navHostController: NavHostController) {
        navHostController.currentBackStackEntry?.savedStateHandle?.set<Int>(AppConstants.NAV_KEY_TIP_PERCENT_INDEX, button.value)
        navHostController.navigate(AppNavigationItems.TipPercentageScreen.route)
    }

    fun onLoad(sharedViewModel: SharedViewModel)
    {
        loadPreferences(sharedViewModel)
    }

    fun onBack(navHostController: NavHostController) {
        navHostController.popBackStack()
    }
}