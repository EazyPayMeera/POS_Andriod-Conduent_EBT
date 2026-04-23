package com.eazypaytech.pos.features.confirmshift.ui

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.analogics.builder_core.data.constants.BuilderConstants
import com.analogics.paymentservicecore.data.listeners.responseListener.IApiServiceResponseListener
import com.analogics.paymentservicecore.data.model.PaymentServiceTxnDetails
import com.analogics.paymentservicecore.data.model.error.ApiServiceError
import com.analogics.paymentservicecore.data.model.error.ApiServiceTimeout
import com.analogics.paymentservicecore.domain.repository.apiService.ApiServiceRepository
import com.analogics.paymentservicecore.utils.PaymentServiceUtils
import com.eazypaytech.pos.R
import com.eazypaytech.pos.features.activity.ui.SharedViewModel
import com.eazypaytech.pos.features.dialogs.ui.CustomDialogBuilder
import com.eazypaytech.pos.navigation.AppNavigationItems
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConfirmShiftViewModel @Inject constructor(private val apiServiceRepository: ApiServiceRepository) : ViewModel() {

    /**
     * Handles cancel action.
     *
     * Safely navigates back in the stack.
     */
    fun onCancel(navController: NavController) {
        try {
            navController.popBackStack()
        }catch (e:Exception)
        {
           // AppLogger.e(AppLogger.MODULE.APP_LOGIC,e.message.toString())
        }
    }

    /**
     * Handles shift end action.
     *
     * Initiates sign-off process with server.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun onShiftEnd(navController: NavController, sharedViewModel: SharedViewModel) {
        try {
            signOff(sharedViewModel,navController)
        }catch (e:Exception)
        {
            //AppLogger.e(AppLogger.MODULE.APP_LOGIC,e.message.toString())
        }
    }

    /**
     * Performs sign-off API call.
     *
     * Flow:
     * - Sends sign-on/off request to server
     * - On success:
     *    - If approved → navigate to Activation screen
     *    - Else → navigate back
     * - Handles timeout with alert dialog
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun signOff(sharedViewModel: SharedViewModel, navHostController: NavController) {
        viewModelScope.launch {
            try {
                apiServiceRepository.signOnOff(paymentServiceTxnDetails = PaymentServiceUtils.transformObject<PaymentServiceTxnDetails>(sharedViewModel.objRootAppPaymentDetail), object :
                    IApiServiceResponseListener {

                    override fun onApiServiceSuccess(response: PaymentServiceTxnDetails) {
                        viewModelScope.launch(Dispatchers.Main) {
                            if(response.hostRespCode == BuilderConstants.ISO_RESP_CODE_APPROVED) {
                                navHostController.navigate(AppNavigationItems.ActivationScreen.route)
                            }else{
                                navHostController.popBackStack()
                            }
                        }

                    }

                    override fun onApiServiceError(error: ApiServiceError) {
                        //navHostController.navigate(AppNavigationItems.ApprovedScreen.route)
                    }
                    override  fun onApiServiceTimeout(apiServiceTimeout: ApiServiceTimeout) {
                        CustomDialogBuilder.Companion.composeAlertDialog(title = navHostController.context.resources?.getString(
                            R.string.default_alert_title_error),message = apiServiceTimeout.message)
                    }

                })
            } catch (e: Exception) {

                Log.e("ApiCallException", e.message ?: "Unknown error")

            }
        }
    }
}