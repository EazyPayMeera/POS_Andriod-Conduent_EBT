package com.eazypaytech.pos.features.cashback.ui

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.navigation.NavHostController
import com.analogics.paymentservicecore.data.model.TxnType
import com.eazypaytech.pos.R
import com.eazypaytech.pos.features.activity.ui.SharedViewModel
import com.eazypaytech.pos.features.dialogs.ui.CustomDialogBuilder
import com.eazypaytech.pos.navigation.AppNavigationItems
import com.eazypaytech.pos.core.utils.formatAmount
import com.eazypaytech.pos.core.utils.navigateAndClean
import com.eazypaytech.pos.core.utils.transformToAmountDouble
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class CashBackViewModel @Inject constructor() : ViewModel() {

    var cashBackAmount by mutableStateOf("")
        private set

    /**
     * Handles cashback amount input change.
     *
     * Flow:
     * - Formats entered amount
     * - Updates internal cashback state
     * - Returns numeric value as string
     */
    fun onCashBackAmountChange(newValue: String) :String{
        cashBackAmount = formatAmount(newValue)
        return transformToAmountDouble(newValue).toString()
    }


    /**
     * Handles confirm action for cashback.
     *
     * Flow:
     * - Saves cashback amount to transaction
     * - Disables cashback flag for PURCHASE_CASHBACK flow
     * - Validates minimum amount (> 0.01)
     * - Calculates total amount
     * - Navigates to Card screen
     */
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

    /**
     * Handles cancel action and navigates back to Dashboard.
     */
    fun onCancel(navHostController: NavHostController) {
        navHostController.navigateAndClean(AppNavigationItems.DashBoardScreen.route)
    }

    /**
     * Calculates total transaction amount.
     *
     * Formula:
     * total = txnAmount + cashback
     */
    @SuppressLint("SuspiciousIndentation")
    private fun calculateTotal(sharedViewModel: SharedViewModel) {
        sharedViewModel.objRootAppPaymentDetail.ttlAmount =
            (sharedViewModel.objRootAppPaymentDetail.txnAmount ?: 0.0) +
                    transformToAmountDouble(cashBackAmount)
    }

}