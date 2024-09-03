package com.analogics.tpaymentsapos.rootUiScreens.onBoarding.viewModel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.analogics.paymentservicecore.repository.paymentService.PaymentServiceRepository
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.PagerState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnBoardingScreenViewModel @Inject constructor(private  var paymentServiceRepository: PaymentServiceRepository) : ViewModel() {
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

    fun onOnboardingCompleted(navController: NavController) {
        viewModelScope.launch {
            navController.navigate(AppNavigationItems.LoginScreen.route)
            paymentServiceRepository.isOnboardingCompleted(navController.context,true)
        }
    }
}
