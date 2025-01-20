package com.eazypaytech.posafrica.rootUiScreens.inactivityTimeout

import android.util.Log
import androidx.lifecycle.ViewModel
import com.eazypaytech.securityframework.database.dbRepository.TxnDBRepository
import com.eazypaytech.posafrica.rootUiScreens.activity.SharedViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class InactivityTimeoutViewModel @Inject constructor(private val dbRepository: TxnDBRepository) : ViewModel() {
    private val _timeout = MutableStateFlow(0) // Set initial value as an integer
    val timeout: StateFlow<Int> = _timeout

    fun updateInactivityTimeout(newValue: Int, sharedViewModel: SharedViewModel?=null): Int {
        _timeout.value = newValue
        return _timeout.value // Return the updated invoice number
    }

    fun onConfirm(sharedViewModel: SharedViewModel)
    {
        sharedViewModel.objPosConfig?.inactivityTimeout = timeout.value
        Log.d("Timeout", "Timeout in onConfirm: ${sharedViewModel.objPosConfig?.batchId}")
    }


}
