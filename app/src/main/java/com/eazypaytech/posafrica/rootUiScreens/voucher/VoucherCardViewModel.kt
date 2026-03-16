package com.eazypaytech.posafrica.rootUiScreens.voucher

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.navigation.NavHostController
import com.eazypaytech.paymentservicecore.repository.apiService.ApiServiceRepository
import com.eazypaytech.posafrica.R
import com.eazypaytech.posafrica.navigation.AppNavigationItems
import com.eazypaytech.posafrica.rootUiScreens.activity.SharedViewModel
import com.eazypaytech.posafrica.rootUiScreens.dialogs.CustomDialogBuilder
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.navigateAndClean
import com.eazypaytech.securityframework.database.dbRepository.TxnDBRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class VoucherCardViewModel @Inject constructor(private  var apiServiceRepository: ApiServiceRepository, private val dbRepository: TxnDBRepository) : ViewModel() {

    var VoucherNumber by mutableStateOf("")
        private set


    @RequiresApi(Build.VERSION_CODES.O)
    fun onLoad(sharedViewModel: SharedViewModel)
    {

    }

    fun onCardNoChange(newValue: String) {
        VoucherNumber = newValue
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun onConfirm(navHostController: NavHostController, sharedViewModel: SharedViewModel) {
        if(VoucherNumber.isEmpty()) {
            CustomDialogBuilder.composeAlertDialog(
                title = navHostController.context.getString(R.string.default_alert_title_error),
                message = "Please Enter Card Details"
            )
        }
        else {
            navHostController.navigate(AppNavigationItems.AmountScreen.route)

        }
    }

    fun onCancel(navHostController: NavHostController) {
        navHostController.navigateAndClean(AppNavigationItems.DashBoardScreen.route)
    }

}