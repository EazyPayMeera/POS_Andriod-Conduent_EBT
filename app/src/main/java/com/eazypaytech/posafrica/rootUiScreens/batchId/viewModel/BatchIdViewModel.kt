package com.eazypaytech.posafrica.rootUiScreens.batchId.viewModel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.eazypaytech.securityframework.database.dbRepository.TxnDBRepository
import com.eazypaytech.securityframework.database.entity.TxnEntity
import com.eazypaytech.posafrica.R
import com.eazypaytech.posafrica.rootModel.ObjRootAppPaymentDetails
import com.eazypaytech.posafrica.rootUiScreens.activity.SharedViewModel
import com.eazypaytech.posafrica.rootUiScreens.dialogs.CustomDialogBuilder
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

    fun updateBatchId(newValue: String): String {
        if (isBatchOpen.value == false)
            _batchid.value = newValue
        return _batchid.value // Return the updated invoice number
    }

    fun onConfirm(sharedViewModel: SharedViewModel)
    {
        if (isBatchOpen.value == false)
            sharedViewModel.objPosConfig?.apply { batchId = _batchid.value}?.saveToPrefs()
    }

    fun isBatchOpen() {
        viewModelScope.launch {
            dbRepository.isBatchOpen().let {
                _isBatchOpen.value = it
            }
        }
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

    private fun convertTxnEntityListToTxnDataList(txnEntityList: List<TxnEntity>): List<ObjRootAppPaymentDetails> {
        val gson = Gson()
        val json = gson.toJson(txnEntityList)
        val txnDataListType = object : TypeToken<List<ObjRootAppPaymentDetails>>() {}.type
        return gson.fromJson(json, txnDataListType)
    }

}

