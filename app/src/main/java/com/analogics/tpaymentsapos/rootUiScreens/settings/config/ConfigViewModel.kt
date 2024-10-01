package com.analogics.tpaymentsapos.rootUiScreens.settings.config

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.navigation.NavHostController
import com.analogics.tpaymentsapos.rootUiScreens.activity.SharedViewModel
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

    fun onLoad(sharedViewModel: SharedViewModel)
    {
        loadPreferences(sharedViewModel)
    }

    fun onBack(navHostController: NavHostController) {
        navHostController.popBackStack()
    }
}