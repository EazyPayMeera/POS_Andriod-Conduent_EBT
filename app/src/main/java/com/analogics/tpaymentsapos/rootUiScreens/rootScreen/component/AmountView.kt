// AmountView.kt
package com.analogics.tpaymentsapos.rootUiScreens.login

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.analogics.tpaymentsapos.R
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.Authorisation
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.CommonTopAppBar
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.CustomSurface
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.TransactionState
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.createAmountTransformation
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.formatAmount
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.getFormattedDateTime

@Composable
fun AmountView(navHostController: NavHostController) {
    var rawInput by remember { mutableStateOf("") }
    var formattedAmount by remember { mutableStateOf("0.00") }
    val transactionDateTime = getFormattedDateTime()

    val isRefund = TransactionState.isRefund
    val isVoid = TransactionState.isVoid
    val isPreauth = TransactionState.isPreauth
    val isAuthcap = Authorisation.isAuthcap

    Log.d("InvoiceView", "isAuthcap: $isAuthcap")

    Column {
        CommonTopAppBar(
            title = if (isRefund) "Refund" else "Purchase",
            onBackButtonClick = { navHostController.popBackStack() }
        )

        CustomSurface(
            imageResourceId = R.drawable.card, // Pass the SVG resource ID
            titleText = if (isRefund) "Enter the Refund Amount" else if(isPreauth) "Enter the authorisation amount" else "Enter the Transaction Amount",
            label = if (isRefund) "Refund Amount" else if(isPreauth) "Authorisation amount" else "Amount",
            placeholder = if (isRefund) "Enter refund amount" else if(isPreauth) "Enter authorisation amount" else "Enter amount",
            value = rawInput,
            onValueChange = { newValue ->
                if (newValue.all { char -> char.isDigit() }) {
                    rawInput = newValue
                    formattedAmount = formatAmount(newValue)
                }
            },
            onDoneAction = {
                if(isRefund || isPreauth) {
                    navHostController.navigate(AppNavigationItems.CardScreen.createRoute(formattedAmount))
                }
                else if (isVoid || isAuthcap)
                {
                    navHostController.navigate(AppNavigationItems.PleaseWaitScreen.route)
                }
                else
                {
                    navHostController.navigate(AppNavigationItems.ConfirmationScreen.createRoute(formattedAmount))
                }
            },
            isRefund = isRefund,
            isVoid = isVoid,
            isAuthcap = isAuthcap,
            keyboardType = KeyboardType.Number,
            visualTransformation = createAmountTransformation()
        ) {
            if (isRefund) {
                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Original Amount : $formattedAmount",
                    fontSize = 15.sp,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(bottom = 10.dp)
                        .align(Alignment.CenterHorizontally)
                )

                Text(
                    text = "Txn Date: $transactionDateTime",
                    fontSize = 15.sp,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(bottom = 10.dp)
                        .align(Alignment.CenterHorizontally)
                )
            }

            if (isVoid || isAuthcap) {
                Log.d("InvoiceView", "Inside if condition, showing All the text")
                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Txn Date: $transactionDateTime",
                    fontSize = 15.sp,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(bottom = 10.dp)
                        .align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(50.dp))
                Text(
                    text = "Card:",
                    fontSize = 20.sp,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(bottom = 10.dp)
                        .align(Alignment.Start)
                )

                Text(
                    text = "Auth Code:",
                    fontSize = 20.sp,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(bottom = 10.dp)
                        .align(Alignment.Start)
                )

                Text(
                    text = "No.:",
                    fontSize = 20.sp,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(bottom = 10.dp)
                        .align(Alignment.Start)
                )

                Text(
                    text = "Invoice Number:",
                    fontSize = 20.sp,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(bottom = 10.dp)
                        .align(Alignment.Start)
                )

                Text(
                    text = "POS Entry:",
                    fontSize = 20.sp,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(bottom = 10.dp)
                        .align(Alignment.Start)
                )
            }
        }
    }
}

