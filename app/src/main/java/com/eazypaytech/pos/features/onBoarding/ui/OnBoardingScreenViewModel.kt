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