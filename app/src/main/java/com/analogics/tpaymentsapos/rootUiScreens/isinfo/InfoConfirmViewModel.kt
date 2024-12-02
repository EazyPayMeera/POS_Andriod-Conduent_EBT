// InfoConfirmViewModel.kt
package com.analogics.tpaymentsapos.rootUiScreens.isinfo

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.analogics.securityframework.database.dbRepository.TxnDBRepository
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import com.analogics.tpaymentsapos.rootUiScreens.activity.SharedViewModel
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.formatAmount
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.getFormattedDateTime
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.navigateAndClean
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.transformToAmountDouble
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InfoConfirmViewModel @Inject constructor(private val dbRepository: TxnDBRepository) : ViewModel() {

    private val _totalAmount = MutableStateFlow<String?>(null)
    val totalAmount: StateFlow<String?> = _totalAmount

    var rawInput by mutableStateOf("")
        private set

    var formattedAmount by mutableStateOf("0.00")
        private set

    val transactionDateTime: String = getFormattedDateTime()


    fun onAmountChange(newValue: String) {
        if (newValue.all { it.isDigit() || it == '.' }) {
            rawInput = newValue
            formattedAmount = formatAmount(newValue)
        }
    }

    fun onConfirm(
        Amount: String,
        sharedViewModel: SharedViewModel,
        navHostController: NavHostController
    ) {
        sharedViewModel.objRootAppPaymentDetail.ttlAmount = transformToAmountDouble(Amount)
        navHostController.navigate(AppNavigationItems.PleaseWaitScreen.route)
    }

    fun onCancel(navHostController: NavHostController) {
        navHostController.navigateAndClean(AppNavigationItems.DashBoardScreen.route)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getTotalAmountByInvoiceNo(invoiceNo: String) {
        viewModelScope.launch {
            val totalAmountString = dbRepository.fetchTotalAmountByInvoiceNo(invoiceNo)
            val totalAmount = totalAmountString?.toDoubleOrNull() ?: 0.0
            val formattedAmount = "%.2f".format(totalAmount)
            _totalAmount.value = formattedAmount
        }
    }
}
