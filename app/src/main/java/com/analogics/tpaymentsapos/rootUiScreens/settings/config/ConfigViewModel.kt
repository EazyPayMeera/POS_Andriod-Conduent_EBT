package com.analogics.tpaymentsapos.rootUiScreens.settings.config

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.analogics.paymentservicecore.constants.AppConstants
import com.analogics.securityframework.database.dbRepository.TxnDBRepository
import com.analogics.tpaymentsapos.R
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import com.analogics.tpaymentsapos.rootModel.Symbol
import com.analogics.tpaymentsapos.rootUiScreens.activity.SharedViewModel
import com.analogics.tpaymentsapos.rootUiScreens.dialogs.CustomDialogBuilder
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.formatAmount
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConfigViewModel @Inject constructor(private val dbRepository: TxnDBRepository) : ViewModel(){

    private val _isBatchOpen = MutableStateFlow<Boolean>(false)
    val isBatchOpen: StateFlow<Boolean> = _isBatchOpen

    private val _isAdmin = MutableStateFlow(false) // Change to StateFlow
    val isAdmin: StateFlow<Boolean> get() = _isAdmin

    var isTrainingMode = mutableStateOf(false)
    var isAutoPrintReport = mutableStateOf(false)
    var isPromptInvoiceNumber = mutableStateOf(false)
    var isAutoPrintMerchant = mutableStateOf(false)
    var isTippingEnabled = mutableStateOf(false)
    var isTaxEnabled = mutableStateOf(false)
    var isInactivity = mutableStateOf(false)
    var isBatchId = mutableStateOf(false)

    private fun loadPreferences(sharedViewModel: SharedViewModel)
    {
        isTrainingMode.value = sharedViewModel.objPosConfig?.isDemoMode == true
        isAutoPrintReport.value = sharedViewModel.objPosConfig?.isAutoPrintReport == true
        isPromptInvoiceNumber.value = sharedViewModel.objPosConfig?.isPromptInvoiceNo == true
        isAutoPrintMerchant.value = sharedViewModel.objPosConfig?.isAutoPrintMerchant == true
        isTippingEnabled.value = sharedViewModel.objPosConfig?.isTipEnabled == true
        isTaxEnabled.value = sharedViewModel.objPosConfig?.isTaxEnabled == true
        isInactivity.value = sharedViewModel.objPosConfig?.isInactivityTimeout == true
        isBatchId.value = sharedViewModel.objPosConfig?.isBatchId == true
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

    fun onInactivityChange(value: Boolean) {
        isInactivity.value = value
    }

    fun onBatchIdChange(value: Boolean) {
        isBatchId.value = value
    }

    fun onInactivityTimeoutChange(timeout: Int,sharedViewModel: SharedViewModel) {
        sharedViewModel.objPosConfig?.apply { this.inactivityTimeout = timeout }?.saveToPrefs()
    }

    fun onBatchIdChange(batchId: Int,sharedViewModel: SharedViewModel) {
        sharedViewModel.objPosConfig?.apply { this.batchId = batchId.toString() }?.saveToPrefs()
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
        checkIfAdmin(sharedViewModel)
        checkBatchStatus()
    }

    fun onShowAdminOnly(context: Context)
    {
        CustomDialogBuilder.composeAlertDialog(
            title = context.resources.getString(
                R.string.restricted
            ),
            subtitle = context.resources.getString(R.string.for_admin)
        )
    }

    fun onShowBatchOpen(context: Context)
    {
        CustomDialogBuilder.composeAlertDialog(
            title = context.resources.getString(
                R.string.restricted
            ),
            subtitle = context.resources.getString(R.string.batch_open)
        )
    }

    fun onBack(navHostController: NavHostController) {
        navHostController.popBackStack()
    }

    fun checkBatchStatus() {
        viewModelScope.launch {
            dbRepository.isBatchOpen().let {
                _isBatchOpen.value = it
            }
        }
    }

    fun checkIfAdmin(sharedViewModel: SharedViewModel) {
        viewModelScope.launch {
            sharedViewModel.objPosConfig?.loginId?.let {
                dbRepository.isAdmin(it).let {
                    _isAdmin.value = it
                }
            }
        }
    }
}