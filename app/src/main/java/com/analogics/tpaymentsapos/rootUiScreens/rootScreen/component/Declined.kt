package com.analogics.tpaymentsapos.rootUiScreens.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.analogics.tpaymentsapos.R
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.CommonLayout

@Composable
fun DeclinedView(navHostController: NavHostController, totalAmount: String) {
    CommonLayout(
        title = "Approved",
        imageResId = R.drawable.close
    ) {
        Spacer(modifier = Modifier.height(10.dp)) // Blank space added here

        Text(
            text = "DECLINED",
            fontSize = 24.sp,
            color = Color.Black,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(bottom = 20.dp)
                .align(Alignment.CenterHorizontally) // Center the subheader text
        )

        Spacer(modifier = Modifier.height(20.dp)) // Blank space added here

        Image(
            painter = painterResource(id = R.drawable.approve), // Replace with your image resource
            contentDescription = null, // Decorative image
            modifier = Modifier
                .size(110.dp)
                .padding(bottom = 16.dp)
                .align(Alignment.CenterHorizontally) // Center the image
        )
    }
}
