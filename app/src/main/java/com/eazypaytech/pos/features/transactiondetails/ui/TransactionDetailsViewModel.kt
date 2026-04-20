package com.eazypaytech.pos.features.transactiondetails.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eazypaytech.pos.domain.model.ObjRootAppPaymentDetails
import com.eazypaytech.pos.core.utils.miscellaneous.PrinterUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TransactionDetailsViewModel @Inject constructor() : ViewModel()
{
    fun printReceipt(
        context: Context,
        objRootAppPaymentDetail: ObjRootAppPaymentDetails,
        isCustomer: Boolean = false,
    ) {
        viewModelScope.launch{
            PrinterUtils.printReceipt(context,objRootAppPaymentDetail,isCustomer)
        }
    }
}