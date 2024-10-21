package com.analogics.tpaymentsapos.rootUiScreens.login

import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.analogics.tpaymentsapos.R
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import com.analogics.tpaymentsapos.rootUiScreens.dialogs.CustomDialogBuilder
import com.analogics.tpaymentsapos.rootUiScreens.pleasewait.viewmodel.PleaseWaitViewModel
import com.google.zxing.BarcodeFormat
import kotlinx.coroutines.delay


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PleaseWaitView(navHostController: NavHostController) {
    val isMerchantReceipt = true
    val isCustomerReceipt = false

    val context = LocalContext.current
    val viewModel: PleaseWaitViewModel = viewModel { PleaseWaitViewModel(context) }
    var isPrintingStarted by remember { mutableStateOf(false) }
    var isDialogVisible by remember { mutableStateOf(false) }

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

           /* viewModel.addReceiptDetails(format, object : IPrinterResultProviderListener {
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
            })*/


        }
        delay(2000) // Additional delay to ensure the printing completes

    }

    /*if (isMerchantReceipt || isCustomerReceipt) {*/
        CustomDialogBuilder.create()
            .setTitle(stringResource(id = R.string.printing))
            .setSubtitle(stringResource(id = R.string.plz_wait))
            .setSmallText(if(isCustomerReceipt) stringResource(id = R.string.cust_recp) else stringResource(
                id = R.string.merchant_recp
            ))
            .setShowCloseButton(true) // Can set to false if you don't want the close button
            .setCancelable(true)
            .setBackgroundColor(androidx.compose.material.MaterialTheme.colors.surface)
            .setProgressColor(color = MaterialTheme.colorScheme.primary) // Orange color
            .setNavAction {
                navHostController.navigate(AppNavigationItems.ApprovedScreen.route)
            }
            .buildDialog(onClose = { isDialogVisible = false })


}
