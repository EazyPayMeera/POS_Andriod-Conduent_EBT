package com.eazypaytech.pos.features.authCode.ui

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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
import com.eazypaytech.pos.R
import com.eazypaytech.pos.features.activity.ui.SharedViewModel
import com.eazypaytech.pos.features.dialogs.ui.CustomDialogBuilder
import com.eazypaytech.pos.navigation.AppNavigationItems
import com.eazypaytech.pos.core.utils.navigateAndClean
import com.analogics.securityframework.data.repository.TxnDBRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(private  var apiServiceRepository: ApiServiceRepository, private val dbRepository: TxnDBRepository) : ViewModel() {

    var authCode by mutableStateOf("")
        private set

    val isFormValid: Boolean
        get() = authCode.isNotBlank() &&
                authCode.length == AppConstants.MAX_LENGTH_AUTH_CODE
    /**
     * Updates authorization code input value.
     */
    fun onCardNoChange(newValue: String) {
        authCode = newValue
    }
    /**
     * Handles confirm action for authorization input.
     *
     * Flow:
     * - Stores auth code in sharedViewModel
     * - Validates input
     * - Shows error dialog if invalid
     * - Navigates to transaction selection screen if valid
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun onConfirm(context: Context,navHostController: NavHostController, sharedViewModel: SharedViewModel) {
        sharedViewModel.objRootAppPaymentDetail.approvalCode = authCode
        if (!isFormValid) {
            CustomDialogBuilder.Companion.composeAlertDialog(
                title = navHostController.context.getString(R.string.default_alert_title_error),
                message = context.getString(R.string.auth_code_limit)
            )
        } else {

            navHostController.navigate(AppNavigationItems.TxnSelScreen.route)

        }
    }
    /**
     * Handles cancel action and navigates back to Dashboard.
     */
    fun onCancel(navHostController: NavHostController) {
        navHostController.navigateAndClean(AppNavigationItems.DashBoardScreen.route)
    }


}