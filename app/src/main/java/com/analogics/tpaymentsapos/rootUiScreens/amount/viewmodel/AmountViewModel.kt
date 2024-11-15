package com.analogics.tpaymentsapos.rootUiScreens.amount.viewmodel

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.analogics.paymentservicecore.models.TxnType
import com.analogics.securityframework.database.dbRepository.TxnDBRepository
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import com.analogics.tpaymentsapos.rootUiScreens.activity.SharedViewModel
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.formatAmount
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.navigateAndClean
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.transformToAmountDouble
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AmountViewModel @Inject constructor(private val dbRepository: TxnDBRepository) : ViewModel() {

    var transAmount by mutableStateOf("")
        private set

    private val _totalAmount = MutableStateFlow<String?>(null)
    val totalAmount: StateFlow<String?> = _totalAmount

    fun onLoad(sharedViewModel: SharedViewModel)
    {
        transAmount.ifEmpty { transAmount = formatAmount(sharedViewModel.objRootAppPaymentDetail.txnAmount?:0.00) }
    }

    fun onAmountChange(newValue: String) :String{
        transAmount = formatAmount(newValue)
        return transformToAmountDouble(newValue).toString()
    }

    fun onConfirm(navHostController: NavHostController, sharedViewModel: SharedViewModel) {
        calculateTotal(sharedViewModel)
        when(sharedViewModel.objRootAppPaymentDetail.txnType) {
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
        navHostController.navigateAndClean(AppNavigationItems.DashBoardScreen.route)
    }

    private fun calculateTax(txnAmount: Double, percent: Double) : Double {
        return txnAmount * percent / 100.00
    }

    @SuppressLint("SuspiciousIndentation")
    private fun calculateTotal(sharedViewModel: SharedViewModel)  {
        sharedViewModel.objRootAppPaymentDetail.txnAmount = transformToAmountDouble(transAmount)
        if(sharedViewModel.objPosConfig?.isTaxEnabled == true)
        {
            sharedViewModel.objRootAppPaymentDetail.CGST = calculateTax(sharedViewModel.objRootAppPaymentDetail.txnAmount?:0.00,sharedViewModel.objPosConfig?.CGSTPercent?:0.00)
            sharedViewModel.objRootAppPaymentDetail.SGST = calculateTax(sharedViewModel.objRootAppPaymentDetail.txnAmount?:0.00,sharedViewModel.objPosConfig?.SGSTPercent?:0.00)
        }
        sharedViewModel.objRootAppPaymentDetail.ttlAmount = (sharedViewModel.objRootAppPaymentDetail.txnAmount?:0.00) +
                (sharedViewModel.objRootAppPaymentDetail.CGST?:0.00) +
                (sharedViewModel.objRootAppPaymentDetail.SGST?:0.00)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getTotalAmountByInvoiceNo(invoiceNo:String) {
        viewModelScope.launch {
            val totalAmount = dbRepository.fetchTotalAmountByInvoiceNo(invoiceNo).toDoubleOrNull()
            _totalAmount.value = "%.2f".format(totalAmount)
        }
    }

    fun generateInvoiceNumber(): String {
        val randomNumber = (10000000..99999999).random()
        return "INVC$randomNumber"
    }
}