package com.analogics.tpaymentsapos.rootUiScreens.txnList.viewModel

import android.content.ContentValues.TAG
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.analogics.paymentservicecore.listeners.responseListener.IApiServiceResponseListener
import com.analogics.paymentservicecore.listeners.responseListener.IPrinterResultProviderListener
import com.analogics.paymentservicecore.logger.AppLogger
import com.analogics.paymentservicecore.model.PaymentServiceTxnDetails
import com.analogics.paymentservicecore.model.error.ApiServiceError
import com.analogics.paymentservicecore.models.TxnStatus
import com.analogics.paymentservicecore.models.TxnType
import com.analogics.paymentservicecore.repository.apiService.ApiServiceRepository
import com.analogics.paymentservicecore.utils.PaymentServiceUtils
import com.analogics.securityframework.database.dbRepository.TxnDBRepository
import com.analogics.securityframework.database.entity.BatchEntity
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
    private val _txnList = MutableStateFlow<List<ObjRootAppPaymentDetails>>(emptyList())
    private val _batchList = MutableStateFlow<List<BatchEntity>>(emptyList())
    private val _listTypeLabel = MutableStateFlow<String>("")
    private val _showFilterMenu = MutableStateFlow<Boolean>(false)
    private val _showBatchPicker = MutableStateFlow<Boolean>(false)
    private val _showDateTimePicker = MutableStateFlow<Boolean>(false)


    val isClosedBatchEnabled = mutableStateOf(false)

    private val _openBatch = MutableStateFlow<String?>(null)
    val openBatch: StateFlow<String?> = _openBatch

    val txnList: StateFlow<List<ObjRootAppPaymentDetails>> = _txnList
    val batchList: StateFlow<List<BatchEntity>> = _batchList
    val listTypeLabel : StateFlow<String> = _listTypeLabel
    val showFilterMenu : StateFlow<Boolean> = _showFilterMenu
    val showBatchPicker : StateFlow<Boolean> = _showBatchPicker
    val showDateTimePicker : StateFlow<Boolean> = _showDateTimePicker
    var allTransactionList: List<TxnEntity>? = null
    private val objRoot = MutableStateFlow(ObjRootAppPaymentDetails())
    var userApiServiceErrorHolder = MutableStateFlow(ApiServiceError())
    private var isFiltered = false

    /*init {
        viewModelScope.launch {
            fetchTransactions()
        }
    }*/

    fun fetchTransactions() {
        viewModelScope.launch {
            allTransactionList = dbRepository.getAllTxnListData()
            allTransactionList?.let {
                // Convert the list to TxnDataList and sort in descending order (assuming there's a timestamp or ID to sort by)
                val txnDataList = convertTxnEntityListToTxnDataList(it)
                    .sortedByDescending { txnData -> txnData.dateTime }  // Replace transactionDate with your sorting field
                _txnList.value = txnDataList
            }
        }
    }
    fun fetchTransactionDetailsTxnByDate(date: String){
        viewModelScope.launch {
            allTransactionList =dbRepository.fetchTransactionDetailsTxnByDate(date)
            allTransactionList?.let {
                val txnDataList = convertTxnEntityListToTxnDataList(it)
                _txnList.value = txnDataList
            }
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun filterTransactionsByDateRange(startDate: LocalDateTime,endDate: LocalDateTime) {
        viewModelScope.launch {
            val filteredList = _txnList.value.filter { transaction ->
                val transactionDateTime = LocalDateTime.parse(transaction.dateTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                transactionDateTime.isAfter(startDate) && transactionDateTime.isBefore(endDate)

            }
            _txnList.value = filteredList
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
                    .sortedByDescending { txnData -> txnData.dateTime }
                _txnList.value = txnDataList
            }
        }
    }

    fun setListTypeLabel(label: String) {
        _listTypeLabel.value = label
    }

    fun filterTransactionsByBatchId(batchId:String) {
        _listTypeLabel.value = "Batch #${batchId.toInt()}"
        viewModelScope.launch {
            dbRepository.fetchTxnListByBatchId(batchId)?.let {
                val txnList = convertTxnEntityListToTxnDataList(it)
                    .sortedByDescending { txnData -> txnData.dateTime }
                _txnList.value = txnList
            }
        }
    }

    fun fetchCurrentBatchTrans() {
        viewModelScope.launch {
            dbRepository.fetchBatchList()?.let {
                _batchList.value = it
            }?.also {
                filterTransactionsByBatchId(_batchList.value[0].batchId?:"")
            }
        }
    }

    fun onLoad() {
        fetchCurrentBatchTrans()
    }

    fun onFilterClick()
    {
        _showFilterMenu.value = true

        _showDateTimePicker.value = false
        _showBatchPicker.value = false
    }

    fun onDateTimeFilterClick()
    {
        _showDateTimePicker.value = true

        _showFilterMenu.value = false
        _showBatchPicker.value = false

    }

    fun onBatchFilterClick()
    {
        _showBatchPicker.value = true

        _showFilterMenu.value = false
        _showDateTimePicker.value = false
    }

    fun onDismissMenu()
    {
        _showBatchPicker.value = false
        _showFilterMenu.value = false
        _showDateTimePicker.value = false
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun isBatchOpen() {
        viewModelScope.launch {
            try {
                // Fetch the open batch ID (or null if no open batch exists)
                val batchId: String? = dbRepository.openBatchId()
                Log.d("Batch Id", "Open batch ID: $batchId")
                batchId?.let { setCloseBatchBtnState(true) }?:setCloseBatchBtnState(false)
            } catch (e: Exception) {
                Log.e("BatchViewModel", "Error fetching open batch ID", e)
            }
        }
    }

    private fun setCloseBatchBtnState(enabled: Boolean) {
        isClosedBatchEnabled.value = enabled
    }

    private fun convertTxnEntityListToTxnDataList(txnEntityList: List<TxnEntity>): List<ObjRootAppPaymentDetails> {
        val gson = Gson()
        val json = gson.toJson(txnEntityList)
        val txnDataListType = object : TypeToken<List<ObjRootAppPaymentDetails>>() {}.type
        return gson.fromJson(json, txnDataListType)
    }

    fun totalPurchaseTransactions(txn: TxnType): Double {
        return _txnList.value
            .filter { it.txnType == txn && it.txnStatus == TxnStatus.APPROVED }
            .sumOf {
                it.ttlAmount ?: 0.0
            }
    }

    fun totalTransactionsCount(txn: TxnType): Int {
        return _txnList.value
            .count { it.txnType == txn}
    }

    fun totalTipAmount(): Double {
        val purchaseTipTotal = _txnList.value
            .filter { it.txnType == TxnType.PURCHASE }
            .sumOf { it.tip ?: 0.0 } // Convert BigDecimal to Double

        val refundTipTotal = _txnList.value
            .filter { it.txnType == TxnType.REFUND }
            .sumOf { it.tip ?: 0.0 } // Convert BigDecimal to Double

        return purchaseTipTotal - refundTipTotal
    }

    fun totalTipCount(): Int {
        val purchaseTipCount = _txnList.value
            .filter { it.txnType == TxnType.PURCHASE && it.tip != null && it.tip != 0.0 } // Count PURCHASE transactions with a valid tip

        val refundTipCount = _txnList.value
            .filter { it.txnType == TxnType.REFUND && it.tip != null && it.tip != 0.0 } // Count REFUND transactions with a valid tip

        return purchaseTipCount.size - refundTipCount.size // Subtract the refund count from the purchase count
    }


    fun onApiBatchClose() {
        viewModelScope.launch {
            try {
                val requestDetails =
                    PaymentServiceUtils.objectToJsonString(_txnList.value)
                apiServiceRepository.apiServiceBatch(
                    PaymentServiceUtils.jsonStringToObject<PaymentServiceTxnDetails>(requestDetails), this@TxnViewModel)
            } catch (e: Exception) {
                AppLogger.d(AppLogger.MODULE.APP_UI, e.message ?: "")
            }
        }
    }

    override fun onApiServiceSuccess(paymentServiceTxnDetails: PaymentServiceTxnDetails) {
        PaymentServiceUtils.transformObject<ObjRootAppPaymentDetails>(paymentServiceTxnDetails)?.let {
            objRoot.value = it
        }
    }

    override fun onApiServiceError(paymentError: ApiServiceError) {
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
                Log.d(TAG, "Approved View Model to Printer Service Repository 1")
                CustomDialogBuilder.composePrintingDialog(
                    title = context.resources.getString(R.string.printing),
                    subtitle = context.resources.getString(R.string.plz_wait),
                    onClose = {

                    }
                )
                val requestDetails =
                    PaymentServiceUtils.objectToJsonString(objRootAppPaymentDetail)
                PrinterServiceRepository(PaymentServiceUtils.jsonStringToObject<PaymentServiceTxnDetails>(requestDetails)).initPrinter(context, iPrinterResultProviderListener)
                if(isSummaryReport) {
                    addDetailedReceipt(
                        sharedViewModel,
                        context,
                        objRootAppPaymentDetail,
                        txnList.value,
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

            val labelList: List<String> = summaryReport.summaryFields.map { it.label }
            val valueList: List<String> = summaryReport.summaryFields.map { it.value }
            val descriptionList: List<String> = summaryReport.summaryFields.map { it.description }
            val fontsize: List<Int> = summaryReport.summaryFields.map { field ->
                when (field.fontsize) { // Accessing the fourth element (the font size)
                    ReceiptBuilder.FontSize.Small -> 24
                    ReceiptBuilder.FontSize.Medium -> 28
                    ReceiptBuilder.FontSize.Big -> 32
                    else -> 24 // Default font size if no match
                }
            }
            PrinterServiceRepository(paymentServiceTxnDetails).printLeftCenterRightDetails(
                labelList,
                valueList,
                descriptionList,
                fontsize,
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
            val labelList: List<String> = detailedReport.detailFields.map { it.label }
            val valueList: List<String> = detailedReport.detailFields.map { it.quantity }
            val descriptionList: List<String> = detailedReport.detailFields.map { it.price }
            val fontsize: List<Int> = detailedReport.detailFields.map { field ->
                when (field.discount) { // Accessing the fourth element (the font size)
                    ReceiptBuilder.FontSize.Small -> 24
                    ReceiptBuilder.FontSize.Medium -> 28
                    ReceiptBuilder.FontSize.Big -> 32
                    else -> 24 // Default font size if no match
                }
            }
            PrinterServiceRepository(paymentServiceTxnDetails).printLeftCenterRightDetails(
                labelList,
                valueList,
                descriptionList,
                fontsize,
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

    override fun onApiServiceDisplayProgress(
        show: Boolean,
        title: String?,
        subTitle: String?,
        message: String?
    ) {
        CustomDialogBuilder.composeProgressDialog(show = show, title = title, subtitle = subTitle, message = message)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun closeOpenBatches() {
        viewModelScope.launch {
            try {
                val closedCount = dbRepository.closeBatch().let {
                    isBatchOpen()
                }
                Log.d("BatchViewModel", "Closed $closedCount open batches")
            } catch (e: Exception) {
                Log.e("BatchViewModel", "Error closing open batches", e)
            }
        }
    }

}
