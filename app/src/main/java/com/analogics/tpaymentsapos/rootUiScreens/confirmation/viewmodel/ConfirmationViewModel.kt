package com.analogics.tpaymentsapos.rootUiScreens.confirmation.viewmodel

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.analogics.paymentservicecore.models.TxnType
import com.analogics.securityframework.database.dbRepository.TxnDBRepository
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import com.analogics.tpaymentsapos.rootModel.Symbol
import com.analogics.tpaymentsapos.rootUiScreens.activity.SharedViewModel
import com.analogics.tpaymentsapos.rootUiScreens.settings.config.TipButton
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.calculateTip
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.calculateTotalAmount
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.formatAmount
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.navigateAndClean
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConfirmationViewModel @Inject constructor(private val dbRepository: TxnDBRepository) : ViewModel() {
    var totalAmount = mutableDoubleStateOf(0.00)
    var tipAmount = mutableDoubleStateOf(0.00)
    var selectedButton = mutableStateOf(TipButton.NONE)
    var isTipButtonEnabled = mutableStateOf(false)

    private val _totalAmount = MutableStateFlow<String?>(null)
    val totalAmountFetch: StateFlow<String?> = _totalAmount

    private val _txnAmount = MutableStateFlow<String?>(null)
    val txnAmountFetch: StateFlow<String?> = _txnAmount

    private val _tipAmount = MutableStateFlow<String?>(null)
    val tipAmountFetch: StateFlow<String?> = _tipAmount

    private fun getTipPercent(button: TipButton, sharedViewModel: SharedViewModel) : Double
    {
        return when(button){
            TipButton.PERCENT1 -> sharedViewModel.objPosConfig?.tipPercent1?:0.00
            TipButton.PERCENT2 -> sharedViewModel.objPosConfig?.tipPercent2?:0.00
            TipButton.PERCENT3 -> sharedViewModel.objPosConfig?.tipPercent3?:0.00
            else -> 0.00
        }
    }

    fun getTipPercentLabel(button: TipButton, sharedViewModel: SharedViewModel) : String
    {
        return formatAmount(getTipPercent(button, sharedViewModel), symbol = Symbol(type = Symbol.Type.PERCENT, position = Symbol.Position.END, noSpace = true), decimalPlaces = 0)
    }

    private fun updateTotal(sharedViewModel: SharedViewModel)
    {
        when(selectedButton.value) {
            TipButton.PERCENT1, TipButton.PERCENT2, TipButton.PERCENT3 -> {
                tipAmount.doubleValue = calculateTip(
                    sharedViewModel.objRootAppPaymentDetail.txnAmount?:0.00,
                    getTipPercent(selectedButton.value, sharedViewModel) / 100.00
                )
            }
            TipButton.CUSTOM -> {tipAmount.doubleValue = sharedViewModel.tipAmount}
            else -> {
                tipAmount.doubleValue = 0.00
            }
        }
        totalAmount.doubleValue = calculateTotalAmount(sharedViewModel.objRootAppPaymentDetail.txnAmount?:0.00, tipAmount.doubleValue, sharedViewModel.objRootAppPaymentDetail.CGST?:0.00, sharedViewModel.objRootAppPaymentDetail.SGST?:0.00)
    }

    fun onTipPercentChange(button : TipButton, sharedViewModel: SharedViewModel){
        selectedButton.value = button
        updateTotal(sharedViewModel)
    }

    fun onCustomTip(navHostController: NavHostController, sharedViewModel: SharedViewModel){
        selectedButton.value = TipButton.CUSTOM
        sharedViewModel.isTipButtonEnabled = isTipButtonEnabled.value
        sharedViewModel.selectedTipButton = selectedButton.value
        navHostController.navigate(AppNavigationItems.TipScreen.route)
    }

    fun onTipToggle(state : Boolean, sharedViewModel: SharedViewModel){
        isTipButtonEnabled.value = state
        if (state == false) {
            selectedButton.value = TipButton.NONE
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

    @RequiresApi(Build.VERSION_CODES.O)
    fun getTotalAmountByInvoiceNo(invoiceNo:String) {
        viewModelScope.launch {
            val totalAmount = dbRepository.fetchTotalAmountByInvoiceNo(invoiceNo).toDoubleOrNull()
            _totalAmount.value = "%.2f".format(totalAmount)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getTxnAmountByInvoiceNo(invoiceNo:String) {
        viewModelScope.launch {
            val txnAmount = dbRepository.fetchTxnAmountByInvoiceNo(invoiceNo).toDoubleOrNull()
            Log.d("txnAmount :", txnAmount.toString())
            _txnAmount.value = "%.2f".format(txnAmount)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getTipAmountByInvoiceNo(invoiceNo:String) {
        viewModelScope.launch {
            val tipAmount = dbRepository.fetchTipAmountByInvoiceNo(invoiceNo).toDoubleOrNull()
            _tipAmount.value = "%.2f".format(tipAmount)
        }
    }
}