// CardView.kt

package com.analogics.tpaymentsapos.rootUiScreens.login

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.analogics.tpaymentsapos.R
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import com.analogics.tpaymentsapos.rootUiScreens.cardview.viewmodel.CardViewModel
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
    val isAuthcap = TransactionState.isAuthcap
    val isPreauth = TransactionState.isPreauth
    val isPurchase = TransactionState.isPurchase
    val viewModel: CardViewModel = hiltViewModel()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

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
                        backgroundColor = colorResource(id = R.color.purple_200), // Replace with any color you want
                        shape = RoundedCornerShape(MaterialTheme.dimens.DP_18_CompactMedium),
                    ) {
                        Column(
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(MaterialTheme.dimens.DP_30_CompactMedium)
                        ) {
                            Text(
                                text = if (isRefund) stringResource(id = R.string.refund_amt_data) else stringResource(
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
                                fontSize = MaterialTheme.dimens.SP_31_CompactMedium,
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
                            imageId = R.drawable.master,
                            shape = RectangleShape,
                            modifier = Modifier.size(MaterialTheme.dimens.DP_50_CompactMedium)
                        )
                    }

                    Spacer(modifier = Modifier.height(MaterialTheme.dimens.DP_21_CompactMedium))

                    if(isPurchase) {
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
    LaunchedEffect(Unit) {
        viewModel.startPayment(context, navHostController)
    }
}



