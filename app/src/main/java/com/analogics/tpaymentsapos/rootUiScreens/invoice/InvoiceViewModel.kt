package com.analogics.tpaymentsapos.rootUiScreens.login

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.analogics.paymentservicecore.constants.AppConstants
import com.analogics.paymentservicecore.listeners.responseListener.IScannerResultProviderListener
import com.analogics.paymentservicecore.models.Acquirer
import com.analogics.paymentservicecore.models.TxnType
import com.analogics.paymentservicecore.utils.PaymentServiceUtils
import com.analogics.paymentservicecore.utils.toDecimalFormat
import com.analogics.securityframework.database.dbRepository.TxnDBRepository
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import com.analogics.tpaymentsapos.rootModel.ObjRootAppPaymentDetails
import com.analogics.tpaymentsapos.rootUiScreens.activity.SharedViewModel
import com.analogics.tpaymentsapos.rootUiScreens.dialogs.CustomDialogBuilder
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.ScannerServiceRepository
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.getAcquirer
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.navigateAndClean
import com.analogics.tpaymentsapos.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InvoiceViewModel @Inject constructor(private val dbRepository: TxnDBRepository) : ViewModel() {
    private val TAG = "InvoiceViewModel"
    private val _invoiceno = MutableStateFlow("")
    val invoiceno: StateFlow<String> = _invoiceno
    private val _isInvoiceFound = MutableStateFlow(false)
    val isInvoiceFound: StateFlow<Boolean> get() = _isInvoiceFound
    lateinit var navHostController: NavHostController

    private val scannerServiceRepository = ScannerServiceRepository() // Instantiate here

    fun updateInvoiceNo(newValue: String, sharedViewModel: SharedViewModel?=null): String {
        sharedViewModel?.takeIf {getAcquirer(it.objRootAppPaymentDetail) == Acquirer.LYRA}?.let {
            _invoiceno.value = newValue.take(AppConstants.LYRA_MAX_INVOICE_LENGTH)
        }?:
        {_invoiceno.value = newValue}
        return _invoiceno.value // Return the updated invoice number
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun onConfirm(navHostController: NavHostController, sharedViewModel: SharedViewModel)
    {
        //sharedViewModel.objRootAppPaymentDetail.invoiceNo = invoiceno.value
        this.navHostController = navHostController
        if(sharedViewModel.objRootAppPaymentDetail.txnType == TxnType.PURCHASE || sharedViewModel.objRootAppPaymentDetail.txnType == TxnType.PREAUTH)
            navigateToAmountScreen(navHostController, sharedViewModel)
        else
            getTxnDetailsByInvoiceNo(navHostController.context,sharedViewModel)
        Log.d("Invoice No", "Invoice No in onConfirm: ${sharedViewModel.objRootAppPaymentDetail.invoiceNo}")
    }

    fun navigateToAmountScreen(navHostController: NavHostController,sharedViewModel: SharedViewModel) {
        sharedViewModel.objRootAppPaymentDetail.invoiceNo = invoiceno.value
        viewModelScope.launch {
            if (sharedViewModel.objRootAppPaymentDetail.txnType == TxnType.AUTHCAP) {
                navHostController.navigate(AppNavigationItems.InfoConfirmScreen.route)
            } else {
                navHostController.navigate(AppNavigationItems.AmountScreen.route)
            }
        }
    }

    fun navigateToTrainingScreen(navHostController: NavHostController) {
        viewModelScope.launch {
            navHostController.navigateAndClean(AppNavigationItems.DashBoardScreen.route)
        }
    }

    suspend fun initScanner(context: Context, iScannerResultProviderListener: IScannerResultProviderListener) {
        Log.d(TAG, "Initializing scanner in viewModel...")
        scannerServiceRepository.initScanner(context, iScannerResultProviderListener)
    }

    suspend fun startScanner(
        context: Context,
        data: Bundle,
        iScannerResultProviderListener: IScannerResultProviderListener

    ) {

        try {
            scannerServiceRepository.startScanner(
                context,
                data,
                iScannerResultProviderListener
            )

            Log.d(TAG, "Scanner started successfully in viewModel")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start scanner in viewModel: ${e.message}")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)   // To check for Refund Transaction is Found or not
    fun isRRLFound(invoiceNo: String) {
        viewModelScope.launch {
            // Check if the invoice exists
            val isFound = dbRepository.isRRLFound(invoiceNo)
            _isInvoiceFound.value = isFound
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getTxnDetailsByInvoiceNo(context: Context, sharedViewModel: SharedViewModel) {
        viewModelScope.launch {
            dbRepository.fetchTransactionByInvoiceNo(invoiceno.value)?.takeIf{ it.isNotEmpty() }?.let {
                if(it[0].isVoided==true || it[0].txnType==TxnType.VOID.toString())
                    CustomDialogBuilder.composeAlertDialog(
                        title = context.getString(R.string.default_alert_title_error),
                        message = context.getString(R.string.err_txn_already_voided))
                else if(sharedViewModel.objRootAppPaymentDetail.txnType == TxnType.REFUND && it[0].isRefunded==true)
                    CustomDialogBuilder.composeAlertDialog(
                        title = context.getString(R.string.default_alert_title_error),
                        message = context.getString(R.string.err_txn_already_refunded))
                else if(sharedViewModel.objRootAppPaymentDetail.txnType == TxnType.REFUND && (it[0].txnType!= TxnType.PURCHASE.toString()))
                    CustomDialogBuilder.composeAlertDialog(
                        title = context.getString(R.string.default_alert_title_error),
                        message = context.getString(R.string.err_only_purchase_can_be_refunded))
                else if(sharedViewModel.objRootAppPaymentDetail.txnType == TxnType.AUTHCAP && it[0].txnType.toString()!= TxnType.PREAUTH.toString())
                    CustomDialogBuilder.composeAlertDialog(
                        title = context.getString(R.string.default_alert_title_error),
                        message = context.getString(R.string.err_only_preauth_can_be_captured))
                else if(sharedViewModel.objRootAppPaymentDetail.txnType == TxnType.AUTHCAP && it[0].isCaptured==true)
                    CustomDialogBuilder.composeAlertDialog(
                        title = context.getString(R.string.default_alert_title_error),
                        message = context.getString(R.string.err_txn_already_captured))
                else if(it[0].isDemoMode != sharedViewModel.objRootAppPaymentDetail.isDemoMode)
                    CustomDialogBuilder.composeAlertDialog(
                        title = context.getString(R.string.default_alert_title_error),
                        message = context.getString(R.string.err_cant_mix_demo_trans))
                else {
                    PaymentServiceUtils.transformObject<ObjRootAppPaymentDetails>(it[0])?.let {
                        sharedViewModel.objRootAppPaymentDetail = it.copy(
                            id = sharedViewModel.objRootAppPaymentDetail.id,
                            txnType = sharedViewModel.objRootAppPaymentDetail.txnType,
                            txnStatus = sharedViewModel.objRootAppPaymentDetail.txnStatus,
                            hostAuthResult = sharedViewModel.objRootAppPaymentDetail.hostAuthResult
                        )
                        sharedViewModel.objRootAppPaymentDetail.originalTxnType = it.txnType
                        sharedViewModel.objRootAppPaymentDetail.originalTip =
                            it.tip.toDecimalFormat()
                        sharedViewModel.objRootAppPaymentDetail.originalCGST =
                            it.CGST.toDecimalFormat()
                        sharedViewModel.objRootAppPaymentDetail.originalSGST =
                            it.SGST.toDecimalFormat()
                        sharedViewModel.objRootAppPaymentDetail.originalCashback =
                            it.cashback.toDecimalFormat()
                        sharedViewModel.objRootAppPaymentDetail.originalTtlAmount =
                            it.ttlAmount.toDecimalFormat()
                        sharedViewModel.objRootAppPaymentDetail.originalTxnAmount =
                            it.txnAmount.toDecimalFormat()
                        sharedViewModel.objRootAppPaymentDetail.originalHostTxnRef = it.hostTxnRef
                    }

                    navigateToAmountScreen(navHostController,sharedViewModel)
                }
            }?:let {
                CustomDialogBuilder.composeAlertDialog(
                    title = context.getString(R.string.default_alert_title_error),
                    message = context.getString(R.string.err_txn_not_found))
            }
        }
    }

    fun onLoad(sharedViewModel: SharedViewModel) {
        if (sharedViewModel.objRootAppPaymentDetail.txnType == TxnType.PURCHASE || sharedViewModel.objRootAppPaymentDetail.txnType == TxnType.PREAUTH) {
        viewModelScope.launch {
                dbRepository.getLastInvoiceNumber().let {
                    _invoiceno.value = (it + 1).toString()
                }
            }
        }
    }
}
