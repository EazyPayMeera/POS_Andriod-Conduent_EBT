// ConfirmationView.kt

package com.analogics.tpaymentsapos.rootUiScreens.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.analogics.tpaymentsapos.R
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import com.analogics.tpaymentsapos.rootUiScreens.settings.config.ConfigurableViewType
import com.analogics.tpaymentsapos.rootUiScreens.settings.config.SettingsItem
import com.analogics.tpaymentsapos.rootUiScreens.settings.config.SettingsSurface
import com.analogics.tpaymentsapos.rootUiScreens.settings.config.TippingView
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.CommonTopAppBar
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.FooterButtons
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.GenericCard
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.TextView
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
                .padding(MaterialTheme.dimens.DP_24_CompactMedium),
            backgroundColor = Color(0xFFFFA500), // Replace with any color you want
            shape = RoundedCornerShape(MaterialTheme.dimens.DP_18_CompactMedium),
        ){
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
                    text = "₹${formatAmount(totalAmount)}",
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
                .padding(start = MaterialTheme.dimens.DP_24_CompactMedium,
                    end = MaterialTheme.dimens.DP_24_CompactMedium,
                    top = MaterialTheme.dimens.DP_4_CompactMedium, // Reduced top padding
                    bottom = MaterialTheme.dimens.DP_24_CompactMedium),
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
                    text = "${stringResource(id = R.string.tnx_amount)} .........................₹${formatAmount(amountDouble)}",
                    fontSize = MaterialTheme.dimens.SP_17_CompactMedium,
                    color = Color.Gray,
                    modifier = Modifier
                        .padding(bottom = MaterialTheme.dimens.DP_5_CompactMedium)
                        .align(Alignment.Start)
                )

                TextView(
                    text = "${stringResource(id = R.string.tip_amt)} ......................................₹${formatAmount(tipAmount)}",
                    fontSize = MaterialTheme.dimens.SP_17_CompactMedium,
                    color = Color.Gray,
                    modifier = Modifier
                        .padding(bottom = MaterialTheme.dimens.DP_5_CompactMedium)
                        .align(Alignment.Start)
                )
                TextView(
                    text = "${stringResource(id = R.string.sgst_amt)} ......................................₹${formatAmount(sgstAmount)}",
                    fontSize = MaterialTheme.dimens.SP_17_CompactMedium,
                    color = Color.Gray,
                    modifier = Modifier
                        .padding(bottom = MaterialTheme.dimens.DP_5_CompactMedium)
                        .align(Alignment.Start)
                )
                TextView(
                    text = "${stringResource(id = R.string.igst_amt)} .......................................₹${formatAmount(igstAmount)}",
                    fontSize = MaterialTheme.dimens.SP_17_CompactMedium,
                    color = Color.Gray,
                    modifier = Modifier
                        .padding(bottom = MaterialTheme.dimens.DP_5_CompactMedium)
                        .align(Alignment.Start)
                )

            }
        }

            val settingsItems = listOf(
                SettingsItem(
                    imageRes = R.drawable.config_training_mode,
                    text = stringResource(id = R.string.training_mode),
                    isChecked = isTaxesEnabled,
                    onCheckedChange = { isTaxesEnabled = it }
                )
            )

            Column {
                androidx.compose.material3.Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = MaterialTheme.dimens.DP_24_CompactMedium,
                            end = MaterialTheme.dimens.DP_24_CompactMedium,
                            top = MaterialTheme.dimens.DP_4_CompactMedium, // Reduced top padding
                            bottom = MaterialTheme.dimens.DP_24_CompactMedium),
                    elevation = CardDefaults.elevatedCardElevation(10.dp),
                    shape = RoundedCornerShape(MaterialTheme.dimens.DP_18_CompactMedium),
                ) {
                    Column {
                        settingsItems.forEachIndexed { index, item ->
                            SettingsSurface(
                                modifier = Modifier.fillMaxWidth(),
                                item = item
                            )

                            if (index < settingsItems.size - 1) {
                                androidx.compose.material3.Divider(
                                    color = Color(0xFFB3B3B3),
                                    thickness = 1.dp
                                )
                            }

                            if (index == 4 && item.isChecked) {
                                TippingView(type = ConfigurableViewType.Percentage)
                            }
                            if (index == 5 && item.isChecked) {
                                TippingView(type = ConfigurableViewType.Taxes)
                            }
                        }
                    }
                }
            }


        FooterButtons(
            firstButtonTitle = stringResource(id = R.string.cancel_btn),
            firstButtonOnClick = { navHostController.navigate(AppNavigationItems.TrainingScreen.route) },
            secondButtonTitle = stringResource(id = R.string.confirm_btn),
            secondButtonOnClick = { navHostController.navigate(AppNavigationItems.CardScreen.createRoute(formatAmount(totalAmount))) }
        )
    }


}


