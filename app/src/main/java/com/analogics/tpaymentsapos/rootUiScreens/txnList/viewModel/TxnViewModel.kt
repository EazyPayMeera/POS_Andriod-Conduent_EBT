package com.analogics.tpaymentsapos.rootUiScreens.txnList.viewModel

import android.content.ContentValues.TAG
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.analogics.builder_core.model.PaymentServiceTxnDetails
import com.analogics.paymentservicecore.listeners.responseListener.IApiServiceResponseListener
import com.analogics.paymentservicecore.listeners.responseListener.IPrinterResultProviderListener
import com.analogics.paymentservicecore.logger.AppLogger
import com.analogics.paymentservicecore.model.error.ApiServiceError
import com.analogics.paymentservicecore.models.TxnType
import com.analogics.paymentservicecore.repository.apiService.ApiServiceRepository
import com.analogics.paymentservicecore.utils.PaymentServiceUtils
import com.analogics.securityframework.database.dbRepository.TxnDBRepository
import com.analogics.securityframework.database.entity.TxnEntity
import com.analogics.tpaymentsapos.R
import com.analogics.tpaymentsapos.rootModel.ObjRootAppPaymentDetails
import com.analogics.tpaymentsapos.rootUiScreens.activity.SharedViewModel
import com.analogics.tpaymentsapos.rootUiScreens.dialogs.CustomDialogBuilder
import com.analogics.tpaymentsapos.rootUiScreens.utility.ReceiptBuilder
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.PrinterServiceRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class TxnViewModel @Inject constructor(private val dbRepository: TxnDBRepository, val apiServiceRepository: ApiServiceRepository) : ViewModel(),
    IApiServiceResponseListener {
    private val _transactionList = MutableStateFlow<List<ObjRootAppPaymentDetails>>(emptyList())
    private val _batchList = MutableStateFlow<List<String>>(emptyList())
    private val _startDateList = MutableStateFlow<List<String>>(emptyList())
    private val _batchStatusList = MutableStateFlow<List<String>>(emptyList())
    private val _endDateList = MutableStateFlow<List<String>>(emptyList())

    val transactionList: StateFlow<List<ObjRootAppPaymentDetails>> = _transactionList
    val batchList: StateFlow<List<String>> = _batchList
    val startDateList: StateFlow<List<String>> = _startDateList
    val batchStatusList: StateFlow<List<String>> = _batchStatusList
    val endDateList: StateFlow<List<String>> = _endDateList
    var allTransactionList: List<TxnEntity>? = null
    private val objRoot = MutableStateFlow(ObjRootAppPaymentDetails())
    var userApiServiceErrorHolder = MutableStateFlow(ApiServiceError())
    private var isFiltered = false

    init {
        viewModelScope.launch {
            fetchTransactions()
        }
    }

    fun fetchTransactions() {
        viewModelScope.launch {
            allTransactionList = dbRepository.getAllTxnListData()
            allTransactionList?.let {
                // Convert the list to TxnDataList and sort in descending order (assuming there's a timestamp or ID to sort by)
                val txnDataList = convertTxnEntityListToTxnDataList(it)
                    .sortedByDescending { txnData -> txnData.dateTime }  // Replace transactionDate with your sorting field
                _transactionList.value = txnDataList
            }
        }
    }
    fun fetchTransactionDetailsTxnByDate(date: String){
        viewModelScope.launch {
            allTransactionList =dbRepository.fetchTransactionDetailsTxnByDate(date)
            allTransactionList?.let {
                val txnDataList = convertTxnEntityListToTxnDataList(it)
                _transactionList.value = txnDataList
            }
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun filterTransactionsByDateRange(startDate: LocalDateTime,endDate: LocalDateTime) {
        viewModelScope.launch {
            val filteredList = _transactionList.value.filter { transaction ->
                val transactionDateTime = LocalDateTime.parse(transaction.dateTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                transactionDateTime.isAfter(startDate) && transactionDateTime.isBefore(endDate)

            }
            _transactionList.value = filteredList
            Log.d("Date Time Picker", "Updated Filter Transaction By Date ")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun filterTransactionsByStartEndDate(startDate: LocalDateTime,endDate: LocalDateTime) {
        viewModelScope.launch {
            allTransactionList = dbRepository.getTransactionsByDateRange(startDate.toString(),
                endDate.toString()
            )
            allTransactionList?.let {
                val txnDataList = convertTxnEntityListToTxnDataList(it)
                _transactionList.value = txnDataList
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun filterTransactionsForBatch() {
        viewModelScope.launch {
            val batchIds = dbRepository.fetchTransactionDetailsByBatchId()
            _batchList.value = batchIds
            Log.d("FilterTransactions", "Fetched Batch IDs: $batchIds")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun filterTransactionsByBatchId(batchId:String) {
        viewModelScope.launch {
            allTransactionList = dbRepository.fetchTransactionByBatch(batchId)
            allTransactionList?.let {
                val txnDataList = convertTxnEntityListToTxnDataList(it)
                _transactionList.value = txnDataList
            }
        }
    }

    fun fetchStartDates(batchIds: List<String>) {
        viewModelScope.launch {
            try {
                val startDates = mutableListOf<String>()
                batchIds.forEach { batchId ->
                    val minStartDate = dbRepository.getStartDate(batchId)
                    minStartDate.let {
                        startDates.add(it.toString()) // Add the non-null start date to the list
                        Log.d("FetchStartDates", "Batch ID: $batchId, Start Date: ${it.toString()}")
                    }
                }
                _startDateList.value = startDates
                 // Print batch ID and start date
            } catch (e: Exception) {
                Log.e("FetchStartDates", "Error fetching start dates: ${e.message}")
            }
        }
    }

    fun fetchBatchStatus(batchIds: List<String>) {
        viewModelScope.launch {
            try {
                val batchStatus = mutableListOf<String>()
                batchIds.forEach { batchId ->
                    val BatchStatus = dbRepository.getBatchStatus(batchId)
                    BatchStatus.let {
                        batchStatus.add(it.toString()) // Add the non-null start date to the list
                        Log.d("Batch Status", "Batch STATUS: $batchId, Start Date: ${it.toString()}")
                    }
                }
                _batchStatusList.value = batchStatus
                // Print batch ID and start date
            } catch (e: Exception) {
                Log.e("FetchStartDates", "Error fetching start dates: ${e.message}")
            }
        }
    }


    fun fetchEndDates(batchIds: List<String>) {
        viewModelScope.launch {
            try {
                val endDates = mutableListOf<String>()
                batchIds.forEach { batchId ->
                    val minStartDate = dbRepository.getEndDate(batchId)
                    minStartDate.let {
                        endDates.add(it.toString()) // Add the non-null start date to the list
                        Log.d("FetchStartDates", "Batch ID: $batchId, End Date: ${it.toString()}")
                    }
                    _endDateList.value = endDates
                }
            } catch (e: Exception) {
                Log.e("FetchMinStartDates", "Error fetching minimum start dates: ${e.message}")
            }
        }
    }

    private fun convertTxnEntityListToTxnDataList(txnEntityList: List<TxnEntity>): List<ObjRootAppPaymentDetails> {
        val gson = Gson()
        val json = gson.toJson(txnEntityList)
        val txnDataListType = object : TypeToken<List<ObjRootAppPaymentDetails>>() {}.type
        return gson.fromJson(json, txnDataListType)
    }

    fun totalPurchaseTransactions(txn: TxnType): Double {
        return _transactionList.value
            .filter { it.txnType == txn }
            .sumOf {
                it.ttlAmount ?: 0.0
            }
    }

    fun totalTransactionsCount(txn: TxnType): Int {
        return _transactionList.value
            .count { it.txnType == txn}
    }


    fun onApiBatchClose() {
        viewModelScope.launch {
            try {
                val requestDetails =
                    PaymentServiceUtils.objectToJsonString(_transactionList.value)
                apiServiceRepository.apiServiceBatch(
                    PaymentServiceUtils.jsonStringToObject<PaymentServiceTxnDetails>(requestDetails), this@TxnViewModel)
            } catch (e: Exception) {
                AppLogger.d(AppLogger.MODULE.APP_UI, e.message ?: "")
            }
        }
    }

    override fun onApiSuccess(response: Any) {
        when (response) {
            is ObjRootAppPaymentDetails -> {
                objRoot.value = response
            }
        }

    }

    override fun onApiError(paymentError: ApiServiceError) {
        Log.e("API Response", paymentError.errorMessage)
        userApiServiceErrorHolder.value = paymentError
    }


    fun printReceipt(
        context: Context,
        customer: Boolean = false,
        isSummaryReport: Boolean = false,
        sharedViewModel: SharedViewModel,
        objRootAppPaymentDetail: ObjRootAppPaymentDetails
    ) {
        viewModelScope.launch {
            // Call getPrinterStatus with a callback
            getPrinterStatus(objRootAppPaymentDetail, object : IPrinterResultProviderListener {
                override fun onSuccess(result: Any?) {
                    val subtitleText = when (result) {
                        -1 -> context.resources.getString(R.string.printer_out_of_paper) // Example error for result -1
                        else -> context.resources.getString(R.string.printer_busy) // Default error for other cases
                    }

                    if (result != 0) {
                        CustomDialogBuilder.composeAlertDialog(
                            title = context.resources.getString(R.string.printer_error_title),
                            subtitle = subtitleText // Dynamic subtitle based on result
                        )
                    } else {
                        launch {
                            try {
                                initPrinter(context,sharedViewModel ,isSummaryReport ,objRootAppPaymentDetail, object : IPrinterResultProviderListener {
                                    override fun onSuccess(result: Any?) {
                                    }
                                    override fun onFailure(exception: Exception) {
                                    }
                                })
                            } catch (e: Exception) {
                                Log.e(TAG, "Error during printer initialization: ${e.message}")
                            }
                        }
                    }
                }

                override fun onFailure(exception: Exception) {
                    Log.e(TAG, "Failed to get printer status: ${exception.message}")
                }
            })
        }
    }

    suspend fun initPrinter(
        context: Context,
        sharedViewModel: SharedViewModel,
        isSummaryReport: Boolean = false,
        objRootAppPaymentDetail: ObjRootAppPaymentDetails,
        iPrinterResultProviderListener: IPrinterResultProviderListener
    )
    {
        viewModelScope.launch {
            try {
                CustomDialogBuilder.composeProgressDialog(
                    title = context.resources.getString(R.string.printing),
                    subtitle = context.resources.getString(R.string.plz_wait)
                )
                val requestDetails =
                    PaymentServiceUtils.objectToJsonString(objRootAppPaymentDetail)
                PrinterServiceRepository(PaymentServiceUtils.jsonStringToObject<PaymentServiceTxnDetails>(requestDetails)).initPrinter(context, iPrinterResultProviderListener)
                if(isSummaryReport) {
                    addDetailedReceipt(
                        sharedViewModel,
                        context,
                        objRootAppPaymentDetail,
                        transactionList.value,
                        object : IPrinterResultProviderListener {
                            override fun onSuccess(result: Any?) {
                                if(result == true)
                                {
                                    CustomDialogBuilder.hideProgress()
                                }
                            }
                            override fun onFailure(exception: Exception) {

                            }
                        }
                    )
                }
                else
                {
                    addReceiptDetails(context,sharedViewModel,objRootAppPaymentDetail,object : IPrinterResultProviderListener{
                        override fun onSuccess(result: Any?) {
                            if(result == true)
                            {
                                CustomDialogBuilder.hideProgress()
                            }
                        }
                        override fun onFailure(exception: Exception) {

                        }
                    })
                }
            } catch (e: Exception) {
                AppLogger.d(AppLogger.MODULE.APP_UI, e.message ?: "")
            }
        }

    }


    suspend fun addReceiptDetails(
        context: Context,
        sharedViewModel: SharedViewModel,
        objRootAppPaymentDetail: ObjRootAppPaymentDetails,
        iPrinterResultProviderListener: IPrinterResultProviderListener
    ) {
        val receiptBuilder = ReceiptBuilder()
        withContext(Dispatchers.IO) {
            val paymentServiceTxnDetails = PaymentServiceUtils.jsonStringToObject<PaymentServiceTxnDetails>(
                PaymentServiceUtils.objectToJsonString(objRootAppPaymentDetail)
            )
            val summaryReport = receiptBuilder.createSummaryReport(context,sharedViewModel, paymentServiceTxnDetails)

            val labelList: List<String> = summaryReport.summaryFields.map { it.first }
            val valueList: List<String> = summaryReport.summaryFields.map { it.second }
            val descriptionList: List<String> = summaryReport.summaryFields.map { it.third }
            PrinterServiceRepository(paymentServiceTxnDetails).printLeftCenterRightDetails(
                labelList,
                valueList,
                descriptionList,
                iPrinterResultProviderListener
            )
        }
    }

    suspend fun addDetailedReceipt(
        sharedViewModel: SharedViewModel,
        context: Context,
        objRootAppPaymentDetail: ObjRootAppPaymentDetails,
        transactionList: List<ObjRootAppPaymentDetails>, // Assuming this is the input type
        iPrinterResultProviderListener: IPrinterResultProviderListener
    ) {
        val receiptBuilder = ReceiptBuilder()
        withContext(Dispatchers.IO) {
            val paymentServiceTxnDetails = PaymentServiceUtils.jsonStringToObject<PaymentServiceTxnDetails>(
                PaymentServiceUtils.objectToJsonString(objRootAppPaymentDetail)
            )
            val transactionDetailsList = transactionList.map { paymentDetail ->
                ReceiptBuilder.TransactionDetails(
                    TxnType = paymentDetail.txnType.toString(), // Replace with actual property
                    Status = paymentDetail.txnStatus.toString(), // Replace with actual property
                    InvoiceNo = paymentDetail.invoiceNo.toString(),
                    AuthCode = paymentDetail.hostAuthCode.toString(),
                    txnAmount = paymentDetail.txnAmount.toString(),
                    ttlAmount = paymentDetail.ttlAmount.toString(),
                    timedate = paymentDetail.dateTime.toString()
                )
            }
            val detailedReport = receiptBuilder.createDetailReport(context,sharedViewModel,paymentServiceTxnDetails, transactionDetailsList)
            val labelList: List<String> = detailedReport.detailFields.map { it.first }
            val valueList: List<String> = detailedReport.detailFields.map { it.second }
            val descriptionList: List<String> = detailedReport.detailFields.map { it.third }
            PrinterServiceRepository(paymentServiceTxnDetails).printLeftCenterRightDetails(
                labelList,
                valueList,
                descriptionList,
                iPrinterResultProviderListener
            )
        }
    }

    suspend fun getPrinterStatus(objRootAppPaymentDetail: ObjRootAppPaymentDetails,iPrinterResultProviderListener: IPrinterResultProviderListener) {
        try {

            val paymentServiceTxnDetails = PaymentServiceUtils.jsonStringToObject<PaymentServiceTxnDetails>(
                PaymentServiceUtils.objectToJsonString(objRootAppPaymentDetail)
            )
            PrinterServiceRepository(paymentServiceTxnDetails).getStatus(iPrinterResultProviderListener)

        } catch (e: Exception) {
            Log.e(TAG, "Failed to get printer status: ${e.message}")
        }
    }

    override fun onDisplayProgress(
        show: Boolean,
        title: String?,
        subTitle: String?,
        message: String?
    ) {
        CustomDialogBuilder.composeProgressDialog(show = show, title = title, subtitle = subTitle, message = message)
    }

    fun closeOpenBatches() {
        viewModelScope.launch {
            try {
                val closedCount = dbRepository.closeBatch()
                Log.d("BatchViewModel", "Closed $closedCount open batches")
            } catch (e: Exception) {
                Log.e("BatchViewModel", "Error closing open batches", e)
            }
        }
    }

}
