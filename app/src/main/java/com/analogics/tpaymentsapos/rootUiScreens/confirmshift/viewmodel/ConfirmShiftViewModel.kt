package com.analogics.tpaymentsapos.rootUiScreens.confirmshift.viewmodel
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.analogics.paymentservicecore.logger.AppLogger
import com.analogics.paymentservicecore.repository.paymentService.PaymentServiceRepository
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ConfirmShiftViewModel @Inject constructor(private val paymentServiceRepository: PaymentServiceRepository) : ViewModel() {

    fun onShiftEnd(navController: NavController) {
        try {
            navController.navigate(AppNavigationItems.LoginScreen.route)
            paymentServiceRepository.getPosConfig().apply { isLoggedIn=false }.saveToPrefs(navController.context)
        }catch (e:Exception)
        {
            AppLogger.e(AppLogger.MODULE.APP_LOGIC,e.message.toString())
        }
    }
}