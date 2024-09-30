package com.analogics.tpaymentsapos.rootUiScreens.barcode

import BarcodeViewModel
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.analogics.tpaymentsapos.R
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import com.analogics.tpaymentsapos.rootUiScreens.activity.localSharedViewModel
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.BackgroundScreen
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.CommonTopAppBar
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.OkButton
import com.analogics.tpaymentsapos.ui.theme.dimens

@Composable
fun BarcodeView(navHostController: NavHostController) {
    // Directly show the QR Code Dialog
    val qrCodeContent = remember { "Sample QR Code Content" } // Replace with actual QR code content
    val sharedViewModel = localSharedViewModel.current

    val viewModel: BarcodeViewModel = hiltViewModel()

    Column {
        CommonTopAppBar(
            onBackButtonClick = { navHostController.popBackStack() }
        )

        BackgroundScreen {
            Column(
                modifier = Modifier
                    .padding(top = MaterialTheme.dimens.DP_40_CompactMedium)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start
            ) {

                Text(
                    text =stringResource(id = R.string.total_amt),
                    fontSize = MaterialTheme.dimens.SP_23_CompactMedium,
                    color = MaterialTheme.colorScheme.tertiary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(bottom = MaterialTheme.dimens.DP_11_CompactMedium)
                        .align(Alignment.CenterHorizontally)
                )

                Text(
                    text = sharedViewModel.objRootAppPaymentDetail.ttlAmount.toString(),
                    fontSize = MaterialTheme.dimens.SP_31_CompactMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                val qrCodeBitmap =
                    viewModel.generateQRCode(
                        qrCodeContent,
                        300,
                        300
                    ) // Adjust size as needed
                val imageBitmap = qrCodeBitmap?.asImageBitmap() // Convert Bitmap to ImageBitmap

                    Box(
                        /*modifier = Modifier
                            //.fillMaxSize(),
                            //.padding(MaterialTheme.dimens.DP_24_CompactMedium),*/
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

                            OkButton(
                                onClick = {
                                    /*viewModel.updateTxnData(sharedViewModel.objRootAppPaymentDetail)*/
                                    navHostController.navigate(AppNavigationItems.CardScreen.route)
                                },
                                title = stringResource(id = R.string.cancel),
                            )

                        }
                    }
            }

        }
    }
}



