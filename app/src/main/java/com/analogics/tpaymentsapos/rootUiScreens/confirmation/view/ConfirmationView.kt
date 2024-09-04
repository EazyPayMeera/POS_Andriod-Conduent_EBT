// ConfirmationView.kt

package com.analogics.tpaymentsapos.rootUiScreens.confirmation.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Switch
import androidx.compose.material.SwitchDefaults
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavHostController
import com.analogics.tpaymentsapos.R
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
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
                    top = MaterialTheme.dimens.DP_24_CompactMedium, // Reduced top padding
                    bottom = MaterialTheme.dimens.DP_5_CompactMedium
                ),
            backgroundColor = colorResource(id = R.color.purple_200), // Replace with any color you want
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

        TransactionSummaryCard(amountDouble,tipAmount,sgstAmount,igstAmount)

        Column {
            GenericCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = MaterialTheme.dimens.DP_24_CompactMedium,
                        end = MaterialTheme.dimens.DP_24_CompactMedium,
                        top = MaterialTheme.dimens.DP_4_CompactMedium, // Reduced top padding
                        bottom = MaterialTheme.dimens.DP_10_CompactMedium
                    ),
                elevation = MaterialTheme.dimens.DP_10_CompactMedium,
                shape = RoundedCornerShape(MaterialTheme.dimens.DP_18_CompactMedium),
            ) {
                Column(
                    modifier = Modifier
                        .padding(MaterialTheme.dimens.DP_20_CompactMedium)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextView(
                            text = stringResource(id = R.string.add_tip),
                            fontSize = MaterialTheme.dimens.SP_18_CompactMedium,
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = MaterialTheme.dimens.DP_20_CompactMedium)
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
                        Spacer(modifier = Modifier.height(MaterialTheme.dimens.DP_4_CompactMedium))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            TipOptionButton("10%", selectedTip) { selectedTip = it }
                            TipOptionButton("15%", selectedTip) { selectedTip = it }
                            TipOptionButton("20%", selectedTip) { selectedTip = it }
                            TipOptionButton("Custom", selectedTip) { selectedTip = it }
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
        shape = RoundedCornerShape(MaterialTheme.dimens.DP_21_CompactMedium),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = if (tip == selectedTip) colorResource(id = R.color.white) else colorResource(id = R.color.purple_200),
            contentColor = Color.Black
        ),
        modifier = Modifier.padding(MaterialTheme.dimens.DP_4_CompactMedium)
    ) {
        Text(text = tip)
    }
}

@Composable
fun TransactionSummaryCard(
    amountDouble: Double,
    tipAmount: Double,
    sgstAmount: Double,
    igstAmount: Double
) {
    GenericCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = MaterialTheme.dimens.DP_24_CompactMedium,
                end = MaterialTheme.dimens.DP_24_CompactMedium,
                bottom = MaterialTheme.dimens.DP_10_CompactMedium
            ),
        shape = RoundedCornerShape(MaterialTheme.dimens.DP_18_CompactMedium),
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start,
            modifier = Modifier.padding(MaterialTheme.dimens.DP_11_CompactMedium)
        ) {
            // Transaction Summary Title
            TextView(
                text = stringResource(id = R.string.txn_sum),
                fontSize = MaterialTheme.dimens.SP_17_CompactMedium,
                color = colorResource(id = R.color.purple_200),
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(bottom = MaterialTheme.dimens.DP_5_CompactMedium)
            )

            Spacer(modifier = Modifier.height(MaterialTheme.dimens.DP_11_CompactMedium))

            // Transaction Amount
            TransactionSummaryItem(
                label = stringResource(id = R.string.tnx_amount),
                amount = amountDouble
            )

            // Tip Amount
            TransactionSummaryItem(
                label = stringResource(id = R.string.tip_amt),
                amount = tipAmount
            )

            // SGST Amount
            TransactionSummaryItem(
                label = stringResource(id = R.string.sgst_amt),
                amount = sgstAmount
            )

            // IGST Amount
            TransactionSummaryItem(
                label = stringResource(id = R.string.igst_amt),
                amount = igstAmount
            )
        }
    }
}

@Composable
fun TransactionSummaryItem(
    label: String,
    amount: Double
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = MaterialTheme.dimens.DP_5_CompactMedium),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextView(
            text = label,
            fontSize = MaterialTheme.dimens.SP_17_CompactMedium,
            color = Color.Gray,
            modifier = Modifier.align(Alignment.CenterVertically)
        )

        TextView(
            text = "₹${formatAmountdouble(amount)}",
            fontSize = MaterialTheme.dimens.SP_17_CompactMedium,
            color = Color.Gray,
            modifier = Modifier.align(Alignment.CenterVertically)
        )
    }
}
