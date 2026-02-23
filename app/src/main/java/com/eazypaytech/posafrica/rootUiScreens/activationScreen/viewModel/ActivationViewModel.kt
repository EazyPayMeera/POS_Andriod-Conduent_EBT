package com.eazypaytech.posafrica.rootUiScreens.activationScreen.viewModel

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.eazypaytech.paymentservicecore.constants.AppConstants
import com.eazypaytech.paymentservicecore.listeners.responseListener.IApiServiceResponseListener
import com.eazypaytech.paymentservicecore.logger.AppLogger
import com.eazypaytech.paymentservicecore.model.PaymentServiceTxnDetails
import com.eazypaytech.paymentservicecore.model.error.ApiServiceError
import com.eazypaytech.paymentservicecore.models.Acquirer
import com.eazypaytech.paymentservicecore.models.TxnStatus
import com.eazypaytech.paymentservicecore.repository.apiService.ApiServiceRepository
import com.eazypaytech.paymentservicecore.utils.PaymentServiceUtils
import com.eazypaytech.securityframework.database.dbRepository.TxnDBRepository
import com.eazypaytech.posafrica.R
import com.eazypaytech.posafrica.navigation.AppNavigationItems
import com.eazypaytech.posafrica.rootUiScreens.activationScreen.ActivationState
import com.eazypaytech.posafrica.rootUiScreens.activity.SharedViewModel
import com.eazypaytech.posafrica.rootUiScreens.dialogs.CustomDialogBuilder
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.getAcquirer
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.navigateAndClean
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ActivationViewModel@Inject constructor(private var apiServiceRepository: ApiServiceRepository, private var txnDBRepository: TxnDBRepository) :
    ViewModel(),
    IApiServiceResponseListener {
    var procIdInput = mutableStateOf("00004000002")
    var midInput = mutableStateOf("")
    private var currentStep = ActivationState.SIGN_ON
    val isActivationBtnEnabled = mutableStateOf(true)
    lateinit var navHostController: NavHostController
    var sharedViewModel : SharedViewModel? = null
    val isFormValid: Boolean
        get() = procIdInput.value.isNotBlank() && procIdInput.value.length == AppConstants.MAX_LENGTH_PROC_ID


    fun setActivationButtonState(enabled: Boolean)  { isActivationBtnEnabled.value = enabled }

    fun collectActivationData()
    {
        sharedViewModel?.objRootAppPaymentDetail?.procId = "00004000002"
        sharedViewModel?.objRootAppPaymentDetail?.merchantId = midInput.value
        sharedViewModel?.objPosConfig?.apply {
            procId = "00004000007"
            merchantId = midInput.value
        }?.saveToPrefs()
    }

    suspend fun startActivateProcess() {
        setActivationButtonState(false)
        collectActivationData()
        currentStep = ActivationState.SIGN_ON
        apiServiceRepository.signOnRequest(
            PaymentServiceUtils.transformObject(
                sharedViewModel?.objRootAppPaymentDetail
            ),
            this   // 👈 use ViewModel as listener
        )
    }

    fun activateDevice() {
        loadDefaultValues(sharedViewModel)
        sharedViewModel?.objPosConfig?.apply {
            isActivationDone = true
        }?.saveToPrefs()
        viewModelScope.launch {
            navHostController.navigateAndClean(AppNavigationItems.DashBoardScreen.route)
//            txnDBRepository.getUserCount().let {
//                if(it>0)
//                    navHostController.navigateAndClean(AppNavigationItems.LoginScreen.route)
//                else
//                    navHostController.navigateAndClean(AppNavigationItems.AddClerkScreen.route)
//            }
        }
    }

    fun loadDefaultValues(sharedViewModel: SharedViewModel?)
    {
        /* Default Flags */
        sharedViewModel?.objPosConfig?.isDemoMode = AppConstants.DEFAULT_DEMO_MODE
        sharedViewModel?.objPosConfig?.isTipEnabled = AppConstants.DEFAULT_TIP_MODE
        sharedViewModel?.objPosConfig?.isServiceChargeEnabled = AppConstants.DEFAULT_SERVICE_CHARGE_MODE
        sharedViewModel?.objPosConfig?.isTaxEnabled = AppConstants.DEFAULT_TAX_MODE
        sharedViewModel?.objPosConfig?.isPromptInvoiceNo = AppConstants.DEFAULT_INVOICE_MODE

        /* Default TIP Percent values */
        sharedViewModel?.objPosConfig?.tipPercent1 = AppConstants.DEFAULT_TIP_PERCENT_1.toDouble()
        sharedViewModel?.objPosConfig?.tipPercent2 = AppConstants.DEFAULT_TIP_PERCENT_2.toDouble()
        sharedViewModel?.objPosConfig?.tipPercent3 = AppConstants.DEFAULT_TIP_PERCENT_3.toDouble()

        /* Default TAX Percent values */
        sharedViewModel?.objPosConfig?.serviceChargePercent1 = AppConstants.DEFAULT_SERVICE_CHARGE_PERCENT_1
        sharedViewModel?.objPosConfig?.serviceChargePercent2 = AppConstants.DEFAULT_SERVICE_CHARGE_PERCENT_2
        sharedViewModel?.objPosConfig?.serviceChargePercent3 = AppConstants.DEFAULT_SERVICE_CHARGE_PERCENT_3

        sharedViewModel?.objPosConfig?.vatPercent = AppConstants.DEFAULT_TAX_PERCENT_VAT

        sharedViewModel?.objPosConfig?.batchId = AppConstants.BATCH_ID_START_VAL.toString()

        /* Default Headers & Footers */
        sharedViewModel?.objPosConfig?.header1 = AppConstants.DEFAULT_HEADER_1
        sharedViewModel?.objPosConfig?.header2 = AppConstants.DEFAULT_HEADER_2
        sharedViewModel?.objPosConfig?.header3 = AppConstants.DEFAULT_HEADER_3
        sharedViewModel?.objPosConfig?.header4 = AppConstants.DEFAULT_HEADER_4

        sharedViewModel?.objPosConfig?.footer1 = AppConstants.DEFAULT_FOOTER_1
        sharedViewModel?.objPosConfig?.footer2 = AppConstants.DEFAULT_FOOTER_2
        sharedViewModel?.objPosConfig?.footer3 = AppConstants.DEFAULT_FOOTER_3
        sharedViewModel?.objPosConfig?.footer4 = AppConstants.DEFAULT_FOOTER_4

        /* Customer Care Info */
        sharedViewModel?.objPosConfig?.deviceSN = PaymentServiceUtils.getDeviceSN()
        if(getAcquirer(sharedViewModel?.objRootAppPaymentDetail)== Acquirer.LYRA) {
            sharedViewModel?.objPosConfig?.customerCareNumber = AppConstants.LYRA_CUSTOMER_CARE
        }
    }

    override fun onApiServiceSuccess(paymentServiceTxnDetails: PaymentServiceTxnDetails) {

        if (paymentServiceTxnDetails.txnStatus != TxnStatus.APPROVED.toString() ||
            paymentServiceTxnDetails.hostRespCode != "00"
        ) {
            setActivationButtonState(true)
            return
        }

        when (currentStep) {
            ActivationState.SIGN_ON -> {
                currentStep = ActivationState.KEY_EXCHANGE

                viewModelScope.launch {
                    apiServiceRepository.keyExchange(
                        PaymentServiceUtils.transformObject(
                            sharedViewModel?.objRootAppPaymentDetail
                        ),
                        this@ActivationViewModel
                    )
                }
            }

            ActivationState.KEY_EXCHANGE -> {
                viewModelScope.launch(Dispatchers.Main) {
                    currentStep = ActivationState.KEY_CHANGE
                    navHostController.navigateAndClean(
                        AppNavigationItems.DashBoardScreen.route
                    )
                }
//                viewModelScope.launch {
//                    apiServiceRepository.keyChange(
//                        PaymentServiceUtils.transformObject(
//                            sharedViewModel?.objRootAppPaymentDetail
//                        ),
//                        this@ActivationViewModel
//                    )
//                }
            }

            ActivationState.KEY_CHANGE -> {
                navHostController.navigateAndClean(
                    AppNavigationItems.DashBoardScreen.route
                )
            }
        }
    }


    override fun onApiServiceError(apiServiceError: ApiServiceError) {
        setActivationButtonState(true)
        CustomDialogBuilder.composeAlertDialog(title = navHostController.context.resources?.getString(R.string.default_alert_title_error),message = apiServiceError.errorMessage)
    }

    override fun onApiServiceDisplayProgress(
        show: Boolean,
        title: String?,
        subTitle: String?,
        message: String?
    ) {
        CustomDialogBuilder.composeProgressDialog(show = show,title = title, subtitle = subTitle, message = message)
    }

    fun onLoad(sharedViewModel : SharedViewModel)
    {

        sharedViewModel.objPosConfig?.procId?.let { procIdInput.value = it }
        sharedViewModel.objPosConfig?.merchantId?.let { midInput.value = it }
    }
}