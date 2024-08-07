package com.analogics.tpaymentsapos.rootUiScreens.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.analogics.tpaymentsapos.R
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.AppButton
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.InputTextField

@Composable
fun LoginScreenView(navHostController: NavHostController?) { // Nullable NavHostController
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Login") },
                backgroundColor = Color(0xFFF8F8F7)
            )
        },
        content = {
            Surface(modifier = Modifier.fillMaxSize().padding(it)) {
                var emailCredentials by remember { mutableStateOf("") }
                var pwdCredentials by remember { mutableStateOf("") }

                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 30.dp)
                ) {
                    // Image above the "Please Login to continue" text
                    Image(
                        painter = painterResource(id = R.drawable.unlock), // Replace with your image resource
                        contentDescription = null, // Decorative image
                        modifier = Modifier
                            .size(70.dp) // Set the size of the image
                            .padding(bottom = 16.dp) // Adds bottom padding to the image
                    )

                    Text(
                        text = "Please login to continue",
                        fontSize = 20.sp,
                        color = Color.LightGray,
                        modifier = Modifier.padding(bottom = 20.dp)
                    )

                    InputTextField(
                        inputValue = emailCredentials,
                        onChange = { emailCredentials = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = "Username",
                        placeHolder = "Username",
                        icon = Icons.Default.Person,
                        keyboardType = KeyboardType.Number
                    )

                    InputTextField(
                        inputValue = pwdCredentials,
                        onChange = { pwdCredentials = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = "Password",
                        placeHolder = "Password",
                        icon = Icons.Default.Lock,
                        keyboardType = KeyboardType.Number
                    )

                    // "Forgot Password?" clickable text
                    Text(
                        text = "Forgot Password?",
                        color = Color.LightGray,
                        fontSize = 16.sp,
                        modifier = Modifier
                            .padding(top = 16.dp)
                            .clickable {
                                navHostController?.navigate(AppNavigationItems.ForgetPasswordScreen.route)
                                // Use safe navigation with ?. to avoid crashes if navHostController is null
                            }
                    )

                    Box(
                        modifier = Modifier.padding(top = 30.dp)
                    ) {
                        AppButton(
                            onClick = {
                                navHostController?.navigate(AppNavigationItems.TrainingScreen.route)
                            },
                            title = "Login →"
                        )
                    }
                }
            }
        }
    )
}
