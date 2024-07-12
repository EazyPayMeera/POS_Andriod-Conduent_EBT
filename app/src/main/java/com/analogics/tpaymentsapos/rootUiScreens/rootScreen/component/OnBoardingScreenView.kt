package com.analogics.tpaymentsapos.rootUiScreens.rootScreen.component


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column


import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults

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
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import androidx.navigation.NavHostController
import com.analogics.tpaymentsapos.R
import com.analogics.tpaymentsapos.model.OnBoardingContentList

import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.AppButton

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
    val imageSlider = listOf<OnBoardingContentList>(
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
                page = (pagerState.currentPage + 1) % (pagerState.pageCount)
            )
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color("#D9D9D9".toColorInt()))
    )
    {
        Column {
            HorizontalPager(
                count = imageSlider.size,
                state = pagerState,
                contentPadding = PaddingValues(horizontal = 2.dp),
                modifier = Modifier
            ) { page ->

                Box(
                    contentAlignment = Alignment.Center, modifier = Modifier
                        .padding(12.dp)
                        .fillMaxSize()
                ) {
                    ShowCardView(pagerState, page, imageSlider,navHostController)
                    /* HorizontalPagerIndicator(
                         pagerState = pagerState,
                         modifier = Modifier

                     )*/
                }

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
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = Color.White, //Card background color
            contentColor = Color.Black  //Card content color,e.g.text
        ),
        modifier = Modifier.fillMaxHeight()
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth()
        )
        {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                Image(
                    painter = painterResource(id = imageSlider[pageIndex].image),
                    contentDescription = "group 360",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .height(180.dp)
                        .width(230.dp)
                )
                Box(
                    contentAlignment = Alignment.BottomStart,
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxSize()
                        .padding(start = 40.dp)

                ) {
                    Image(
                        painter = painterResource(id = R.drawable.men_frame),
                        contentDescription = "group 360",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(60.dp, 120.dp)

                    )
                }

            }


            Box(
                contentAlignment = Alignment.BottomCenter,
                modifier = Modifier.fillMaxHeight()
            )
            {
                Column(
                    modifier = Modifier
                        .wrapContentSize()
                        .padding(15.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                )
                {

                    Box(
                        contentAlignment = Alignment.TopStart,
                        modifier = Modifier
                            .fillMaxWidth()

                    ) {
                        Column(
                            modifier = Modifier
                                .wrapContentSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        )
                        {
                            Text(
                                text = imageSlider[pageIndex].headNote,
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.Black,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(bottom = 20.dp, top = 20.dp)
                            )
                            Text(
                                text = imageSlider[pageIndex].subNote,
                                style = MaterialTheme.typography.titleMedium,
                                color = Color("#B3B3B3".toColorInt()),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(bottom = 60.dp)
                            )
                        }
                    }
                    if (imageSlider[pageIndex].isIndicatorShow) {
                    Text(
                        text = "Skip",
                        color = Color("#B3B3B3".toColorInt()),
                        modifier = Modifier
                            .wrapContentSize()
                            .align(Alignment.CenterHorizontally)
                            .padding(15.dp)
                    )

                        HorizontalPagerIndicator(
                            pagerState = pagerState,
                            modifier = Modifier.padding(bottom = 20.dp)

                        )
                    } else {

                        AppButton(onClick = {
                            navHostController.navigate(AppNavigationItems.LoginScreen.route)
                        }, title = "Get Started")
                    }

                }
            }
        }


    }
}