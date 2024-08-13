// AmountView.kt
package com.analogics.tpaymentsapos.rootUiScreens.login

import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.analogics.tpaymentsapos.R
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.Appbarheader
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.Authorisation
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.CommonTopAppBar
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.CustomSurface
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

        CustomSurface(
            imageResourceId = R.drawable.card, // Pass the SVG resource ID
            titleText = when {
                isRefund -> stringResource(R.string.refund_amt)
                isPreauth -> stringResource(R.string.auth_amt)
                else -> stringResource(R.string.purchase_amt)
            },
            label = "",
            placeholder = when {
                isRefund -> stringResource(R.string.refund_amt)
                isPreauth -> stringResource(R.string.auth_amt)
                else -> stringResource(R.string.purchase_amt)
            },
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
                Spacer(modifier = Modifier.height(MaterialTheme.dimens.DP_10_CompactMedium))

                Text(
                    text = "${stringResource(id = R.string.original_amount)} $formattedAmount",
                    fontSize = MaterialTheme.dimens.SP_18_CompactMedium,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(bottom = MaterialTheme.dimens.DP_10_CompactMedium)
                        .align(Alignment.CenterHorizontally)
                )

                Text(
                    text = "${stringResource(id = R.string.date)} $transactionDateTime",
                    fontSize = MaterialTheme.dimens.SP_18_CompactMedium,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(bottom = MaterialTheme.dimens.DP_10_CompactMedium)
                        .align(Alignment.CenterHorizontally)
                )
            }

            if (isVoid || isAuthcap) {
                Spacer(modifier = Modifier.height(MaterialTheme.dimens.DP_10_CompactMedium))

                Text(
                    text = "${stringResource(id = R.string.date)} $transactionDateTime",
                    fontSize = MaterialTheme.dimens.SP_18_CompactMedium,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(bottom = MaterialTheme.dimens.DP_10_CompactMedium)
                        .align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(MaterialTheme.dimens.DP_50_CompactMedium))
                Text(
                    text = stringResource(id = R.string.card),
                    fontSize = MaterialTheme.dimens.SP_23_CompactMedium,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(bottom = MaterialTheme.dimens.DP_10_CompactMedium)
                        .align(Alignment.Start)
                )

                Text(
                    text = stringResource(id = R.string.auth_code),
                    fontSize = MaterialTheme.dimens.SP_23_CompactMedium,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(bottom = MaterialTheme.dimens.DP_10_CompactMedium)
                        .align(Alignment.Start)
                )

                Text(
                    text = stringResource(id = R.string.no),
                    fontSize = MaterialTheme.dimens.SP_23_CompactMedium,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(bottom = MaterialTheme.dimens.DP_10_CompactMedium)
                        .align(Alignment.Start)
                )

                Text(
                    text = stringResource(id = R.string.inc_no),
                    fontSize = MaterialTheme.dimens.SP_23_CompactMedium,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(bottom = MaterialTheme.dimens.DP_10_CompactMedium)
                        .align(Alignment.Start)
                )
                Text(
                    text = stringResource(id = R.string.pos_entry),
                    fontSize = MaterialTheme.dimens.SP_23_CompactMedium,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(bottom = MaterialTheme.dimens.DP_10_CompactMedium)
                        .align(Alignment.Start)
                )
            }
        }
    }
}

