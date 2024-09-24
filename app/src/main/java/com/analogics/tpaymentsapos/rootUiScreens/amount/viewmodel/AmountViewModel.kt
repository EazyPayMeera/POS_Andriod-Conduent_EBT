package com.analogics.tpaymentsapos.rootUiScreens.amount.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.navigation.NavHostController
import com.analogics.paymentservicecore.models.TxnInfo
import com.analogics.paymentservicecore.models.TxnType
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.formatAmount
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.formatAmountToDouble
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.getFormattedDateTime

class AmountViewModel : ViewModel() {

    var transAmount by mutableStateOf("")
        private set

    val transactionDateTime: String = getFormattedDateTime()

    fun onAmountChange(newValue: String) :String{
        TxnInfo.tip = 0.00
        transAmount = formatAmount(newValue)
        return formatAmountToDouble(newValue).toString()
    }

    fun onConfirm(navHostController: NavHostController) {
        when(TxnInfo.txnType) {
            TxnType.REFUND,TxnType.PREAUTH -> {
                navHostController.navigate(AppNavigationItems.CardScreen.createRoute(transAmount))
            }
            TxnType.VOID,TxnType.AUTHCAP -> {
                navHostController.navigate(AppNavigationItems.PleaseWaitScreen.route)
            }
            else -> {
                navHostController.navigate(AppNavigationItems.ConfirmationScreen.createRoute(transAmount))
            }
        }
    }

    fun onCancel(navHostController: NavHostController) {
        navHostController.navigate(AppNavigationItems.TrainingScreen.route)
    }
}