package com.analogics.tpaymentsapos.rootUiScreens.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.analogics.tpaymentsapos.R
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.Authorisation
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.CommonLayout
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.GifImage
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.TransactionState
import kotlinx.coroutines.delay

@Composable
fun PleaseWaitView(navHostController: NavHostController) {

    var invoiceno by remember { mutableStateOf("") }
    val isRefund = TransactionState.isRefund
    val isVoid = TransactionState.isVoid
    val isPreauth = TransactionState.isPreauth
    val isAuthcap = Authorisation.isAuthcap

    LaunchedEffect(Unit) {
        delay(2000) // Delay for 2 seconds (2000 milliseconds)
        if(isVoid)
            navHostController.navigate(AppNavigationItems.ApprovedScreen.route) // Navigate to the desired screen
        else
        navHostController.navigate(AppNavigationItems.ApprovedScreen.route) // Navigate to the desired screen
    }

    CommonLayout(
        title = if (isRefund) "Refund" else if (isVoid) "Void" else if (isPreauth) "Pre-Auth" else "Purchase",
        imageResId = R.drawable.close
    ) {
        Spacer(modifier = Modifier.height(40.dp)) // Blank space added here

        Text(
            text = "Please Wait",
            fontSize = 24.sp,
            color = Color.Black,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(bottom = 20.dp)
                .align(Alignment.CenterHorizontally) // Center the subheader text
        )

        Spacer(modifier = Modifier.height(20.dp)) // Blank space added here

        GifImage(
            gifResId = R.drawable.wait, // Use your GIF resource here
            modifier = Modifier
                .size(110.dp)
                .padding(bottom = 16.dp)
                .align(Alignment.CenterHorizontally) // Center the GIF
        )
    }
}


