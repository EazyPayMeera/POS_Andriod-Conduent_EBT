// ConfirmationView.kt

package com.analogics.tpaymentsapos.rootUiScreens.login

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import java.text.DecimalFormat

@Composable
fun ConfirmationView(navHostController: NavHostController, amount: String) {
    var selectedTipPercentage by remember { mutableStateOf(0) }
    val amountDouble = amount.toDoubleOrNull() ?: 0.0

    val sgstAmount = calculateTax(amountDouble)
    val igstAmount = calculateTax(amountDouble)
    val tipAmount = calculateTip(amountDouble, selectedTipPercentage)

    val totalAmount = calculateTotalAmount(amountDouble, tipAmount, sgstAmount, igstAmount)

    Column(
        modifier = Modifier
            .padding(25.dp)
            .fillMaxSize()
    ) {
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

        Spacer(modifier = Modifier.height(20.dp))

        Surface(
            color = Color(0xFFFFA500),
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp),
            shape = RoundedCornerShape(18.dp),
            elevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Total Amount:",
                    fontSize = 20.sp,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(bottom = 10.dp)
                )
                Text(
                    text = "₹${formatAmount(totalAmount)}",
                    fontSize = 24.sp,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Surface(
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp),
            shape = RoundedCornerShape(18.dp),
            elevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Transaction Summary:",
                    fontSize = 16.sp,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(bottom = 10.dp)
                )

                Text(
                    text = "Transaction Amount: ₹${formatAmount(amountDouble)}",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 5.dp)
                )
                Text(
                    text = "Tip Amount: ₹${formatAmount(tipAmount)}",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 5.dp)
                )
                Text(
                    text = "SGST Amount: ₹${formatAmount(sgstAmount)}",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 5.dp)
                )
                Text(
                    text = "IGST Amount: ₹${formatAmount(igstAmount)}",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 5.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Surface(
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp),
            shape = RoundedCornerShape(18.dp),
            elevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Add Tip:",
                    fontSize = 20.sp,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(bottom = 5.dp) // Bottom padding
                )


                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = { selectedTipPercentage = 10 },
                        modifier = Modifier.width(60.dp),
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFe0e0e0)) // Light gray color
                    ) {
                        Text(text = "10%", color = Color.Black, fontSize = 10.sp)
                    }

                    Button(
                        onClick = { selectedTipPercentage = 15 },
                        modifier = Modifier.width(60.dp),
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFe0e0e0)) // Light gray color
                    ) {
                        Text(text = "15%", color = Color.Black, fontSize = 10.sp)
                    }

                    Button(
                        onClick = { selectedTipPercentage = 20 },
                        modifier = Modifier.width(60.dp),
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFe0e0e0)) // Light gray color
                    ) {
                        Text(text = "20%", color = Color.Black, fontSize = 10.sp)
                    }

                    Button(
                        onClick = { navHostController.navigate(AppNavigationItems.TipScreen.route) },
                        modifier = Modifier.width(80.dp),
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFe0e0e0)) // Light gray color
                    ) {
                        Text(text = "Custom", color = Color.Black, fontSize = 10.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 25.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = {
                    // Passing totalAmount as a route argument
                    navHostController.navigate(AppNavigationItems.CardScreen.createRoute(formatAmount(totalAmount)))
                },
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.White) // White color
            ) {
                Text(text = "Confirm", color = Color.Black)
            }

            Button(
                onClick = { navHostController.popBackStack() },
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.White)
            ) {
                Text(text = "Cancel", color = Color.Black)
            }
        }
    }
}


fun calculateTax(amount: Double): Double {
    return amount * 0.15
}


fun calculateTip(amount: Double, tipPercentage: Int): Double {
    return amount * (tipPercentage / 100.0)
}


fun calculateTotalAmount(transactionAmount: Double, tipAmount: Double, sgstAmount: Double, igstAmount: Double): Double {
    return transactionAmount + tipAmount + sgstAmount + igstAmount
}


fun formatAmount(amount: Double): String {
    val decimalFormat = DecimalFormat("#.00")
    return decimalFormat.format(amount)
}
