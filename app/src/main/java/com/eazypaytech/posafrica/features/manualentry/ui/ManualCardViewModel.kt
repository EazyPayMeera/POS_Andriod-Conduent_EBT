package com.eazypaytech.posafrica.features.manualentry.ui

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.analogics.builder_core.data.constants.BuilderConstants

import com.eazypaytech.paymentservicecore.constants.AppConstants
import com.analogics.paymentservicecore.data.listeners.responseListener.IApiServiceResponseListener
import com.analogics.paymentservicecore.data.model.PaymentServiceTxnDetails
import com.analogics.paymentservicecore.data.model.emv.CardEntryMode
import com.analogics.paymentservicecore.data.model.error.ApiServiceError
import com.analogics.paymentservicecore.data.model.error.ApiServiceTimeout
import com.analogics.paymentservicecore.data.model.TxnStatus
import com.analogics.paymentservicecore.data.model.TxnType
import com.analogics.paymentservicecore.domain.repository.apiService.ApiServiceRepository
import com.analogics.paymentservicecore.domain.repository.emvService.EmvServiceRepository
import com.analogics.paymentservicecore.utils.PaymentServiceUtils
import com.eazypaytech.posafrica.R
import com.eazypaytech.posafrica.core.navigation.routes.AppNavigationItems
import com.eazypaytech.posafrica.features.activity.ui.SharedViewModel
import com.eazypaytech.posafrica.features.dialogs.ui.CustomDialogBuilder
import com.eazypaytech.posafrica.core.utils.navigateAndClean
import com.eazypaytech.posafrica.core.utils.miscellaneous.NetworkUtils
import com.analogics.securityframework.data.repository.TxnDBRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ManualCardViewModel @Inject constructor(
    private  var apiServiceRepository: ApiServiceRepository,
    private val emvServiceRepository: EmvServiceRepository,
    private val dbRepository: TxnDBRepository
) : ViewModel() {
    lateinit var context: Context
    lateinit var sharedViewModel: SharedViewModel
    lateinit var navHostController : NavHostController

    var cardNumber by mutableStateOf("")
        private set
    private val _cardExists = MutableStateFlow<Boolean?>(null)
    val cardExists: StateFlow<Boolean?> = _cardExists

    val isFormValid: Boolean
        get() = cardNumber.isNotBlank() &&
                cardNumber.length == AppConstants.MAX_LENGTH_CARD_NO

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
            CardEntryMode.MANUAL
        when {
            cardNumber.isEmpty() -> {
                showError(
                    navHostController,
                    "Please Enter Card Details"
                )
            }

            sharedViewModel.objRootAppPaymentDetail.txnType == TxnType.E_VOUCHER -> {
                navHostController.navigate(AppNavigationItems.VoucherScreen.route)
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
                    val pinBlockHex = pinBlock.toHexString().lowercase()
                    sharedViewModel.objRootAppPaymentDetail.pinBlock = pinBlockHex
                    Log.d("TXN_TYPE", sharedViewModel.objRootAppPaymentDetail.txnType.toString())
                    if(sharedViewModel.objRootAppPaymentDetail.txnType == TxnType.CASH_WITHDRAWAL)
                    {
                        viewModelScope.launch(Dispatchers.Main) {
                            navHostController.navigate(AppNavigationItems.LoginScreen.route)
                        }

                    }
                    else {
                        authenticateTransaction(sharedViewModel, navHostController)
                    }

                } else {
                    Log.e("PIN_FLOW", "PIN entry cancelled or failed")
                }
            }
        }
    }


    fun isCardExists(context: Context): Boolean {
        return emvServiceRepository.isCardExists(context)
    }


    fun checkNetwork(context: Context)
    {
        if(NetworkUtils().checkForInternet(context)!=true)
        {
            CustomDialogBuilder.composeAlertDialog(
                title = context.getString(R.string.default_alert_title_error),
                message = context.getString(R.string.err_no_internet_connection),
            )
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

    fun onInvalidFormData(context: Context) {
        var message = if(cardNumber.length != AppConstants.MAX_LENGTH_CARD_NO)
            context.resources.getString(R.string.max_card_length_err)
        else
            context.resources.getString(R.string.act_empty_card_cvv)

        CustomDialogBuilder.composeAlertDialog(title = context.resources.getString(R.string.default_alert_title_error),
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
                CustomDialogBuilder.composeProgressDialog(true)
                apiServiceRepository.apiServiceRequestOnlineAuth(paymentServiceTxnDetails = PaymentServiceUtils.transformObject<PaymentServiceTxnDetails>(sharedViewModel.objRootAppPaymentDetail), object :
                    IApiServiceResponseListener {

                    override fun onApiServiceSuccess(response: PaymentServiceTxnDetails) {
                        CustomDialogBuilder.composeProgressDialog(false)
                        sharedViewModel.objRootAppPaymentDetail.hostResMessage = BuilderConstants.getIsoResponseMessage(response.hostRespCode.toString())
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