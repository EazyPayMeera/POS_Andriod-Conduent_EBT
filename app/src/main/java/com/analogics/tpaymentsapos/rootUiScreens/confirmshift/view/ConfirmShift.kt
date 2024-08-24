package com.analogics.tpaymentsapos.rootUiScreens.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavHostController
import com.analogics.tpaymentsapos.R
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.AppButton
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.BackgroundScreen
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.CommonLayout
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.CommonTopAppBar
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.FooterButtons
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.HeaderImage
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.TopBoldText
import com.analogics.tpaymentsapos.ui.theme.dimens

@Composable
fun ConfirmShiftView(navHostController: NavHostController) {
    Column {
        CommonTopAppBar(
            title = stringResource(id = R.string.forget_pswd),
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
                    .padding(MaterialTheme.dimens.DP_24_CompactMedium) // Padding for the content inside the inner Surface
                    .fillMaxSize(), // Fill the entire available space
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start // Align content to the start
            ) {
                Spacer(modifier = Modifier.height(MaterialTheme.dimens.DP_20_CompactMedium)) // Blank space added here
                Text(
                    text = stringResource(id = R.string.confirm_btn),
                    fontSize = MaterialTheme.dimens.SP_23_CompactMedium,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(bottom = MaterialTheme.dimens.DP_20_CompactMedium)
                        .align(Alignment.CenterHorizontally) // Center the header text
                )
                Spacer(modifier = Modifier.height(MaterialTheme.dimens.DP_20_CompactMedium)) // Blank space added here

                Image(
                    painter = painterResource(id = R.drawable.logout), // Replace with your image resource
                    contentDescription = null, // Decorative image
                    modifier = Modifier
                        .size(MaterialTheme.dimens.DP_70_CompactMedium)
                        .padding(bottom = MaterialTheme.dimens.DP_24_CompactMedium)
                        .align(Alignment.CenterHorizontally) // Center the image
                )
                Spacer(modifier = Modifier.height(MaterialTheme.dimens.DP_10_CompactMedium)) // Blank space added here

                Text(
                    text = "Are you sure you want to end your shift?",
                    fontSize = MaterialTheme.dimens.SP_17_CompactMedium,
                    color = Color.LightGray,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        //.padding(bottom = MaterialTheme.dimens.DP_20_CompactMedium)
                        .align(Alignment.CenterHorizontally) // Center the subheader text
                )

                Text(
                    text = "end your shift?",
                    fontSize = MaterialTheme.dimens.SP_17_CompactMedium,
                    color = Color.LightGray,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(bottom = MaterialTheme.dimens.DP_20_CompactMedium)
                        .align(Alignment.CenterHorizontally) // Center the subheader text
                )

                Spacer(modifier = Modifier.height(MaterialTheme.dimens.DP_40_CompactMedium)) // Blank space added here

                FooterButtons(
                    firstButtonTitle = stringResource(id = R.string.cancel),
                    firstButtonOnClick = { navHostController.navigate(AppNavigationItems.TrainingScreen.route) },
                    secondButtonTitle = stringResource(id = R.string.yes),
                    secondButtonOnClick = { navHostController.navigate(AppNavigationItems.TrainingScreen.route) }
                )

                Spacer(modifier = Modifier.height(MaterialTheme.dimens.DP_20_CompactMedium)) // Blank space added here

                Box(
                    modifier = Modifier.padding(top = MaterialTheme.dimens.DP_10_CompactMedium)
                    .align(Alignment.CenterHorizontally), // Aligns the Box itself horizontally within the parent
                contentAlignment = Alignment.Center
                ) {
                    AppButton(
                        onClick = {
                            navHostController.navigate(AppNavigationItems.TrainingScreen.route)
                        },
                        title = stringResource(id = R.string.print_shift_report),
                    )
                }

            }

        }
    }
}

