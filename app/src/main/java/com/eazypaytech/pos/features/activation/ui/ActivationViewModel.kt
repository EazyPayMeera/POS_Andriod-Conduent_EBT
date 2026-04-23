package com.eazypaytech.pos.features.activation.ui

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.eazypaytech.paymentservicecore.constants.AppConstants
import com.analogics.paymentservicecore.data.listeners.responseListener.IApiServiceResponseListener
import com.analogics.paymentservicecore.data.model.PaymentServiceTxnDetails
import com.analogics.paymentservicecore.data.model.error.ApiServiceError
import com.analogics.paymentservicecore.data.model.error.ApiServiceTimeout
import com.analogics.paymentservicecore.data.model.TxnStatus
import com.analogics.paymentservicecore.domain.repository.apiService.ApiServiceRepository
import com.analogics.paymentservicecore.utils.PaymentServiceUtils
import com.eazypaytech.pos.features.activity.ui.SharedViewModel
import com.eazypaytech.pos.features.dialogs.ui.CustomDialogBuilder
import com.eazypaytech.pos.navigation.AppNavigationItems
import com.eazypaytech.pos.core.utils.navigateAndClean
import com.eazypaytech.pos.core.service.KeepAliveService
import com.analogics.securityframework.data.repository.TxnDBRepository
import com.eazypaytech.pos.R
import com.google.gson.Gson
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
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



    /**
     * Updates Processor ID input value.
     */
    fun onProcIdChange(tid: String) {
        procIdInput.value = tid
    }
    /**
     * Updates Terminal ID input value.
     */

    fun onTidChange(tid: String) {
        tidInput.value = tid
    }
    /**
     * Updates Merchant ID input value.
     */

    fun onMidChange(mid: String) {
        midInput.value = mid
    }
    /**
     * Enables or disables activation button.
     */

    fun setActivationButtonState(enabled: Boolean)  { isActivationBtnEnabled.value = enabled }
    /**
     * Handles activation button click.
     *
     * Flow:
     * - Stores navigation and sharedViewModel references
     * - Starts activation process asynchronously
     */
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
    /**
     * Validates input fields and shows error dialog if invalid.
     */

    fun onInvalidFormData(context: Context) {
        var message = if(tidInput.value.length != AppConstants.MAX_LENGTH_TID)
            context.resources.getString(R.string.act_tid_length)
        else if(midInput.value.length != AppConstants.MAX_LENGTH_MID)
            context.resources.getString(R.string.act_mid_length)
        else if(procIdInput.value.length != AppConstants.MAX_LENGTH_PROC_ID)
            context.resources.getString(R.string.act_mid_length)
        else
            context.resources.getString(R.string.act_empty_tid_mid)

        CustomDialogBuilder.Companion.composeAlertDialog(title = context.resources.getString(R.string.default_alert_title_error),
            message = message
        )
    }
    /**
     * Collects activation input data and stores it in sharedViewModel and config.
     */
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

    /**
     * Initiates activation process starting with SIGN_ON step.
     *
     * Flow:
     * - Disables button
     * - Collects data
     * - Triggers SIGN_ON API call
     */
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

    /**
     * Copies default Config.json from assets to external storage if not present.
     */
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

    /**
     * Reads master KEK from config file and stores it in sharedViewModel.
     */

    fun readMasterKEK(context: Context, sharedViewModel: SharedViewModel): String? {
        val configFile = File(context.getExternalFilesDir(null), "Config.json")

        if (!configFile.exists()) {
            return null
        }

        return try {
            val json = configFile.readText()
            val jsonObject = Gson().fromJson(json, JsonObject::class.java)

            val masterKey = jsonObject.get("master_kek")?.asString

            if (masterKey.isNullOrEmpty()) {
                null
            } else {
                sharedViewModel.objRootAppPaymentDetail.masterKey = masterKey
                masterKey
            }
        } catch (e: Exception) {
            Log.e("ConfigRead", "Error reading config: ${e.message}", e)
            null
        }
    }
    /**
     * Loads default header and footer values into POS configuration.
     */

    fun loadDefaultValues(sharedViewModel: SharedViewModel?)
    {

        sharedViewModel?.objPosConfig?.header1 = AppConstants.DEFAULT_HEADER_1
        sharedViewModel?.objPosConfig?.header2 = AppConstants.DEFAULT_HEADER_2
        sharedViewModel?.objPosConfig?.header3 = AppConstants.DEFAULT_HEADER_3
        sharedViewModel?.objPosConfig?.header4 = AppConstants.DEFAULT_HEADER_4

        sharedViewModel?.objPosConfig?.footer1 = AppConstants.DEFAULT_FOOTER_1
        sharedViewModel?.objPosConfig?.footer2 = AppConstants.DEFAULT_FOOTER_2
        sharedViewModel?.objPosConfig?.footer3 = AppConstants.DEFAULT_FOOTER_3
        sharedViewModel?.objPosConfig?.footer4 = AppConstants.DEFAULT_FOOTER_4
        //sharedViewModel?.objPosConfig?.deviceSN = PaymentServiceUtils.getDeviceSN()

    }
    /**
     * Handles API success response for activation flow.
     *
     * Flow:
     * - Validates response
     * - Executes next step based on current activation state
     * - Completes activation and navigates accordingly
     */
    override fun onApiServiceSuccess(paymentServiceTxnDetails: PaymentServiceTxnDetails) {

        viewModelScope.launch(Dispatchers.Main) {
            if (paymentServiceTxnDetails.txnStatus != TxnStatus.APPROVED.toString() ||
                paymentServiceTxnDetails.hostRespCode != "00"
            ) {
                Log.e("ACTIVATION", "❌ Failed at step: $currentStep — enabling button")
                setActivationButtonState(true)
                return@launch
            }

            when (currentStep) {

                ActivationState.SIGN_ON -> {
                    sharedViewModel?.objRootAppPaymentDetail?.workKey = paymentServiceTxnDetails.workKey
                    currentStep = ActivationState.KEY_CHANGE
                }

                ActivationState.KEY_EXCHANGE -> {
//
                }

                ActivationState.KEY_CHANGE -> {
                    currentStep = ActivationState.HAND_SHAKE
                    apiServiceRepository.keyChange(
                        PaymentServiceUtils.transformObject(
                            sharedViewModel?.objRootAppPaymentDetail
                        ),
                        this@ActivationViewModel
                    )

                }

                ActivationState.HAND_SHAKE -> {
                    loadDefaultValues(sharedViewModel)
                    sharedViewModel?.objPosConfig?.apply {
                        isActivationDone = true
                    }?.saveToPrefs()
                    sharedViewModel?.objRootAppPaymentDetail?.let { detail ->
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            KeepAliveService.start(
                                navHostController.context,
                                detail
                            )
                        }
                    }
                    val userCount = txnDBRepository.getUserCount()

                    if (userCount > 0) {
                        navHostController.navigateAndClean(AppNavigationItems.LoginScreen.route)
                    } else {
                        navHostController.navigateAndClean(AppNavigationItems.AddClerkScreen.route)
                    }
                }
            }

        }
    }

    /**
     * Handles API error during activation.
     */

    override fun onApiServiceError(apiServiceError: ApiServiceError) {
        setActivationButtonState(true)
    }
    /**
     * Handles API timeout and shows alert dialog.
     */

    override fun onApiServiceTimeout(apiServiceTimeout: ApiServiceTimeout) {
        CustomDialogBuilder.composeAlertDialog(title = navHostController.context.resources?.getString(
            R.string.default_alert_title_error),message = apiServiceTimeout.message)
    }
    /**
     * Displays or hides progress dialog during API calls.
     */

    override fun onApiServiceDisplayProgress(
        show: Boolean,
        title: String?,
        subTitle: String?,
        message: String?
    ) {
        CustomDialogBuilder.composeProgressDialog(show = show,title = title, subtitle = subTitle, message = message)
    }
    /**
     * Loads saved configuration values into input fields.
     */

    fun onLoad(sharedViewModel : SharedViewModel)
    {
        sharedViewModel.objPosConfig?.procId?.let { procIdInput.value = it }
        sharedViewModel.objPosConfig?.merchantId?.let { midInput.value = it }
        sharedViewModel.objPosConfig?.terminalId?.let { tidInput.value = it }
    }
}