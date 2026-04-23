package com.eazypaytech.pos.features.readerSetting.ui

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.eazypaytech.pos.features.activity.ui.SharedViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ReaderSettingViewModel @Inject constructor() : ViewModel() {

    /**
     * Holds UI state for Tap and Insert (EMV) enablement.
     */
    val isTapEnabled = mutableStateOf(true)
    val isInsertEnabled = mutableStateOf(true)

    /**
     * Handles Tap (NFC) toggle state change.
     *
     * Behavior:
     * - Updates UI state
     * - Saves preference in POS configuration
     * - Updates transaction-level flag
     *
     * @param sharedViewModel Shared ViewModel containing config and txn data
     * @param enabled Boolean flag indicating whether Tap is enabled
     */
    fun onTapToggle(sharedViewModel: SharedViewModel,enabled: Boolean) {
        isTapEnabled.value = enabled
        sharedViewModel.objPosConfig?.apply { isTapEnable = enabled }?.saveToPrefs()
        sharedViewModel.objRootAppPaymentDetail.isTapEnable = enabled
    }

    /**
     * Handles Insert (Chip/EMV) toggle state change.
     *
     * Behavior:
     * - Updates UI state
     * - Saves preference in POS configuration
     * - Updates transaction-level flag
     *
     * @param sharedViewModel Shared ViewModel containing config and txn data
     * @param enabled Boolean flag indicating whether Insert is enabled
     */
    fun onInsertToggle(sharedViewModel: SharedViewModel,enabled: Boolean) {
        isInsertEnabled.value = enabled
        sharedViewModel.objPosConfig?.apply { isEMVEnable = enabled }?.saveToPrefs()
        sharedViewModel.objRootAppPaymentDetail.isEMVEnable = enabled
    }

    /**
     * Initializes toggle states from persisted configuration.
     *
     * Behavior:
     * - Reads saved Tap and Insert settings from POS config
     * - Defaults to true if config is null
     *
     * @param sharedViewModel Shared ViewModel containing saved configuration
     */
    fun initOnce(sharedViewModel: SharedViewModel?) {
        isTapEnabled.value = sharedViewModel?.objPosConfig?.isTapEnable ?: true
        isInsertEnabled.value = sharedViewModel?.objPosConfig?.isEMVEnable ?: true
    }
}