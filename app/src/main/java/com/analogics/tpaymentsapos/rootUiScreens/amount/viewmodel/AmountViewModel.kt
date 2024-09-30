package com.analogics.tpaymentsapos.rootUiScreens.amount.viewmodel

import android.annotation.SuppressLint
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.navigation.NavHostController
import com.analogics.paymentservicecore.models.TxnInfo
import com.analogics.paymentservicecore.models.TxnType
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import com.analogics.tpaymentsapos.rootUiScreens.activity.SharedViewModel
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.formatAmount
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.formatAmountToDouble

class AmountViewModel : ViewModel() {

    var transAmount by mutableStateOf("")
        private set

    fun onLoad(sharedViewModel: SharedViewModel)
    {
        transAmount.ifEmpty { transAmount = formatAmount(sharedViewModel.objRootAppPaymentDetail.txnAmount?:0.00) }
    }

    fun onAmountChange(newValue: String) :String{
        transAmount = formatAmount(newValue)
        return formatAmountToDouble(newValue).toString()
    }

    fun onConfirm(navHostController: NavHostController, sharedViewModel: SharedViewModel) {
        calculateTotal(sharedViewModel)
        when(TxnInfo.txnType) {
            TxnType.REFUND,TxnType.PREAUTH -> {
                navHostController.navigate(AppNavigationItems.CardScreen.route)
            }
            TxnType.VOID,TxnType.AUTHCAP -> {
                navHostController.navigate(AppNavigationItems.PleaseWaitScreen.route)
            }
            else -> {
                navHostController.navigate(AppNavigationItems.ConfirmationScreen.route)
            }
        }
    }

    fun onCancel(navHostController: NavHostController) {
        navHostController.navigate(AppNavigationItems.TrainingScreen.route)
    }

    private fun calculateTax(txnAmount: Double, percent: Double) : Double {
        return txnAmount * percent / 100.00
    }

    @SuppressLint("SuspiciousIndentation")
    private fun calculateTotal(sharedViewModel: SharedViewModel)  {
        sharedViewModel.objRootAppPaymentDetail.txnAmount = formatAmountToDouble(transAmount)
        sharedViewModel.objRootAppPaymentDetail.CGST = calculateTax(sharedViewModel.objRootAppPaymentDetail.txnAmount?:0.00,10.00)
        sharedViewModel.objRootAppPaymentDetail.SGST = calculateTax(sharedViewModel.objRootAppPaymentDetail.txnAmount?:0.00,10.00)
        sharedViewModel.objRootAppPaymentDetail.ttlAmount = (sharedViewModel.objRootAppPaymentDetail.txnAmount?:0.00)
                                                            + (sharedViewModel.objRootAppPaymentDetail.CGST?:0.00)
                                                            + (sharedViewModel.objRootAppPaymentDetail.SGST?:0.00)
    }
}