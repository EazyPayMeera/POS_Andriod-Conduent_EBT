package com.analogics.tpaymentsapos.rootUiScreens.splash.viewModel

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.analogics.paymentservicecore.constants.AppConstants
import com.analogics.paymentservicecore.repository.paymentService.PaymentServiceRepository
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import com.analogics.tpaymentsapos.rootUiScreens.activity.SharedViewModel
import com.analogics.tpaymentsapos.rootUiScreens.activity.localSharedViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashScreenViewModel @Inject constructor(private  var paymentServiceRepository: PaymentServiceRepository) : ViewModel() {
    fun onSplashScreenFinished(navController: NavController, sharedViewModel: SharedViewModel) {
        viewModelScope.launch {
            sharedViewModel.objPosConfig = paymentServiceRepository.getPosConfig(navController.context)
            if(sharedViewModel.objPosConfig?.isLoggedIn==true)
                navController.navigate(AppNavigationItems.DashBoardScreen.route)
            else if(sharedViewModel.objPosConfig?.isOnboardingComplete==true)
                navController.navigate(AppNavigationItems.LoginScreen.route)
            else {
                delay(AppConstants.SPLASH_SCREEN_TIMEOUT_MS) //  delay for splash screen
                navController.navigate(AppNavigationItems.OnBoardingScreen.route)
            }
        }
    }
}