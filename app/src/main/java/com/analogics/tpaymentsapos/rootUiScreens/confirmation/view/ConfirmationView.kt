// ConfirmationView.kt

package com.analogics.tpaymentsapos.rootUiScreens.confirmation.view

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
import androidx.navigation.NavHostController
import com.analogics.tpaymentsapos.R
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.Authorisation
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.CommonTopAppBar
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.FooterButtons
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.GenericCard
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.TextView
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.TransactionState
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.calculateTax
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.calculateTip
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.calculateTotalAmount
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.formatAmountdouble
import com.analogics.tpaymentsapos.ui.theme.dimens

@Composable
fun ConfirmationView(navHostController: NavHostController, amount: String) {
    var selectedTipPercentage by remember { mutableStateOf(0) }
    val amountDouble = amount.toDoubleOrNull() ?: 0.0
    var selectedTip by remember { mutableStateOf("10%") }

    val sgstAmount = calculateTax(amountDouble)
    val igstAmount = calculateTax(amountDouble)
    var isTipEnabled by remember { mutableStateOf(false) }
    val tipAmount = calculateTip(amountDouble, selectedTipPercentage)

    val isRefund = TransactionState.isRefund
    val isVoid = TransactionState.isVoid
    val isPreauth = TransactionState.isPreauth
    val isAuthcap = Authorisation.isAuthcap

    val totalAmount = calculateTotalAmount(amountDouble, tipAmount, sgstAmount, igstAmount)

    var isTaxesEnabled by remember { mutableStateOf(false) }


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
        GenericCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = MaterialTheme.dimens.DP_24_CompactMedium,
                    end = MaterialTheme.dimens.DP_24_CompactMedium,
                    top = MaterialTheme.dimens.DP_10_CompactMedium, // Reduced top padding
                    bottom = MaterialTheme.dimens.DP_10_CompactMedium
                ),
            backgroundColor = Color(0xFFFFA500), // Replace with any color you want
            shape = RoundedCornerShape(MaterialTheme.dimens.DP_18_CompactMedium),
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(MaterialTheme.dimens.DP_30_CompactMedium)
            ) {
                TextView(
                    text = stringResource(id = R.string.total_amt),
                    fontSize = MaterialTheme.dimens.SP_23_CompactMedium,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(bottom = MaterialTheme.dimens.DP_11_CompactMedium)
                        .align(Alignment.Start)
                )
                TextView(
                    text = "₹${formatAmountdouble(totalAmount)}",
                    fontSize = MaterialTheme.dimens.SP_27_CompactMedium,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Start)
                )
            }
        }

        GenericCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = MaterialTheme.dimens.DP_24_CompactMedium,
                    end = MaterialTheme.dimens.DP_24_CompactMedium, // Reduced top padding
                    bottom = MaterialTheme.dimens.DP_10_CompactMedium
                ),
            shape = RoundedCornerShape(MaterialTheme.dimens.DP_18_CompactMedium),
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(MaterialTheme.dimens.DP_11_CompactMedium)
            ) {

                TextView(
                    text = stringResource(id = R.string.txn_sum),
                    fontSize = MaterialTheme.dimens.SP_17_CompactMedium,
                    color = Color(0xFFFFA500),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(bottom = MaterialTheme.dimens.DP_5_CompactMedium)
                        .align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(MaterialTheme.dimens.DP_11_CompactMedium))
                TextView(
                    text = "${stringResource(id = R.string.tnx_amount)}₹${
                        formatAmountdouble(
                            amountDouble
                        )
                    }",
                    fontSize = MaterialTheme.dimens.SP_17_CompactMedium,
                    color = Color.Gray,
                    modifier = Modifier
                        .padding(bottom = MaterialTheme.dimens.DP_5_CompactMedium)
                        .align(Alignment.Start)
                )

                TextView(
                    text = "${stringResource(id = R.string.tip_amt)}₹${formatAmountdouble(tipAmount)}",
                    fontSize = MaterialTheme.dimens.SP_17_CompactMedium,
                    color = Color.Gray,
                    modifier = Modifier
                        .padding(bottom = MaterialTheme.dimens.DP_5_CompactMedium)
                        .align(Alignment.Start)
                )
                TextView(
                    text = "${stringResource(id = R.string.sgst_amt)}₹${
                        formatAmountdouble(
                            sgstAmount
                        )
                    }",
                    fontSize = MaterialTheme.dimens.SP_17_CompactMedium,
                    color = Color.Gray,
                    modifier = Modifier
                        .padding(bottom = MaterialTheme.dimens.DP_5_CompactMedium)
                        .align(Alignment.Start)
                )
                TextView(
                    text = "${stringResource(id = R.string.igst_amt)}₹${
                        formatAmountdouble(
                            igstAmount
                        )
                    }",
                    fontSize = MaterialTheme.dimens.SP_17_CompactMedium,
                    color = Color.Gray,
                    modifier = Modifier
                        .padding(bottom = MaterialTheme.dimens.DP_5_CompactMedium)
                        .align(Alignment.Start)
                )

            }
        }



        Column {
            GenericCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = MaterialTheme.dimens.DP_24_CompactMedium,
                        end = MaterialTheme.dimens.DP_24_CompactMedium,
                        top = MaterialTheme.dimens.DP_4_CompactMedium, // Reduced top padding
                        bottom = MaterialTheme.dimens.DP_24_CompactMedium
                    ),
                elevation = MaterialTheme.dimens.DP_10_CompactMedium,
                shape = RoundedCornerShape(MaterialTheme.dimens.DP_18_CompactMedium),
            ) {
                Column(
                    modifier = Modifier
                        .padding(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextView(
                            text = "Add Tip?",
                            fontSize = MaterialTheme.dimens.SP_20_CompactMedium,
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = MaterialTheme.dimens.DP_4_CompactMedium)
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Switch(
                            checked = isTipEnabled,
                            onCheckedChange = { isTipEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFFFFA000), // Orange color
                                uncheckedThumbColor = Color.Gray
                            )
                        )
                    }

                    if (isTipEnabled) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            TipOptionButton("10%", selectedTip) { selectedTip = it }
                            TipOptionButton("15%", selectedTip) { selectedTip = it }
                            TipOptionButton("20%", selectedTip) { selectedTip = it }
                            TipOptionButton("Custom Tip", selectedTip) { selectedTip = it }
                        }
                    }
                }
            }
        }

        FooterButtons(
            firstButtonTitle = stringResource(id = R.string.cancel_btn),
            firstButtonOnClick = { navHostController.navigate(AppNavigationItems.TrainingScreen.route) },
            secondButtonTitle = stringResource(id = R.string.confirm_btn),
            secondButtonOnClick = {
                navHostController.navigate(
                    AppNavigationItems.CardScreen.createRoute(
                        formatAmountdouble(totalAmount)
                    )
                )
            }
        )
    }


}

@Composable
fun TipOptionButton(tip: String, selectedTip: String, onSelect: (String) -> Unit) {
    Button(
        onClick = { onSelect(tip) },
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = if (tip == selectedTip) Color(0xFFFFA000) else Color(0xFFF0F0F0),
            contentColor = Color.Black
        ),
        modifier = Modifier.padding(4.dp)
    ) {
        Text(text = tip)
    }
}

