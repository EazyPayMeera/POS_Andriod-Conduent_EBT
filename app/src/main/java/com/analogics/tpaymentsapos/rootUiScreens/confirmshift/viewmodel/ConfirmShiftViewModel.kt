package com.analogics.tpaymentsapos.rootUiScreens.confirmshift.viewmodel
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.analogics.paymentservicecore.logger.AppLogger
import com.analogics.paymentservicecore.repository.paymentService.PaymentServiceRepository
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import com.analogics.tpaymentsapos.rootUiScreens.activity.SharedViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ConfirmShiftViewModel @Inject constructor(private val paymentServiceRepository: PaymentServiceRepository) : ViewModel() {

    fun onShiftEnd(navController: NavController, sharedViewModel: SharedViewModel) {
        try {
            navController.navigate(AppNavigationItems.LoginScreen.route)
            sharedViewModel.objPosConfig?.apply { isLoggedIn=false }?.saveToPrefs()
        }catch (e:Exception)
        {
            AppLogger.e(AppLogger.MODULE.APP_LOGIC,e.message.toString())
        }
    }
}