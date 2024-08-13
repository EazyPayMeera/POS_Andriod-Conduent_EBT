// ConfirmationView.kt

package com.analogics.tpaymentsapos.rootUiScreens.login

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.analogics.tpaymentsapos.R
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.Authorisation
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.CommonTopAppBar
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.ConfirmationButton
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.TransactionState
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.calculateTax
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.calculateTip
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.calculateTotalAmount
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.formatAmount
import com.analogics.tpaymentsapos.ui.theme.dimens

@Composable
fun ConfirmationView(navHostController: NavHostController, amount: String) {
    var selectedTipPercentage by remember { mutableStateOf(0) }
    val amountDouble = amount.toDoubleOrNull() ?: 0.0

    val sgstAmount = calculateTax(amountDouble)
    val igstAmount = calculateTax(amountDouble)
    val tipAmount = calculateTip(amountDouble, selectedTipPercentage)

    val isRefund = TransactionState.isRefund
    val isVoid = TransactionState.isVoid
    val isPreauth = TransactionState.isPreauth
    val isAuthcap = Authorisation.isAuthcap

    val totalAmount = calculateTotalAmount(amountDouble, tipAmount, sgstAmount, igstAmount)

    Column {
        CommonTopAppBar(
            title = when {
                isRefund -> stringResource(R.string.refund)
                isVoid -> stringResource(R.string.void_trans)
                isPreauth -> stringResource(R.string.pre_auth)
                else -> stringResource(R.string.purchase)
            },
            onBackButtonClick = { navHostController.popBackStack() }
        )

        Surface(
            color = Color(0xFFFFA500),
            modifier = Modifier
                .padding(MaterialTheme.dimens.DP_24_CompactMedium)
                .fillMaxWidth()
                .height(MaterialTheme.dimens.DP_120_CompactMedium),
            shape = RoundedCornerShape(MaterialTheme.dimens.DP_18_CompactMedium),
            elevation = MaterialTheme.dimens.DP_20_CompactMedium
        ) {
            Column(
                modifier = Modifier
                    .padding(MaterialTheme.dimens.DP_24_CompactMedium)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = stringResource(id = R.string.total_amt),
                    fontSize = MaterialTheme.dimens.SP_23_CompactMedium,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(bottom = MaterialTheme.dimens.DP_11_CompactMedium)
                )
                Text(
                    text = "₹${formatAmount(totalAmount)}",
                    fontSize = MaterialTheme.dimens.SP_27_CompactMedium,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Surface(
            color = Color.White,
            modifier = Modifier
                .padding(horizontal = MaterialTheme.dimens.DP_24_CompactMedium, vertical = MaterialTheme.dimens.DP_20_CompactMedium)
                .fillMaxWidth()
                .height(MaterialTheme.dimens.DP_170_CompactMedium),
            shape = RoundedCornerShape(MaterialTheme.dimens.DP_18_CompactMedium),
            elevation = MaterialTheme.dimens.DP_20_CompactMedium
        ) {
            Column(
                modifier = Modifier
                    .padding(MaterialTheme.dimens.DP_24_CompactMedium)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = stringResource(id = R.string.txn_sum),
                    fontSize = MaterialTheme.dimens.SP_19_CompactMedium,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(bottom = MaterialTheme.dimens.DP_11_CompactMedium)
                )

                Text(
                    text = "${stringResource(id = R.string.tnx_amount)} ₹${formatAmount(amountDouble)}",
                    fontSize = MaterialTheme.dimens.SP_17_CompactMedium,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = MaterialTheme.dimens.DP_5_CompactMedium)
                )
                Text(
                    text = "${stringResource(id = R.string.tip_amt)} ₹${formatAmount(tipAmount)}",
                    fontSize = MaterialTheme.dimens.SP_17_CompactMedium,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = MaterialTheme.dimens.DP_5_CompactMedium)
                )
                Text(
                    text = "${stringResource(id = R.string.sgst_amt)} ₹${formatAmount(sgstAmount)}",
                    fontSize = MaterialTheme.dimens.SP_17_CompactMedium,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = MaterialTheme.dimens.DP_5_CompactMedium)
                )
                Text(
                    text = "${stringResource(id = R.string.igst_amt)} ₹${formatAmount(igstAmount)}",
                    fontSize = MaterialTheme.dimens.SP_17_CompactMedium,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = MaterialTheme.dimens.DP_5_CompactMedium)
                )
            }
        }



        Surface(
            color = Color.White,
            modifier = Modifier
                .padding(horizontal = MaterialTheme.dimens.DP_24_CompactMedium, vertical = MaterialTheme.dimens.DP_20_CompactMedium)
                .fillMaxWidth()
                .height(MaterialTheme.dimens.DP_110_CompactMedium),
            shape = RoundedCornerShape(MaterialTheme.dimens.DP_18_CompactMedium),
            elevation = MaterialTheme.dimens.DP_20_CompactMedium
        ) {
            Column(
                modifier = Modifier
                    .padding(MaterialTheme.dimens.DP_24_CompactMedium)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = stringResource(id = R.string.add_tip),
                    fontSize = MaterialTheme.dimens.SP_23_CompactMedium,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(bottom = MaterialTheme.dimens.DP_5_CompactMedium) // Bottom padding
                )


                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = MaterialTheme.dimens.DP_11_CompactMedium),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = { selectedTipPercentage = 10 },
                        modifier = Modifier.width(MaterialTheme.dimens.DP_60_CompactMedium),
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFe0e0e0)) // Light gray color
                    ) {
                        Text(text = stringResource(id = R.string.ten), color = Color.Black, fontSize = MaterialTheme.dimens.SP_13_CompactMedium)
                    }

                    Button(
                        onClick = { selectedTipPercentage = 15 },
                        modifier = Modifier.width(MaterialTheme.dimens.DP_60_CompactMedium),
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFe0e0e0)) // Light gray color
                    ) {
                        Text(text = stringResource(id = R.string.fifteen), color = Color.Black, fontSize = MaterialTheme.dimens.SP_13_CompactMedium)
                    }

                    Button(
                        onClick = { selectedTipPercentage = 20 },
                        modifier = Modifier.width(MaterialTheme.dimens.DP_60_CompactMedium),
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFe0e0e0)) // Light gray color
                    ) {
                        Text(text = stringResource(id = R.string.twenty), color = Color.Black, fontSize = MaterialTheme.dimens.SP_13_CompactMedium)
                    }

                    Button(
                        onClick = { navHostController.navigate(AppNavigationItems.TipScreen.route) },
                        modifier = Modifier.width(MaterialTheme.dimens.DP_100_CompactMedium),
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFe0e0e0)) // Light gray color
                    ) {
                        Text(text = stringResource(id = R.string.custom), color = Color.Black, fontSize = MaterialTheme.dimens.SP_13_CompactMedium)
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = MaterialTheme.dimens.DP_24_CompactMedium), // Adjust padding as needed
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ConfirmationButton(
                onClick = { navHostController.navigate(AppNavigationItems.CardScreen.createRoute(totalAmount.toString())) },
                title = stringResource(id = R.string.confirm_btn)
            )
            ConfirmationButton(
                onClick = { /* Your second button action */ },
                title = stringResource(id = R.string.cancel_btn)
            )
        }

    }


}
