package com.analogics.tpaymentsapos.rootUiScreens.txnList.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.analogics.securityframework.database.dbRepository.TxnDBRepository
import com.analogics.securityframework.database.entity.TxnEntity
import com.analogics.tpaymentsapos.rootModel.ObjRootAppPaymentDetails
import com.analogics.tpaymentsapos.rootUiScreens.txnList.model.TxnDataList
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TxnViewModel @Inject constructor(private val dbRepository: TxnDBRepository) : ViewModel() {
    private val _transactionList = MutableStateFlow<List<TxnDataList>>(emptyList())
    val transactionList: StateFlow<List<TxnDataList>> = _transactionList
    private var allTransactionList: List<TxnEntity>? = null

    init {
        // Fetch transactions asynchronously
        viewModelScope.launch {
            fetchTransactions()
        }
    }

    fun fetchTransactions() {
        viewModelScope.launch {
            allTransactionList = dbRepository.getAllTxnListData()
            Log.d("db data", allTransactionList.toString())
            allTransactionList?.let {
                val txnDataList = convertTxnEntityListToTxnDataList(it)
                _transactionList.value = txnDataList
            }
        }
    }

    private fun convertTxnEntityListToTxnDataList(txnEntityList: List<TxnEntity>): List<TxnDataList> {
        val gson = Gson()
        val json = gson.toJson(txnEntityList)
        val txnDataListType = object : TypeToken<List<TxnDataList>>() {}.type
        return gson.fromJson(json, txnDataListType)
    }
}
