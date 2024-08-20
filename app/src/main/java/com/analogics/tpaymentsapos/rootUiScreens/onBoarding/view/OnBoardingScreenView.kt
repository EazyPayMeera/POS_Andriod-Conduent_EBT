package com.analogics.tpaymentsapos.rootUiScreens.onBoarding.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.core.graphics.toColorInt
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.analogics.tpaymentsapos.R
import com.analogics.tpaymentsapos.rootUiScreens.onBoarding.model.OnBoardingContentList
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import com.analogics.tpaymentsapos.rootUiScreens.onBoarding.viewModel.OnBoardingScreenViewModel
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.AppButton
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.GenericCard
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.TextView
import com.analogics.tpaymentsapos.ui.theme.dimens
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.HorizontalPagerIndicator
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.delay
import kotlinx.coroutines.yield

@OptIn(ExperimentalPagerApi::class)
@Composable
fun OnBoardSlideView(navHostController: NavHostController, viewModel: OnBoardingScreenViewModel = viewModel()) {
    val pagerState = rememberPagerState(initialPage = 0)
    val imageSlider = listOf(
        OnBoardingContentList(
            headNote = stringResource(id = R.string.safe_fast_txn),
            subNote = stringResource(id = R.string.single_use_pass)
        ),
        OnBoardingContentList(
            image = R.drawable.onboarding2,
            headNote = stringResource(id = R.string.analysis_data),
            subNote = stringResource(id = R.string.improving_customer)
        ),
        OnBoardingContentList(
            image = R.drawable.onboarding3,
            headNote = stringResource(id = R.string.quick_and_easy_payments),
            subNote = stringResource(id = R.string.creating_pay),
            isIndicatorShow = false
        )
    )

    LaunchedEffect(Unit) {
        while (true) {
            yield()
            delay(3000)
            pagerState.animateScrollToPage(
                page = (pagerState.currentPage + 1) % pagerState.pageCount
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color("#D9D9D9".toColorInt()))
    ) {
        HorizontalPager(
            count = imageSlider.size,
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            ShowCardView(pagerState, page, imageSlider, navHostController)
        }

        // "Skip" text and HorizontalPagerIndicator
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = MaterialTheme.dimens.DP_120_CompactMedium) // Adjust padding as needed
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TextView(
                    text = stringResource(id = R.string.skip),
                    fontSize = MaterialTheme.dimens.SP_17_CompactMedium,
                    color = Color.Gray,
                    fontWeight = FontWeight.Normal,
                    maxLines = 1,
                    modifier = Modifier.padding(bottom = MaterialTheme.dimens.DP_15_CompactMedium), // Adjust padding
                    textAlign = TextAlign.Center,
                    onClick = { navHostController.navigate(AppNavigationItems.LoginScreen.route) }
                )

                HorizontalPagerIndicator(
                    pagerState = pagerState,
                    modifier = Modifier
                        .padding(horizontal = MaterialTheme.dimens.DP_20_CompactMedium)
                )
            }
        }

        // "Get Started" button only on the third page
        if (pagerState.currentPage == 2) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = MaterialTheme.dimens.DP_20_CompactMedium) // Adjust the padding as needed
            ) {
                AppButton(
                    onClick = {
                        navHostController.navigate(AppNavigationItems.LoginScreen.route)
                    },
                    title = stringResource(R.string.get_started)
                )
            }
        }
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun ShowCardView(
    pagerState: PagerState,
    pageIndex: Int,
    imageSlider: List<OnBoardingContentList>,
    navHostController: NavHostController
) {
    GenericCard(
        shape = RoundedCornerShape(MaterialTheme.dimens.DP_20_CompactMedium),
        elevation = MaterialTheme.dimens.DP_4_CompactMedium,
        backgroundColor = Color.White, // Card background color
        modifier = Modifier
            .fillMaxHeight()
            .padding(MaterialTheme.dimens.DP_15_CompactMedium)
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Image section
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(MaterialTheme.dimens.DP_200_CompactMedium)
            ) {
                Image(
                    painter = painterResource(id = imageSlider[pageIndex].image),
                    contentDescription = "group 360",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .height(MaterialTheme.dimens.DP_180_CompactMedium)
                        .width(MaterialTheme.dimens.DP_200_CompactMedium)
                )
            }

            // Text section
            Column(
                modifier = Modifier
                    .wrapContentSize()
                    .padding(MaterialTheme.dimens.DP_15_CompactMedium),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TextView(
                    text = imageSlider[pageIndex].headNote,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(
                        vertical = MaterialTheme.dimens.DP_20_CompactMedium
                    ),
                    fontSize = MaterialTheme.dimens.SP_16_CompactMedium
                )
                TextView(
                    text = imageSlider[pageIndex].subNote,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = MaterialTheme.dimens.DP_60_CompactMedium),
                    color = Color("#B3B3B3".toColorInt()),
                    textAlign = TextAlign.Center,
                    fontSize = MaterialTheme.dimens.SP_14_CompactMedium
                )
            }
        }
    }
}
