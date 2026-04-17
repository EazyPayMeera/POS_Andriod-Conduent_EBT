package com.eazypaytech.posafrica.features.txnList.ui

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.eazypaytech.paymentservicecore.constants.AppConstants
import com.analogics.paymentservicecore.data.listeners.responseListener.IApiServiceResponseListener
import com.analogics.paymentservicecore.data.model.PaymentServiceTxnDetails
import com.analogics.paymentservicecore.data.model.error.ApiServiceError
import com.analogics.paymentservicecore.data.model.error.ApiServiceTimeout
import com.analogics.paymentservicecore.data.model.TxnType
import com.analogics.paymentservicecore.domain.repository.apiService.ApiServiceRepository
import com.analogics.paymentservicecore.utils.PaymentServiceUtils
import com.eazypaytech.posafrica.R
import com.eazypaytech.posafrica.features.activity.ui.SharedViewModel
import com.eazypaytech.posafrica.features.dialogs.ui.CustomDialogBuilder
import com.eazypaytech.posafrica.domain.model.ObjRootAppPaymentDetails
import com.eazypaytech.posafrica.core.utils.miscellaneous.PrinterUtils
import com.eazypaytech.posafrica.core.utils.miscellaneous.ReportBuilder
import com.analogics.securityframework.data.repository.TxnDBRepository
import com.analogics.securityframework.database.entity.BatchEntity
import com.analogics.securityframework.database.entity.TxnEntity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
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
    private val _showProgress = MutableStateFlow<Boolean>(false)
    private val _isBatchOpen = MutableStateFlow<Boolean>(false)

    val txnList: StateFlow<List<ObjRootAppPaymentDetails>> = _txnList
    val batchList: StateFlow<List<BatchEntity>> = _batchList
    val listTypeLabel : StateFlow<String> = _listTypeLabel
    val showFilterMenu : StateFlow<Boolean> = _showFilterMenu
    val showBatchPicker : StateFlow<Boolean> = _showBatchPicker
    val showDateTimePicker : StateFlow<Boolean> = _showDateTimePicker
    val showProgress : StateFlow<Boolean> = _showProgress
    val isBatchOpen : StateFlow<Boolean> = _isBatchOpen
    val minAnimDelayMS = 500L

    lateinit var navHostController: NavHostController
    private val objRoot = MutableStateFlow(ObjRootAppPaymentDetails())

    fun fetchAllTrans() {
        viewModelScope.launch {
            _showProgress.value = true
            dbRepository.getAllTxnListData()?.takeIf { it.isNotEmpty() }?.let {
                val txnDataList = convertTxnEntityListToTxnDataList(it)
                    .sortedByDescending { txnData -> txnData.dateTime }
                _txnList.value = txnDataList
                _listTypeLabel.value =
                    txnDataList[txnDataList.size - 1].dateTime.toString() + " " + navHostController.context.resources.getString(
                        R.string.to
                    ) +
                            "\n" + txnDataList[0].dateTime.toString()
                _isBatchOpen.value = false
            } ?: let {
                _listTypeLabel.value = ""
                _isBatchOpen.value = false
            }
            _showProgress.value = false
        }
    }

    @SuppressLint("SuspiciousIndentation")
    @RequiresApi(Build.VERSION_CODES.O)
    fun filterTransactionsByStartEndDate(startDate: LocalDateTime?, endDate: LocalDateTime?) {
        viewModelScope.launch {
            _showProgress.value = true
            dbRepository.getTransactionsByDateRange(
                startDate?.format(DateTimeFormatter.ofPattern(AppConstants.DEFAULT_DATE_TIME_FORMAT))?.toString() ?: "",
                endDate?.format(DateTimeFormatter.ofPattern(AppConstants.DEFAULT_DATE_TIME_FORMAT))?.toString() ?: ""
            )?.let {
                val txnDataList = convertTxnEntityListToTxnDataList(it)
                    .sortedByDescending { txnData -> txnData.dateTime }
                _txnList.value = txnDataList
            } ?: let {
                _txnList.value = emptyList()
            }

            _listTypeLabel.value =
                startDate?.format(DateTimeFormatter.ofPattern(AppConstants.DEFAULT_DATE_TIME_FORMAT))
                    .toString() + " " + navHostController.context.resources.getString(R.string.to) +
                        "\n" +
                        endDate?.format(DateTimeFormatter.ofPattern(AppConstants.DEFAULT_DATE_TIME_FORMAT))
                            .toString()
            _isBatchOpen.value = false
            _showProgress.value = false
        }
    }

    fun filterTransactionsByBatchId(batchId:String) {
        _listTypeLabel.value =
            navHostController.context.resources.getString(R.string.lbl_batch_id) + (batchId.toIntOrNull()
                ?: "")
        viewModelScope.launch {
            _showProgress.value = true
            dbRepository.isBatchOpen(batchId).let { _isBatchOpen.value = it }
            dbRepository.fetchTxnListByBatchId(batchId)?.let {
                val txnList = convertTxnEntityListToTxnDataList(it)
                    .sortedByDescending { txnData -> txnData.dateTime }
                _txnList.value = txnList
            }
            _showProgress.value = false
        }
    }

    fun fetchCurrentBatchTrans() {
        viewModelScope.launch {
            dbRepository.fetchBatchList()?.takeIf { it.isNotEmpty() }?.let {
                _batchList.value = it
            }?.also {
                filterTransactionsByBatchId(_batchList.value[0].batchId ?: "")
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

    fun getPurchaseTotal(): Double {
        return ReportBuilder(_txnList.value).getPurchaseTotal()
    }

    fun getRefundTotal(): Double {
        return ReportBuilder(_txnList.value).getRefundTotal()
    }

    fun getNetTotal(): Double {
        return ReportBuilder(_txnList.value).getNetTotal()
    }

    fun totalTransactionsCount(txn: TxnType): Int {
        return _txnList.value
            .count { it.txnType == txn}
    }

    fun totalTipAmount(): Double {
        val purchaseTipTotal = _txnList.value
            .filter { it.txnType == TxnType.FOOD_PURCHASE }
            .sumOf { it.tip ?: 0.0 } // Convert BigDecimal to Double

        val refundTipTotal = _txnList.value
            .filter { it.txnType == TxnType.FOODSTAMP_RETURN }
            .sumOf { it.tip ?: 0.0 } // Convert BigDecimal to Double

        return purchaseTipTotal - refundTipTotal
    }

    fun totalTipCount(): Int {
        val purchaseTipCount = _txnList.value
            .filter { it.txnType == TxnType.FOOD_PURCHASE && it.tip != null && it.tip != 0.0 } // Count PURCHASE transactions with a valid tip

        val refundTipCount = _txnList.value
            .filter { it.txnType == TxnType.FOODSTAMP_RETURN && it.tip != null && it.tip != 0.0 } // Count REFUND transactions with a valid tip

        return purchaseTipCount.size - refundTipCount.size // Subtract the refund count from the purchase count
    }


//    fun onApiBatchClose() {
//        viewModelScope.launch {
//            try {
//                val requestDetails =
//                    PaymentServiceUtils.objectToJsonString(_txnList.value)
//                apiServiceRepository.apiServiceBatch(
//                    PaymentServiceUtils.jsonStringToObject<PaymentServiceTxnDetails>(requestDetails), this@TxnViewModel)
//            } catch (e: Exception) {
//                AppLogger.d(AppLogger.MODULE.APP_UI, e.message ?: "")
//            }
//        }
//    }

    override fun onApiServiceSuccess(paymentServiceTxnDetails: PaymentServiceTxnDetails) {
        PaymentServiceUtils.transformObject<ObjRootAppPaymentDetails>(paymentServiceTxnDetails)?.let {
            objRoot.value = it
        }
    }

    override fun onApiServiceError(paymentError: ApiServiceError) {
        Log.e("API Response", paymentError.errorMessage)
    }

    override fun onApiServiceTimeout(apiServiceTimeout: ApiServiceTimeout) {
        CustomDialogBuilder.Companion.composeAlertDialog(title = navHostController.context.resources?.getString(
            R.string.default_alert_title_error),message = apiServiceTimeout.message)
    }

    fun printSummary(
        context: Context,
        listObjRootAppPaymentDetail: List<ObjRootAppPaymentDetails>?
    ) {
        PrinterUtils.printSummary(context,listObjRootAppPaymentDetail)
    }

    fun printDetailed(
        context: Context,
        listObjRootAppPaymentDetail: List<ObjRootAppPaymentDetails>?
    ) {
        PrinterUtils.printDetailed(context,listObjRootAppPaymentDetail)
    }

    override fun onApiServiceDisplayProgress(
        show: Boolean,
        title: String?,
        subTitle: String?,
        message: String?
    ) {
        CustomDialogBuilder.Companion.composeProgressDialog(show = show, title = title, subtitle = subTitle, message = message)
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