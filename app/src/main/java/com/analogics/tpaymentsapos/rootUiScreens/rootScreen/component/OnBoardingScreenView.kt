package com.analogics.tpaymentsapos.rootUiScreens.rootScreen.component

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import androidx.navigation.NavHostController
import com.analogics.tpaymentsapos.R
import com.analogics.tpaymentsapos.model.OnBoardingContentList
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.AppButton
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
fun OnBoardSlideView(navHostController: NavHostController) {
    val pagerState = rememberPagerState(initialPage = 0)
    val imageSlider = listOf(
        OnBoardingContentList(
            headNote = "Safe and fast transaction",
            subNote = "Single use password that won’t work twice, keeping your details safe even if they get exposed"
        ),
        OnBoardingContentList(
            headNote = "Analysis of data",
            subNote = "Improving customer retention through data analytics, there are several takeaways to keep in mind"
        ),
        OnBoardingContentList(
            headNote = "Quick and easy payments",
            subNote = "Creating a seamless payment experience is crucial for user satisfaction and conversion",
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
        // HorizontalPager
        HorizontalPager(
            count = imageSlider.size,
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            ShowCardView(pagerState, page, imageSlider, navHostController)
        }

        // Static HorizontalPagerIndicator
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = MaterialTheme.dimens.DP_20_CompactMedium)
        ) {
            HorizontalPagerIndicator(
                pagerState = pagerState,
                modifier = Modifier
                    .padding(horizontal = MaterialTheme.dimens.DP_20_CompactMedium)
            )
        }

        // "Get Started" button only on the third page
        if (pagerState.currentPage == 2) {

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = MaterialTheme.dimens.DP_60_CompactMedium) // Adjust the padding as needed
            ) {
                AppButton(
                    onClick = {
                        navHostController.navigate(AppNavigationItems.LoginScreen.route)
                    },
                    title = stringResource(R.string.get_start)
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
    Card(
        shape = RoundedCornerShape(MaterialTheme.dimens.DP_20_CompactMedium),
        elevation = CardDefaults.cardElevation(
            defaultElevation = MaterialTheme.dimens.extraSmall
        ),
        colors = CardDefaults.cardColors(
            containerColor = Color.White, // Card background color
            contentColor = Color.Black  // Card content color, e.g., text
        ),
        modifier = Modifier.fillMaxHeight()
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
                Box(
                    contentAlignment = Alignment.BottomStart,
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxSize()
                        .padding(start = MaterialTheme.dimens.DP_40_CompactMedium)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.men_frame),
                        contentDescription = "group 360",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(MaterialTheme.dimens.DP_60_CompactMedium, MaterialTheme.dimens.DP_120_CompactMedium)
                    )
                }
            }

            // Text section
            Column(
                modifier = Modifier
                    .wrapContentSize()
                    .padding(MaterialTheme.dimens.DP_15_CompactMedium),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = imageSlider[pageIndex].headNote,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(
                        bottom = MaterialTheme.dimens.DP_20_CompactMedium,
                        top = MaterialTheme.dimens.DP_20_CompactMedium
                    ),
                )
                Text(
                    text = imageSlider[pageIndex].subNote,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color("#B3B3B3".toColorInt()),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = MaterialTheme.dimens.DP_60_CompactMedium)
                )
            }
        }
    }
}