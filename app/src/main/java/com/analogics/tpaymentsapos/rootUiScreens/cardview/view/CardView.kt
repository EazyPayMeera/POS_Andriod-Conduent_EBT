// CardView.kt

package com.analogics.tpaymentsapos.rootUiScreens.cardview.view

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
import androidx.compose.runtime.collectAsState
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
import com.analogics.paymentservicecore.models.TxnType
import com.analogics.tpaymentsapos.R
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import com.analogics.tpaymentsapos.rootUiScreens.activity.localSharedViewModel
import com.analogics.tpaymentsapos.rootUiScreens.cardview.viewmodel.CardViewModel
import com.analogics.tpaymentsapos.rootUiScreens.dialogs.CustomDialogBuilder
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.CommonTopAppBar
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.FooterButtons
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.GenericCard
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.ImageView
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.TextView
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.getCurrentDateTime
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.getEmvMsgIdString
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.toAmountFormat
import com.analogics.tpaymentsapos.ui.theme.dimens
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
    val openBatchId = viewModel.openBatch.collectAsState().value
    val lastBatchId = viewModel.lastBatch.collectAsState().value
    val isAnyBatchPresent = viewModel.isBatchPresent.collectAsState().value
    Log.d("Batch Id","Present Batch Id $openBatchId")
    // Disable the hardware back button
    BackHandler(enabled = true) {
        // Do nothing or handle custom behavior here
    }

    // State to manage QR code dialog visibility
    val (showQRCodeDialog, setShowQRCodeDialog) = remember { mutableStateOf(false) }

    Column {

        CommonTopAppBar(
            onBackButtonClick = { navHostController.popBackStack() },
            showBackIcon = false
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
                    // Second GenericCard at the top of the first card
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
                                text = if (sharedViewModel.objRootAppPaymentDetail.txnType==TxnType.REFUND) stringResource(id = R.string.refund_amt_data) else stringResource(
                                    id = R.string.total_amt
                                ),
                                fontSize = MaterialTheme.dimens.SP_23_CompactMedium,
                                color = MaterialTheme.colorScheme.tertiary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .padding(bottom = MaterialTheme.dimens.DP_11_CompactMedium)
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
                        fontWeight = FontWeight.Bold,
                        onClick = {
                            if(isAnyBatchPresent.isEmpty())
                            {
                                Log.d("Batch Id","Initial Batch Id Inserted")
                                sharedViewModel.batchEntity.batchId = "000001"
                                sharedViewModel.objRootAppPaymentDetail.batchId = sharedViewModel.batchEntity.batchId
                                sharedViewModel.batchEntity.batchStatus = "open"
                                sharedViewModel.batchEntity.cashierId = sharedViewModel.objPosConfig?.loginId
                                viewModel.insertBatchData(sharedViewModel.batchEntity)
                            }
                            else
                            {
                                if(openBatchId.isNullOrBlank())
                                {
                                   val newBatchId = lastBatchId?.toIntOrNull()?.let { String.format("%06d", it + 1) }
                                    Log.d("Batch Id", "Last batch ID Present: $lastBatchId")
                                    Log.d("Batch Id", "Generated new batch ID: $newBatchId")
                                    sharedViewModel.batchEntity.batchId = newBatchId
                                    sharedViewModel.objRootAppPaymentDetail.batchId = sharedViewModel.batchEntity.batchId
                                    sharedViewModel.batchEntity.batchStatus = "open"
                                    sharedViewModel.batchEntity.cashierId = sharedViewModel.objPosConfig?.loginId
                                    viewModel.insertBatchData(sharedViewModel.batchEntity)
                                }
                                else {
                                    Log.d("Batch Id", "Open Batch Id Found and set same Batch Id")
                                    sharedViewModel.objRootAppPaymentDetail.batchId = openBatchId
                                }
                            }
                            navHostController.navigate(AppNavigationItems. CardDetectScreen.route)
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
                            modifier = Modifier.size(MaterialTheme.dimens.DP_50_CompactMedium),
                            contentDescription = ""
                        )

                        ImageView(
                            imageId = R.drawable.visa,
                            shape = RectangleShape,
                            modifier = Modifier.size(MaterialTheme.dimens.DP_50_CompactMedium),
                            contentDescription = ""
                        )

                        ImageView(
                            imageId = R.drawable.rupay,
                            shape = RectangleShape,
                            modifier = Modifier.size(MaterialTheme.dimens.DP_50_CompactMedium),
                            contentDescription = ""
                        )
                    }

                    Spacer(modifier = Modifier.height(MaterialTheme.dimens.DP_21_CompactMedium))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(MaterialTheme.dimens.DP_160_CompactMedium)
                            .padding(horizontal = MaterialTheme.dimens.DP_24_CompactMedium),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            if (viewModel.emvInProgress.value == false) {
                                if(sharedViewModel.objRootAppPaymentDetail.txnType == TxnType.PURCHASE) {
                                    Log.d("Batch Id", "EMV Progress Value is False")
                                    TextView(
                                        text = stringResource(id = R.string.or),
                                        fontSize = MaterialTheme.dimens.SP_23_CompactMedium,
                                        color = MaterialTheme.colorScheme.tertiary,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier
                                            .align(Alignment.CenterHorizontally)
                                    )

                                    Spacer(modifier = Modifier.height(MaterialTheme.dimens.DP_21_CompactMedium))

                                    Button(
                                        onClick = { viewModel.onUpiClick(navHostController) },
                                        colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colorScheme.onPrimary),
                                        shape = RoundedCornerShape(MaterialTheme.dimens.DP_18_CompactMedium),
                                        modifier = Modifier
                                            .width(MaterialTheme.dimens.DP_160_CompactMedium)
                                            .height(MaterialTheme.dimens.DP_70_CompactMedium)
                                            .padding(horizontal = MaterialTheme.dimens.DP_4_CompactMedium)
                                            .padding(bottom = MaterialTheme.dimens.DP_21_CompactMedium)
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.upi),
                                            contentDescription = null,
                                            modifier = Modifier
                                                .fillMaxSize(),
                                            tint = Color.Unspecified // Keeps the original colors of the icon
                                        )
                                    }
                                }

                            }
                            else
                            {
                                Log.d("Batch Id", "EMV Progress Value is True")
                                    TextView(
                                        text = getEmvMsgIdString(displayMsgId = viewModel.displayInfoMsgId.value),
                                        fontSize = MaterialTheme.dimens.SP_23_CompactMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier
                                            .padding(bottom = MaterialTheme.dimens.DP_10_CompactMedium)
                                            .align(Alignment.CenterHorizontally)
                                    )

                                viewModel.showProgressVar.value.takeIf { it == true }?.let {
                                    Log.d("Batch Id", "Fetched batches: Card Inserted")
                                    CircularProgressIndicator(
                                        modifier = Modifier
                                            .padding(MaterialTheme.dimens.DP_10_CompactMedium)
                                            .size(MaterialTheme.dimens.DP_70_CompactMedium),
                                        color = MaterialTheme.colorScheme.primary,
                                        strokeWidth = MaterialTheme.dimens.DP_4_CompactMedium,
                                    )
                                }


                            }
                        }

                    }
                }
            }
        }
    }

    FooterButtons(stringResource(id = R.string.cancel),{viewModel.onCancelClick(navHostController)}, enabled = viewModel.emvInProgress.value==false)


    LaunchedEffect(Unit) {
        viewModel.startPayment(context, sharedViewModel.objRootAppPaymentDetail, sharedViewModel, navHostController)
        sharedViewModel.objRootAppPaymentDetail.dateTime = getCurrentDateTime()
        sharedViewModel.objRootAppPaymentDetail.batchId = sharedViewModel.objPosConfig?.batchId
    }

    // QR Code Dialog
    if (showQRCodeDialog) {
        QRCodeDialog(
            qrCodeContent = "Your QR Code Content Here", // Replace with the content you want to encode
            onDismiss = { setShowQRCodeDialog(false) }
        )
    }

    CustomDialogBuilder.ShowComposed()
}

