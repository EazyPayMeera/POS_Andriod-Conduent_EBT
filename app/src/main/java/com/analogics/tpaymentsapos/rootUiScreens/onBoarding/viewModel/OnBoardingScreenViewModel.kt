package com.analogics.tpaymentsapos.rootUiScreens.onBoarding.viewModel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.PagerState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class OnBoardingScreenViewModel : ViewModel() {
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
}
