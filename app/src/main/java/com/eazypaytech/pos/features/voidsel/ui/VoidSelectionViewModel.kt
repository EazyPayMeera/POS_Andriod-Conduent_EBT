package com.eazypaytech.pos.features.voidsel.ui

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.analogics.paymentservicecore.data.model.TxnType
import com.analogics.paymentservicecore.data.model.emv.CardEntryMode
import com.analogics.paymentservicecore.domain.repository.emvService.EmvServiceRepository
import com.analogics.paymentservicecore.utils.PaymentServiceUtils
import com.analogics.paymentservicecore.utils.toDecimalFormat
import com.analogics.securityframework.data.repository.TxnDBRepository
import com.eazypaytech.pos.R
import com.eazypaytech.pos.core.utils.navigateAndClean
import com.eazypaytech.pos.domain.model.ObjRootAppPaymentDetails
import com.eazypaytech.pos.features.activity.ui.SharedViewModel
import com.eazypaytech.pos.features.dialogs.ui.CustomDialogBuilder
import com.eazypaytech.pos.navigation.AppNavigationItems
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VoidSelectionViewModel @Inject constructor(
    private var emvServiceRepository: EmvServiceRepository,
    private val dbRepository: TxnDBRepository
) : ViewModel() {

    private val _availableTransactions = MutableStateFlow<List<TransactionItem>>(emptyList())
    val availableTransactions: StateFlow<List<TransactionItem>> = _availableTransactions.asStateFlow()

    // Backing store of raw DB records — used when user taps a transaction
    private val _rawTransactions = MutableStateFlow<List<Any>>(emptyList())

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val filteredTransactions: StateFlow<List<TransactionItem>> = combine(
        _availableTransactions,
        _searchQuery
    ) { transactions, query ->
        if (query.isEmpty()) transactions
        else transactions.filter {
            it.displayName.contains(query, ignoreCase = true) ||
//                    it.amount?.contains(query) == true ||
//                    it.dateTime?.contains(query) == true ||
                    it.rrn?.contains(query) == true ||
                    it.authCode?.contains(query) == true
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun onLoad(context: Context, sharedViewModel: SharedViewModel) {
        viewModelScope.launch {
            fetchListVoidTransaction(context, sharedViewModel)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun fetchListVoidTransaction(
        context: Context,
        sharedViewModel: SharedViewModel
    ) {

        val txnList = dbRepository.fetchAllVoidableTransactions()
        Log.d("DB_DEBUG", "voidable transactions: $txnList")
        txnList.forEach {
            Log.d("DB_DEBUG", "Transaction ID: ${it.id}")
        }
        if (txnList.isEmpty()) {
            CustomDialogBuilder.composeAlertDialog(
                title = context.getString(R.string.default_alert_title_error),
                message = context.getString(R.string.err_txn_not_found)
            )
            return
        }

        // Store raw records for later use when user selects a transaction
        _rawTransactions.value = txnList

        // Map DB records to UI model
        _availableTransactions.value = txnList.mapNotNull { txn ->
            val transformedTxn = PaymentServiceUtils.transformObject<ObjRootAppPaymentDetails>(txn)
            transformedTxn?.let {
                TransactionItem(
                    id          = it.id ?: 0L,
                    txnType     = TxnType.valueOf(it.txnType?.toString() ?: return@mapNotNull null),
                    displayName = it.txnType?.toString() ?: "",
                    amount      = it.ttlAmount.toDecimalFormat(),
                    dateTime    = it.dateTime,
                    rrn         = it.rrn,
                    authCode    = it.hostAuthCode
                )
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun onTransactionSelectedForVoid(
        selected: TransactionItem,
        navHostController: NavHostController,
        sharedViewModel: SharedViewModel,
        context: Context
    ) {
        viewModelScope.launch {
            // Find the raw DB record matching the selected item
            val rawTxn = _rawTransactions.value.find { raw ->
                val transformed = PaymentServiceUtils.transformObject<ObjRootAppPaymentDetails>(raw)
                transformed?.id == selected.id
            }

            if (rawTxn == null) {
                CustomDialogBuilder.composeAlertDialog(
                    title = context.getString(R.string.default_alert_title_error),
                    message = context.getString(R.string.err_txn_not_found)
                )
                return@launch
            }

            val transformedTxn = PaymentServiceUtils.transformObject<ObjRootAppPaymentDetails>(rawTxn)

            transformedTxn?.let { it ->
                sharedViewModel.objRootAppPaymentDetail = it.copy(
                    originalId = it.id,
                    id                   = sharedViewModel.objRootAppPaymentDetail.id,
                    txnType              = sharedViewModel.objRootAppPaymentDetail.txnType,
                    fnsNumber            = sharedViewModel.objPosConfig?.fnsNumber,
                    merchantNameLocation = sharedViewModel.objPosConfig?.merchantNameLocation,
                    merchantBankName     = sharedViewModel.objPosConfig?.merchantBankName,
                    merchantType         = sharedViewModel.objPosConfig?.merchantType,
                    procId               = sharedViewModel.objPosConfig?.procId,
                    stateCode            = sharedViewModel.objPosConfig?.stateCode,
                    countyCode           = sharedViewModel.objPosConfig?.countyCode,
                    postalServiceCode    = sharedViewModel.objPosConfig?.postalServiceCode

                )

                with(sharedViewModel.objRootAppPaymentDetail) {
                    isFallback         = it.isFallback
                    processingCode     = it.processingCode
                    rrn                = it.rrn
                    localTime          = it.localTime
                    localDate          = it.localDate
                    dateTime           = it.dateTime
                    settlementDate     = it.settlementDate
                    posConditionCode   = it.posConditionCode
                    stan               = it.stan
                    posEntryMode       = it.posEntryMode
                    cardEntryMode      = if (it.isFallback == true) CardEntryMode.FALLBACK_MAGSTRIPE
                    else it.cardEntryMode
                    originalTxnType    = it.txnType
                    currencyCode       = it.currencyCode
                    originalDateTime   = it.originalDateTime
                    hostAuthCode       = it.hostAuthCode
                    receiptEmvData     = it.receiptEmvData
                    originalCashback   = it.cashback.toDecimalFormat()
                    originalTtlAmount  = it.ttlAmount.toDecimalFormat()
                    originalTxnAmount  = it.txnAmount.toDecimalFormat()
                    originalHostTxnRef = it.hostTxnRef
                }
            }

            // Navigate based on txnType
            val destination = AppNavigationItems.AmountScreen.route
            navHostController.navigate(destination) {
                popUpTo(AppNavigationItems.EBTSelScreen.route) { inclusive = false }
                launchSingleTop = true
            }
        }
    }
}