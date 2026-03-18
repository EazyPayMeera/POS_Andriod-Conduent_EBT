package com.eazypaytech.posafrica.rootUiScreens.manualentry

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.eazypaytech.builder_core.model.CardEntryMode
import com.eazypaytech.paymentservicecore.listeners.responseListener.IApiServiceResponseListener
import com.eazypaytech.paymentservicecore.model.PaymentServiceTxnDetails
import com.eazypaytech.paymentservicecore.model.error.ApiServiceError
import com.eazypaytech.paymentservicecore.model.error.ApiServiceTimeout
import com.eazypaytech.paymentservicecore.models.TxnStatus
import com.eazypaytech.paymentservicecore.models.TxnType
import com.eazypaytech.paymentservicecore.repository.apiService.ApiServiceRepository
import com.eazypaytech.paymentservicecore.repository.emvService.EmvServiceRepository
import com.eazypaytech.paymentservicecore.utils.PaymentServiceUtils
import com.eazypaytech.posafrica.R
import com.eazypaytech.posafrica.navigation.AppNavigationItems
import com.eazypaytech.posafrica.rootUiScreens.activity.SharedViewModel
import com.eazypaytech.posafrica.rootUiScreens.dialogs.CustomDialogBuilder
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.navigateAndClean
import com.eazypaytech.securityframework.database.dbRepository.TxnDBRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ManualCardViewModel @Inject constructor(
    private  var apiServiceRepository: ApiServiceRepository,
    private val emvServiceRepository: EmvServiceRepository,
    private val dbRepository: TxnDBRepository
) : ViewModel() {


    var cardNumber by mutableStateOf("")
        private set


    fun onCardNoChange(newValue: String) {
        cardNumber = newValue
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun onConfirm(
        navHostController: NavHostController,
        sharedViewModel: SharedViewModel
    ) {
        sharedViewModel.objRootAppPaymentDetail.cardMaskedPan = cardNumber
        sharedViewModel.objRootAppPaymentDetail.cardEntryMode =
            com.eazypaytech.paymentservicecore.model.emv.CardEntryMode.MANUAL
        when {
            cardNumber.isEmpty() -> {
                showError(
                    navHostController,
                    "Please Enter Card Details"
                )
            }

            sharedViewModel.objRootAppPaymentDetail.txnType == TxnType.E_VOUCHER -> {
                navHostController.navigate(AppNavigationItems.AuthCodeScreen.route)
            }

            else -> {
                generatePinAndProceed(navHostController,sharedViewModel)
            }
        }
    }

    fun onCancel(navHostController: NavHostController) {
        navHostController.navigateAndClean(AppNavigationItems.DashBoardScreen.route)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun generatePinAndProceed(
        navHostController: NavHostController,
        sharedViewModel: SharedViewModel
    ) {
        sharedViewModel.objRootAppPaymentDetail.ttlAmount.toString().let {
            emvServiceRepository.pinGeneration(
                pan = cardNumber,
                amount = it
            ) { pinBlock ->

                if (pinBlock != null) {
                    val pinBlockHex = pinBlock.toHexString()
                    sharedViewModel.objRootAppPaymentDetail.pinBlock = pinBlockHex
                    authenticateTransaction(sharedViewModel,navHostController)

                } else {
                    Log.e("PIN_FLOW", "PIN entry cancelled or failed")
                }
            }
        }
    }


    private fun showError(
        navHostController: NavHostController,
        message: String
    ) {
        CustomDialogBuilder.composeAlertDialog(
            title = navHostController.context.getString(R.string.default_alert_title_error),
            message = message
        )
    }

    private fun ByteArray.toHexString(): String {
        return joinToString("") { "%02X".format(it) }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun authenticateTransaction(sharedViewModel: SharedViewModel, navHostController: NavHostController) {
        viewModelScope.launch {
            try {
                Log.d("AuthTransaction","Going For Authenticate the transaction")
                apiServiceRepository.apiServiceRequestOnlineAuth(paymentServiceTxnDetails = PaymentServiceUtils.transformObject<PaymentServiceTxnDetails>(sharedViewModel.objRootAppPaymentDetail), object :
                    IApiServiceResponseListener {

                    override fun onApiServiceSuccess(response: PaymentServiceTxnDetails) {
                        sharedViewModel.objRootAppPaymentDetail.txnStatus = if(response.txnStatus == TxnStatus.APPROVED.toString()) TxnStatus.APPROVED else TxnStatus.DECLINED
                        navHostController.navigate(AppNavigationItems.ApprovedScreen.route)
                    }

                    override fun onApiServiceError(error: ApiServiceError) {
                        navHostController.navigate(AppNavigationItems.ApprovedScreen.route)
                    }
                    override  fun onApiServiceTimeout(apiServiceTimeout: ApiServiceTimeout) {
                        CustomDialogBuilder.composeAlertDialog(title = navHostController.context.resources?.getString(R.string.default_alert_title_error),message = apiServiceTimeout.message)
                    }

                })
            } catch (e: Exception) {

                Log.e("ApiCallException", e.message ?: "Unknown error")
                navHostController.navigate(AppNavigationItems.DeclineScreen.route)
            }
        }
    }
}