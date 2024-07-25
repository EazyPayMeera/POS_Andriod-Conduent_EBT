

package com.analogics.tpaymentsapos.rootUiScreens.rootScreen.component

import android.view.animation.OvershootInterpolator
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.analogics.tpaymentsapos.R
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import kotlinx.coroutines.delay


@Composable
fun SplashScreenView(navController: NavController)
{
    val scale = remember {
        androidx.compose.animation.core.Animatable(0f)
    }
    LaunchedEffect(key1 = true) {
        scale.animateTo(
            targetValue = 10f,
            animationSpec = tween(
                durationMillis = 100,
                easing = {
                    OvershootInterpolator(10f).getInterpolation(it)
                })
        )
        delay(3000L)
        navController.navigate(AppNavigationItems.OnBoardingScreen.route)

    }
    Box(contentAlignment = Alignment.BottomStart, modifier =
    Modifier.fillMaxSize()
        .background(MaterialTheme.colorScheme.background)) {
        Image(painter = painterResource(R.drawable.splash_background),
            contentDescription = "TPaymentApp",
            modifier = Modifier.scale(scale.value))
//        Image(
//            painter = painterResource(R.drawable.image1),
//            contentDescription = "Food APP",
//            modifier = Modifier.size(300.dp,250.dp),
//            Alignment.BottomStart
//        )

        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize())
        {
            Text(
                text = "T Payment",
                modifier = Modifier.wrapContentSize(),
                color = Color.White,
                fontSize = 50.sp,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}