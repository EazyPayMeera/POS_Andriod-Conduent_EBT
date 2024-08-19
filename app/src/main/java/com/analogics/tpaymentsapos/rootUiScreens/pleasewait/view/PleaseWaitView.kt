package com.analogics.tpaymentsapos.rootUiScreens.login

import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavHostController
import com.analogics.tpaymentsapos.R
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.Authorisation
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.BackgroundScreen
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.CommonLayout
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.CommonTopAppBar
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.GifImage
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.TextView
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.TransactionState
import com.analogics.tpaymentsapos.ui.theme.dimens
import kotlinx.coroutines.delay

@Composable
fun PleaseWaitView(navHostController: NavHostController) {
    // Define state and resources
    var invoiceno by remember { mutableStateOf("") }
    val isRefund = TransactionState.isRefund
    val isVoid = TransactionState.isVoid
    val isPreauth = TransactionState.isPreauth
    val isAuthcap = Authorisation.isAuthcap

    // Access string resources
    val refund = stringResource(id = R.string.refund)
    val void = stringResource(id = R.string.void_trans)
    val preAuth = stringResource(id = R.string.pre_auth)
    val purchase = stringResource(id = R.string.purchase)

    // Navigation with delay
    LaunchedEffect(Unit) {
        delay(2000) // Delay for 2 seconds (2000 milliseconds)
        val destination = if (isVoid) {
            AppNavigationItems.ApprovedScreen.route
        } else {
            AppNavigationItems.ApprovedScreen.route
        }
        navHostController.navigate(destination) // Navigate to the desired screen
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
                TextView(
                    text = stringResource(id = R.string.plz_wait),
                    fontSize = MaterialTheme.dimens.SP_27_CompactMedium,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(bottom = MaterialTheme.dimens.DP_20_CompactMedium)
                        .align(Alignment.CenterHorizontally) // Center the TextView horizontally within the Column
                )

                Spacer(modifier = Modifier.height(MaterialTheme.dimens.DP_30_CompactMedium)) // Blank space

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



