package com.eazypaytech.pos.features.readerSetting.ui

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.eazypaytech.pos.features.activity.ui.SharedViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ReaderSettingViewModel @Inject constructor() : ViewModel() {

    val isTapEnabled = mutableStateOf(true)
    val isInsertEnabled = mutableStateOf(true)
    fun onTapToggle(sharedViewModel: SharedViewModel,enabled: Boolean) {
        isTapEnabled.value = enabled
        sharedViewModel.objPosConfig?.apply { isTapEnable = enabled }?.saveToPrefs()
        sharedViewModel.objRootAppPaymentDetail.isTapEnable = enabled
    }

    fun onInsertToggle(sharedViewModel: SharedViewModel,enabled: Boolean) {
        isInsertEnabled.value = enabled
        sharedViewModel.objPosConfig?.apply { isEMVEnable = enabled }?.saveToPrefs()
        sharedViewModel.objRootAppPaymentDetail.isEMVEnable = enabled
    }

    fun initOnce(sharedViewModel: SharedViewModel?) {
        isTapEnabled.value = sharedViewModel?.objPosConfig?.isTapEnable ?: true
        isInsertEnabled.value = sharedViewModel?.objPosConfig?.isEMVEnable ?: true
    }
}