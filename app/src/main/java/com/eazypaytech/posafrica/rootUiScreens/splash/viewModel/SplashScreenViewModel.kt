package com.eazypaytech.posafrica.rootUiScreens.splash.viewModel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.eazypaytech.networkservicecore.serviceutils.NetworkConstants
import com.eazypaytech.posafrica.data.TmsConfigParser
import com.eazypaytech.paymentservicecore.constants.AppConstants
import com.eazypaytech.paymentservicecore.repository.apiService.ApiServiceRepository
import com.eazypaytech.securityframework.database.dbRepository.TxnDBRepository
import com.eazypaytech.posafrica.navigation.AppNavigationItems
import com.eazypaytech.posafrica.rootModel.UiLanguage
import com.eazypaytech.posafrica.rootModel.toUiLanguage
import com.eazypaytech.posafrica.rootUiScreens.activity.SharedViewModel
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.navigateAndClean
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.setUiLanguage
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashScreenViewModel @Inject constructor(private  var apiServiceRepository: ApiServiceRepository, private var dbRepository: TxnDBRepository, @ApplicationContext private val context: Context) : ViewModel() {
    fun onSplashScreenFinished(navController: NavController, sharedViewModel: SharedViewModel) {
        viewModelScope.launch {
            //val tmsConfig = TmsConfigParser.loadConfig(navController.context)
            val tmsConfig = TmsConfigParser.loadFromAssets(context)
            val savedConfig = apiServiceRepository.getPosConfig()

            val finalConfig = tmsConfig ?: savedConfig
            finalConfig.isActivationDone = savedConfig.isActivationDone
            finalConfig.isLoggedIn = savedConfig.isLoggedIn
            finalConfig.isOnboardingComplete = savedConfig.isOnboardingComplete
            finalConfig.header1 = savedConfig.header1
            finalConfig.header2 = savedConfig.header2
            finalConfig.header3 = savedConfig.header3
            finalConfig.header4 = savedConfig.header4
            finalConfig.footer1 = savedConfig.footer1
            finalConfig.footer2 = savedConfig.footer2
            finalConfig.footer3 = savedConfig.footer3
            finalConfig.footer4 = savedConfig.footer4
            sharedViewModel.objPosConfig = finalConfig
            finalConfig.saveToPrefs()

            NetworkConstants.updateHost(
                baseUrl = finalConfig.baseUrl,
                port = finalConfig.port
            )

            /* Apply UI Language */
            setUiLanguage(navController.context,sharedViewModel.objPosConfig?.language?.toUiLanguage()?: UiLanguage.ENGLISH)

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