// CardScreen.kt

package com.eazypaytech.pos.features.cards.ui

import android.annotation.SuppressLint
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.analogics.paymentservicecore.data.model.TxnType
import com.eazypaytech.pos.R
import com.eazypaytech.pos.features.activity.ui.localSharedViewModel
import com.eazypaytech.pos.features.dialogs.ui.CustomDialogBuilder
import com.eazypaytech.pos.features.dialogs.ui.CustomDialogBuilder.Companion.composeProgressDialog
import com.eazypaytech.pos.core.ui.components.inputfields.CommonTopAppBar
import com.eazypaytech.pos.core.ui.components.inputfields.FooterButtons
import com.eazypaytech.pos.core.ui.components.textview.GenericCard
import com.eazypaytech.pos.core.ui.components.inputfields.ImageView
import com.eazypaytech.pos.core.ui.components.textview.TextView
import com.eazypaytech.pos.core.utils.toAmountFormat
import com.eazypaytech.pos.core.themes.dimens

@SuppressLint("DefaultLocale")
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CardScreen(navHostController: NavHostController, viewModel: CardViewModel = hiltViewModel()) {

    val context = LocalContext.current
    val sharedViewModel = localSharedViewModel.current


    BackHandler(enabled = true) {

    }

    val (showQRCodeDialog, setShowQRCodeDialog) = remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {

        CommonTopAppBar(
            onBackButtonClick = { navHostController.popBackStack() },
            showBackIcon = true
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(MaterialTheme.dimens.DP_24_CompactMedium)
        ) {
            GenericCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopStart),
                shape = RoundedCornerShape(MaterialTheme.dimens.DP_18_CompactMedium),
            ) {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    if(sharedViewModel.objRootAppPaymentDetail.txnType != TxnType.BALANCE_ENQUIRY_CASH && sharedViewModel.objRootAppPaymentDetail.txnType != TxnType.BALANCE_ENQUIRY_SNAP) {// Second GenericCard at the top of the first card
                        GenericCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight(),
                            backgroundColor = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(MaterialTheme.dimens.DP_18_CompactMedium),
                        ) {

                            Column(
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(MaterialTheme.dimens.DP_30_CompactMedium)
                            ) {
                                Text(
                                    text = if (sharedViewModel.objRootAppPaymentDetail.txnType == TxnType.FOODSTAMP_RETURN) stringResource(
                                        id = R.string.refund_amt_data
                                    ) else stringResource(
                                        id = R.string.total_amt
                                    ),
                                    fontSize = MaterialTheme.dimens.SP_23_CompactMedium,
                                    color = MaterialTheme.colorScheme.tertiary,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .padding(bottom = MaterialTheme.dimens.DP_2_CompactMedium)
                                        .align(Alignment.Start)
                                )

                                // Display the totalAmount here
                                Text(
                                    text = sharedViewModel.objRootAppPaymentDetail.ttlAmount.toAmountFormat(),
                                    fontSize = MaterialTheme.dimens.SP_31_CompactMedium,
                                    color = MaterialTheme.colorScheme.tertiary,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .align(Alignment.Start)
                                )
                            }

                        }
                    }

                    Spacer(modifier = Modifier.height(MaterialTheme.dimens.DP_11_CompactMedium))

                    ImageView(
                        imageId = R.drawable.swip_card,
                        size = MaterialTheme.dimens.DP_40_CompactMedium,
                        shape = RectangleShape,
                        modifier = Modifier.size(MaterialTheme.dimens.DP_50_CompactMedium),
                        contentDescription = ""
                    )

                    Spacer(modifier = Modifier.height(MaterialTheme.dimens.DP_11_CompactMedium))

                    TextView(
                        text = stringResource(id = R.string.tap_swipe_insert),
                        fontSize = MaterialTheme.dimens.SP_23_CompactMedium,
                        color = MaterialTheme.colorScheme.tertiary,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(MaterialTheme.dimens.DP_11_CompactMedium))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = MaterialTheme.dimens.DP_24_CompactMedium),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ImageView(
                            imageId = R.drawable.ebt,
                            shape = RectangleShape,
                            modifier = Modifier.size(MaterialTheme.dimens.DP_50_CompactMedium),
                            contentDescription = ""
                        )

                    }

                    Spacer(modifier = Modifier.height(MaterialTheme.dimens.DP_21_CompactMedium))
                    composeProgressDialog(viewModel.showProgressVar.value)
                }
            }

        }
    }

    FooterButtons(stringResource(id = R.string.cancel),{viewModel.onCancelClick(navHostController)}, enabled = viewModel.emvInProgress.value==false)

    LaunchedEffect(Unit) {
        viewModel.startPayment(context, sharedViewModel, navHostController)
    }

    CustomDialogBuilder.ShowComposed()
}

