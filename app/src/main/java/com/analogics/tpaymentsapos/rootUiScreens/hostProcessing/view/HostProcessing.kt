package com.analogics.tpaymentsapos.rootUiScreens.hostProcessing.view

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
import androidx.compose.material.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.analogics.tpaymentsapos.R
import com.analogics.tpaymentsapos.rootUiScreens.activity.localSharedViewModel
import com.analogics.tpaymentsapos.rootUiScreens.login.viewModel.LoginViewModel
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.BackgroundScreen
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.TextView
import com.analogics.tpaymentsapos.ui.theme.dimens

/*@Composable
fun HostProcessingDialog(
    navHostController: NavHostController?,
    viewModel: LoginViewModel = hiltViewModel(),
    onDismissRequest: () -> Unit
) {
    Dialog(onDismissRequest = { onDismissRequest() }) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.padding(MaterialTheme.dimens.DP_0_CompactMedium)
                .fillMaxSize()
        ) {
            HostProcessingContent(navHostController, viewModel)
        }
    }
}*/

@Composable
fun HostProcessingDialog(
    navHostController: NavHostController?,
    viewModel: LoginViewModel = hiltViewModel(),
    onDismissRequest: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
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
    val context = LocalContext.current
    val sharedViewModel = localSharedViewModel.current

    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = MaterialTheme.dimens.DP_30_CompactMedium)
            .padding(top = MaterialTheme.dimens.DP_20_CompactMedium)
    ) {

        BackgroundScreen() {
            Column(
                modifier = Modifier
                    .padding(MaterialTheme.dimens.DP_24_CompactMedium)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start
            ) {
                Spacer(modifier = Modifier.height(MaterialTheme.dimens.DP_40_CompactMedium))

                TextView(
                    text = stringResource(id = R.string.processing),
                    fontSize = MaterialTheme.dimens.SP_22_CompactMedium,
                    color = MaterialTheme.colorScheme.tertiary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(bottom = MaterialTheme.dimens.DP_20_CompactMedium)
                        .align(Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.height(MaterialTheme.dimens.DP_30_CompactMedium))

                TextView(
                    text = stringResource(id = R.string.plz_wait),
                    fontSize = MaterialTheme.dimens.SP_27_CompactMedium,
                    color = MaterialTheme.colorScheme.tertiary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(bottom = MaterialTheme.dimens.DP_20_CompactMedium)
                        .align(Alignment.CenterHorizontally)
                )

                if (isMerchantReceipt) {
                    TextView(
                        text = stringResource(id = R.string.merchant_recp),
                        fontSize = MaterialTheme.dimens.SP_15_CompactMedium,
                        color = MaterialTheme.colorScheme.onSecondary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .padding(bottom = MaterialTheme.dimens.DP_20_CompactMedium)
                            .align(Alignment.CenterHorizontally)
                    )
                }

                GifImage(
                    gifResId = R.drawable.wait,
                    modifier = Modifier
                        .size(MaterialTheme.dimens.DP_120_CompactMedium)
                        .padding(bottom = MaterialTheme.dimens.DP_24_CompactMedium)
                        .align(Alignment.CenterHorizontally)
                )
            }
        }

    }
}
