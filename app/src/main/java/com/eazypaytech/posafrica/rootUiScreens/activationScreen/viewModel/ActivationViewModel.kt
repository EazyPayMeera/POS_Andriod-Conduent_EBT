package com.eazypaytech.posafrica.rootUiScreens.activationScreen.viewModel

import android.content.Context
import android.graphics.Bitmap
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
import com.eazypaytech.paymentservicecore.model.error.ApiServiceTimeout
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
import com.eazypaytech.tpaymentcore.utils.HardwareUtils
import com.google.gson.Gson
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ActivationViewModel@Inject constructor(private var apiServiceRepository: ApiServiceRepository, private var txnDBRepository: TxnDBRepository) :
    ViewModel(),
    IApiServiceResponseListener {
    var procIdInput = mutableStateOf("")
    var tidInput = mutableStateOf("")
    var midInput = mutableStateOf("")
    private var currentStep = ActivationState.SIGN_ON
    val isActivationBtnEnabled = mutableStateOf(true)
    lateinit var navHostController: NavHostController
    var sharedViewModel : SharedViewModel? = null
    val isFormValid: Boolean
        get() = procIdInput.value.isNotBlank() && procIdInput.value.length == AppConstants.MAX_LENGTH_PROC_ID

    fun startKeepAlive() {
        viewModelScope.launch {
            while (isActive) {
                try {
                    keepAliveProcess()  // 'this' inside keepAliveProcess will be ViewModel
                    delay(5 * 60 * 1000L)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                delay(5 * 60 * 1000L)  // 5 minutes
            }
        }
    }

    fun onProcIdChange(tid: String) {
        procIdInput.value = tid
    }

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

    fun onInvalidFormData(context: Context) {
        var message = if(tidInput.value.length != AppConstants.MAX_LENGTH_TID)
            context.resources.getString(R.string.act_tid_length)
        else if(midInput.value.length != AppConstants.MAX_LENGTH_MID)
            context.resources.getString(R.string.act_mid_length)
        else if(procIdInput.value.length != AppConstants.MAX_LENGTH_PROC_ID)
            context.resources.getString(R.string.act_mid_length)
        else
            context.resources.getString(R.string.act_empty_tid_mid)

        CustomDialogBuilder.composeAlertDialog(title = context.resources.getString(R.string.default_alert_title_error),
            message = message
        )
    }

    fun collectActivationData()
    {
        sharedViewModel?.objRootAppPaymentDetail?.procId = procIdInput.value
        sharedViewModel?.objRootAppPaymentDetail?.merchantId = midInput.value
        sharedViewModel?.objRootAppPaymentDetail?.terminalId = tidInput.value
        sharedViewModel?.objPosConfig?.apply {
            terminalId = tidInput.value
            procId = procIdInput.value
            merchantId = midInput.value
        }?.saveToPrefs()
    }


    suspend fun keepAliveProcess() {
        currentStep = ActivationState.HAND_SHAKE
        apiServiceRepository.handShakeRequest(
            PaymentServiceUtils.transformObject(
                sharedViewModel?.objRootAppPaymentDetail
            ),
            this   // 👈 use ViewModel as listener
        )
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


    fun copyConfigToExternal(context: Context) {                                             // TODO TO COPY CONFIGURATION FILE TO EXTERNAL STORAGE
        val configFile = File(context.getExternalFilesDir(null), "Config.json")
        if (!configFile.exists()) {
            try {
                val defaultJson = context.assets.open("Config.json")
                    .bufferedReader()
                    .use { it.readText() }

                configFile.writeText(defaultJson)
            } catch (e: Exception) {
                Log.e("ConfigCopy", "Error copying config file: ${e.message}", e)
            }

        } else {
            Log.d("ConfigCopy", "Config file already exists. No copy needed.")
        }
    }

    fun readMasterKEK(context: Context,sharedViewModel: SharedViewModel): String? {
        val configFile = File(context.getExternalFilesDir(null), "Config.json")

        if (!configFile.exists()) {
            Log.d("ConfigRead", "Config file does not exist!")
            return null
        }

        return try {
            val json = configFile.readText()
            val jsonObject = Gson().fromJson(json, JsonObject::class.java)

            // Safely get the master_kek value
            val masterKey = jsonObject.get("master_kek")?.asString

            if (masterKey.isNullOrEmpty()) {
                Log.d("ConfigRead", "Master KEK is missing or empty in config!")
                null
            } else {
                // Assign to sharedViewModel safely
                sharedViewModel.objRootAppPaymentDetail.masterKey = masterKey
                masterKey
            }
        } catch (e: Exception) {
            Log.e("ConfigRead", "Error reading config: ${e.message}", e)
            null
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
        Log.d("ActivationCheck", "txnStatus = ${paymentServiceTxnDetails.txnStatus}, hostRespCode = ${paymentServiceTxnDetails.hostRespCode}")

        viewModelScope.launch(Dispatchers.Main) {

            if (paymentServiceTxnDetails.txnStatus != TxnStatus.APPROVED.toString() ||
                paymentServiceTxnDetails.hostRespCode != "00"
            ) {
                setActivationButtonState(true) // UI update
                return@launch
            }

            when (currentStep) {
                ActivationState.SIGN_ON -> {
                    currentStep = ActivationState.KEY_CHANGE
                    // launch key exchange here if needed
                }

                ActivationState.KEY_EXCHANGE -> {
                    currentStep = ActivationState.HAND_SHAKE
                    sharedViewModel?.objPosConfig?.apply {
                        isActivationDone = true
                    }?.saveToPrefs()

                    txnDBRepository.getUserCount().let {
                        if (it > 0)
                            navHostController.navigateAndClean(AppNavigationItems.LoginScreen.route)
                        else
                            navHostController.navigateAndClean(AppNavigationItems.AddClerkScreen.route)
                    }
                }

                ActivationState.KEY_CHANGE -> {
                    sharedViewModel?.objPosConfig?.apply {
                        isActivationDone = true
                    }?.saveToPrefs()

                    txnDBRepository.getUserCount().let {
                        if (it > 0)
                            navHostController.navigateAndClean(AppNavigationItems.LoginScreen.route)
                        else
                            navHostController.navigateAndClean(AppNavigationItems.AddClerkScreen.route)
                    }
                }

                ActivationState.HAND_SHAKE -> {
                    startKeepAlive()
                }
            }
        }
    }


    override fun onApiServiceError(apiServiceError: ApiServiceError) {
        setActivationButtonState(true)
        CustomDialogBuilder.composeAlertDialog(title = navHostController.context.resources?.getString(R.string.default_alert_title_error),message = apiServiceError.errorMessage)
    }

    override fun onApiServiceTimeout(apiServiceTimeout: ApiServiceTimeout) {
        CustomDialogBuilder.composeAlertDialog(title = navHostController.context.resources?.getString(R.string.default_alert_title_error),message = apiServiceTimeout.message)
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