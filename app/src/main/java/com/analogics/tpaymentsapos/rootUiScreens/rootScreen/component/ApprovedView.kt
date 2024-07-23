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
import androidx.navigation.compose.navigation

@Composable
fun ApprovedView(navHostController: NavHostController, totalAmount: String) {
    Column {
        TopAppBar(
            title = { Text("NAMEHERE") },
            backgroundColor = Color(0xFFF8F8F7),
            navigationIcon = {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                        .clickable { navHostController.popBackStack() }
                )
            }
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


                    Image(
                        painter = painterResource(id = R.drawable.close), // Replace with your image resource
                        contentDescription = null, // Decorative image
                        modifier = Modifier
                            .size(40.dp)
                            .padding(bottom = 16.dp)
                            .align(Alignment.End) // Center the image
                    )
                    Spacer(modifier = Modifier.height(10.dp)) // Blank space added here

                    Text(
                        text = "APPROVED",
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
        }
    }
}
