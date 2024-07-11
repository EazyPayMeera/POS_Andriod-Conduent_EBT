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
import androidx.compose.foundation.magnifier
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
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import com.analogics.tpaymentsapos.R
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.HorizontalPagerIndicator
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.delay
import kotlinx.coroutines.yield


@OptIn(ExperimentalPagerApi::class)
@Composable
fun OnBoardSlideView() {
    val pagerState = rememberPagerState(initialPage = 0)
    val imageSlider = listOf(
        painterResource(id = R.drawable.group_360),
        painterResource(id = R.drawable.group_360),
        painterResource(id = R.drawable.group_360)
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
Box(modifier = Modifier
    .fillMaxSize()
    .background(Color("#D9D9D9".toColorInt())) )
{
    Column {
        HorizontalPager(
            count = imageSlider.size,
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 2.dp),
            modifier = Modifier
        ) { page ->

            Box( contentAlignment = Alignment.Center, modifier = Modifier
                .padding(12.dp)
                .fillMaxSize()) {
                    ShowCardView(pagerState,imageSlider[page])
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
fun ShowCardView(pagerState: PagerState, imageSlider: Painter)
{
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
                    modifier = Modifier.fillMaxHeight().fillMaxWidth()
                )
                {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    ) {
                        Image(
                            painter = imageSlider,
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
                                    .size(60.dp,120.dp)

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
                            Text(
                                text = "Single use password that won’t work twice, keeping your details safe even if they get exposed",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color("#B3B3B3".toColorInt())
                            )
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
                                modifier = Modifier

                            )

                        }
                    }
                }


        }
}