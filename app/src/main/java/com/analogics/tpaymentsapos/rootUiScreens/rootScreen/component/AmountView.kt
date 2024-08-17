// AmountView.kt
package com.analogics.tpaymentsapos.rootUiScreens.login

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.analogics.tpaymentsapos.R
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.Appbarheader
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.Authorisation
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.FooterButtons
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.GenericCard
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.Image
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.OutlinedTextField
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.TextView
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.TransactionState
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.createAmountTransformation
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.formatAmount
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.getFormattedDateTime
import com.analogics.tpaymentsapos.ui.theme.dimens

@Composable
fun AmountView(navHostController: NavHostController) {
    var rawInput by remember { mutableStateOf("") }
    var formattedAmount by remember { mutableStateOf("0.00") }
    val transactionDateTime = getFormattedDateTime()

    val isRefund = TransactionState.isRefund
    val isVoid = TransactionState.isVoid
    val isPreauth = TransactionState.isPreauth
    val isAuthcap = Authorisation.isAuthcap


    Column {
        Appbarheader(
            title = when {
                isRefund -> stringResource(R.string.refund)
                isVoid -> stringResource(R.string.void_trans)
                isPreauth -> stringResource(R.string.pre_auth)
                else -> stringResource(R.string.purchase)
            },
            onBackButtonClick = { navHostController.popBackStack() },
            onIcon1Click = { navHostController.popBackStack() },
            icon2 = Icons.Default.Close,
            onIcon2Click = { navHostController.navigate(AppNavigationItems.TrainingScreen.route) }
        )

        GenericCard(
            modifier = Modifier.padding(20.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(14.dp)
            ) {
                TextView(
                    text = stringResource(id = R.string.auth_amt),
                    fontSize = MaterialTheme.dimens.SP_17_CompactMedium,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    1,
                    Modifier.padding(16.dp),
                    textAlign = TextAlign.Center
                )
                Image(
                    imageId = R.drawable.card, size = 60.dp,
                    shape = RectangleShape, // Example shape, can be any Shape
                    alignment = Alignment.Center,
                )

                OutlinedTextField(
                    value = rawInput,
                    onValueChange = { newValue ->
                        // Update rawInput and formattedAmount only if the new value is valid
                        if (newValue.all { char -> char.isDigit() || char == '.' }) {
                            rawInput = newValue
                            formattedAmount = formatAmount(newValue)
                        }
                    },
                    placeholder = stringResource(id = R.string.auth_amt),
                    textStyle = TextStyle(fontWeight = FontWeight.Bold, fontSize = 18.sp),
                    keyboardType = KeyboardType.Number,
                    onDoneAction = {

                    },
                    visualTransformation = createAmountTransformation(),
                    isPassword = true
                ) // Set this to true for password fields)

            }

        }

        FooterButtons(
            firstButtonTitle = stringResource(id = R.string.confirm_btn),
            firstButtonOnClick = { if(isRefund || isPreauth) {
                navHostController.navigate(AppNavigationItems.CardScreen.createRoute(formattedAmount))
            }
            else if (isVoid || isAuthcap)
            {
                navHostController.navigate(AppNavigationItems.PleaseWaitScreen.route)
            }
            else
            {
                navHostController.navigate(AppNavigationItems.ConfirmationScreen.createRoute(formattedAmount))
            } },
            secondButtonTitle = stringResource(id = R.string.cancel_btn),
            secondButtonOnClick = { navHostController.navigate(AppNavigationItems.TrainingScreen.route) }
        )

    }
}

