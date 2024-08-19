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
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.CommonLayout
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.GifImage
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.TransactionState
import com.analogics.tpaymentsapos.ui.theme.dimens
import kotlinx.coroutines.delay

@Composable
fun PleaseWaitView(navHostController: NavHostController) {

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

    LaunchedEffect(Unit) {
        delay(2000) // Delay for 2 seconds (2000 milliseconds)
        if(isVoid)
            navHostController.navigate(AppNavigationItems.ApprovedScreen.route) // Navigate to the desired screen
        else
        navHostController.navigate(AppNavigationItems.ApprovedScreen.route) // Navigate to the desired screen
    }

    CommonLayout(
        title = when {
            isRefund -> stringResource(R.string.refund)
            isVoid -> stringResource(R.string.void_trans)
            isPreauth -> stringResource(R.string.pre_auth)
            else -> stringResource(R.string.purchase)
        },
        imageResId = R.drawable.close
    ) {
        Spacer(modifier = Modifier.height(MaterialTheme.dimens.DP_40_CompactMedium)) // Blank space added here

        Text(
            text = stringResource(id = R.string.plz_wait),
            fontSize = MaterialTheme.dimens.SP_27_CompactMedium,
            color = Color.Black,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(bottom = MaterialTheme.dimens.DP_20_CompactMedium)
                .align(Alignment.CenterHorizontally) // Center the subheader text
        )

        Spacer(modifier = Modifier.height(MaterialTheme.dimens.DP_30_CompactMedium)) // Blank space added here

        GifImage(
            gifResId = R.drawable.wait, // Use your GIF resource here
            modifier = Modifier
                .size(MaterialTheme.dimens.DP_120_CompactMedium)
                .padding(bottom = MaterialTheme.dimens.DP_24_CompactMedium)
                .align(Alignment.CenterHorizontally) // Center the GIF
        )
    }
}


