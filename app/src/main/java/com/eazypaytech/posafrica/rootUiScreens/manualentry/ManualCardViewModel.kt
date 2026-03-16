package com.eazypaytech.posafrica.rootUiScreens.manualentry

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.navigation.NavHostController
import com.eazypaytech.paymentservicecore.models.TxnType
import com.eazypaytech.paymentservicecore.repository.apiService.ApiServiceRepository
import com.eazypaytech.posafrica.R
import com.eazypaytech.posafrica.navigation.AppNavigationItems
import com.eazypaytech.posafrica.rootUiScreens.activity.SharedViewModel
import com.eazypaytech.posafrica.rootUiScreens.dialogs.CustomDialogBuilder
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.navigateAndClean
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.transformToAmountDouble
import com.eazypaytech.securityframework.database.dbRepository.TxnDBRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class ManualCardViewModel @Inject constructor(private  var apiServiceRepository: ApiServiceRepository, private val dbRepository: TxnDBRepository) : ViewModel() {

    var cardnumber by mutableStateOf("")
        private set


    @RequiresApi(Build.VERSION_CODES.O)
    fun onLoad(sharedViewModel: SharedViewModel)
    {

    }

    fun onCardNoChange(newValue: String) {
        cardnumber = newValue
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun onConfirm(navHostController: NavHostController, sharedViewModel: SharedViewModel) {

        if (cardnumber.isEmpty()) {
            CustomDialogBuilder.composeAlertDialog(
                title = navHostController.context.getString(R.string.default_alert_title_error),
                message = "Please Enter Card Details"
            )
        } else if (sharedViewModel.objRootAppPaymentDetail.txnType == TxnType.E_VOUCHER) {

            navHostController.navigate(AppNavigationItems.AuthCodeScreen.route)

        } else {

            navHostController.navigate(AppNavigationItems.PinScreen.route)

        }
    }

    fun onCancel(navHostController: NavHostController) {
        navHostController.navigateAndClean(AppNavigationItems.DashBoardScreen.route)
    }

}