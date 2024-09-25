package com.analogics.tpaymentsapos.rootUiScreens.confirmation.viewmodel

import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.navigation.NavHostController
import com.analogics.paymentservicecore.models.TxnType
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import com.analogics.tpaymentsapos.rootModel.Symbol
import com.analogics.tpaymentsapos.rootUiScreens.activity.SharedViewModel
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.calculateTip
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.calculateTotalAmount
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.formatAmount

class ConfirmationViewModel : ViewModel() {
    var totalAmount = mutableDoubleStateOf(0.00)
    var tipAmount = mutableDoubleStateOf(0.00)
    var selectedButton = mutableIntStateOf(0)
    var isTipEnabled = mutableStateOf(false)

    private fun getTipPercent(button: Int) : Double
    {
        return when(button){
            1 -> 10.00
            2 -> 15.00
            3 -> 20.00
            else -> 0.00
        }
    }

    fun getTipPercentLabel(button: Int) : String
    {
        return formatAmount(getTipPercent(button), symbol = Symbol(type = Symbol.Type.PERCENT, position = Symbol.Position.END, noSpace = true), decimalPlaces = 0)
    }

    private fun updateTotal(sharedViewModel: SharedViewModel)
    {
        when(selectedButton.intValue) {
            1, 2, 3 -> {
                tipAmount.doubleValue = calculateTip(
                    sharedViewModel.objRootAppPaymentDetail.txnAmount?:0.00,
                    getTipPercent(selectedButton.intValue) / 100.00
                )
            }
            4 -> {tipAmount.doubleValue = sharedViewModel.tipAmount}
            else -> {
                tipAmount.doubleValue = 0.00
            }
        }
        totalAmount.doubleValue = calculateTotalAmount(sharedViewModel.objRootAppPaymentDetail.txnAmount?:0.00, tipAmount.doubleValue, sharedViewModel.objRootAppPaymentDetail.CGST?:0.00, sharedViewModel.objRootAppPaymentDetail.SGST?:0.00)
    }

    fun onTipPercentChange(button : Int, sharedViewModel: SharedViewModel){
        selectedButton.intValue = button
        updateTotal(sharedViewModel)
    }

    fun onCustomTip(navHostController: NavHostController, sharedViewModel: SharedViewModel){
        selectedButton.intValue = 4
        sharedViewModel.isTipEnabled = isTipEnabled.value
        sharedViewModel.selectedTipButton = selectedButton.intValue
        navHostController.navigate(AppNavigationItems.TipScreen.route)
    }

    fun onTipToggle(state : Boolean, sharedViewModel: SharedViewModel){
        isTipEnabled.value = state
        if (state == false) {
            selectedButton.intValue = 0
            tipAmount.doubleValue = 0.00
        }
    }

    fun onConfirm(navHostController: NavHostController, sharedViewModel: SharedViewModel) {
        sharedViewModel.objRootAppPaymentDetail.ttlAmount = totalAmount.doubleValue
        when(sharedViewModel.objRootAppPaymentDetail.txnType) {
            TxnType.PURCHASE,TxnType.REFUND,TxnType.PREAUTH -> {
                navHostController.navigate(AppNavigationItems.CardScreen.createRoute(totalAmount.toString()))
            }
            TxnType.VOID,TxnType.AUTHCAP -> {
                navHostController.navigate(AppNavigationItems.PleaseWaitScreen.route)
            }
            else -> {
                navHostController.navigate(AppNavigationItems.TrainingScreen.route)
            }
        }
    }

    fun onCancel(navHostController: NavHostController) {
        navHostController.navigate(AppNavigationItems.TrainingScreen.route)
    }

    fun onLoad(customTipAmount : Double?,sharedViewModel: SharedViewModel)
    {
        if(customTipAmount?.isFinite()==true)
            sharedViewModel.tipAmount = customTipAmount
        else {
            isTipEnabled.value = sharedViewModel.isTipEnabled
            selectedButton.intValue = sharedViewModel.selectedTipButton
        }
        updateTotal(sharedViewModel)
    }

    fun onBack(navHostController: NavHostController,sharedViewModel: SharedViewModel)
    {
        sharedViewModel.isTipEnabled = isTipEnabled.value
        sharedViewModel.selectedTipButton = selectedButton.intValue
        navHostController.popBackStack()
    }
}