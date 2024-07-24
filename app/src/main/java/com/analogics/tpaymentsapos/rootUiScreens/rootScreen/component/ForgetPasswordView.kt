package com.analogics.tpaymentsapos.rootUiScreens.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.CommonTopAppBar

@Composable
fun ForgetPasswordView(navHostController: NavHostController) {
    Column {
        CommonTopAppBar(
            title = "Forget Password",
            onBackButtonClick = { navHostController.popBackStack() }
        )

        Surface(
            color = Color(0xFFF7931E), // Orange color for the outer Surface
            modifier = Modifier
                .padding(25.dp) // Padding for the outer Surface
                .height(540.dp) // Adjust the height as per your requirement
                .width(430.dp), // Adjust the width as per your requirement
            shape = RoundedCornerShape(18.dp) // Rounded corners for the outer Surface
        ) {
            Surface(
                color = Color.White, // White color for the inner Surface
                modifier = Modifier
                    .padding(10.dp) // Padding for the inner Surface
                    .height(440.dp) // Adjust the height as per your requirement
                    .width(390.dp), // Adjust the width as per your requirement
                shape = RoundedCornerShape(16.dp) // Rounded corners for the inner Surface
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp) // Padding for the content inside the inner Surface
                        .fillMaxSize(), // Fill the entire available space
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.Start // Align content to the start
                ) {
                    Spacer(modifier = Modifier.height(20.dp)) // Blank space added here
                    Text(
                        text = "Forgot Password?",
                        fontSize = 20.sp,
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .padding(bottom = 20.dp)
                            .align(Alignment.CenterHorizontally) // Center the header text
                    )
                    Spacer(modifier = Modifier.height(20.dp)) // Blank space added here

                    Image(
                        painter = painterResource(id = R.drawable.unlock), // Replace with your image resource
                        contentDescription = null, // Decorative image
                        modifier = Modifier
                            .size(70.dp)
                            .padding(bottom = 16.dp)
                            .align(Alignment.CenterHorizontally) // Center the image
                    )
                    Spacer(modifier = Modifier.height(10.dp)) // Blank space added here

                    Text(
                        text = "Call Customer Care for Password",
                        fontSize = 14.sp,
                        color = Color.LightGray,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .padding(bottom = 20.dp)
                            .align(Alignment.CenterHorizontally) // Center the subheader text
                    )

                    Spacer(modifier = Modifier.height(60.dp)) // Blank space added here

                    // Aligning the following text to the start (left) of the inner Surface
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 20.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.user), // Replace with your icon resource
                            contentDescription = "MID Icon",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp)) // Space between icon and text
                        Text(
                            text = "MID:",
                            fontSize = 14.sp,
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Aligning the following text to the start (left) of the inner Surface
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 20.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.terminal), // Replace with your icon resource
                            contentDescription = "MID Icon",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp)) // Space between icon and text
                        Text(
                            text = "TID:",
                            fontSize = 14.sp,
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Aligning the following text to the start (left) of the inner Surface
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 20.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.list), // Replace with your icon resource
                            contentDescription = "MID Icon",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp)) // Space between icon and text
                        Text(
                            text = "Sr.No:",
                            fontSize = 14.sp,
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Aligning the following text to the start (left) of the inner Surface
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 20.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.callcenter), // Replace with your icon resource
                            contentDescription = "MID Icon",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp)) // Space between icon and text
                        Text(
                            text = "Call Center:",
                            fontSize = 14.sp,
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
