package com.analogics.tpaymentsapos.rootUiScreens.splash.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.analogics.paymentservicecore.repository.paymentService.PaymentServiceRepository
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashScreenViewModel @Inject constructor(private  var paymentServiceRepository: PaymentServiceRepository) : ViewModel() {
    fun onSplashScreenFinished(navController: NavController) {
        viewModelScope.launch {
            paymentServiceRepository.getPosConfig().loadFromPrefs(navController.context)
            if(paymentServiceRepository.getPosConfig().isLoggedIn==true)
                navController.navigate(AppNavigationItems.DashBoardScreen.route)
            else if(paymentServiceRepository.getPosConfig().isOnboardingComplete==true)
                navController.navigate(AppNavigationItems.LoginScreen.route)
            else {
                delay(3000L) //  delay for splash screen
                navController.navigate(AppNavigationItems.OnBoardingScreen.route)
            }
        }
    }
}