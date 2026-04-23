package com.eazypaytech.pos.features.voucher.ui

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.navigation.NavHostController
import com.analogics.paymentservicecore.domain.repository.apiService.ApiServiceRepository
import com.eazypaytech.pos.navigation.AppNavigationItems
import com.eazypaytech.pos.features.activity.ui.SharedViewModel
import com.eazypaytech.pos.features.dialogs.ui.CustomDialogBuilder
import com.eazypaytech.pos.core.utils.navigateAndClean
import com.analogics.securityframework.data.repository.TxnDBRepository
import com.eazypaytech.pos.R
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class VoucherCardViewModel @Inject constructor(private  var apiServiceRepository: ApiServiceRepository, private val dbRepository: TxnDBRepository) : ViewModel() {

    var VoucherNumber by mutableStateOf("")
        private set

    /**
     * Updates voucher number input.
     *
     * @param newValue Entered voucher number
     */
    fun onCardNoChange(newValue: String) {
        VoucherNumber = newValue
    }

    /**
     * Handles confirmation of voucher entry.
     *
     * Behavior:
     * - Stores voucher number in transaction object
     * - Validates if voucher number is entered
     * - Shows error dialog if empty
     * - Navigates to Auth Code screen if valid
     *
     * @param context Application context for resource access
     * @param navHostController Navigation controller for screen transitions
     * @param sharedViewModel Shared ViewModel containing transaction data
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun onConfirm(context: Context, navHostController: NavHostController, sharedViewModel: SharedViewModel) {
        sharedViewModel.objRootAppPaymentDetail.voucherNumber = VoucherNumber
        if(VoucherNumber.isEmpty()) {
            CustomDialogBuilder.composeAlertDialog(
                title = navHostController.context.getString(R.string.default_alert_title_error),
                message = context.getString(R.string.plz_enter_voucher),
            )
        }
        else {
            navHostController.navigate(AppNavigationItems.AuthCodeScreen.route)

        }
    }

    /**
     * Handles cancel action.
     *
     * Behavior:
     * - Navigates user back to Dashboard
     * - Clears navigation stack
     *
     * @param navHostController Navigation controller for screen transitions
     */
    fun onCancel(navHostController: NavHostController) {
        navHostController.navigateAndClean(AppNavigationItems.DashBoardScreen.route)
    }

}