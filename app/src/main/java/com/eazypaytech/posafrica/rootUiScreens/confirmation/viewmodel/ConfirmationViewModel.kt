package com.eazypaytech.posafrica.rootUiScreens.confirmation.viewmodel

import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.navigation.NavHostController
import com.eazypaytech.paymentservicecore.models.TxnType
import com.eazypaytech.securityframework.database.dbRepository.TxnDBRepository
import com.eazypaytech.posafrica.navigation.AppNavigationItems
import com.eazypaytech.posafrica.rootModel.Symbol
import com.eazypaytech.posafrica.rootUiScreens.activity.SharedViewModel
import com.eazypaytech.posafrica.rootUiScreens.settings.config.PercentButton
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.calculateTip
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.calculateTotalAmount
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.formatAmount
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.navigateAndClean
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class ConfirmationViewModel @Inject constructor(private val dbRepository: TxnDBRepository) : ViewModel() {
    var totalAmount = mutableDoubleStateOf(0.00)
    var tipAmount = mutableDoubleStateOf(0.00)
    var selectedButton = mutableStateOf(PercentButton.NONE)
    var isTipButtonEnabled = mutableStateOf(false)
    var isServiceChargeButtonEnabled = mutableStateOf(false)

    private val _totalAmount = MutableStateFlow<String?>(null)
    val totalAmountFetch: StateFlow<String?> = _totalAmount



    private fun getTipPercent(button: PercentButton, sharedViewModel: SharedViewModel) : Double
    {
        return when(button){
            PercentButton.PERCENT1 -> sharedViewModel.objPosConfig?.tipPercent1?:0.00
            PercentButton.PERCENT2 -> sharedViewModel.objPosConfig?.tipPercent2?:0.00
            PercentButton.PERCENT3 -> sharedViewModel.objPosConfig?.tipPercent3?:0.00
            else -> 0.00
        }
    }

    fun getTipPercentLabel(button: PercentButton, sharedViewModel: SharedViewModel) : String
    {
        return formatAmount(getTipPercent(button, sharedViewModel), symbol = Symbol(type = Symbol.Type.PERCENT, position = Symbol.Position.END, noSpace = true), decimalPlaces = 0)
    }

    private fun updateTotal(sharedViewModel: SharedViewModel)
    {
        when(selectedButton.value) {
            PercentButton.PERCENT1, PercentButton.PERCENT2, PercentButton.PERCENT3 -> {
                tipAmount.doubleValue = calculateTip(
                    sharedViewModel.objRootAppPaymentDetail.txnAmount?:0.00,
                    getTipPercent(selectedButton.value, sharedViewModel) / 100.00
                )
            }
            PercentButton.CUSTOM -> {tipAmount.doubleValue = sharedViewModel.tipAmount}
            else -> {
                tipAmount.doubleValue = 0.00
            }
        }
        totalAmount.doubleValue = calculateTotalAmount(sharedViewModel.objRootAppPaymentDetail.txnAmount?:0.00, tipAmount.doubleValue, sharedViewModel.objRootAppPaymentDetail.VAT?:0.00, sharedViewModel.objRootAppPaymentDetail.serviceCharge?:0.00)
    }

    fun onTipPercentChange(button : PercentButton, sharedViewModel: SharedViewModel){
        selectedButton.value = button
        updateTotal(sharedViewModel)
    }

    fun onCustomTip(navHostController: NavHostController, sharedViewModel: SharedViewModel){
        selectedButton.value = PercentButton.CUSTOM
        sharedViewModel.isTipButtonEnabled = isTipButtonEnabled.value
        sharedViewModel.selectedTipButton = selectedButton.value
        navHostController.navigate(AppNavigationItems.TipScreen.route)
    }

    fun onTipToggle(state : Boolean, sharedViewModel: SharedViewModel){
        isTipButtonEnabled.value = state
        if (state == false) {
            selectedButton.value = PercentButton.NONE
            tipAmount.doubleValue = 0.00
        }
    }

    fun onConfirm(navHostController: NavHostController, sharedViewModel: SharedViewModel) {
        sharedViewModel.objRootAppPaymentDetail.ttlAmount = totalAmount.doubleValue
        sharedViewModel.objRootAppPaymentDetail.tip = tipAmount.doubleValue
        when(sharedViewModel.objRootAppPaymentDetail.txnType) {
            TxnType.PURCHASE,TxnType.REFUND,TxnType.PREAUTH -> {
                navHostController.navigate(AppNavigationItems.CardScreen.route)
            }
            TxnType.VOID,TxnType.AUTHCAP -> {
                navHostController.navigate(AppNavigationItems.PleaseWaitScreen.route)
            }
            else -> {
                navHostController.navigateAndClean(AppNavigationItems.DashBoardScreen.route)
            }
        }
    }

    fun onCancel(navHostController: NavHostController) {
        navHostController.navigateAndClean(AppNavigationItems.DashBoardScreen.route)
    }

    fun onLoad(customTipAmount : Double?,sharedViewModel: SharedViewModel)
    {
        if(customTipAmount?.isFinite()==true)
            sharedViewModel.tipAmount = customTipAmount
        else {
            isTipButtonEnabled.value = sharedViewModel.isTipButtonEnabled
            selectedButton.value = sharedViewModel.selectedTipButton
        }
        updateTotal(sharedViewModel)
    }

    fun onBack(navHostController: NavHostController,sharedViewModel: SharedViewModel)
    {
        sharedViewModel.isTipButtonEnabled = isTipButtonEnabled.value
        sharedViewModel.selectedTipButton = selectedButton.value
        navHostController.popBackStack()
    }

}