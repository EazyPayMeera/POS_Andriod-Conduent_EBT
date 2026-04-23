package com.eazypaytech.pos.features.onBoarding.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.analogics.paymentservicecore.domain.repository.apiService.ApiServiceRepository
import com.eazypaytech.pos.features.activity.ui.SharedViewModel
import com.eazypaytech.pos.navigation.AppNavigationItems
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.PagerState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnBoardingScreenViewModel @Inject constructor(private  var apiServiceRepository: ApiServiceRepository) : ViewModel() {

    /**
     * Handles completion of onboarding flow.
     *
     * Behavior:
     * - Navigates user to Activation screen
     * - Updates onboarding completion flag in POS config
     * - Persists configuration to local storage
     *
     * @param navController Navigation controller for screen transition
     * @param sharedViewModel Shared ViewModel containing POS configuration
     */
    fun onOnboardingCompleted(navController: NavController, sharedViewModel: SharedViewModel) {
        viewModelScope.launch {
            navController.navigate(AppNavigationItems.ActivationScreen.route)
            sharedViewModel.objPosConfig?.apply { isOnboardingComplete=true }?.saveToPrefs()
        }
    }
}