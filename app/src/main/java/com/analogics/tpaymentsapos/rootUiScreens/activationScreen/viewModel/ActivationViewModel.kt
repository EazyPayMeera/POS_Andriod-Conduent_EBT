package com.analogics.tpaymentsapos.rootUiScreens.activationScreen.viewModel

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.analogics.paymentservicecore.constants.AppConstants
import com.analogics.paymentservicecore.listeners.responseListener.IApiServiceResponseListener
import com.analogics.paymentservicecore.logger.AppLogger
import com.analogics.paymentservicecore.model.PaymentServiceTxnDetails
import com.analogics.paymentservicecore.model.error.ApiServiceError
import com.analogics.paymentservicecore.models.Acquirer
import com.analogics.paymentservicecore.models.TxnStatus
import com.analogics.paymentservicecore.repository.apiService.ApiServiceRepository
import com.analogics.paymentservicecore.utils.PaymentServiceUtils
import com.analogics.tpaymentsapos.R
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import com.analogics.tpaymentsapos.rootUiScreens.activity.SharedViewModel
import com.analogics.tpaymentsapos.rootUiScreens.dialogs.CustomDialogBuilder
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.getAcquirer
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.getIsoResponseCodeString
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.navigateAndClean
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.io.encoding.ExperimentalEncodingApi

@HiltViewModel
class ActivationViewModel@Inject constructor(private var apiServiceRepository: ApiServiceRepository) :
    ViewModel(),
    IApiServiceResponseListener {
    var tidInput = mutableStateOf("")
    var midInput = mutableStateOf("")
    val isActivationBtnEnabled = mutableStateOf(true)
    lateinit var navHostController: NavHostController
    var sharedViewModel : SharedViewModel? = null
    val isFormValid: Boolean
        get() = tidInput.value.isNotBlank() && tidInput.value.length == AppConstants.MAX_LENGTH_TID &&
                midInput.value.isNotBlank() &&  midInput.value.length == AppConstants.MAX_LENGTH_MID

    fun onTidChange(tid: String) {
        tidInput.value = tid
    }

    fun onMidChange(mid: String) {
        midInput.value = mid
    }
    fun setActivationButtonState(enabled: Boolean)  { isActivationBtnEnabled.value = enabled }
    fun onActivationClick(navHost: NavHostController?, sharedViewModel : SharedViewModel) {
        this.navHostController = navHost!!
        this.sharedViewModel = sharedViewModel

        viewModelScope.launch {
            try {
                startActivateProcess()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    @OptIn(ExperimentalEncodingApi::class)
    fun collectActivationData()
    {
        sharedViewModel?.objRootAppPaymentDetail?.terminalId = tidInput.value
        sharedViewModel?.objRootAppPaymentDetail?.merchantId = midInput.value
        sharedViewModel?.objPosConfig?.apply {
            terminalId = tidInput.value
            merchantId = midInput.value
        }?.saveToPrefs()
    }

    fun onInvalidFormData(context: Context) {
        var message = if(tidInput.value.length != AppConstants.MAX_LENGTH_TID)
            context.resources.getString(R.string.act_tid_length)
        else if(midInput.value.length != AppConstants.MAX_LENGTH_MID)
            context.resources.getString(R.string.act_mid_length)
        else
            context.resources.getString(R.string.act_empty_tid_mid)

        CustomDialogBuilder.composeAlertDialog(title = context.resources.getString(R.string.default_alert_title_error),
            message = message
        )
    }

    fun startActivateProcess() {
        viewModelScope.launch {
            try {
                // Disable the activation button to prevent multiple clicks
                setActivationButtonState(false)

                // Collect required activation data
                collectActivationData()

                // Uncomment the following line if needed to make the API request with the transformed object
                apiServiceRepository.apiServiceRklRequest(
                    PaymentServiceUtils.transformObject<PaymentServiceTxnDetails>(
                        sharedViewModel?.objRootAppPaymentDetail
                    ), this@ActivationViewModel
                )

                // Inject keys and activate the device if key injection is successful
                /*
                val keyInjectionSuccess = PaymentServiceUtils.injectKeys(
                    "FB7BB5FC24E765B61E1FB80F6AD4FB83",
                    "FFFF6986499C2C600000",
                    "44934E"
                )

                if (keyInjectionSuccess) {
                    activateDevice()
                }
                 */

            } catch (e: Exception) {
                AppLogger.d(AppLogger.MODULE.APP_UI, e.message ?: "Unknown error occurred during activation.")
            }
        }
    }

    fun activateDevice() {
        loadDefaultValues(sharedViewModel)
        sharedViewModel?.objPosConfig?.apply {
            isActivationDone = true
        }?.saveToPrefs()
        navHostController.navigateAndClean(AppNavigationItems.AddClerkScreen.route)
    }

    fun loadDefaultValues(sharedViewModel: SharedViewModel?)
    {
        /* Default Flags */
        sharedViewModel?.objPosConfig?.isDemoMode = AppConstants.DEFAULT_DEMO_MODE
        sharedViewModel?.objPosConfig?.isTipEnabled = AppConstants.DEFAULT_TIP_MODE
        sharedViewModel?.objPosConfig?.isTaxEnabled = AppConstants.DEFAULT_TAX_MODE
        sharedViewModel?.objPosConfig?.isPromptInvoiceNo = AppConstants.DEFAULT_INVOICE_MODE

        /* Default TIP Percent values */
        sharedViewModel?.objPosConfig?.tipPercent1 = AppConstants.DEFAULT_TIP_PERCENT_1.toDouble()
        sharedViewModel?.objPosConfig?.tipPercent2 = AppConstants.DEFAULT_TIP_PERCENT_2.toDouble()
        sharedViewModel?.objPosConfig?.tipPercent3 = AppConstants.DEFAULT_TIP_PERCENT_3.toDouble()

        /* Default TAX Percent values */
        sharedViewModel?.objPosConfig?.SGSTPercent = AppConstants.DEFAULT_TAX_PERCENT_SGST
        sharedViewModel?.objPosConfig?.CGSTPercent = AppConstants.DEFAULT_TAX_PERCENT_CGST

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
        if (paymentServiceTxnDetails.txnStatus == TxnStatus.APPROVED.toString()) {
            activateDevice()
        } else {
            CustomDialogBuilder.composeAlertDialog(
                title = navHostController.context.resources?.getString(
                    R.string.default_alert_title_error
                ),
                message = getIsoResponseCodeString(
                    navHostController.context,
                    paymentServiceTxnDetails.hostRespCode
                )
            )
            setActivationButtonState(true)
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
        sharedViewModel.objPosConfig?.terminalId?.let { tidInput.value = it }
        sharedViewModel.objPosConfig?.merchantId?.let { midInput.value = it }
    }
}