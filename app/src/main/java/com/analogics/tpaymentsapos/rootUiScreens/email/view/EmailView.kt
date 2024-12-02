package com.analogics.tpaymentsapos.rootUiScreens.email.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavHostController
import com.analogics.tpaymentsapos.R
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import com.analogics.tpaymentsapos.rootUiScreens.enteremail.viewmodel.globalVariable
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.BackgroundScreen
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.CommonTopAppBar
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.ImageView
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.OkButton
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.TextView
import com.analogics.tpaymentsapos.ui.theme.dimens

@Composable
fun EmailView(navHostController: NavHostController, email: String) {

    val updated_email = globalVariable
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
                horizontalAlignment = Alignment.CenterHorizontally // Align content to the start
            ) {
                Spacer(modifier = Modifier.height(MaterialTheme.dimens.DP_40_CompactMedium)) // Blank space added here

                TextView(
                    text = stringResource(id = R.string.sucess),
                    fontSize = MaterialTheme.dimens.SP_31_CompactMedium,
                    color = MaterialTheme.colorScheme.tertiary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(bottom = MaterialTheme.dimens.DP_20_CompactMedium)
                        .align(Alignment.CenterHorizontally) // Center the subheader text
                )

                Spacer(modifier = Modifier.height(MaterialTheme.dimens.DP_20_CompactMedium)) // Blank space added here

                TextView(
                    text = stringResource(id = R.string.sent_email),
                    fontSize = MaterialTheme.dimens.SP_44_CompactMedium,
                    color = MaterialTheme.colorScheme.tertiary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(bottom = MaterialTheme.dimens.DP_20_CompactMedium)
                        .align(Alignment.CenterHorizontally) // Center the subheader text
                )

                Spacer(modifier = Modifier.height(MaterialTheme.dimens.DP_33_CompactMedium)) // Blank space added here

                ImageView(
                    imageId = R.drawable.approve,
                    size = MaterialTheme.dimens.DP_125_CompactMedium,
                    alignment = Alignment.Center,
                    modifier = Modifier
                        .padding(bottom = MaterialTheme.dimens.DP_21_CompactMedium)
                        .align(Alignment.CenterHorizontally),
                    contentDescription = ""
                )

                TextView(
                    text = stringResource(id = R.string.ereceipt_sent),
                    fontSize = MaterialTheme.dimens.SP_18_CompactMedium,
                    color = MaterialTheme.colorScheme.onSecondary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(bottom = MaterialTheme.dimens.DP_20_CompactMedium)
                        .align(Alignment.CenterHorizontally) // Center the subheader text
                )

                TextView(
                    text = "on $updated_email",
                    fontSize = MaterialTheme.dimens.SP_18_CompactMedium,
                    color = MaterialTheme.colorScheme.onSecondary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(bottom = MaterialTheme.dimens.DP_20_CompactMedium)
                        .align(Alignment.CenterHorizontally) // Center the subheader text
                )


                Box(
                    modifier = Modifier.padding(top = MaterialTheme.dimens.DP_70_CompactMedium)
                ) {
                    OkButton(
                        onClick = {
                            navHostController.navigate(AppNavigationItems.SucessScreen.route)
                        },
                        title = stringResource(id = R.string.ok)
                    )
                }
            }

        }
    }
}
