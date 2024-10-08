package com.analogics.tpaymentsapos.rootUiScreens.txnList.viewModel

import android.content.ContentValues.TAG
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.analogics.builder_core.model.PaymentServiceTxnDetails
import com.analogics.paymentservicecore.listeners.responseListener.IPrinterResultProviderListener
import com.analogics.paymentservicecore.listeners.rootListener.IOnRootAppPaymentListener
import com.analogics.paymentservicecore.logger.AppLogger
import com.analogics.paymentservicecore.model.error.PaymentServiceError
import com.analogics.paymentservicecore.models.TxnType
import com.analogics.paymentservicecore.repository.paymentService.PaymentServiceRepository
import com.analogics.paymentservicecore.utils.PaymentServiceUtils
import com.analogics.securityframework.database.dbRepository.TxnDBRepository
import com.analogics.securityframework.database.entity.TxnEntity
import com.analogics.tpaymentsapos.rootModel.ObjRootAppPaymentDetails
import com.analogics.tpaymentsapos.rootUiScreens.utility.ReceiptBuilder
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.PrinterServiceRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
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
    val isPrinting = mutableStateOf(false)
    val isCustomer = mutableStateOf(false)

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
                // Parse the transaction dateTime as LocalDateTime
                val transactionDateTime = LocalDateTime.parse(transaction.dateTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

                // Compare the transaction date and time with the selected date and time
                transactionDateTime.isBefore(selectedDate)
            }
            // Update the filterTxn value with the filtered list
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

    fun totalTransactionsCount(txn: TxnType): Int {
        return allTransactionList
            ?.count { it.txnType == txn.toString() } // Count the transactions of the specified type
            ?: 0 // Return 0 if the list is null
    }


    fun onApiBatchClose() {
        viewModelScope.launch {
            try {
                val requestDetails =
                    PaymentServiceUtils.objectToJsonString(_transactionList.value)
                paymentServiceRepository.apiServiceBatch(
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


    @OptIn(DelicateCoroutinesApi::class)
    fun printReceipt(
        context: Context,
        customer: Boolean = false,
        objRootAppPaymentDetail: ObjRootAppPaymentDetails
    )
    {
        isPrinting.value = true
        isCustomer.value = customer

        GlobalScope.launch {
            initPrinter(context,objRootAppPaymentDetail, object : IPrinterResultProviderListener {
                override fun onSuccess(result: Any?) {
                    isPrinting.value = false
                }

                override fun onFailure(exception: Exception) {
                    isPrinting.value = false
                }
            })
        }
    }

    suspend fun initPrinter(
        context: Context,
        objRootAppPaymentDetail: ObjRootAppPaymentDetails,
        iPrinterResultProviderListener: IPrinterResultProviderListener
    )
    {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Approved View Model to Printer Service Repository 1")
                val requestDetails =
                    PaymentServiceUtils.objectToJsonString(objRootAppPaymentDetail)
                PrinterServiceRepository(PaymentServiceUtils.jsonStringToObject<PaymentServiceTxnDetails>(requestDetails)).initPrinter(context, iPrinterResultProviderListener)
                addReceiptDetails(objRootAppPaymentDetail,iPrinterResultProviderListener)
                Log.d(TAG, "Approved View Model to Printer Service Repository 2 ${PaymentServiceUtils.jsonStringToObject<PaymentServiceTxnDetails>(requestDetails)}")
            } catch (e: Exception) {
                AppLogger.d(AppLogger.MODULE.APP_UI, e.message ?: "")
            }
        }

    }


    suspend fun addReceiptDetails(objRootAppPaymentDetail: ObjRootAppPaymentDetails,iPrinterResultProviderListener: IPrinterResultProviderListener)
    {
        // Create an instance of ReceiptBuilder
        val receiptBuilder = ReceiptBuilder()

        val paymentServiceTxnDetails = PaymentServiceUtils.jsonStringToObject<PaymentServiceTxnDetails>(
            PaymentServiceUtils.objectToJsonString(objRootAppPaymentDetail)
        )

        // Generate the receipt
        // Generate the summary report using ReceiptBuilder
        val summaryReport = receiptBuilder.createSummaryReport(paymentServiceTxnDetails)

        // Create separate lists for label, value, and description
        val labelList: List<String> = summaryReport.summaryFields.map { it.first }
        val valueList: List<String> = summaryReport.summaryFields.map { it.second }
        val descriptionList: List<String> = summaryReport.summaryFields.map { it.third }

        // Pass the receipt details to the PrinterServiceRepository
        PrinterServiceRepository(paymentServiceTxnDetails).printLeftCenterRightDetails(labelList,valueList,descriptionList,iPrinterResultProviderListener)
    }
}
