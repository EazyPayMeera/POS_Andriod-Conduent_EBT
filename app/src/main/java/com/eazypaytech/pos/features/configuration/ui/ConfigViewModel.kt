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


    /**
     * Loads POS configuration flags into local UI state.
     *
     * Maps values like training mode, printing options,
     * tipping, tax, and other feature toggles.
     */
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

    /**
     * Initializes screen data.
     *
     * Flow:
     * - Loads preferences
     * - Checks admin privileges
     * - Checks batch status
     */
    fun onLoad(sharedViewModel: SharedViewModel)
    {
        loadPreferences(sharedViewModel)
        checkIfAdmin(sharedViewModel)
        checkBatchStatus()
    }

    /**
     * Displays restricted access dialog for non-admin users.
     */
    fun onShowAdminOnly(context: Context)
    {
        CustomDialogBuilder.Companion.composeAlertDialog(
            title = context.resources.getString(
                R.string.restricted
            ),
            subtitle = context.resources.getString(R.string.for_admin)
        )
    }


    /**
     * Handles back navigation.
     */
    fun onBack(navHostController: NavHostController) {
        navHostController.popBackStack()
    }

    /**
     * Checks whether a batch is currently open
     * and updates UI state accordingly.
     */
    fun checkBatchStatus() {
        viewModelScope.launch {
            dbRepository.isBatchOpen().let {
                _isBatchOpen.value = it
            }
        }
    }

    /**
     * Verifies if the current user has admin privileges
     * based on login ID and updates UI state.
     */
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