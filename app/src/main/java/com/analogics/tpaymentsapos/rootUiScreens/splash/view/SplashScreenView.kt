

package com.analogics.tpaymentsapos.rootUiScreens.splash.view


import android.view.animation.OvershootInterpolator
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.analogics.paymentservicecore.logger.AppLogger
import com.analogics.tpaymentsapos.BuildConfig
import com.analogics.tpaymentsapos.R
import com.analogics.tpaymentsapos.rootUiScreens.activity.localSharedViewModel
import com.analogics.tpaymentsapos.rootUiScreens.splash.viewModel.SplashScreenViewModel
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.GenericCard
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.ImageView
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.TextView
import com.analogics.tpaymentsapos.ui.theme.Roboto
import com.analogics.tpaymentsapos.ui.theme.dashboardOrangeColor
import com.analogics.tpaymentsapos.ui.theme.dimens

@Composable
fun SplashScreenView(navController: NavController, viewModel: SplashScreenViewModel = hiltViewModel()) {
/*
*
* AppLogger.d(TAG, "onCreate: Activity started")
   AppLogger.i(TAG, "onCreate: Initializing UI components")
        AppLogger.w(TAG, "onCreate: Potential configuration issue detected")
        AppLogger.e(TAG, "onCreate: Error initializing app", Throwable("Simulated error"))
*
* */
    AppLogger.setLogLevel(BuildConfig.LOG_LEVEL)
    val scale = remember { Animatable(0f) }
    var sharedViewModel = localSharedViewModel.current

    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = 10f,
            animationSpec = tween(
                durationMillis = 100,
                easing = {
                    OvershootInterpolator(10f).getInterpolation(it)
                })
        )
        viewModel.onSplashScreenFinished(navController, sharedViewModel)
    }
    GenericCard(
        Modifier
            .fillMaxSize()
            .padding(MaterialTheme.dimens.DP_15_CompactMedium)) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TextView(
                text = stringResource(id = R.string.welcome),
                fontSize = MaterialTheme.dimens.SP_30_CompactMedium,
                fontFamily = Roboto,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(MaterialTheme.dimens.DP_40_CompactMedium))

            ImageView(
                imageId= R.drawable.ic_launcher_foreground,
                contentDescription = "",
                modifier = Modifier.size(MaterialTheme.dimens.DP_70_CompactMedium)
            )

            Spacer(modifier = Modifier.height(MaterialTheme.dimens.DP_40_CompactMedium))

            TextView(
                text = stringResource(id = R.string.application_name),
                fontSize = MaterialTheme.dimens.SP_44_CompactMedium,
                color = dashboardOrangeColor
            )
        }
    }
}


