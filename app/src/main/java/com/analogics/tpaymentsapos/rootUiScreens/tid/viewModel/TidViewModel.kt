package com.analogics.tpaymentsapos.rootUiScreens.tid.viewModel

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.analogics.builder_core.model.PaymentServiceTxnDetails
import com.analogics.paymentservicecore.listeners.responseListener.IApiServiceResponseListener
import com.analogics.paymentservicecore.logger.AppLogger
import com.analogics.paymentservicecore.model.error.ApiServiceError
import com.analogics.paymentservicecore.repository.apiService.ApiServiceRepository
import com.analogics.paymentservicecore.utils.PaymentServiceUtils
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import com.analogics.tpaymentsapos.rootModel.ObjRootAppPaymentDetails
import com.analogics.tpaymentsapos.rootUiScreens.activity.SharedViewModel
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.navigateAndClean
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
@HiltViewModel
class TidViewModel@Inject constructor(private var apiServiceRepository: ApiServiceRepository) :
    ViewModel(),
    IApiServiceResponseListener {
    var tidInput = mutableStateOf("")
    var midInput = mutableStateOf("")
    val isActivationEnabled = mutableStateOf(true)
    var useRootAppPaymentDetails = MutableStateFlow(ObjRootAppPaymentDetails())
    var userApiServiceErrorHolder = MutableStateFlow(ApiServiceError())
    lateinit var navHostController: NavHostController
    var sharedViewModel : SharedViewModel? = null
    val isFormValid: Boolean
        get() = tidInput.value.isNotBlank() && midInput.value.isNotBlank()

    fun onTidChange(tid: String) {
        tidInput.value = tid
    }

    fun onMidChange(mid: String) {
        midInput.value = mid
    }
    fun setActivationButtonState(enabled: Boolean)  { isActivationEnabled.value = enabled }
    fun onActivationClick(navHost: NavHostController?, sharedViewModel : SharedViewModel) {
        this.navHostController = navHost!!
        this.sharedViewModel = sharedViewModel

        viewModelScope.launch {
            try {

                setActivationButtonState(false)
                startActivateProcess()
                navHostController.navigateAndClean(AppNavigationItems.ClerkLoginScreen.route)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun startActivateProcess() {
        viewModelScope.launch {
            try {
                val requestDetails =
                    PaymentServiceUtils.objectToJsonString(useRootAppPaymentDetails.value)
                apiServiceRepository.apiServiceAccessToken(
                    PaymentServiceUtils.jsonStringToObject<PaymentServiceTxnDetails>(requestDetails), this@TidViewModel)
            } catch (e: Exception) {
                AppLogger.d(AppLogger.MODULE.APP_UI, e.message ?: "")
            }
        }
    }

    override fun onApiSuccess(response: Any) {
        when (response) {
            is ObjRootAppPaymentDetails -> {
                useRootAppPaymentDetails.value = response
            }

            else -> {
                //userApiSuccessHolder.value = response as ObjEmployeeResponse
                if (isFormValid) {
                    sharedViewModel?.objPosConfig?.apply { isActivationDone = true }?.saveToPrefs()
                    navHostController.navigateAndClean(AppNavigationItems.DashBoardScreen.route)
                }
                Log.e("API Response", response.toString())
            }
        }
    }

    override fun onApiError(apiServiceError: ApiServiceError) {
       //
    }

    override fun onDisplayProgress(
        show: Boolean,
        title: String?,
        subTitle: String?,
        message: String?
    ) {
      //
    }
}