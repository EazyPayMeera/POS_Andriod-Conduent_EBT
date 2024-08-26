package com.analogics.tpaymentsapos.rootUiScreens.login

import android.os.Build
import android.os.Bundle
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
import com.analogics.tpaymentsapos.R
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import com.analogics.tpaymentsapos.rootUiScreens.approved.viewmodel.ApprovedViewModel
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
    // Define state and resources
    var invoiceno by remember { mutableStateOf("") }
    val isRefund = TransactionState.isRefund
    val isVoid = TransactionState.isVoid
    val isPreauth = TransactionState.isPreauth
    val isprinting = Authorisation.isprinting

    val context = LocalContext.current

    // Create the ViewModel with the context
    val viewModel: PleaseWaitViewModel = viewModel { PleaseWaitViewModel(context) }

    val printStatus by viewModel.printStatus

    // Access string resources
    val refund = stringResource(id = R.string.refund)
    val void = stringResource(id = R.string.void_trans)
    val preAuth = stringResource(id = R.string.pre_auth)
    val purchase = stringResource(id = R.string.purchase)

    // Navigation with delay
    LaunchedEffect(Unit) {
        if(isprinting) {
            //viewModel.feedLine(200)
            val bitmap = viewModel.getLogoBitmap(context, R.drawable.master_mono)
            val imageData: ByteArray = viewModel.getBitmapBytes(bitmap)
                ?: ByteArray(0) // Provide default empty ByteArray if null
            var format = Bundle()
            format.putInt("align", -1)
            format.putInt("width", 300)
            format.putInt("height", 100)
            format.putInt("offset", 0)
            viewModel.addImage(format, imageData)

            val receipt = ApprovedViewModel.Receipt(
                merchantName = "EazyPayTech Store",
                address = "123 Main Street\nCity, State 12345",
                phone = "(123) 456-7890",
                transactionDetails = ApprovedViewModel.TransactionDetails(
                    dateTime = currentDateTime, // Use the current date and time here
                    receiptNumber = "000123",
                    terminalNumber = "001"
                ),
                items = listOf(
                    ApprovedViewModel.ReceiptItem("Item A", 10.00),
                    ApprovedViewModel.ReceiptItem("Item B", 15.00),
                    ApprovedViewModel.ReceiptItem("Item C", 7.50)
                ),
                subtotal = 32.50,
                tax = 1.63,
                total = 34.13,
                paymentMethod = "Credit Card",
                cardNumber = "**** **** **** 1234",
                authCode = "123456",
                customerService = ApprovedViewModel.CustomerService(
                    phone = "(123) 456-7890",
                    email = "support@eazypaytech.com"
                )
            )

            viewModel.printReceiptDetails(receipt)
            format = Bundle()
            format.putInt("align", 1)
            format.putInt("width", 300)
            format.putInt("height", 100)
            format.putSerializable(
                "barcode_type",
                BarcodeFormat.CODE_39
            )
            viewModel.addBarcode(format, "33333333333333")
            viewModel.feedLine(3)
            format = Bundle()
            format.putInt("align", 1)
            format.putInt("offset", 1)
            format.putInt("expectedHeight", 300)
            viewModel.addQRCode(format, "222222222222222222222")
            viewModel.feedLine(3)

        }
        delay(2000) // Delay for 2 seconds (2000 milliseconds)
        val destination = if (isprinting) {
            AppNavigationItems.TrainingScreen.route
        } else {
            AppNavigationItems.ApprovedScreen.route
        }
        navHostController.navigate(destination) // Navigate to the desired screen
        viewModel.printReceipt(context)
        Authorisation.isprinting = false
    }

    Column {
        // Top App Bar with back button
        CommonTopAppBar(
            title = stringResource(id = R.string.approved),
            onBackButtonClick = { navHostController.popBackStack() }
        )

        // Outer Surface with background color, padding, and rounded corners
        BackgroundScreen(
//            color = Color(0xFFF7931E), // Orange color for the outer Surface
//            modifier = Modifier
//                .padding(MaterialTheme.dimens.DP_25_CompactMedium) // Padding for the outer Surface
//                .height(MaterialTheme.dimens.DP_540_CompactMedium) // Adjust the height as per your requirement
//                .width(MaterialTheme.dimens.DP_410_CompactMedium), // Adjust the width as per your requirement
//            shape = RoundedCornerShape(MaterialTheme.dimens.DP_18_CompactMedium) // Rounded corners for the outer Surface
        ) {
            Column(
                modifier = Modifier
                    .padding(MaterialTheme.dimens.DP_24_CompactMedium) // Padding for the content inside the inner Surface
                    .fillMaxSize(), // Fill the entire available space
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start // Align content to the start
            ) {
                Spacer(modifier = Modifier.height(MaterialTheme.dimens.DP_40_CompactMedium)) // Blank space

                // TextView for "Please Wait"
                if(isprinting) {

                    TextView(
                        text = stringResource(id = R.string.printing),
                        fontSize = MaterialTheme.dimens.SP_22_CompactMedium,
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .padding(bottom = MaterialTheme.dimens.DP_20_CompactMedium)
                            .align(Alignment.CenterHorizontally) // Center the TextView horizontally within the Column
                    )
                }
                else
                {
                    TextView(
                        text = stringResource(id = R.string.processing),
                        fontSize = MaterialTheme.dimens.SP_22_CompactMedium,
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .padding(bottom = MaterialTheme.dimens.DP_20_CompactMedium)
                            .align(Alignment.CenterHorizontally) // Center the TextView horizontally within the Column
                    )
                }
                Spacer(modifier = Modifier.height(MaterialTheme.dimens.DP_30_CompactMedium)) // Blank space
                // TextView for "Please Wait"
                TextView(
                    text = stringResource(id = R.string.plz_wait),
                    fontSize = MaterialTheme.dimens.SP_27_CompactMedium,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(bottom = MaterialTheme.dimens.DP_20_CompactMedium)
                        .align(Alignment.CenterHorizontally) // Center the TextView horizontally within the Column
                )

                // TextView for "Please Wait"
                if(isprinting) {
                    TextView(
                        text = stringResource(id = R.string.merchant_recp),
                        fontSize = MaterialTheme.dimens.SP_15_CompactMedium,
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .padding(bottom = MaterialTheme.dimens.DP_20_CompactMedium)
                            .align(Alignment.CenterHorizontally) // Center the TextView horizontally within the Column
                    )
                }

                //Spacer(modifier = Modifier.height(MaterialTheme.dimens.DP_30_CompactMedium)) // Blank space

                // GIF Image
                GifImage(
                    gifResId = R.drawable.wait, // Use your GIF resource here
                    modifier = Modifier
                        .size(MaterialTheme.dimens.DP_120_CompactMedium)
                        .padding(bottom = MaterialTheme.dimens.DP_24_CompactMedium)
                        .align(Alignment.CenterHorizontally) // Center the GIF horizontally within the Column
                )
            }
        }
    }
}



