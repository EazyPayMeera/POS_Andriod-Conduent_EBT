package com.analogics.tpaymentsapos.rootUiScreens.txnList.viewModel

import android.content.ContentValues.TAG
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.analogics.paymentservicecore.constants.AppConstants
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
import getPrinterStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
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
    private val _isBatchOpen = MutableStateFlow<Boolean>(false)
    lateinit var navHostController: NavHostController

    val txnList: StateFlow<List<ObjRootAppPaymentDetails>> = _txnList
    val batchList: StateFlow<List<BatchEntity>> = _batchList
    val listTypeLabel : StateFlow<String> = _listTypeLabel
    val showFilterMenu : StateFlow<Boolean> = _showFilterMenu
    val showBatchPicker : StateFlow<Boolean> = _showBatchPicker
    val showDateTimePicker : StateFlow<Boolean> = _showDateTimePicker
    val isBatchOpen : StateFlow<Boolean> = _isBatchOpen
    private val objRoot = MutableStateFlow(ObjRootAppPaymentDetails())

    fun fetchAllTrans() {
        viewModelScope.launch {
            dbRepository.getAllTxnListData()?.takeIf { it.isNotEmpty() }?.let {
                val txnDataList = convertTxnEntityListToTxnDataList(it)
                    .sortedByDescending { txnData -> txnData.dateTime }
                _txnList.value = txnDataList
                _listTypeLabel.value =
                    txnDataList[txnDataList.size-1].dateTime.toString() + " " + navHostController.context.resources.getString(R.string.to) +
                            "\n" + txnDataList[0].dateTime.toString()
                _isBatchOpen.value = false
            }?:let {
                _listTypeLabel.value = ""
                _isBatchOpen.value = false
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun filterTransactionsByStartEndDate(startDate: LocalDateTime?, endDate: LocalDateTime?) {
        viewModelScope.launch {
                dbRepository.getTransactionsByDateRange(startDate?.toString()?:"",
                    endDate?.toString()?:""
                )?.let {
                val txnDataList = convertTxnEntityListToTxnDataList(it)
                    .sortedByDescending { txnData -> txnData.dateTime }
                _txnList.value = txnDataList
            }?:let{
                    _txnList.value = emptyList()
                }

            _listTypeLabel.value =
                startDate?.format(DateTimeFormatter.ofPattern(AppConstants.DEFAULT_DATE_TIME_FORMAT)).toString() + " " + navHostController.context.resources.getString(R.string.to) +
                        "\n" +
                        endDate?.format(DateTimeFormatter.ofPattern(AppConstants.DEFAULT_DATE_TIME_FORMAT)).toString()
            _isBatchOpen.value = false
        }
    }

    fun filterTransactionsByBatchId(batchId:String) {
        _listTypeLabel.value = navHostController.context.resources.getString(R.string.lbl_batch_id)+(batchId.toIntOrNull()?:"")
        viewModelScope.launch {
            dbRepository.isBatchOpen(batchId).let { _isBatchOpen.value = it }
            dbRepository.fetchTxnListByBatchId(batchId)?.let {
                val txnList = convertTxnEntityListToTxnDataList(it)
                    .sortedByDescending { txnData -> txnData.dateTime }
                _txnList.value = txnList
            }
        }
    }

    fun fetchCurrentBatchTrans() {
        viewModelScope.launch {
            dbRepository.fetchBatchList()?.takeIf { it.isNotEmpty() }?.let {
                delay(400)
                _batchList.value = it
            }?.also {
                filterTransactionsByBatchId(_batchList.value[0].batchId?:"")
            }
        }
    }

    fun onLoad(navHostController : NavHostController) {
        this.navHostController = navHostController
        fetchCurrentBatchTrans()
    }

    fun onFilterClick()
    {
        onDismissMenu()
        _showFilterMenu.value = true
    }

    fun onDateTimeFilterClick()
    {
        onDismissMenu()
        _showDateTimePicker.value = true
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun onDateTimeFilterApplied(startDate: LocalDateTime?, endDate: LocalDateTime?)
    {
        filterTransactionsByStartEndDate(startDate,endDate)
    }

    fun onBatchFilterClick()
    {
        onDismissMenu()
        _showBatchPicker.value = true
    }

    fun onSeeAllClicked()
    {
        onDismissMenu()
        fetchAllTrans()
    }

    fun onDismissMenu()
    {
        _showBatchPicker.value = false
        _showFilterMenu.value = false
        _showDateTimePicker.value = false
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


    override fun onApiServiceDisplayProgress(
        show: Boolean,
        title: String?,
        subTitle: String?,
        message: String?
    ) {
        CustomDialogBuilder.composeProgressDialog(show = show, title = title, subtitle = subTitle, message = message)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun closeOpenBatches(sharedViewModel: SharedViewModel) {
        viewModelScope.launch {
            try {
                val closedCount = dbRepository.closeBatch().let {
                    fetchCurrentBatchTrans()    /* To refresh the batch list & statues */
                    if(it>=1) setNextBatchId(sharedViewModel)
                }
                Log.d("BatchViewModel", "Closed $closedCount open batches")
            } catch (e: Exception) {
                Log.e("BatchViewModel", "Error closing open batches", e)
            }
        }
    }

    fun setNextBatchId(sharedViewModel: SharedViewModel)
    {
        viewModelScope.launch{
            dbRepository.fetchLastBatchId()?.let {
                sharedViewModel.objPosConfig?.apply {
                    batchId = ((it.toIntOrNull()?:0) + 1).toString()
                }?.saveToPrefs()
            }
        }
    }
}
