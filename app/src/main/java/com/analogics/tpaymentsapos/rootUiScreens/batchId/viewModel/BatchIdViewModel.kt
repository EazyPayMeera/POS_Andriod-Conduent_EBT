package com.analogics.tpaymentsapos.rootUiScreens.batchId.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.analogics.securityframework.database.dbRepository.TxnDBRepository
import com.analogics.tpaymentsapos.rootUiScreens.activity.SharedViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class BatchIdViewModel @Inject constructor(private val dbRepository: TxnDBRepository) : ViewModel() {
    private val _batchid = MutableStateFlow("")
    val batchId: StateFlow<String> = _batchid

    fun updateBatchId(newValue: String, sharedViewModel: SharedViewModel?=null): String {
        _batchid.value = newValue
        return _batchid.value // Return the updated invoice number
    }

    fun onConfirm(sharedViewModel: SharedViewModel)
    {
        sharedViewModel.objPosConfig?.batchId = batchId.value
        Log.d("Batch No", "Batch No in onConfirm: ${sharedViewModel.objPosConfig?.batchId}")
    }


}

