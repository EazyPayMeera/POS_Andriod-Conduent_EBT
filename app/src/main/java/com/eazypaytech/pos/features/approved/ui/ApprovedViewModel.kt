package com.eazypaytech.pos.features.approved.ui

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.eazypaytech.paymentservicecore.constants.AppConstants
import com.analogics.paymentservicecore.domain.repository.emvService.EmvServiceRepository
import com.analogics.paymentservicecore.utils.PaymentServiceUtils
import com.eazypaytech.pos.features.activity.ui.SharedViewModel
import com.eazypaytech.pos.navigation.AppNavigationItems
import com.eazypaytech.pos.domain.model.ObjRootAppPaymentDetails
import com.analogics.paymentservicecore.domain.repository.printerService.PrinterServiceRepository
import com.eazypaytech.pos.core.utils.navigateAndClean
import com.eazypaytech.pos.core.utils.miscellaneous.PrinterUtils
import com.analogics.securityframework.data.repository.TxnDBRepository
import com.eazypaytech.hardwarecore.data.model.EmvSdkResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ApprovedViewModel @Inject constructor(private val emvServiceRepository: EmvServiceRepository, private var dbRepository: TxnDBRepository, private var printerServiceRepository: PrinterServiceRepository): ViewModel()
{
    lateinit var context: Context
    private val _hasDbRecord = MutableStateFlow<Boolean>(false)
    val hasDbRecord: StateFlow<Boolean> = _hasDbRecord

    lateinit var sharedViewModel: SharedViewModel
    lateinit var navHostController : NavHostController



    fun onLoad(context: Context, sharedViewModel: SharedViewModel)
    {
        this.context = context
        viewModelScope.launch {
            dbRepository.fetchTxnById(sharedViewModel.objRootAppPaymentDetail.id)?.let {
                _hasDbRecord.value = true
                sharedViewModel.objRootAppPaymentDetail = PaymentServiceUtils.transformObject<ObjRootAppPaymentDetails>(it)?:sharedViewModel.objRootAppPaymentDetail
            }

            if (_hasDbRecord.value == true && sharedViewModel.objPosConfig?.isAutoPrintMerchant == true) {
                delay(AppConstants.AUTO_PRINT_RECEIPT_DELAY_MS)
                printReceipt(context, sharedViewModel, false)
            }
        }
    }

    fun onDone(navHostController: NavHostController)
    {
        viewModelScope.launch {
            navHostController.navigateAndClean(AppNavigationItems.DashBoardScreen.route)
        }
    }

    fun printReceipt(
        context: Context,
        sharedViewModel: SharedViewModel,
        isCustomer: Boolean = false
    ) {
        viewModelScope.launch{
            dbRepository.fetchTxnById(sharedViewModel.objRootAppPaymentDetail.id)?.let {
                PrinterUtils.printReceipt(context, PaymentServiceUtils.transformObject<ObjRootAppPaymentDetails>(it)?: ObjRootAppPaymentDetails(), isCustomer)
            }
        }
    }

    fun isCardExists(context: Context): Boolean {
        return emvServiceRepository.isCardExists(context)
    }

    fun isCardDetected(context: Context): EmvSdkResult.CardCheckStatus {
        return emvServiceRepository.isCardDetected(context)
    }



}

