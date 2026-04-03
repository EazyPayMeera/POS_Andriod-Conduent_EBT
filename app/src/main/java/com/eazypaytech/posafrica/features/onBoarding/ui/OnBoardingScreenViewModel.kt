package com.eazypaytech.posafrica.features.onBoarding.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.eazypaytech.paymentservicecore.repository.apiService.ApiServiceRepository
import com.eazypaytech.posafrica.features.activity.ui.SharedViewModel
import com.eazypaytech.posafrica.core.navigation.routes.AppNavigationItems
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.PagerState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnBoardingScreenViewModel @Inject constructor(private  var apiServiceRepository: ApiServiceRepository) : ViewModel() {
    @OptIn(ExperimentalPagerApi::class)
    fun autoScrollPager(pagerState: PagerState) {
        viewModelScope.launch {
            while (true) {
                delay(3000)
                pagerState.animateScrollToPage(
                    page = (pagerState.currentPage + 1) % pagerState.pageCount
                )
            }
        }
    }

    fun onOnboardingCompleted(navController: NavController, sharedViewModel: SharedViewModel) {
        viewModelScope.launch {
            navController.navigate(AppNavigationItems.ActivationScreen.route)
            sharedViewModel.objPosConfig?.apply { isOnboardingComplete=true }?.saveToPrefs()
        }
    }
}