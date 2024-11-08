package com.analogics.tpaymentsapos.rootUiScreens.splash.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.analogics.paymentservicecore.constants.AppConstants
import com.analogics.paymentservicecore.repository.apiService.ApiServiceRepository
import com.analogics.securityframework.database.dbRepository.TxnDBRepository
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import com.analogics.tpaymentsapos.rootUiScreens.activity.SharedViewModel
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.navigateAndClean
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashScreenViewModel @Inject constructor(private  var apiServiceRepository: ApiServiceRepository, private var dbRepository: TxnDBRepository) : ViewModel() {
    fun onSplashScreenFinished(navController: NavController, sharedViewModel: SharedViewModel) {
        viewModelScope.launch {
            sharedViewModel.objPosConfig = apiServiceRepository.getPosConfig()

            /* Check if onboarding carousel is shown */
            if(sharedViewModel.objPosConfig?.isOnboardingComplete!=true)
            {
                delay(AppConstants.SPLASH_SCREEN_TIMEOUT_MS) //  delay for splash screen
                navController.navigateAndClean(AppNavigationItems.OnBoardingScreen.route)
            }
            /* Check if Terminal Activation is done */
            else if(sharedViewModel.objPosConfig?.isActivationDone!=true)
                navController.navigateAndClean(AppNavigationItems.ActivationScreen.route)
            /* If admin is not added, then add it */
            else if(dbRepository.getUserCount()<1)
                navController.navigateAndClean(AppNavigationItems.AddClerkScreen.route)
            /* If not logged in, then prompt login */
            else if(sharedViewModel.objPosConfig?.isLoggedIn!=true)
                navController.navigateAndClean(AppNavigationItems.LoginScreen.route)
            /* If logged in, then navigate to dashboard */
            else
                navController.navigateAndClean(AppNavigationItems.DashBoardScreen.route)
        }
    }
}