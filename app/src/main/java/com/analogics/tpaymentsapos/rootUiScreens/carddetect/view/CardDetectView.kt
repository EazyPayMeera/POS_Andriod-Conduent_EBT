package com.analogics.tpaymentsapos.rootUiScreens.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavHostController
import com.analogics.tpaymentsapos.R
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.Authorisation
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.CommonTopAppBar
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.TransactionState
import com.analogics.tpaymentsapos.ui.theme.dimens
import androidx.lifecycle.viewmodel.compose.viewModel
import com.analogics.tpaymentsapos.rootUiScreens.carddetect.viewmodel.CardDetectViewModel

@Composable
fun CardDetectView(navHostController: NavHostController, totalAmount: String) {
    // Use the ViewModel
    val viewModel: CardDetectViewModel = viewModel()

    // State for transaction status
    val isRefund = TransactionState.isRefund
    val isVoid = TransactionState.isVoid
    val isPreauth = TransactionState.isPreauth
    val isAuthcap = Authorisation.isAuthcap

    // Use LaunchedEffect to handle the delay and navigation
    LaunchedEffect(Unit) {
        viewModel.navigateAfterDelay(navHostController)
    }

    Column {
        CommonTopAppBar(
            title = when {
                isRefund -> stringResource(R.string.refund)
                isVoid -> stringResource(R.string.void_trans)
                isPreauth -> stringResource(R.string.pre_auth)
                else -> stringResource(R.string.purchase)
            },
            onBackButtonClick = { navHostController.popBackStack() }
        )

        Surface(
            color = Color.White,
            modifier = Modifier
                .padding(MaterialTheme.dimens.DP_24_CompactMedium)
                .fillMaxWidth()
                .height(MaterialTheme.dimens.DP_500_CompactMedium)
                .width(MaterialTheme.dimens.DP_430_CompactMedium),
            shape = RoundedCornerShape(MaterialTheme.dimens.DP_18_CompactMedium),
            elevation = MaterialTheme.dimens.DP_20_CompactMedium
        ) {
            Column(
                modifier = Modifier
                    .padding(MaterialTheme.dimens.extraSmall)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Inner Surface with orange color
                Surface(
                    color = colorResource(id = R.color.Orange), // Orange color
                    modifier = Modifier
                        .padding(MaterialTheme.dimens.extraSmall)
                        .width(MaterialTheme.dimens.DP_450_CompactMedium)
                        .height(MaterialTheme.dimens.DP_120_CompactMedium), // Adjust size as needed
                    shape = RoundedCornerShape(MaterialTheme.dimens.DP_18_CompactMedium),
                    elevation = MaterialTheme.dimens.DP_20_CompactMedium
                ) {
                    // Content inside the inner Surface
                    Column(
                        modifier = Modifier
                            .padding(MaterialTheme.dimens.DP_24_CompactMedium)
                            .fillMaxSize(),
                        verticalArrangement = Arrangement.Top,
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = if (isRefund) stringResource(id = R.string.refund_amt) else stringResource(id = R.string.total_amt),
                            fontSize = MaterialTheme.dimens.SP_23_CompactMedium,
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .padding(bottom = MaterialTheme.dimens.DP_11_CompactMedium) // Bottom padding
                        )
                        // Display the totalAmount here
                        Text(
                            text = "₹$totalAmount",
                            fontSize = MaterialTheme.dimens.SP_27_CompactMedium,
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(MaterialTheme.dimens.DP_11_CompactMedium)) // Space between surfaces

                Image(
                    painter = painterResource(id = R.drawable.swip_card), // Replace with your image resource
                    contentDescription = null, // Decorative image
                    modifier = Modifier
                        .size(MaterialTheme.dimens.DP_33_CompactMedium) // Set the size of the image
                        .padding(bottom = MaterialTheme.dimens.DP_24_CompactMedium) // Adds bottom padding to the image
                )

                Spacer(modifier = Modifier.height(MaterialTheme.dimens.DP_5_CompactMedium))

                Text(
                    text = stringResource(id = R.string.tap_swipe_insert),
                    fontSize = MaterialTheme.dimens.SP_23_CompactMedium,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(bottom = MaterialTheme.dimens.DP_21_CompactMedium)
                        .align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(MaterialTheme.dimens.DP_5_CompactMedium))

                // Row to hold images horizontally
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = MaterialTheme.dimens.DP_24_CompactMedium),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.master), // Replace with your image resource
                        contentDescription = null,
                        modifier = Modifier.size(MaterialTheme.dimens.DP_50_CompactMedium) // Adjust size as needed
                    )
                    Image(
                        painter = painterResource(id = R.drawable.visa), // Replace with your image resource
                        contentDescription = null,
                        modifier = Modifier.size(MaterialTheme.dimens.DP_50_CompactMedium) // Adjust size as needed
                    )
                    Image(
                        painter = painterResource(id = R.drawable.rupay), // Replace with your image resource
                        contentDescription = null,
                        modifier = Modifier.size(MaterialTheme.dimens.DP_50_CompactMedium) // Adjust size as needed
                    )
                }

                Spacer(modifier = Modifier.height(MaterialTheme.dimens.DP_5_CompactMedium))

                Text(
                    text = stringResource(id = R.string.chip_detected),
                    fontSize = MaterialTheme.dimens.SP_23_CompactMedium,
                    color = Color(0xFFF7931E),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(bottom = MaterialTheme.dimens.DP_30_CompactMedium)
                        .align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(MaterialTheme.dimens.DP_11_CompactMedium)) // Space before the CANCEL button

                // CANCEL Button
                Button(
                    onClick = { navHostController.navigate(AppNavigationItems.TrainingScreen.route) },
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color.White), // Red button color for emphasis
                    shape = RoundedCornerShape(MaterialTheme.dimens.DP_18_CompactMedium), // Adjust the shape as needed
                    modifier = Modifier
                        .width(MaterialTheme.dimens.DP_200_CompactMedium) // Set the fixed width here
                        .height(MaterialTheme.dimens.DP_50_CompactMedium)
                        .padding(horizontal = MaterialTheme.dimens.DP_24_CompactMedium) // Optional padding for spacing
                ) {
                    Text(
                        text = stringResource(id = R.string.cancel_btn),
                        fontSize = MaterialTheme.dimens.SP_18_CompactMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black // Text color for contrast
                    )
                }
            }
        }
    }
}
