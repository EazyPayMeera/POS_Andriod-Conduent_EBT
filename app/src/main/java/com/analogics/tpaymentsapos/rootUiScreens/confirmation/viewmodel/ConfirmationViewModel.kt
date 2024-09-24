package com.analogics.tpaymentsapos.rootUiScreens.confirmation.viewmodel

import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.navigation.NavHostController
import com.analogics.paymentservicecore.models.TxnType
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import com.analogics.tpaymentsapos.rootUiScreens.activity.SharedViewModel
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.calculateTip
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.calculateTotalAmount

class ConfirmationViewModel : ViewModel() {
    var totalAmount = mutableDoubleStateOf(0.00)
    var tipAmount = mutableDoubleStateOf(0.00)
    var selectedPercent = mutableDoubleStateOf(0.00)
    var selectedButton = mutableIntStateOf(0)
    var isTipEnabled = mutableStateOf(false)

    fun onTipPercentChange(button : Int, newValue: Double, sharedViewModel: SharedViewModel){
        selectedButton.intValue = button
        selectedPercent.doubleValue = newValue
        tipAmount.doubleValue = calculateTip(sharedViewModel.objRootAppPaymentDetail.txnAmount?:0.00, newValue/100.00)
        totalAmount.doubleValue = calculateTotalAmount(sharedViewModel.objRootAppPaymentDetail.txnAmount?:0.00, tipAmount.doubleValue, sharedViewModel.objRootAppPaymentDetail.CGST?:0.00, sharedViewModel.objRootAppPaymentDetail.SGST?:0.00)
    }

    fun onCustomTip(button : Int, navHostController: NavHostController){
        selectedButton.intValue = button
        navHostController.navigate(AppNavigationItems.TipScreen.route)
    }

    fun onTipToggle(state : Boolean){
        isTipEnabled.value = state;
        if (state == false) {
            selectedButton.intValue = 0
            tipAmount.doubleValue = 0.00
        }
    }

    fun onConfirm(navHostController: NavHostController, sharedViewModel: SharedViewModel) {
        sharedViewModel.objRootAppPaymentDetail.ttlAmount = totalAmount.doubleValue
        when(sharedViewModel.objRootAppPaymentDetail.txnType) {
            TxnType.REFUND,TxnType.PREAUTH -> {
                navHostController.navigate(AppNavigationItems.CardScreen.createRoute(totalAmount.toString()))
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
}