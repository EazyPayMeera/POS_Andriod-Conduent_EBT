package com.eazypaytech.posafrica.features.cashback.ui

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.navigation.NavHostController
import com.eazypaytech.paymentservicecore.models.TxnType
import com.eazypaytech.posafrica.R
import com.eazypaytech.posafrica.features.activity.ui.SharedViewModel
import com.eazypaytech.posafrica.features.dialogs.ui.CustomDialogBuilder
import com.eazypaytech.posafrica.core.navigation.routes.AppNavigationItems
import com.eazypaytech.posafrica.core.utils.formatAmount
import com.eazypaytech.posafrica.core.utils.navigateAndClean
import com.eazypaytech.posafrica.core.utils.transformToAmountDouble
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

    private val _origDateTime = MutableStateFlow<String?>(null)
    val origDateTime: StateFlow<String?> = _origDateTime


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
        if(transformToAmountDouble(cashBackAmount) <0.01) {
            CustomDialogBuilder.Companion.composeAlertDialog(
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