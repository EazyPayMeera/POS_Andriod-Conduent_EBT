package com.eazypaytech.posafrica.core.ui.components.hostprocess

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.eazypaytech.posafrica.R
import com.eazypaytech.posafrica.core.ui.components.inputfields.DialogueSurface
import com.eazypaytech.posafrica.core.ui.components.textview.TextView
import com.eazypaytech.posafrica.features.login.ui.LoginViewModel
import com.eazypaytech.posafrica.core.themes.dimens


@Composable
fun HostProcessingDialog(
    navHostController: NavHostController?,
    viewModel: LoginViewModel = hiltViewModel(),
    onDismissRequest: () -> Unit
) {
    // Intercept the hardware back press
    BackHandler(onBack = {})

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = MaterialTheme.dimens.DP_57_CompactMedium)
            .background(MaterialTheme.colorScheme.background) // Optional background
            .clickable { onDismissRequest() }, // Dismiss on outside click if desired
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.fillMaxSize()  // Ensures full-screen content without padding
        ) {
            HostProcessingContent(navHostController)
        }
    }
}

@Composable
fun HostProcessingContent(navHostController: NavHostController?) {

    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(MaterialTheme.dimens.DP_20_CompactMedium)
    ) {

        DialogueSurface() {
            Column(
                modifier = Modifier
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start
            ) {
                Spacer(modifier = Modifier.height(MaterialTheme.dimens.DP_100_CompactMedium))

                TextView(
                    text = stringResource(id = R.string.processing),
                    fontSize = MaterialTheme.dimens.SP_27_CompactMedium,
                    color = MaterialTheme.colorScheme.tertiary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(
                            bottom = MaterialTheme.dimens.DP_20_CompactMedium,
                            top = MaterialTheme.dimens.DP_21_CompactMedium
                        )
                        .align(Alignment.CenterHorizontally)
                )
                //Spacer(modifier = Modifier.height(MaterialTheme.dimens.DP_30_CompactMedium))

                TextView(
                    text = stringResource(id = R.string.plz_wait),
                    fontSize = MaterialTheme.dimens.SP_29_CompactMedium,
                    color = MaterialTheme.colorScheme.tertiary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(bottom = MaterialTheme.dimens.DP_20_CompactMedium/*, top = MaterialTheme.dimens.DP_24_CompactMedium*/)
                        .align(Alignment.CenterHorizontally)
                )

                CircularProgressIndicator(
                    modifier = Modifier
                        .padding(top = MaterialTheme.dimens.DP_55_CompactMedium)
                        .size(MaterialTheme.dimens.DP_120_CompactMedium)
                        .align(Alignment.CenterHorizontally),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = MaterialTheme.dimens.DP_4_CompactMedium,
                )
            }
        }

    }
}
