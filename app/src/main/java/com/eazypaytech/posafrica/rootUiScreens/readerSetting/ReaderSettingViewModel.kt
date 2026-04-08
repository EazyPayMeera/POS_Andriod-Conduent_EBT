package com.eazypaytech.posafrica.rootUiScreens.readerSetting

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.eazypaytech.posafrica.rootUiScreens.activity.SharedViewModel
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.navigateAndClean
import com.eazypaytech.posafrica.navigation.AppNavigationItems
import com.eazypaytech.securityframework.database.dbRepository.TxnDBRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReaderSettingViewModel @Inject constructor(
    private var dbRepository: TxnDBRepository
) : ViewModel() {

    val isTapEnabled = mutableStateOf(false)
    val isInsertEnabled = mutableStateOf(false)

    fun onTapToggle(enabled: Boolean) {
        isTapEnabled.value = enabled
    }

    fun onInsertToggle(enabled: Boolean) {
        isInsertEnabled.value = enabled

    }


}