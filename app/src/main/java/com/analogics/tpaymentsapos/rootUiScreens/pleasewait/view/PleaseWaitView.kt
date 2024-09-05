package com.analogics.tpaymentsapos.rootUiScreens.login

import android.content.ContentValues.TAG
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.analogics.paymentservicecore.listeners.responseListener.IPrinterResultProviderListener
import com.analogics.tpaymentsapos.R
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import com.analogics.tpaymentsapos.rootUiScreens.pleasewait.viewmodel.PleaseWaitViewModel
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.Authorisation
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.BackgroundScreen
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.CommonTopAppBar
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.GifImage
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.TextView
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.TransactionState
import com.analogics.tpaymentsapos.ui.theme.dimens
import com.google.zxing.BarcodeFormat
import kotlinx.coroutines.delay


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PleaseWaitView(navHostController: NavHostController) {
    var invoiceno by remember { mutableStateOf("") }
    val isRefund = TransactionState.isRefund
    val isVoid = TransactionState.isVoid
    val isPreauth = TransactionState.isPreauth
    val isMerchantReceipt = Authorisation.isMerchantReceipt
    val isEreceipt = Authorisation.isEreceipt

    val context = LocalContext.current
    val viewModel: PleaseWaitViewModel = viewModel { PleaseWaitViewModel(context) }
    var isPrintingStarted by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (isMerchantReceipt) {
            // Phase 1: Show "Please Wait" for a few seconds
            delay(2000) // Delay for 4 seconds before starting the printing process
            isPrintingStarted = true

            // Phase 2: Printing process
            val bitmap = viewModel.getLogoBitmap(context, R.drawable.master_mono)
            val imageData: ByteArray = viewModel.getBitmapBytes(bitmap) ?: ByteArray(0)
            var format = Bundle().apply {
                putInt("align", -1)
                putInt("width", 300)
                putInt("height", 100)
                putInt("offset", 0)
            }
            viewModel.addImage(format, imageData)

            format = Bundle().apply {
                putInt("align", 1)
                putInt("width", 300)
                putInt("height", 100)
                putSerializable("barcode_type", BarcodeFormat.CODE_39)
            }

            viewModel.addReceiptDetails(format, object : IPrinterResultProviderListener {
                override fun onSuccess(result: Any?) {
                    if (result == true) {
                        Log.d(TAG, "Receipt printed successfully")
                    } else {
                        Log.d(TAG, "Receipt print failed")
                    }
                }

                override fun onFailure(exception: Exception) {
                    Log.e(TAG, "Receipt print failed with exception: ${exception.message}")
                }
            })

            // Phase 3: Navigate to the appropriate screen

        }
        delay(2000) // Additional delay to ensure the printing completes
        // For non-merchant receipt cases, navigate without waiting for printing
        val destination = when {
            isMerchantReceipt -> AppNavigationItems.TrainingScreen.route
            isEreceipt -> AppNavigationItems.EmailScreen.route
            else -> AppNavigationItems.ApprovedScreen.route
        }
        navHostController.navigate(destination)
    }

    Column {
        CommonTopAppBar(
            title = stringResource(id = R.string.approved),
            onBackButtonClick = { navHostController.popBackStack() }
        )

        BackgroundScreen() {
            Column(
                modifier = Modifier
                    .padding(MaterialTheme.dimens.DP_24_CompactMedium)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start
            ) {
                Spacer(modifier = Modifier.height(MaterialTheme.dimens.DP_40_CompactMedium))

                TextView(
                    text = if (isMerchantReceipt) stringResource(id = R.string.printing) else stringResource(id = R.string.processing),
                    fontSize = MaterialTheme.dimens.SP_22_CompactMedium,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(bottom = MaterialTheme.dimens.DP_20_CompactMedium)
                        .align(Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.height(MaterialTheme.dimens.DP_30_CompactMedium))

                TextView(
                    text = stringResource(id = R.string.plz_wait),
                    fontSize = MaterialTheme.dimens.SP_27_CompactMedium,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(bottom = MaterialTheme.dimens.DP_20_CompactMedium)
                        .align(Alignment.CenterHorizontally)
                )

                if (isMerchantReceipt) {
                    TextView(
                        text = stringResource(id = R.string.merchant_recp),
                        fontSize = MaterialTheme.dimens.SP_15_CompactMedium,
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .padding(bottom = MaterialTheme.dimens.DP_20_CompactMedium)
                            .align(Alignment.CenterHorizontally)
                    )
                }

                GifImage(
                    gifResId = R.drawable.wait,
                    modifier = Modifier
                        .size(MaterialTheme.dimens.DP_120_CompactMedium)
                        .padding(bottom = MaterialTheme.dimens.DP_24_CompactMedium)
                        .align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}
