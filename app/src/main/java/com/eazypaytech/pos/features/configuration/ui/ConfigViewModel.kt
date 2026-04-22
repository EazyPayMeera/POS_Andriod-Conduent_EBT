package com.eazypaytech.pos.features.configuration.ui

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.eazypaytech.pos.R
import com.eazypaytech.pos.domain.model.Symbol
import com.eazypaytech.pos.features.activity.ui.SharedViewModel
import com.eazypaytech.pos.features.dialogs.ui.CustomDialogBuilder
import com.eazypaytech.pos.core.utils.formatAmount
import com.analogics.securityframework.data.repository.TxnDBRepository
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
    var isServiceChargeEnabled = mutableStateOf(false)
    var isTaxEnabled = mutableStateOf(false)
    var isInactivity = mutableStateOf(false)
    var isBatchId = mutableStateOf(false)
    var isSettings = mutableStateOf(false)
    var isTap = mutableStateOf(false)
    var isInsert = mutableStateOf(false)

    private fun loadPreferences(sharedViewModel: SharedViewModel)
    {
        isTrainingMode.value = sharedViewModel.objPosConfig?.isDemoMode == true
        isAutoPrintReport.value = sharedViewModel.objPosConfig?.isAutoPrintReport == true
        isPromptInvoiceNumber.value = sharedViewModel.objPosConfig?.isPromptInvoiceNo == true
        isAutoPrintMerchant.value = sharedViewModel.objPosConfig?.isAutoPrintMerchant == true
        isTippingEnabled.value = sharedViewModel.objPosConfig?.isTipEnabled == true
        isServiceChargeEnabled.value = sharedViewModel.objPosConfig?.isServiceChargeEnabled == true
        isTaxEnabled.value = sharedViewModel.objPosConfig?.isTaxEnabled == true
        isInactivity.value = sharedViewModel.objPosConfig?.isInactivityTimeout == true
        isBatchId.value = sharedViewModel.objPosConfig?.isBatchId == true
    }

    private fun getTipPercent(button: PercentButton, sharedViewModel: SharedViewModel) : Double
    {
        return when(button){
            PercentButton.PERCENT1 -> sharedViewModel.objPosConfig?.tipPercent1?:0.00
            PercentButton.PERCENT2 -> sharedViewModel.objPosConfig?.tipPercent2?:0.00
            PercentButton.PERCENT3 -> sharedViewModel.objPosConfig?.tipPercent3?:0.00
            else -> 0.00
        }
    }

    private fun getServiceChargePercent(button: PercentButton, sharedViewModel: SharedViewModel) : Double
    {
        return when(button){
            PercentButton.PERCENT1 -> sharedViewModel.objPosConfig?.serviceChargePercent1?:0.00
            PercentButton.PERCENT2 -> sharedViewModel.objPosConfig?.serviceChargePercent2?:0.00
            PercentButton.PERCENT3 -> sharedViewModel.objPosConfig?.serviceChargePercent3?:0.00
            else -> 0.00
        }
    }

    fun getTipPercentLabel(button: PercentButton, sharedViewModel: SharedViewModel) : String
    {
        return formatAmount(
            getTipPercent(button, sharedViewModel),
            symbol = Symbol(
                type = Symbol.Type.PERCENT,
                position = Symbol.Position.END,
                noSpace = true
            ),
            decimalPlaces = 0
        )
    }

    fun getServiceChargePercentLabel(button: PercentButton, sharedViewModel: SharedViewModel) : String
    {
        return formatAmount(
            getServiceChargePercent(button, sharedViewModel),
            symbol = Symbol(
                type = Symbol.Type.PERCENT,
                position = Symbol.Position.END,
                noSpace = true
            ),
            decimalPlaces = 2
        )
    }











    fun onInactivityTimeoutChange(navHostController : NavHostController, timeout: Int, sharedViewModel: SharedViewModel) {
        if (isAdmin.value != true) {
            onShowAdminOnly(navHostController.context)
        } else {
            sharedViewModel.objPosConfig?.apply { this.inactivityTimeout = timeout }?.saveToPrefs()
        }
    }

    fun onBatchIdChange(navHostController : NavHostController, batchId: Int, sharedViewModel: SharedViewModel) {
        if (isAdmin.value != true) {
            onShowAdminOnly(navHostController.context)
        } else {
            sharedViewModel.objPosConfig?.apply { this.batchId = batchId.toString() }?.saveToPrefs()
        }
    }




    fun onLoad(sharedViewModel: SharedViewModel)
    {
        loadPreferences(sharedViewModel)
        checkIfAdmin(sharedViewModel)
        checkBatchStatus()
    }

    fun onShowAdminOnly(context: Context)
    {
        CustomDialogBuilder.Companion.composeAlertDialog(
            title = context.resources.getString(
                R.string.restricted
            ),
            subtitle = context.resources.getString(R.string.for_admin)
        )
    }

    fun onShowBatchOpen(context: Context)
    {
        CustomDialogBuilder.Companion.composeAlertDialog(
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