package com.eazypaytech.pos.features.confirmshift.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import com.eazypaytech.pos.R
import com.eazypaytech.pos.navigation.AppNavigationItems
import com.eazypaytech.pos.features.activity.ui.localSharedViewModel
import com.eazypaytech.pos.core.ui.components.inputfields.AppButton
import com.eazypaytech.pos.core.ui.components.inputfields.BackgroundScreen
import com.eazypaytech.pos.core.ui.components.inputfields.CommonTopAppBar
import com.eazypaytech.pos.core.ui.components.inputfields.FooterButtons
import com.eazypaytech.pos.core.utils.navigateAndClean
import com.eazypaytech.pos.core.themes.dimens

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ConfirmShiftView(navHostController: NavHostController) {
    val viewModel: ConfirmShiftViewModel = hiltViewModel()
    val sharedViewModel = localSharedViewModel.current
    Column {
        CommonTopAppBar(
            title = stringResource(id = R.string.end_shift_title),
            onBackButtonClick = { navHostController.popBackStack() }
        )

        BackgroundScreen(
//            color = Color(0xFFF7931E), // Orange color for the outer Surface
//            modifier = Modifier
//                .padding(MaterialTheme.dimens.DP_25_CompactMedium) // Padding for the outer Surface
//                .height(MaterialTheme.dimens.DP_540_CompactMedium) // Adjust the height as per your requirement
//                .width(MaterialTheme.dimens.DP_410_CompactMedium), // Adjust the width as per your requirement
//            shape = RoundedCornerShape(MaterialTheme.dimens.DP_18_CompactMedium) // Rounded corners for the outer Surface
        ) {

            Column(
                modifier = Modifier
                    .padding(top = MaterialTheme.dimens.DP_24_CompactMedium,
                        start = MaterialTheme.dimens.DP_0_CompactMedium,
                        end = MaterialTheme.dimens.DP_0_CompactMedium) // Padding for the content inside the inner Surface
                    .fillMaxSize(), // Fill the entire available space
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start // Align content to the start
            ) {
                Spacer(modifier = Modifier.height(MaterialTheme.dimens.DP_20_CompactMedium)) // Blank space added here
                Text(
                    text = stringResource(id = R.string.confirm_btn),
                    fontSize = MaterialTheme.dimens.SP_31_CompactMedium,
                    color = MaterialTheme.colorScheme.tertiary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(bottom = MaterialTheme.dimens.DP_33_CompactMedium)
                        .align(Alignment.CenterHorizontally) // Center the header text
                )

                Image(
                    painter = painterResource(id = R.drawable.logout), // Replace with your image resource
                    contentDescription = null, // Decorative image
                    modifier = Modifier
                        .size(MaterialTheme.dimens.DP_100_CompactMedium)
                        .padding(bottom = MaterialTheme.dimens.DP_33_CompactMedium)
                        .align(Alignment.CenterHorizontally) // Center the image
                )

                Text(
                    text = stringResource(id = R.string.end_shift_message_1),
                    fontSize = MaterialTheme.dimens.SP_22_CompactMedium,
                    color = MaterialTheme.colorScheme.onSecondary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        //.padding(bottom = MaterialTheme.dimens.DP_20_CompactMedium)
                        .align(Alignment.CenterHorizontally) // Center the subheader text
                )

                Text(
                    text = stringResource(id = R.string.end_shift_message_2),
                    fontSize = MaterialTheme.dimens.SP_22_CompactMedium,
                    color = MaterialTheme.colorScheme.onSecondary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(bottom = MaterialTheme.dimens.DP_180_CompactMedium)
                        .align(Alignment.CenterHorizontally) // Center the subheader text
                )


                FooterButtons(
                    firstButtonTitle = stringResource(id = R.string.cancel),
                    firstButtonOnClick = { viewModel.onCancel(navHostController) },
                    secondButtonTitle = stringResource(id = R.string.yes),
                    secondButtonOnClick = { viewModel.onShiftEnd(navHostController,sharedViewModel) },
                    alignment = Alignment.TopCenter
                )

                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.Bottom
                ) {
                    AppButton(
                        onClick = {navHostController.navigateAndClean(AppNavigationItems.DashBoardScreen.route)  },
                        title = stringResource(id = R.string.print_last_receipt),
                        image = painterResource(id = R.drawable.ic_print)
                    )
                }

            }

        }
    }
}

