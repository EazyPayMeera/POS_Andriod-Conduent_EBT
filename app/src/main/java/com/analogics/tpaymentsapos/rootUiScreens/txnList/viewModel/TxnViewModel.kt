package com.analogics.tpaymentsapos.rootUiScreens.txnList.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.analogics.tpaymentsapos.rootUiScreens.txnList.model.TxnDataList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

open class TxnViewModel  : ViewModel() {
    val _transactionList = MutableStateFlow<List<TxnDataList>>(emptyList())
    val transactionList: StateFlow<List<TxnDataList>> = _transactionList

    init {
        // Simulate data fetching
        viewModelScope.launch {
            _transactionList.value = fetchTransactions()
        }
    }

    private fun fetchTransactions(): List<TxnDataList> {
        // This would normally come from a repository or database
        return listOf(
            TxnDataList(1, "Today @ 14:15:30", "Purchase", 450.00, true),
            TxnDataList(2, "Today @ 14:15:30", "Refund", 50.00, false),
            TxnDataList(3, "26-2-2020 @ 14:15:30", "Purchase", 50.00, true)
        )
    }
}