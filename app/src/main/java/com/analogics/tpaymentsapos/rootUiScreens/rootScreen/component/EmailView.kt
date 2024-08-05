package com.analogics.tpaymentsapos.rootUiScreens.login

import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.analogics.tpaymentsapos.R
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.Authorisation
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.CommonLayout
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.OkButton
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.TransactionState

@Composable
fun EmailView(navHostController: NavHostController, email: String) {

    val isRefund = TransactionState.isRefund
    val isVoid = TransactionState.isVoid
    val isPreauth = TransactionState.isPreauth
    val isAuthcap = Authorisation.isAuthcap

    CommonLayout(
        title = if (isRefund) "Refund" else if (isVoid) "Void" else if (isPreauth) "Pre-Auth" else "Purchase",
        imageResId = R.drawable.close
    ) {
        Spacer(modifier = Modifier.height(40.dp)) // Blank space added here

        Text(
            text = "Success",
            fontSize = 24.sp,
            color = Color.Black,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(bottom = 20.dp)
                .align(Alignment.CenterHorizontally) // Center the subheader text
        )

        Spacer(modifier = Modifier.height(20.dp)) // Blank space added here

        Text(
            text = "Email Sent",
            fontSize = 24.sp,
            color = Color.Black,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(bottom = 20.dp)
                .align(Alignment.CenterHorizontally) // Center the subheader text
        )

        Spacer(modifier = Modifier.height(20.dp)) // Blank space added here

        Text(
            text = "e-Receipt was successfully sent",
            fontSize = 12.sp,
            color = Color.LightGray,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(bottom = 20.dp)
                .align(Alignment.CenterHorizontally) // Center the subheader text
        )

        Text(
            text = "on $email",
            fontSize = 12.sp,
            color = Color.LightGray,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(bottom = 20.dp)
                .align(Alignment.CenterHorizontally) // Center the subheader text
        )


        Box(
            modifier = Modifier.padding(top = 30.dp)
        ) {
            OkButton(
                onClick = {
                    navHostController.navigate(AppNavigationItems.TrainingScreen.route)
                },
                title = "OK"
            )
        }
    }
}
