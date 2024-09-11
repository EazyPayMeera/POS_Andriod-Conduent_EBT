// ConfirmationView.kt

package com.analogics.tpaymentsapos.rootUiScreens.confirmation.view

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.material3.Icon
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavHostController
import com.analogics.tpaymentsapos.R
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import com.analogics.tpaymentsapos.rootUiScreens.tip.viewmodel.updated_tip
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.CommonTopAppBar
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.CustomSwitch
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.FooterButtons
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.GenericCard
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.TextView
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.calculateTax
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.calculateTip
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.calculateTotalAmount
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.formatAmount
import com.analogics.tpaymentsapos.ui.theme.dashboardOrangeColor
import com.analogics.tpaymentsapos.ui.theme.dimens
import com.analogics.tpaymentsapos.ui.theme.tipBColor


@Composable
fun ConfirmationView(navHostController: NavHostController, amount: String) {

    val updated_tip = updated_tip
    val transAmount = formatAmount(amount, withSymbol = false, withSeparator = false)
    var selectedTipPercentage by remember { mutableStateOf(0.0) }
    val amountDouble = transAmount.toDoubleOrNull() ?: 0.0
    var selectedTip by remember { mutableStateOf("") }

    val sgstAmount = calculateTax(amountDouble)
    val igstAmount = calculateTax(amountDouble)
    var isTipEnabled by remember { mutableStateOf(false) }

    val calculatedTip = calculateTip(amountDouble, selectedTipPercentage / 100)
    val tipAmount = if (calculatedTip != 0.0) calculatedTip else updated_tip
    val totalAmount = calculateTotalAmount(amountDouble, tipAmount, sgstAmount, igstAmount)
    var isTaxesEnabled by remember { mutableStateOf(false) }

    Log.d("TipChange", "Updated TipAmt: $updated_tip")

    Column {
        CommonTopAppBar(
            onBackButtonClick = { navHostController.popBackStack() }
        )
        GenericCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = MaterialTheme.dimens.DP_24_CompactMedium,
                    end = MaterialTheme.dimens.DP_24_CompactMedium,
                    top = MaterialTheme.dimens.DP_24_CompactMedium,
                    bottom = MaterialTheme.dimens.DP_5_CompactMedium
                ),
            backgroundColor = colorResource(id = R.color.purple_200),
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
                    text = formatAmount(totalAmount),
                    fontSize = MaterialTheme.dimens.SP_35_CompactMedium,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Start),
                )
            }
        }

        TransactionSummaryCard(amountDouble,tipAmount,sgstAmount,igstAmount)

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
                    CustomSwitch(
                        checked = isTipEnabled,
                        onCheckedChange = { isTipEnabled = it },
                        checkedImage = R.drawable.switch_checked,
                        uncheckedImage = R.drawable.switch_unchecked,
                    )
                }

                Spacer(modifier = Modifier.height(MaterialTheme.dimens.DP_4_CompactMedium))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly

                ) {
                    Button(
                        onClick = { selectedTip = "10%"; selectedTipPercentage = 10.0 },
                        enabled = isTipEnabled,
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = if (selectedTip == stringResource(id = R.string.ten) && isTipEnabled) {
                                dashboardOrangeColor
                            } else {
                                tipBColor.copy(alpha = if (isTipEnabled) 1f else 0.5f)
                            },
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(MaterialTheme.dimens.DP_15_CompactMedium),
                        modifier = Modifier
                            .padding(horizontal = MaterialTheme.dimens.DP_4_CompactMedium),
                        elevation = ButtonDefaults.elevation(
                            defaultElevation = MaterialTheme.dimens.DP_20_CompactMedium,
                            pressedElevation = MaterialTheme.dimens.DP_20_CompactMedium, // Adjust pressed elevation based on isTipEnabled
                            disabledElevation = MaterialTheme.dimens.DP_20_CompactMedium
                        )
                    ) {
                        Text(text = stringResource(id = R.string.ten))
                    }

                    Button(
                        onClick = { selectedTip = "15%"; selectedTipPercentage = 15.0  },
                        enabled = isTipEnabled,
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = if (selectedTip == "15%" && isTipEnabled) {
                                dashboardOrangeColor
                            } else {
                                tipBColor.copy(alpha = if (isTipEnabled) 1f else 0.5f)
                            },
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(MaterialTheme.dimens.DP_15_CompactMedium),
                        modifier = Modifier
                            .padding(horizontal = MaterialTheme.dimens.DP_4_CompactMedium),
                        elevation = ButtonDefaults.elevation(
                            defaultElevation = MaterialTheme.dimens.DP_20_CompactMedium,
                            pressedElevation = MaterialTheme.dimens.DP_20_CompactMedium, // Adjust pressed elevation based on isTipEnabled
                            disabledElevation = MaterialTheme.dimens.DP_20_CompactMedium
                        )

                    ) {
                        Text(text = stringResource(id = R.string.fifteen))
                    }

                    Button(
                        onClick = { selectedTip = "20%"; selectedTipPercentage = 20.0  },
                        enabled = isTipEnabled,
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = if (selectedTip == stringResource(id = R.string.twenty) && isTipEnabled) {
                                dashboardOrangeColor
                            } else {
                                tipBColor.copy(alpha = if (isTipEnabled) 1f else 0.5f)
                            },
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(MaterialTheme.dimens.DP_15_CompactMedium),
                        modifier = Modifier
                            .padding(horizontal = MaterialTheme.dimens.DP_4_CompactMedium),
                        elevation = ButtonDefaults.elevation(
                            defaultElevation = MaterialTheme.dimens.DP_20_CompactMedium,
                            pressedElevation = MaterialTheme.dimens.DP_20_CompactMedium, // Adjust pressed elevation based on isTipEnabled
                            disabledElevation = MaterialTheme.dimens.DP_20_CompactMedium
                        )
                    ) {
                        Text(text = stringResource(id = R.string.twenty))
                    }

                    Button(
                        onClick = { navHostController.navigate(AppNavigationItems.TipScreen.route); selectedTip = "Custom" },
                        enabled = isTipEnabled,
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = if (selectedTip == stringResource(id = R.string.custom) && isTipEnabled) {
                                dashboardOrangeColor
                            } else {
                                tipBColor.copy(alpha = if (isTipEnabled) 1f else 0.5f)
                            },
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(MaterialTheme.dimens.DP_15_CompactMedium),
                        //modifier = Modifier.padding(horizontal = MaterialTheme.dimens.DP_4_CompactMedium),
                        modifier = Modifier
                            .padding(horizontal = MaterialTheme.dimens.DP_4_CompactMedium),
                        elevation = ButtonDefaults.elevation(
                            defaultElevation = MaterialTheme.dimens.DP_20_CompactMedium,
                            pressedElevation = MaterialTheme.dimens.DP_20_CompactMedium, // Adjust pressed elevation based on isTipEnabled
                            disabledElevation = MaterialTheme.dimens.DP_20_CompactMedium
                        )
                    ) {
                        Text(text = stringResource(id = R.string.custom))
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
                        formatAmount(totalAmount)
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
            backgroundColor = if (tip == selectedTip) colorResource(id = R.color.purple_200) else colorResource(id = R.color.white),
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
        // Add the bullet icon
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.orange_bullet), // Replace with your orange bullet drawable resource
                contentDescription = null,
                tint = dashboardOrangeColor, // Adjust the color to match the bullet color
                modifier = Modifier
                    .size(MaterialTheme.dimens.DP_25_CompactMedium) // Adjust size as needed
                    .padding(end = MaterialTheme.dimens.DP_10_CompactMedium) // Spacing between bullet and text
            )

            TextView(
                text = label,
                fontSize = MaterialTheme.dimens.SP_17_CompactMedium,
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.CenterVertically)
            )
        }

        TextView(
            text = formatAmount(amount),
            fontSize = MaterialTheme.dimens.SP_17_CompactMedium,
            color = Color.Black,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterVertically)
        )
    }
}


