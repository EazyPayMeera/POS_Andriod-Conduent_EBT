package com.analogics.tpaymentsapos.rootUiScreens.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.analogics.tpaymentsapos.R
import com.analogics.tpaymentsapos.navigation.AppNavigationItems

@Composable
fun InvoiceView(navHostController: NavHostController) {
    var invoiceno by remember { mutableStateOf("") }

    Column {
        TopAppBar(
            title = { Text("Purchase") },
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
            color = Color.White,
            modifier = Modifier
                .padding(25.dp)
                .fillMaxWidth()
                .height(250.dp)
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
                    text = "Enter the Invoice Number",
                    fontSize = 14.sp,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(bottom = 20.dp)
                        .align(Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.height(4.dp))

                // Image above the "Please Login to continue" text
                Image(
                    painter = painterResource(id = R.drawable.card), // Replace with your image resource
                    contentDescription = null, // Decorative image
                    modifier = Modifier
                        .size(70.dp) // Set the size of the image
                        .padding(bottom = 16.dp) // Adds bottom padding to the image
                )

                OutlinedTextField(
                    value = invoiceno,
                    onValueChange = { invoiceno = it },
                    label = { Text("Invoice Number") },
                    placeholder = { Text("Invoice Number") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { navHostController.navigate(AppNavigationItems.AmountScreen.route) }
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
