package com.analogics.tpaymentsapos.rootUiScreens.login

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.CommonTopAppBar

val OrangeColor = Color(0xFFF7931E)

@Composable
fun TrainingView(navHostController: NavHostController) {
    // State to track which button is selected
    val selectedButton = remember { mutableStateOf<String?>(null) }

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
                .height(540.dp)
                .width(430.dp),
            shape = RoundedCornerShape(18.dp),
            elevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Training",
                    fontSize = 20.sp,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(bottom = 20.dp)
                        .align(Alignment.CenterHorizontally)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // First set of buttons
                    Button(
                        onClick = {
                            selectedButton.value = "Purchase"
                            navHostController.navigate(AppNavigationItems.InvoiceScreen.route)
                        },
                        modifier = Modifier
                            .size(120.dp)
                            .padding(8.dp)
                            .border(
                                width = 2.dp,
                                color = if (selectedButton.value == "Purchase") OrangeColor else Color.Transparent,
                                shape = RoundedCornerShape(10.dp)
                            ),
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color.White),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(text = "Purchase", color = Color.Black)
                    }

                    Button(
                        onClick = {
                            selectedButton.value = "Refund"
                            navHostController.navigate(AppNavigationItems.PasswordScreen.route)
                        },
                        modifier = Modifier
                            .size(120.dp)
                            .padding(8.dp)
                            .border(
                                width = 2.dp,
                                color = if (selectedButton.value == "Refund") OrangeColor else Color.Transparent,
                                shape = RoundedCornerShape(10.dp)
                            ),
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color.White),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(text = "Refund", color = Color.Black)
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Second set of buttons
                    Button(
                        onClick = {
                            selectedButton.value = "Pre-Auth"
                            navHostController.navigate(AppNavigationItems.EmailScreen.route)
                        },
                        modifier = Modifier
                            .size(120.dp)
                            .padding(8.dp)
                            .border(
                                width = 2.dp,
                                color = if (selectedButton.value == "Pre-Auth") OrangeColor else Color.Transparent,
                                shape = RoundedCornerShape(10.dp)
                            ),
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color.White),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(text = "Pre-Auth", color = Color.Black)
                    }

                    Button(
                        onClick = {
                            selectedButton.value = "Void"
                            navHostController.navigate(AppNavigationItems.SettingsScreen.route)
                        },
                        modifier = Modifier
                            .size(120.dp)
                            .padding(8.dp)
                            .border(
                                width = 2.dp,
                                color = if (selectedButton.value == "Void") OrangeColor else Color.Transparent,
                                shape = RoundedCornerShape(10.dp)
                            ),
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color.White),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(text = "Void", color = Color.Black)
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Third set of buttons
                    Button(
                        onClick = {
                            selectedButton.value = "Transactions"
                        },
                        modifier = Modifier
                            .size(120.dp)
                            .padding(8.dp)
                            .border(
                                width = 2.dp,
                                color = if (selectedButton.value == "Transactions") OrangeColor else Color.Transparent,
                                shape = RoundedCornerShape(10.dp)
                            ),
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color.White),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(text = "Transactions", color = Color.Black)
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Fourth set of buttons
                    Button(
                        onClick = { /* Handle print receipt click */ },
                        modifier = Modifier
                            .size(260.dp)
                            .padding(8.dp),
                        colors = ButtonDefaults.buttonColors(backgroundColor = OrangeColor),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(text = "Print Last Receipt", color = Color.Black)
                    }
                }
            }
        }
    }
}