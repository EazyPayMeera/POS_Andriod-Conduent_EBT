package com.analogics.tpaymentsapos.rootUiScreens.activationScreen.viewModel

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.analogics.builder_core.constants.BuilderConstants
import com.analogics.builder_core.model.PaymentServiceTxnDetails
import com.analogics.paymentservicecore.constants.AppConstants
import com.analogics.paymentservicecore.listeners.responseListener.IApiServiceResponseListener
import com.analogics.paymentservicecore.logger.AppLogger
import com.analogics.paymentservicecore.model.error.ApiServiceError
import com.analogics.paymentservicecore.repository.apiService.ApiServiceRepository
import com.analogics.paymentservicecore.utils.PaymentServiceUtils
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import com.analogics.tpaymentsapos.rootModel.ObjRootAppPaymentDetails
import com.analogics.tpaymentsapos.rootUiScreens.activity.SharedViewModel
import com.analogics.tpaymentsapos.rootUiScreens.dialogs.CustomDialogBuilder
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.navigateAndClean
import com.analogics.tpaymentsapos.R
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.getIsoResponseCodeString
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.apply
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.text.isNotBlank

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
                setActivationButtonState(false)
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
                collectActivationData()
                apiServiceRepository.apiServiceRklRequest(
                    PaymentServiceUtils.transformObject<PaymentServiceTxnDetails>(sharedViewModel?.objRootAppPaymentDetail), this@ActivationViewModel)
            } catch (e: Exception) {
                AppLogger.d(AppLogger.MODULE.APP_UI, e.message ?: "")
            }
        }
    }

    override fun onApiSuccess(response: Any) {
        when (response) {
            is PaymentServiceTxnDetails ->{
                var objRootAppPaymentDetails = PaymentServiceUtils.transformObject<ObjRootAppPaymentDetails>(response)
                if(objRootAppPaymentDetails?.hostRespCode == BuilderConstants.ISO_RESP_CODE_APPROVED) {
                    activateDevice(objRootAppPaymentDetails)
                }
                else {
                    CustomDialogBuilder.composeAlertDialog(
                        title = navHostController.context.resources?.getString(
                            R.string.default_alert_title_error
                        ),
                        message = getIsoResponseCodeString(navHostController.context,objRootAppPaymentDetails?.hostRespCode)
                    )
                    setActivationButtonState(true)
                }
            }
            is ObjRootAppPaymentDetails -> {
                sharedViewModel?.objRootAppPaymentDetail = response
            }

            else -> {
                if (isFormValid) {
                    sharedViewModel?.objPosConfig?.apply {
                        isActivationDone = true }?.saveToPrefs()
                    navHostController.navigateAndClean(AppNavigationItems.AddClerkScreen.route)
                }
                Log.e("API Response", response.toString())
            }
        }
    }

    fun activateDevice(objRootAppPaymentDetails: ObjRootAppPaymentDetails) {
        sharedViewModel?.objPosConfig?.apply { isActivationDone = true }?.saveToPrefs()
        navHostController.navigateAndClean(AppNavigationItems.AddClerkScreen.route)
    }

    override fun onApiError(apiServiceError: ApiServiceError) {
        setActivationButtonState(true)
        CustomDialogBuilder.composeAlertDialog(title = navHostController.context.resources?.getString(R.string.default_alert_title_error),message = apiServiceError.errorMessage)
    }

    override fun onDisplayProgress(
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