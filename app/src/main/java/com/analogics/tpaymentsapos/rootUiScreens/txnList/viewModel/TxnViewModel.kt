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
    private val _endDateList = MutableStateFlow<List<String>>(emptyList())

    val transactionList: StateFlow<List<ObjRootAppPaymentDetails>> = _transactionList
    val batchList: StateFlow<List<String>> = _batchList
    val startDateList: StateFlow<List<String>> = _startDateList
    val endDateList: StateFlow<List<String>> = _endDateList
    var allTransactionList: List<TxnEntity>? = null
    private val objRoot = MutableStateFlow(ObjRootAppPaymentDetails())
    var userApiServiceErrorHolder = MutableStateFlow(ApiServiceError())
    val isPrinting = mutableStateOf(false)
    val isCustomer = mutableStateOf(false)
    private var isFiltered = false

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
    fun filterTransactionsByDateRange(startDate: LocalDateTime,endDate: LocalDateTime) {
        viewModelScope.launch {
            // Filter the transactions that occurred between the start date and end date
            val filteredList = _transactionList.value.filter { transaction ->
                val transactionDateTime = LocalDateTime.parse(transaction.dateTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                isFiltered = true
                Log.d("TransactionFilter", "Transaction DateTime: $transactionDateTime, Start Date: $startDate, End Date: $endDate")
                // Check if the transaction date and time is within the specified range
                transactionDateTime.isAfter(startDate) && transactionDateTime.isBefore(endDate)

            }

            Log.d("TransactionFilter", "Filtered Transactions: $filteredList")
            // Update the filterTxn value with the filtered list
            _transactionList.value = filteredList
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun filterTransactionsForBatch() {
        viewModelScope.launch {
            val batchIds = dbRepository.fetchTransactionDetailsByBatchId()
            _batchList.value = batchIds
            Log.d("db  batch data", batchIds.toString())
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun filterTransactionsByBatchId(batchId:String) {
        viewModelScope.launch {
            allTransactionList = dbRepository.fetchTransactionByBatch(batchId)
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

    fun fetchStartDates(batchIds: List<String>) {
        viewModelScope.launch {
            try {
                val startDates = mutableListOf<String>()
                batchIds.forEach { batchId ->
                    val minStartDate = dbRepository.getStartDate(batchId)
                    minStartDate.let {
                        startDates.add(it.toString()) // Add the non-null start date to the list
                    }
                }
                _startDateList.value = startDates
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
            .count { it.txnType == txn} // Count the transactions of the specified type
            ?: 0 // Return 0 if the list is null
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
            //delete entery from db
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
                    Log.d(TAG, "Printer status retrieved: $result")

                    val subtitleText = when (result) {
                        -1 -> context.resources.getString(R.string.printer_out_of_paper) // Example error for result -1
                        else -> context.resources.getString(R.string.printer_busy) // Default error for other cases
                    }

                    if (result != 0) {
                        Log.d(TAG, "Printer status retrieved inside result not equal to zero: $result")
                        CustomDialogBuilder.composeAlertDialog(
                            title = context.resources.getString(R.string.printer_error_title),
                            subtitle = subtitleText // Dynamic subtitle based on result
                        )
                    } else {
                        // If the printer status is OK, call initPrinter
                        launch { // Start a new coroutine to call initPrinter
                            try {
                                initPrinter(context,sharedViewModel ,isSummaryReport ,objRootAppPaymentDetail, object : IPrinterResultProviderListener {
                                    override fun onSuccess(result: Any?) {
                                        Log.d(TAG, "Printer initialized successfully.")
                                    }

                                    override fun onFailure(exception: Exception) {
                                        Log.e(TAG, "Failed to initialize printer: ${exception.message}")
                                        // Handle failure for printer initialization here
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
                    // Handle failure for getting printer status here
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
                Log.d(TAG, "Approved View Model to Printer Service Repository 1")
                CustomDialogBuilder.composeProgressDialog(
                    title = context.resources.getString(R.string.printing),
                    subtitle = context.resources.getString(R.string.plz_wait)
                )
                val requestDetails =
                    PaymentServiceUtils.objectToJsonString(objRootAppPaymentDetail)
                PrinterServiceRepository(PaymentServiceUtils.jsonStringToObject<PaymentServiceTxnDetails>(requestDetails)).initPrinter(context, iPrinterResultProviderListener)
                if(isSummaryReport) {
                    addDetailedReceipt(
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
                    addReceiptDetails(sharedViewModel,objRootAppPaymentDetail,object : IPrinterResultProviderListener{
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
                Log.d(TAG, "Approved View Model to Printer Service Repository 2 ${PaymentServiceUtils.jsonStringToObject<PaymentServiceTxnDetails>(requestDetails)}")
            } catch (e: Exception) {
                AppLogger.d(AppLogger.MODULE.APP_UI, e.message ?: "")
            }
        }

    }


    suspend fun addReceiptDetails(
        sharedViewModel: SharedViewModel,
        objRootAppPaymentDetail: ObjRootAppPaymentDetails,
        iPrinterResultProviderListener: IPrinterResultProviderListener
    ) {
        // Create an instance of ReceiptBuilder
        val receiptBuilder = ReceiptBuilder()

        // Use IO context for background processing
        withContext(Dispatchers.IO) {
            val paymentServiceTxnDetails = PaymentServiceUtils.jsonStringToObject<PaymentServiceTxnDetails>(
                PaymentServiceUtils.objectToJsonString(objRootAppPaymentDetail)
            )

            // Generate the receipt
            // Generate the summary report using ReceiptBuilder
            val summaryReport = receiptBuilder.createSummaryReport(sharedViewModel, paymentServiceTxnDetails)

            // Create separate lists for label, value, and description
            val labelList: List<String> = summaryReport.summaryFields.map { it.first }
            val valueList: List<String> = summaryReport.summaryFields.map { it.second }
            val descriptionList: List<String> = summaryReport.summaryFields.map { it.third }

            // Pass the receipt details to the PrinterServiceRepository
            PrinterServiceRepository(paymentServiceTxnDetails).printLeftCenterRightDetails(
                labelList,
                valueList,
                descriptionList,
                iPrinterResultProviderListener
            )
        }
    }

    suspend fun addDetailedReceipt(
        objRootAppPaymentDetail: ObjRootAppPaymentDetails,
        transactionList: List<ObjRootAppPaymentDetails>, // Assuming this is the input type
        iPrinterResultProviderListener: IPrinterResultProviderListener
    ) {
        // Create an instance of ReceiptBuilder
        val receiptBuilder = ReceiptBuilder()

        // Use IO context for background processing
        withContext(Dispatchers.IO) {
            val paymentServiceTxnDetails = PaymentServiceUtils.jsonStringToObject<PaymentServiceTxnDetails>(
                PaymentServiceUtils.objectToJsonString(objRootAppPaymentDetail)
            )

            // Map ObjRootAppPaymentDetails to TransactionDetails
            val transactionDetailsList = transactionList.map { paymentDetail ->
                // Create TransactionDetails based on ObjRootAppPaymentDetails
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

            // Generate the receipt
            val detailedReport = receiptBuilder.createDetailReport(paymentServiceTxnDetails, transactionDetailsList)

            // Create separate lists for label, value, and description
            val labelList: List<String> = detailedReport.detailFields.map { it.first }
            val valueList: List<String> = detailedReport.detailFields.map { it.second }
            val descriptionList: List<String> = detailedReport.detailFields.map { it.third }

            // Pass the receipt details to the PrinterServiceRepository
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

    fun resetTransactionList()
    {
        viewModelScope.launch {
        if (isFiltered) {
            fetchTransactions() // Re-fetch the full transaction list from the repository
            isFiltered = false
            }
        }
    }
}
