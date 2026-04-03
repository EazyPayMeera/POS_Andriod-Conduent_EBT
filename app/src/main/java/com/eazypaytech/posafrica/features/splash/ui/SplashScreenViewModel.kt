package com.eazypaytech.posafrica.features.splash.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.eazypaytech.paymentservicecore.constants.AppConstants
import com.eazypaytech.paymentservicecore.repository.apiService.ApiServiceRepository
import com.eazypaytech.posafrica.features.activity.ui.SharedViewModel
import com.eazypaytech.posafrica.core.navigation.routes.AppNavigationItems
import com.eazypaytech.posafrica.core.utils.navigateAndClean
import com.eazypaytech.posafrica.core.utils.setUiLanguage
import com.eazypaytech.posafrica.core.utils.language.UiLanguage
import com.eazypaytech.posafrica.core.utils.language.toUiLanguage
import com.eazypaytech.securityframework.database.dbRepository.TxnDBRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashScreenViewModel @Inject constructor(private  var apiServiceRepository: ApiServiceRepository, private var dbRepository: TxnDBRepository) : ViewModel() {
    fun onSplashScreenFinished(navController: NavController, sharedViewModel: SharedViewModel) {
        viewModelScope.launch {
            sharedViewModel.objPosConfig = apiServiceRepository.getPosConfig()

            /* Apply UI Language */
            setUiLanguage(
                navController.context,
                sharedViewModel.objPosConfig?.language?.toUiLanguage() ?: UiLanguage.ENGLISH
            )

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