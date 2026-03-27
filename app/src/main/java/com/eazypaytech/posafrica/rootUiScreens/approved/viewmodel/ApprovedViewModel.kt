package com.eazypaytech.posafrica.rootUiScreens.approved.viewmodel


import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.eazypaytech.paymentservicecore.constants.AppConstants
import com.eazypaytech.paymentservicecore.listeners.responseListener.IEmvServiceResponseListener
import com.eazypaytech.paymentservicecore.model.PaymentServiceTxnDetails
import com.eazypaytech.paymentservicecore.model.emv.EmvServiceResult
import com.eazypaytech.paymentservicecore.repository.emvService.EmvServiceRepository
import com.eazypaytech.paymentservicecore.utils.PaymentServiceUtils
import com.eazypaytech.posafrica.R
import com.eazypaytech.securityframework.database.dbRepository.TxnDBRepository
import com.eazypaytech.posafrica.navigation.AppNavigationItems
import com.eazypaytech.posafrica.rootModel.ObjRootAppPaymentDetails
import com.eazypaytech.posafrica.rootUiScreens.activity.SharedViewModel
import com.eazypaytech.posafrica.rootUiScreens.dialogs.CustomDialogBuilder
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.navigateAndClean
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.PrinterServiceRepository
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.getCurrentDateTime
import com.eazypaytech.posafrica.rootUtils.miscellaneous.NetworkUtils
import com.eazypaytech.posafrica.rootUtils.miscellaneous.PrinterUtils
import com.google.gson.Gson
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

}

