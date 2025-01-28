package com.eazypaytech.posafrica.rootUiScreens.approved.viewmodel


import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.eazypaytech.paymentservicecore.constants.AppConstants
import com.eazypaytech.paymentservicecore.listeners.responseListener.IPrinterServiceResponseListener
import com.eazypaytech.paymentservicecore.model.emv.PrinterServiceResult
import com.eazypaytech.securityframework.database.dbRepository.TxnDBRepository
import com.eazypaytech.posafrica.navigation.AppNavigationItems
import com.eazypaytech.posafrica.rootModel.ObjRootAppPaymentDetails
import com.eazypaytech.posafrica.rootUiScreens.activity.SharedViewModel
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.navigateAndClean
import com.eazypaytech.posafrica.R
import com.eazypaytech.posafrica.rootUiScreens.dialogs.CustomDialogBuilder
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.PrinterServiceRepository
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.PrinterServiceRepository.Align
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.PrinterServiceRepository.FontSize
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.PrinterServiceRepository.PrintFormat
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ApprovedViewModel @Inject constructor(private var dbRepository: TxnDBRepository, private var printerServiceRepository: PrinterServiceRepository): ViewModel()
{
    lateinit var context: Context
    private val _hasDbRecord = MutableStateFlow<Boolean>(false)
    val hasDbRecord: StateFlow<Boolean> = _hasDbRecord

    fun onLoad(context: Context, sharedViewModel: SharedViewModel)
    {
        this.context = context
        viewModelScope.launch {
            dbRepository.fetchTxnById(sharedViewModel.objRootAppPaymentDetail.id)?.let {
                _hasDbRecord.value = true
            }
            printerServiceRepository.init(context, object :IPrinterServiceResponseListener {
                override fun onPrinterServiceResponse(response: Any) {
                    when (response) {
                        is PrinterServiceResult.Result -> {
                            when (response.status) {
                                PrinterServiceResult.Status.INIT_SUCCESS -> { }
                                PrinterServiceResult.Status.INIT_FAILURE -> {
                                    CustomDialogBuilder.composeAlertDialog(
                                        title = context.resources.getString(R.string.printer_error_title),
                                        message = context.resources.getString(R.string.printer_init_failed)
                                    )
                                }
                            }
                        }
                    }
                }
            }
            ).addText("Start Printing").addText().addText().addText("End Of Receipt")
                .print()

            if (_hasDbRecord.value == true && sharedViewModel.objPosConfig?.isAutoPrintMerchant == true) {
                viewModelScope.launch {
                    delay(AppConstants.AUTO_PRINT_RECEIPT_DELAY_MS)
                    printReceipt(
                        R.drawable.master_mono,
                        sharedViewModel,
                        context,
                        objRootAppPaymentDetail = sharedViewModel.objRootAppPaymentDetail
                    )
                }
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
        logoResId: Int,
        sharedViewModel: SharedViewModel,
        context: Context,
        customer: Boolean = false,
        objRootAppPaymentDetail: ObjRootAppPaymentDetails
    ) {

        viewModelScope.launch{
            printerServiceRepository.init(context, object :IPrinterServiceResponseListener {
                override fun onPrinterServiceResponse(response: Any) {
                    when (response) {
                        is PrinterServiceResult.Result -> {
                            when (response.status) {
                                PrinterServiceResult.Status.INIT_SUCCESS -> { }
                                PrinterServiceResult.Status.INIT_FAILURE -> {
                                    CustomDialogBuilder.composeAlertDialog(
                                        title = context.resources.getString(R.string.printer_error_title),
                                        message = context.resources.getString(R.string.printer_init_failed)
                                    )
                                }
                            }
                        }
                    }
                }
            }
            )
                .addText("Start Printing")
                .addText("Extra Small", format = PrintFormat().align(Align.LEFT).fontSize(FontSize.EXTRA_SMALL))
                .addText("Extra Small Bold", format = PrintFormat().align(Align.LEFT).fontSize(FontSize.EXTRA_SMALL).style(
                    PrinterServiceRepository.Style.BOLD))
                .addText("Small", format = PrintFormat().align(Align.LEFT).fontSize(FontSize.SMALL))
                .addText("Medium", format = PrintFormat().align(Align.LEFT).fontSize(FontSize.MEDIUM))
                .addText("Large", format = PrintFormat().align(Align.LEFT).fontSize(FontSize.LARGE))
                .addText("Extra Large", format = PrintFormat().align(Align.LEFT).fontSize(FontSize.EXTRA_LARGE).style(
                    PrinterServiceRepository.Style.BOLD))
                .addText()
                .addText()
                .feedLine()
                .addText("Left", format = PrintFormat().align(Align.LEFT).fontSize(FontSize.EXTRA_LARGE))
                .addText("Center", format = PrintFormat().align(Align.CENTER).fontSize(FontSize.EXTRA_LARGE))
                .addText("Right", format = PrintFormat().align(Align.RIGHT).fontSize(FontSize.EXTRA_SMALL))
                .addText("123456789","12345678")
                .addText("123456789", "123456789", "12345678",format = PrintFormat().fontSize(FontSize.EXTRA_SMALL))
                .feedLine(2)
                .addText("End Of Receipt")
                .feedLine(5)
                .print()
        }
    }
}

