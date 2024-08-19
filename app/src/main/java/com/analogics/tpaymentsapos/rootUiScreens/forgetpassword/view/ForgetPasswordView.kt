package com.analogics.tpaymentsapos.rootUiScreens.rootScreen.component

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
import androidx.navigation.NavHostController
import com.analogics.tpaymentsapos.R
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.BackgroundScreen
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.CommonTopAppBar
import com.analogics.tpaymentsapos.ui.theme.dimens

@Composable
fun ForgetPasswordView(navHostController: NavHostController) {
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
                        text = stringResource(id = R.string.password_),
                        fontSize = MaterialTheme.dimens.SP_23_CompactMedium,
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .padding(bottom = MaterialTheme.dimens.DP_20_CompactMedium)
                            .align(Alignment.CenterHorizontally) // Center the header text
                    )
                    Spacer(modifier = Modifier.height(MaterialTheme.dimens.DP_20_CompactMedium)) // Blank space added here

                    Image(
                        painter = painterResource(id = R.drawable.unlock), // Replace with your image resource
                        contentDescription = null, // Decorative image
                        modifier = Modifier
                            .size(MaterialTheme.dimens.DP_70_CompactMedium)
                            .padding(bottom = MaterialTheme.dimens.DP_24_CompactMedium)
                            .align(Alignment.CenterHorizontally) // Center the image
                    )
                    Spacer(modifier = Modifier.height(MaterialTheme.dimens.DP_10_CompactMedium)) // Blank space added here

                    Text(
                        text = stringResource(id = R.string.call_customercare),
                        fontSize = MaterialTheme.dimens.SP_17_CompactMedium,
                        color = Color.LightGray,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .padding(bottom = MaterialTheme.dimens.DP_20_CompactMedium)
                            .align(Alignment.CenterHorizontally) // Center the subheader text
                    )

                    Spacer(modifier = Modifier.height(MaterialTheme.dimens.DP_60_CompactMedium)) // Blank space added here

                    // Aligning the following text to the start (left) of the inner Surface
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = MaterialTheme.dimens.DP_20_CompactMedium)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.user), // Replace with your icon resource
                            contentDescription = "MID Icon",
                            modifier = Modifier.size(MaterialTheme.dimens.DP_24_CompactMedium)
                        )
                        Spacer(modifier = Modifier.width(MaterialTheme.dimens.DP_20_CompactMedium)) // Space between icon and text
                        Text(
                            text = stringResource(id = R.string.mid),
                            fontSize = MaterialTheme.dimens.SP_17_CompactMedium,
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Aligning the following text to the start (left) of the inner Surface
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = MaterialTheme.dimens.DP_20_CompactMedium)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.terminal), // Replace with your icon resource
                            contentDescription = "MID Icon",
                            modifier = Modifier.size(MaterialTheme.dimens.DP_24_CompactMedium)
                        )
                        Spacer(modifier = Modifier.width(MaterialTheme.dimens.DP_20_CompactMedium)) // Space between icon and text
                        Text(
                            text = stringResource(id = R.string.tid),
                            fontSize = MaterialTheme.dimens.SP_17_CompactMedium,
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Aligning the following text to the start (left) of the inner Surface
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = MaterialTheme.dimens.DP_20_CompactMedium)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.list), // Replace with your icon resource
                            contentDescription = "MID Icon",
                            modifier = Modifier.size(MaterialTheme.dimens.DP_24_CompactMedium)
                        )
                        Spacer(modifier = Modifier.width(MaterialTheme.dimens.DP_20_CompactMedium)) // Space between icon and text
                        Text(
                            text = stringResource(id = R.string.sr_no),
                            fontSize = MaterialTheme.dimens.SP_17_CompactMedium,
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Aligning the following text to the start (left) of the inner Surface
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = MaterialTheme.dimens.DP_20_CompactMedium)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.callcenter), // Replace with your icon resource
                            contentDescription = "MID Icon",
                            modifier = Modifier.size(MaterialTheme.dimens.DP_24_CompactMedium)
                        )
                        Spacer(modifier = Modifier.width(MaterialTheme.dimens.DP_20_CompactMedium)) // Space between icon and text
                        Text(
                            text = stringResource(id = R.string.call_center),
                            fontSize = MaterialTheme.dimens.SP_17_CompactMedium,
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            
        }
    }
}
