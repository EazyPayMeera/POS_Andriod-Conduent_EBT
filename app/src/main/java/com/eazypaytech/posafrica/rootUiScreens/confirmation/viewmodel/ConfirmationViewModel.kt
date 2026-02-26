package com.eazypaytech.posafrica.rootUiScreens.confirmation.viewmodel

import android.util.Log
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
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.multiplyValues
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
    var serviceCharge = mutableDoubleStateOf(0.00)
    var selectedTipButton = mutableStateOf(PercentButton.NONE)
    var isTipButtonEnabled = mutableStateOf(false)
    var selectedServiceChargeButton = mutableStateOf(PercentButton.NONE)
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

    private fun getServiceChargePercent(button: PercentButton, sharedViewModel: SharedViewModel) : Double
    {
        return when(button){
            PercentButton.PERCENT1 -> sharedViewModel.objPosConfig?.serviceChargePercent1?:0.00
            PercentButton.PERCENT2 -> sharedViewModel.objPosConfig?.serviceChargePercent2?:0.00
            PercentButton.PERCENT3 -> sharedViewModel.objPosConfig?.serviceChargePercent3?:0.00
            else -> 0.00
        }
    }

    fun getServiceChargePercentLabel(button: PercentButton, sharedViewModel: SharedViewModel) : String
    {
        return formatAmount(getServiceChargePercent(button, sharedViewModel), symbol = Symbol(type = Symbol.Type.PERCENT, position = Symbol.Position.END, noSpace = true), decimalPlaces = 2)
    }

    private fun updateTotal(sharedViewModel: SharedViewModel)
    {
        /* Update Tip */
        when(selectedTipButton.value) {
            PercentButton.PERCENT1, PercentButton.PERCENT2, PercentButton.PERCENT3 -> {
                tipAmount.doubleValue = multiplyValues(
                    sharedViewModel.objRootAppPaymentDetail.txnAmount?:0.00,
                    getTipPercent(selectedTipButton.value, sharedViewModel) / 100.00
                )
            }
            PercentButton.CUSTOM -> {tipAmount.doubleValue = sharedViewModel.tipAmount}
            else -> {
                tipAmount.doubleValue = 0.00
            }
        }

        /* Update Service Charge */
        when(selectedServiceChargeButton.value) {
            PercentButton.PERCENT1, PercentButton.PERCENT2, PercentButton.PERCENT3 -> {
                serviceCharge.doubleValue = multiplyValues(
                    sharedViewModel.objRootAppPaymentDetail.txnAmount?:0.00,
                    getServiceChargePercent(selectedServiceChargeButton.value, sharedViewModel) / 100.00
                )
            }
            PercentButton.CUSTOM -> {serviceCharge.doubleValue = sharedViewModel.serviceCharge}
            else -> {
                serviceCharge.doubleValue = 0.00
            }
        }

        /* TAX on TOP of Service Charge */
        if (sharedViewModel.objPosConfig?.isTaxEnabled == true) {
            sharedViewModel.objRootAppPaymentDetail.VAT = multiplyValues(
                (sharedViewModel.objRootAppPaymentDetail.txnAmount ?: 0.00) + (serviceCharge.doubleValue),
                (sharedViewModel.objPosConfig?.vatPercent ?: 0.00) / 100.00
            )
        }

        totalAmount.doubleValue = calculateTotalAmount(sharedViewModel.objRootAppPaymentDetail.txnAmount?:0.00, tipAmount.doubleValue, sharedViewModel.objRootAppPaymentDetail.VAT?:0.00, serviceCharge.doubleValue)
    }

    fun onTipPercentChange(button : PercentButton, sharedViewModel: SharedViewModel){
        selectedTipButton.value = button
        updateTotal(sharedViewModel)
    }

    fun onCustomTip(navHostController: NavHostController, sharedViewModel: SharedViewModel){
        selectedTipButton.value = PercentButton.CUSTOM
        sharedViewModel.isTipButtonEnabled = isTipButtonEnabled.value
        sharedViewModel.selectedTipButton = selectedTipButton.value
        navHostController.navigate(AppNavigationItems.TipScreen.route)
    }

    fun onTipToggle(state : Boolean){
        isTipButtonEnabled.value = state
        if (state == false) {
            selectedTipButton.value = PercentButton.NONE
            tipAmount.doubleValue = 0.00
        }
    }

    fun onServiceChargePercentChange(button : PercentButton, sharedViewModel: SharedViewModel){
        selectedServiceChargeButton.value = button
        updateTotal(sharedViewModel)
    }

    fun onCustomServiceCharge(navHostController: NavHostController, sharedViewModel: SharedViewModel){
        selectedServiceChargeButton.value = PercentButton.CUSTOM
        sharedViewModel.isServiceChargeButtonEnabled = isServiceChargeButtonEnabled.value
        sharedViewModel.selectedServiceChargeButton = selectedServiceChargeButton.value
        navHostController.navigate(AppNavigationItems.ServiceChargeScreen.route)
    }

    fun onServiceChargeToggle(state : Boolean){
        isServiceChargeButtonEnabled.value = state
        if (state == false) {
            selectedServiceChargeButton.value = PercentButton.NONE
            serviceCharge.doubleValue = 0.00
        }
    }

    fun onConfirm(navHostController: NavHostController, sharedViewModel: SharedViewModel) {
        sharedViewModel.objRootAppPaymentDetail.ttlAmount = totalAmount.doubleValue
        sharedViewModel.objRootAppPaymentDetail.tip = tipAmount.doubleValue
        sharedViewModel.objRootAppPaymentDetail.serviceCharge = serviceCharge.doubleValue
        Log.d("TRANSACTION_TYPE", "Txn Type Selected: ${sharedViewModel.objRootAppPaymentDetail.txnType}")
        when(sharedViewModel.objRootAppPaymentDetail.txnType) {
            TxnType.FOOD_PURCHASE,TxnType.FOODSTAMP_RETURN ,TxnType.CASH_PURCHASE-> {
                navHostController.navigate(AppNavigationItems.CardScreen.route)
            }
            TxnType.PURCHASE_CASHBACK ->
            {
                navHostController.navigate(AppNavigationItems.CardScreen.route)
            }
            TxnType.VOID_LAST , TxnType.E_VOUCHER,TxnType.VOUCHER_RETURN-> {
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

    fun onLoad(customTipAmount : Double?, customServiceCharge : Double?, sharedViewModel: SharedViewModel)
    {
        if(customTipAmount?.isFinite()==true) {
            sharedViewModel.tipAmount = customTipAmount
        }
        else if(customServiceCharge?.isFinite()==true) {
            sharedViewModel.serviceCharge = customServiceCharge
        }
        else {
            isTipButtonEnabled.value = sharedViewModel.isTipButtonEnabled
            isServiceChargeButtonEnabled.value = sharedViewModel.isServiceChargeButtonEnabled
            selectedTipButton.value = sharedViewModel.selectedTipButton
            selectedServiceChargeButton.value = sharedViewModel.selectedServiceChargeButton
        }
        updateTotal(sharedViewModel)
    }

    fun onBack(navHostController: NavHostController,sharedViewModel: SharedViewModel)
    {
        sharedViewModel.isTipButtonEnabled = isTipButtonEnabled.value
        sharedViewModel.selectedTipButton = selectedTipButton.value
        navHostController.popBackStack()
    }

}