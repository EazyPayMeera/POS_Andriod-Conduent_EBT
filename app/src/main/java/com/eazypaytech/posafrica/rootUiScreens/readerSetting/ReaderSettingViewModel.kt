package com.eazypaytech.posafrica.rootUiScreens.readerSetting

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.analogics.securityframework.data.repository.TxnDBRepository
import com.eazypaytech.posafrica.features.activity.ui.SharedViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ReaderSettingViewModel @Inject constructor(
    private var dbRepository: TxnDBRepository
) : ViewModel() {

    val isTapEnabled = mutableStateOf(false)
    val isInsertEnabled = mutableStateOf(false)
    fun onTapToggle(sharedViewModel: SharedViewModel,enabled: Boolean) {
        isTapEnabled.value = enabled
        sharedViewModel.objPosConfig?.apply { isTapEnable = enabled }?.saveToPrefs()
        sharedViewModel.objRootAppPaymentDetail?.isTapEnable = enabled
    }

    fun onInsertToggle(sharedViewModel: SharedViewModel,enabled: Boolean) {
        isInsertEnabled.value = enabled
        sharedViewModel.objPosConfig?.apply { isEMVEnable = enabled }?.saveToPrefs()
        sharedViewModel.objRootAppPaymentDetail.isEMVEnable = enabled
    }

    fun initOnce(sharedViewModel: SharedViewModel?) {
        isTapEnabled.value = sharedViewModel?.objPosConfig?.isTapEnable ?: false
        isInsertEnabled.value = sharedViewModel?.objPosConfig?.isEMVEnable ?: false
    }


}