@Composable
fun QRCodeDialog(
    qrCodeContent: String,
    onDismiss: () -> Unit
) {
    // Generate QR Code bitmap
    val qrCodeBitmap = generateQRCode(qrCodeContent, 300, 300) // Adjust size as needed
    val imageBitmap = qrCodeBitmap?.asImageBitmap() // Convert Bitmap to ImageBitmap

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(MaterialTheme.dimens.DP_24_CompactMedium),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Display QR Code
                imageBitmap?.let {
                    Image(
                        bitmap = it,
                        contentDescription = "",
                        modifier = Modifier.size(MaterialTheme.dimens.DP_300_CompactMedium)
                    )
                }

                Spacer(modifier = Modifier.height(MaterialTheme.dimens.DP_24_CompactMedium))

                // Close button
                Button(onClick = onDismiss) {
                    Text(text = stringResource(id = R.string.close))
                }
            }
        }
    }
}

fun generateQRCode(content: String, width: Int, height: Int): Bitmap? {
    try {
        val hints = EnumMap<EncodeHintType, Any>(EncodeHintType::class.java)
        hints[EncodeHintType.CHARACTER_SET] = "UTF-8"

        // Generate the BitMatrix for the QR code
        val bitMatrix: BitMatrix = MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, width, height, hints)

        // Convert the BitMatrix to a Bitmap
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(x, y, if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
            }
        }
        return bitmap
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }
}
