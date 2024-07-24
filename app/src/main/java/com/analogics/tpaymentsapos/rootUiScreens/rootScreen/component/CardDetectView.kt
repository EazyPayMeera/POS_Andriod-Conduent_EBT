package com.analogics.tpaymentsapos.rootUiScreens.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
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
import kotlinx.coroutines.delay
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.CommonTopAppBar

@Composable
fun CardDetectView(navHostController: NavHostController, totalAmount: String) {
    // Use LaunchedEffect to handle the delay and navigation
    LaunchedEffect(Unit) {
        delay(2000) // Delay for 2 seconds (2000 milliseconds)
        navHostController.navigate(AppNavigationItems.PleaseWaitScreen.route) // Navigate to the desired screen
    }

    Column {
        CommonTopAppBar(
            title = "Purchase",
            onBackButtonClick = { navHostController.popBackStack() }
        )

        Surface(
            color = Color.White,
            modifier = Modifier
                .padding(25.dp)
                .fillMaxWidth()
                .height(500.dp)
                .width(430.dp),
            shape = RoundedCornerShape(18.dp),
            elevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(0.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Inner Surface with orange color
                Surface(
                    color = Color(0xFFF7931E), // Orange color
                    modifier = Modifier
                        .padding(0.dp)
                        .width(450.dp)
                        .height(110.dp), // Adjust size as needed
                    shape = RoundedCornerShape(18.dp),
                    elevation = 8.dp
                ) {
                    // Content inside the inner Surface
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxSize(),
                        verticalArrangement = Arrangement.Top,
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = "Total Amount:",
                            fontSize = 20.sp,
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .padding(bottom = 10.dp) // Bottom padding
                        )
                        // Display the totalAmount here
                        Text(
                            text = "₹$totalAmount",
                            fontSize = 24.sp,
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(5.dp)) // Space between surfaces

                Image(
                    painter = painterResource(id = R.drawable.card), // Replace with your image resource
                    contentDescription = null, // Decorative image
                    modifier = Modifier
                        .size(70.dp) // Set the size of the image
                        .padding(bottom = 16.dp) // Adds bottom padding to the image
                )

                Spacer(modifier = Modifier.height(5.dp))

                Text(
                    text = "Tap/Swipe/Insert",
                    fontSize = 20.sp,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(bottom = 20.dp)
                        .align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(5.dp))

                // Row to hold images horizontally
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.master), // Replace with your image resource
                        contentDescription = null,
                        modifier = Modifier.size(50.dp) // Adjust size as needed
                    )
                    Image(
                        painter = painterResource(id = R.drawable.visa), // Replace with your image resource
                        contentDescription = null,
                        modifier = Modifier.size(50.dp) // Adjust size as needed
                    )
                    Image(
                        painter = painterResource(id = R.drawable.rupay), // Replace with your image resource
                        contentDescription = null,
                        modifier = Modifier.size(50.dp) // Adjust size as needed
                    )
                }

                Spacer(modifier = Modifier.height(5.dp))

                Text(
                    text = "Chip Detected",
                    fontSize = 20.sp,
                    color = Color(0xFFF7931E),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(bottom = 20.dp)
                        .align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(10.dp)) // Space before the CANCEL button

                // CANCEL Button
                Button(
                    onClick = { /* Handle CANCEL button click */ },
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color.White), // Red button color for emphasis
                    shape = RoundedCornerShape(18), // Adjust the shape as needed
                    modifier = Modifier
                        .width(200.dp) // Set the fixed width here
                        .height(50.dp)
                        .padding(horizontal = 16.dp) // Optional padding for spacing
                ) {
                    Text(
                        text = "CANCEL",
                        fontSize = 16.sp,
                        color = Color.Black // Text color for contrast
                    )
                }
            }
        }
    }
}