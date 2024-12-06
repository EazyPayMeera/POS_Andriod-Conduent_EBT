package com.analogics.tpaymentsapos.rootUiScreens.batchId.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.analogics.securityframework.database.dbRepository.TxnDBRepository
import com.analogics.securityframework.database.entity.TxnEntity
import com.analogics.tpaymentsapos.rootModel.ObjRootAppPaymentDetails
import com.analogics.tpaymentsapos.rootUiScreens.activity.SharedViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BatchIdViewModel @Inject constructor(private val dbRepository: TxnDBRepository) : ViewModel() {
    private val _batchid = MutableStateFlow("")
    val batchId: StateFlow<String> = _batchid
    private val _showProgress = MutableStateFlow<Boolean>(false)
    private val _isBatchOpen = MutableStateFlow<Boolean>(false)

    lateinit var navHostController: NavHostController

    val isBatchOpen : StateFlow<Boolean> = _isBatchOpen

    fun updateBatchId(newValue: String, sharedViewModel: SharedViewModel?=null): String {
        _batchid.value = newValue
        return _batchid.value // Return the updated invoice number
    }

    fun onConfirm(sharedViewModel: SharedViewModel)
    {
        sharedViewModel.objPosConfig?.batchId = batchId.value
        Log.d("Batch No", "Batch No in onConfirm: ${sharedViewModel.objPosConfig?.batchId}")
    }

    fun isBatchOpen() {
        viewModelScope.launch {
            _showProgress.value = true
            Log.d("isBatchOpen", "Progress started") // Log progress start

            dbRepository.isBatchOpen().let {
                _isBatchOpen.value = it
                Log.d("isBatchOpen", "Batch open status: $it") // Log the result
            }

            _showProgress.value = false
            Log.d("isBatchOpen", "Progress ended") // Log progress end
        }
    }

    private fun convertTxnEntityListToTxnDataList(txnEntityList: List<TxnEntity>): List<ObjRootAppPaymentDetails> {
        val gson = Gson()
        val json = gson.toJson(txnEntityList)
        val txnDataListType = object : TypeToken<List<ObjRootAppPaymentDetails>>() {}.type
        return gson.fromJson(json, txnDataListType)
    }

}

