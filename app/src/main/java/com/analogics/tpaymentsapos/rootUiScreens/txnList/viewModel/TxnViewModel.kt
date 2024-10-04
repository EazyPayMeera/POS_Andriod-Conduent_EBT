package com.analogics.tpaymentsapos.rootUiScreens.txnList.viewModel

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.analogics.builder_core.model.PaymentServiceTxnDetails
import com.analogics.paymentservicecore.listeners.rootListener.IOnRootAppPaymentListener
import com.analogics.paymentservicecore.logger.AppLogger
import com.analogics.paymentservicecore.model.error.PaymentServiceError
import com.analogics.paymentservicecore.models.TxnType
import com.analogics.paymentservicecore.repository.paymentService.PaymentServiceRepository
import com.analogics.paymentservicecore.utils.PaymentServiceUtils
import com.analogics.securityframework.database.dbRepository.TxnDBRepository
import com.analogics.securityframework.database.entity.TxnEntity
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import com.analogics.tpaymentsapos.rootModel.ObjRootAppPaymentDetails
import com.analogics.tpaymentsapos.rootUiScreens.txnList.model.TxnDataList
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.getCurrentDateTime
import com.example.example.ObjEmployeeResponse
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class TxnViewModel @Inject constructor(private val dbRepository: TxnDBRepository, val paymentServiceRepository: PaymentServiceRepository) : ViewModel(),
    IOnRootAppPaymentListener {
    private val _transactionList = MutableStateFlow<List<ObjRootAppPaymentDetails>>(emptyList())
    val transactionList: StateFlow<List<ObjRootAppPaymentDetails>> = _transactionList
     var allTransactionList: List<TxnEntity>? = null
    var selectedDateTime = mutableStateOf(Date())
    private val objRoot = MutableStateFlow(ObjRootAppPaymentDetails())
    var userApiErrorHolder = MutableStateFlow(PaymentServiceError())
    private val filterTxn = MutableStateFlow<List<ObjRootAppPaymentDetails>>(emptyList())

    init {
        // Fetch transactions asynchronously
        viewModelScope.launch {
         fetchTransactions()
        }
    }

    fun fetchTransactions() {
        viewModelScope.launch {

            // _transactionList.value=PaymentServiceUtils.jsonStringToObjectList<ObjRootAppPaymentDetails>(PaymentServiceUtils.objectToJsonString(dbRepository.getAllTxnListData()))

            allTransactionList = dbRepository.getAllTxnListData()
            Log.d("db data", allTransactionList.toString())
            allTransactionList?.let {
                val txnDataList = convertTxnEntityListToTxnDataList(it)
                _transactionList.value = txnDataList
            }

            Log.d("all data", allTransactionList?.let {
                val txnDataList = convertTxnEntityListToTxnDataList(it)
                _transactionList.value = txnDataList
            }.toString())
        }
    }
    fun fetchTransactionDetailsTxnByDate(date: String){
        Log.d("filter viewmodel1",date)
        viewModelScope.launch {
            allTransactionList =dbRepository.fetchTransactionDetailsTxnByDate(date)
            allTransactionList?.let {
                val txnDataList = convertTxnEntityListToTxnDataList(it)
                _transactionList.value = txnDataList
            }
        }

        Log.d("filter by date", allTransactionList?.let {
            val txnDataList = convertTxnEntityListToTxnDataList(it)
            _transactionList.value = txnDataList
        }.toString())
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun filterTransactionsByDate(selectedDate: LocalDateTime) {
        viewModelScope.launch {
            // Filter the transactions that occurred before the selected date
            val filteredList = _transactionList.value.filter { transaction ->
                // Assuming `transaction.date` is in the format "yyyy-MM-dd"
                val transactionDate = LocalDate.parse(transaction.dateTime, DateTimeFormatter.ISO_LOCAL_DATE)

                // Compare the transaction date with the selected date (using toLocalDate to ignore time)
                transactionDate.isBefore(selectedDate.toLocalDate())
            }
            filterTxn.value = filteredList
        }
    }
    private fun convertTxnEntityListToTxnDataList(txnEntityList: List<TxnEntity>): List<ObjRootAppPaymentDetails> {
        val gson = Gson()
        val json = gson.toJson(txnEntityList)
        val txnDataListType = object : TypeToken<List<ObjRootAppPaymentDetails>>() {}.type
        return gson.fromJson(json, txnDataListType)
    }

    fun totalPurchaseTransactions(txn:TxnType): Double {
        return allTransactionList
            ?.filter { it.txnType == txn.toString()  }
            ?.sumOf { it.ttlAmount?.toDoubleOrNull() ?: 0.0 }  // Handle possible null amounts
            ?: 0.0  // Return 0.0 if the list is null
    }

    fun onApiBatchClose() {
        viewModelScope.launch {
            try {
                val requestDetails =
                    PaymentServiceUtils.objectToJsonString(_transactionList.value)
                paymentServiceRepository.apiServiceLogin(
                    PaymentServiceUtils.jsonStringToObject<PaymentServiceTxnDetails>(requestDetails), this@TxnViewModel)
            } catch (e: Exception) {
                AppLogger.d(AppLogger.MODULE.APP_UI, e.message ?: "")
            }
        }
    }

    override fun onPaymentSuccess(response: Any) {
        when (response) {
            is ObjRootAppPaymentDetails -> {
                objRoot.value = response
            }
            //delete entery from db
        }

    }

    override fun onPaymentError(paymentError: PaymentServiceError) {
        Log.e("API Response", paymentError.errorMessage)
        userApiErrorHolder.value = paymentError
    }
}
