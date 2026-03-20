package com.eazypaytech.posafrica.rootUiScreens.amount.viewmodel

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.navigation.NavHostController
import com.eazypaytech.paymentservicecore.models.TxnType
import com.eazypaytech.posafrica.navigation.AppNavigationItems
import com.eazypaytech.posafrica.rootUiScreens.activity.SharedViewModel
import com.eazypaytech.posafrica.rootUiScreens.dialogs.CustomDialogBuilder
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.formatAmount
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.navigateAndClean
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.transformToAmountDouble
import com.eazypaytech.posafrica.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class CashBackViewModel @Inject constructor() : ViewModel() {

    var cashBackAmount by mutableStateOf("")
        private set

    var isReadOnly by mutableStateOf(true)
        private set

    private val _totalAmount = MutableStateFlow<String?>(null)
    val totalAmount: StateFlow<String?> = _totalAmount
    private val _origTotalAmount = MutableStateFlow<String?>(null)
    val origTotalAmount: StateFlow<String?> = _origTotalAmount

    private val _timeDate = MutableStateFlow<String?>(null)
    private val _origDateTime = MutableStateFlow<String?>(null)
    val origDateTime: StateFlow<String?> = _origDateTime

    @RequiresApi(Build.VERSION_CODES.O)
    fun onLoad(sharedViewModel: SharedViewModel)
    {
        cashBackAmount.ifEmpty {
            cashBackAmount = formatAmount(sharedViewModel.objRootAppPaymentDetail.cashback ?: 0.00)
            _origTotalAmount.value = formatAmount(sharedViewModel.objRootAppPaymentDetail.originalTtlAmount?.toDoubleOrNull() ?: 0.00)
            _origDateTime.value = sharedViewModel.objRootAppPaymentDetail.dateTime
            isReadOnly = false
        }
    }

    fun onCashBackAmountChange(newValue: String) :String{
        cashBackAmount = formatAmount(newValue)
        return transformToAmountDouble(newValue).toString()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun onConfirm(navHostController: NavHostController, sharedViewModel: SharedViewModel) {
        sharedViewModel.objRootAppPaymentDetail.cashback = transformToAmountDouble(cashBackAmount)
        if (sharedViewModel.objRootAppPaymentDetail.txnType == TxnType.PURCHASE_CASHBACK) {
            sharedViewModel.objPosConfig?.apply { isCashback = false }
        }
        if(transformToAmountDouble(cashBackAmount)<0.01) {
            CustomDialogBuilder.composeAlertDialog(
                title = navHostController.context.getString(R.string.default_alert_title_error),
                message = navHostController.context.getString(R.string.err_zero_amt_not_allowed)
            )
        }
        else {
            calculateTotal(sharedViewModel)
            navHostController.navigate(AppNavigationItems.CardScreen.route)
        }
    }

    fun onCancel(navHostController: NavHostController) {
        navHostController.navigateAndClean(AppNavigationItems.DashBoardScreen.route)
    }

    @SuppressLint("SuspiciousIndentation")
    private fun calculateTotal(sharedViewModel: SharedViewModel) {
        sharedViewModel.objRootAppPaymentDetail.ttlAmount =
            (sharedViewModel.objRootAppPaymentDetail.txnAmount ?: 0.0) +
                    transformToAmountDouble(cashBackAmount)
    }


}