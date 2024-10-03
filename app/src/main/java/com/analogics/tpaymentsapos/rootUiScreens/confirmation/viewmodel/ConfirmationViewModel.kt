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
    var isTipButtonEnabled = mutableStateOf(false)

    private fun getTipPercent(button: Int, sharedViewModel: SharedViewModel) : Double
    {
        return when(button){
            1 -> sharedViewModel.objPosConfig?.tipPercent1?:0.00
            2 -> sharedViewModel.objPosConfig?.tipPercent2?:0.00
            3 -> sharedViewModel.objPosConfig?.tipPercent3?:0.00
            else -> 0.00
        }
    }

    fun getTipPercentLabel(button: Int, sharedViewModel: SharedViewModel) : String
    {
        return formatAmount(getTipPercent(button, sharedViewModel), symbol = Symbol(type = Symbol.Type.PERCENT, position = Symbol.Position.END, noSpace = true), decimalPlaces = 0)
    }

    private fun updateTotal(sharedViewModel: SharedViewModel)
    {
        when(selectedButton.intValue) {
            1, 2, 3 -> {
                tipAmount.doubleValue = calculateTip(
                    sharedViewModel.objRootAppPaymentDetail.txnAmount?:0.00,
                    getTipPercent(selectedButton.intValue, sharedViewModel) / 100.00
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
        sharedViewModel.isTipButtonEnabled = isTipButtonEnabled.value
        sharedViewModel.selectedTipButton = selectedButton.intValue
        navHostController.navigate(AppNavigationItems.TipScreen.route)
    }

    fun onTipToggle(state : Boolean, sharedViewModel: SharedViewModel){
        isTipButtonEnabled.value = state
        if (state == false) {
            selectedButton.intValue = 0
            tipAmount.doubleValue = 0.00
        }
    }

    fun onConfirm(navHostController: NavHostController, sharedViewModel: SharedViewModel) {
        sharedViewModel.objRootAppPaymentDetail.ttlAmount = totalAmount.doubleValue
        when(sharedViewModel.objRootAppPaymentDetail.txnType) {
            TxnType.PURCHASE,TxnType.REFUND,TxnType.PREAUTH -> {
                navHostController.navigate(AppNavigationItems.CardScreen.route)
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
            isTipButtonEnabled.value = sharedViewModel.isTipButtonEnabled
            selectedButton.intValue = sharedViewModel.selectedTipButton
        }
        updateTotal(sharedViewModel)
    }

    fun onBack(navHostController: NavHostController,sharedViewModel: SharedViewModel)
    {
        sharedViewModel.isTipButtonEnabled = isTipButtonEnabled.value
        sharedViewModel.selectedTipButton = selectedButton.intValue
        navHostController.popBackStack()
    }
}