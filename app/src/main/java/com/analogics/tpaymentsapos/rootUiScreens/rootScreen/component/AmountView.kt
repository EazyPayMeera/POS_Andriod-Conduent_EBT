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
import androidx.compose.ui.platform.LocalTextInputService
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.analogics.tpaymentsapos.R
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import java.text.DecimalFormat

@Composable
fun AmountView(navHostController: NavHostController) {
    var rawInput by remember { mutableStateOf("") }
    var formattedAmount by remember { mutableStateOf("0.00") }

    fun formatAmount(input: String): String {
        return if (input.isEmpty()) {
            "0.00"
        } else {
            val doubleValue = input.toDouble() / 100
            DecimalFormat("#0.00").format(doubleValue)
        }
    }

    val transformation = remember {
        object : VisualTransformation {
            override fun filter(text: AnnotatedString): TransformedText {
                val formatted = formatAmount(text.text)
                val offsetMapping = object : OffsetMapping {
                    override fun originalToTransformed(offset: Int): Int = formatted.length
                    override fun transformedToOriginal(offset: Int): Int = text.length
                }
                return TransformedText(AnnotatedString(formatted), offsetMapping)
            }
        }
    }

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
                    text = "Enter the Transaction Amount",
                    fontSize = 14.sp,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(bottom = 20.dp)
                        .align(Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.height(4.dp))

                Image(
                    painter = painterResource(id = R.drawable.card), // Replace with your image resource
                    contentDescription = null, // Decorative image
                    modifier = Modifier
                        .size(70.dp) // Set the size of the image
                        .padding(bottom = 16.dp) // Adds bottom padding to the image
                )

                OutlinedTextField(
                    value = rawInput,
                    onValueChange = { newValue ->
                        // Allow only numeric values and update the raw input
                        if (newValue.all { char -> char.isDigit() }) {
                            rawInput = newValue
                            formattedAmount = formatAmount(newValue)
                        }
                    },
                    label = { Text("Amount") },
                    placeholder = { Text("Enter amount") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number, // Numeric keyboard
                        imeAction = ImeAction.Done // Optional: Handle IME action
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            // Navigate to ConfirmationScreen with the amount
                            navHostController.navigate(AppNavigationItems.ConfirmationScreen.createRoute(formattedAmount))
                        }
                    ),
                    leadingIcon = {
                        Text(
                            text = "₹",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )
                    },
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color(0xFFF7931E), // Orange color when focused
                        unfocusedBorderColor = Color(0xFFF7931E) // Orange color when unfocused
                    ),
                    visualTransformation = transformation,
                    modifier = Modifier
                        .width(240.dp)
                        .height(58.dp) // Adjust the height of the text field
                )
            }
        }
    }
}
