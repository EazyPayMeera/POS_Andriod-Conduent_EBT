package com.analogics.tpaymentsapos.rootUiScreens.login

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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.analogics.paymentservicecore.models.TxnInfo
import com.analogics.paymentservicecore.models.TxnType
import com.analogics.tpaymentsapos.R
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import com.analogics.tpaymentsapos.rootUiScreens.carddetect.viewmodel.CardDetectViewModel
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.CommonTopAppBar
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.GenericCard
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.ImageView
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.TextView
import com.analogics.tpaymentsapos.ui.theme.dimens

@Composable
fun CardDetectView(navHostController: NavHostController, totalAmount: String) {
    val viewModel: CardDetectViewModel = hiltViewModel()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()


    // Use LaunchedEffect to handle the delay and navigation
    LaunchedEffect(Unit) {
        // Delay for 2 seconds
        kotlinx.coroutines.delay(2000)
        // Navigate to another screen after the delay without removing the current screen from the back stack
        navHostController.navigate(AppNavigationItems.PinScreen.route)
    }
    viewModel.setTotalAmount(totalAmount)
    Column {

        CommonTopAppBar(
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
                        backgroundColor = colorResource(id = R.color.purple_200),
                        shape = RoundedCornerShape(MaterialTheme.dimens.DP_18_CompactMedium),
                    ) {
                        Column(
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(MaterialTheme.dimens.DP_30_CompactMedium)
                        ) {
                            Text(
                                text = if (TxnInfo.txnType == TxnType.REFUND) stringResource(id = R.string.refund_amt_data) else stringResource(
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
                                fontSize = MaterialTheme.dimens.SP_35_CompactMedium,
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

                    TextView(
                        text = stringResource(id = R.string.chip_detected),
                        fontSize = MaterialTheme.dimens.SP_23_CompactMedium,
                        color = colorResource(id = R.color.purple_200),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .padding(bottom = MaterialTheme.dimens.DP_40_CompactMedium)
                            .align(Alignment.CenterHorizontally)
                    )
                }
            }
        }
    }
}


