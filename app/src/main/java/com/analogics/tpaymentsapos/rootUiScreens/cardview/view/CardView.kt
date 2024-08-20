// CardView.kt

package com.analogics.tpaymentsapos.rootUiScreens.login

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavHostController
import com.analogics.tpaymentsapos.R
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.Authorisation
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.CommonTopAppBar
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.GenericCard
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.ImageView
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.TextView
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.TransactionState
import com.analogics.tpaymentsapos.ui.theme.dimens


@Composable
fun CardView(navHostController: NavHostController, totalAmount: String) {

    val isRefund = TransactionState.isRefund
    val isVoid = TransactionState.isVoid
    val isPreauth = TransactionState.isPreauth
    val isAuthcap = Authorisation.isAuthcap

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

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(MaterialTheme.dimens.DP_24_CompactMedium)
        ) {
            GenericCard(
                modifier = Modifier
                    .wrapContentHeight() // Wraps content height
                    .fillMaxWidth()
                    .align(Alignment.TopStart),
                shape = RoundedCornerShape(MaterialTheme.dimens.DP_18_CompactMedium),
            ) {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    // Second GenericCard at the top of the first card
                    GenericCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight(), // Wraps content height
                        backgroundColor = Color(0xFFFFA500), // Replace with any color you want
                        shape = RoundedCornerShape(MaterialTheme.dimens.DP_18_CompactMedium),
                    ) {
                        Column(
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(MaterialTheme.dimens.DP_30_CompactMedium)
                        ) {
                            Text(
                                text = if (isRefund) stringResource(id = R.string.refund_amt) else stringResource(
                                    id = R.string.total_amt
                                ),
                                fontSize = MaterialTheme.dimens.SP_23_CompactMedium,
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .padding(bottom = MaterialTheme.dimens.DP_11_CompactMedium)
                                    .align(Alignment.Start)
                            )

                            // Display the totalAmount here
                            Text(
                                text = "₹$totalAmount",
                                fontSize = MaterialTheme.dimens.SP_27_CompactMedium,
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .align(Alignment.Start)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(MaterialTheme.dimens.DP_11_CompactMedium))

                    ImageView(
                        imageId = R.drawable.swip_card,
                        size = MaterialTheme.dimens.DP_40_CompactMedium,
                        shape = RectangleShape,
                        modifier = Modifier.size(MaterialTheme.dimens.DP_50_CompactMedium)
                    )

                    Spacer(modifier = Modifier.height(MaterialTheme.dimens.DP_11_CompactMedium))

                    TextView(
                        text = stringResource(id = R.string.tap_swipe_insert),
                        fontSize = MaterialTheme.dimens.SP_23_CompactMedium,
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clickable {
                                navHostController.navigate(
                                    AppNavigationItems.CardDetectScreen.createRoute(totalAmount)
                                )
                            }
                    )

                    Spacer(modifier = Modifier.height(MaterialTheme.dimens.DP_11_CompactMedium))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = MaterialTheme.dimens.DP_24_CompactMedium),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ImageView(
                            imageId = R.drawable.master,
                            shape = RectangleShape,
                            modifier = Modifier.size(MaterialTheme.dimens.DP_50_CompactMedium)
                        )

                        ImageView(
                            imageId = R.drawable.visa,
                            shape = RectangleShape,
                            modifier = Modifier.size(MaterialTheme.dimens.DP_50_CompactMedium)
                        )

                        ImageView(
                            imageId = R.drawable.rupay,
                            shape = RectangleShape,
                            modifier = Modifier.size(MaterialTheme.dimens.DP_50_CompactMedium)
                        )
                    }

                    Spacer(modifier = Modifier.height(MaterialTheme.dimens.DP_21_CompactMedium))

                    if(!isRefund) {
                        //Spacer(modifier = Modifier.height(MaterialTheme.dimens.DP_11_CompactMedium))

                        TextView(
                            text = stringResource(id = R.string.or),
                            fontSize = MaterialTheme.dimens.SP_23_CompactMedium,
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clickable {
                                    navHostController.navigate(
                                        AppNavigationItems.CardDetectScreen.createRoute(
                                            totalAmount
                                        )
                                    )
                                }
                                .align(Alignment.CenterHorizontally)
                        )

                        Spacer(modifier = Modifier.height(MaterialTheme.dimens.DP_21_CompactMedium))

                        Button(
                            onClick = { /* Handle button click */ },
                            colors = ButtonDefaults.buttonColors(backgroundColor = Color.White),
                            shape = RoundedCornerShape(MaterialTheme.dimens.DP_18_CompactMedium),
                            modifier = Modifier
                                .width(MaterialTheme.dimens.DP_200_CompactMedium)
                                .padding(horizontal = MaterialTheme.dimens.DP_24_CompactMedium)
                                .padding(bottom = MaterialTheme.dimens.DP_40_CompactMedium)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.upi_icon),
                                contentDescription = null,
                                modifier = Modifier.size(MaterialTheme.dimens.DP_34_CompactMedium)
                            )
                        }
                    }
                }
            }
        }
    }
}



