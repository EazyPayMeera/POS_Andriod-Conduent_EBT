package com.eazypaytech.posafrica.rootUiScreens.confirmshift.viewmodel
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.eazypaytech.paymentservicecore.logger.AppLogger
import com.eazypaytech.paymentservicecore.repository.apiService.ApiServiceRepository
import com.eazypaytech.posafrica.navigation.AppNavigationItems
import com.eazypaytech.posafrica.rootUiScreens.activity.SharedViewModel
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.navigateAndClean
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ConfirmShiftViewModel @Inject constructor(private val apiServiceRepository: ApiServiceRepository) : ViewModel() {

    fun onCancel(navController: NavController) {
        try {
            navController.popBackStack()
        }catch (e:Exception)
        {
            AppLogger.e(AppLogger.MODULE.APP_LOGIC,e.message.toString())
        }
    }

    fun onShiftEnd(navController: NavController, sharedViewModel: SharedViewModel) {
        try {
            navController.navigateAndClean(AppNavigationItems.LoginScreen.route)
            sharedViewModel.objPosConfig?.apply { isLoggedIn=false;isPaymentSDKInit=false }?.saveToPrefs()
        }catch (e:Exception)
        {
            AppLogger.e(AppLogger.MODULE.APP_LOGIC,e.message.toString())
        }
    }
}