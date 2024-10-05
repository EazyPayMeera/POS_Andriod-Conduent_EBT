package com.analogics.tpaymentsapos.rootUiScreens.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.analogics.tpaymentsapos.R
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import com.analogics.tpaymentsapos.rootUiScreens.activity.localSharedViewModel
import com.analogics.tpaymentsapos.rootUiScreens.confirmshift.viewmodel.ConfirmShiftViewModel
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.AppButton
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.BackgroundScreen
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.CommonTopAppBar
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.FooterButtons
import com.analogics.tpaymentsapos.ui.theme.dimens

@Composable
fun ConfirmShiftView(navHostController: NavHostController) {
    val viewModel: ConfirmShiftViewModel = hiltViewModel()
    val sharedViewModel = localSharedViewModel.current

    Column {
        CommonTopAppBar(
            title = stringResource(id = R.string.end_shift_title),
            onBackButtonClick = { navHostController.popBackStack() }
        )

        BackgroundScreen {
            Column(
                modifier = Modifier
                    .padding(MaterialTheme.dimens.DP_24_CompactMedium)
                    .fillMaxHeight(), // Fill the entire available space
                verticalArrangement = Arrangement.SpaceBetween, // Space between the header and footer
                horizontalAlignment = Alignment.CenterHorizontally // Center content horizontally
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Spacer(modifier = Modifier.height(MaterialTheme.dimens.DP_20_CompactMedium))

                    Text(
                        text = stringResource(id = R.string.confirm_btn),
                        fontSize = MaterialTheme.dimens.SP_28_CompactMedium,
                        color = MaterialTheme.colorScheme.tertiary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = MaterialTheme.dimens.DP_33_CompactMedium)
                    )

                    Image(
                        painter = painterResource(id = R.drawable.logout),
                        contentDescription = null,
                        modifier = Modifier
                            .size(MaterialTheme.dimens.DP_70_CompactMedium)
                            .padding(bottom = MaterialTheme.dimens.DP_21_CompactMedium)
                    )

                    Text(
                        text = stringResource(id = R.string.end_shift_message),
                        fontSize = MaterialTheme.dimens.SP_22_CompactMedium,
                        color = MaterialTheme.colorScheme.onSecondary,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Footer buttons with enough space to show the rounded button below
                FooterButtons(
                    firstButtonTitle = stringResource(id = R.string.cancel),
                    firstButtonOnClick = { navHostController.navigate(AppNavigationItems.TrainingScreen.route) },
                    secondButtonTitle = stringResource(id = R.string.yes),
                    secondButtonOnClick = { viewModel.onShiftEnd(navHostController, sharedViewModel) },
                    alignment = Alignment.TopCenter
                )
            }


            Row(
                modifier = Modifier
                    //.fillMaxWidth()
                    .height(MaterialTheme.dimens.DP_40_CompactMedium),
                    //.padding(top = MaterialTheme.dimens.DP_145_CompactMedium),
                horizontalArrangement = Arrangement.Absolute.Center,
                verticalAlignment = Alignment.Bottom
            ) {
                AppButton(
                    onClick = {  },
                    title = stringResource(id = R.string.print_last_receipt),
                    image = painterResource(id = R.drawable.ic_print),
                )
            }
        }
    }
}


