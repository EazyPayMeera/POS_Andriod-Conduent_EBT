package com.analogics.tpaymentsapos.rootUiScreens.onBoarding.viewModel
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableIntStateOf

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.accompanist.pager.ExperimentalPagerApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class OnBoardingScreenViewModel :ViewModel() {

    private val _currentPage = mutableIntStateOf(0)
    val currentPage: State<Int> = _currentPage


    @OptIn(ExperimentalPagerApi::class)
    fun startAutoScroll(pagerState: com.google.accompanist.pager.PagerState) {
        viewModelScope.launch {
            while (true) {
                delay(3000) // Change page every 3 seconds
                pagerState.animateScrollToPage(
                    page = (pagerState.currentPage + 1) % pagerState.pageCount
                )
            }
        }
    }

    // Function to set the current page
    fun setCurrentPage(page: Int) {
        _currentPage.value = page
    }
}
