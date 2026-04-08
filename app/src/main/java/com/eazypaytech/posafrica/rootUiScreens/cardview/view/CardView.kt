// CardView.kt

package com.eazypaytech.posafrica.rootUiScreens.cardview.view

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Build
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
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
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.eazypaytech.paymentservicecore.models.TxnType
import com.eazypaytech.posafrica.R
import com.eazypaytech.posafrica.navigation.AppNavigationItems
import com.eazypaytech.posafrica.rootUiScreens.activity.localSharedViewModel
import com.eazypaytech.posafrica.rootUiScreens.cardview.viewmodel.CardViewModel
import com.eazypaytech.posafrica.rootUiScreens.dialogs.CustomDialogBuilder
import com.eazypaytech.posafrica.rootUiScreens.dialogs.CustomDialogBuilder.Companion.composeProgressDialog
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.CommonTopAppBar
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.FooterButtons
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.GenericCard
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.ImageView
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.TextView
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.emvMsgIdToStringId
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.getEmvMsgIdString
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.toAmountFormat
import com.eazypaytech.posafrica.ui.theme.dimens
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import java.util.EnumMap

@SuppressLint("DefaultLocale")
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CardView(navHostController: NavHostController, viewModel: CardViewModel = hiltViewModel()) {

    val context = LocalContext.current
    val sharedViewModel = localSharedViewModel.current


    BackHandler(enabled = true) {

    }

    val (showQRCodeDialog, setShowQRCodeDialog) = remember { mutableStateOf(false) }

    Column {

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

