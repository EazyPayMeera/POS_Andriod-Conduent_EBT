// ConfirmationView.kt

package com.analogics.tpaymentsapos.rootUiScreens.confirmation.view

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.analogics.tpaymentsapos.R
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import com.analogics.tpaymentsapos.rootUiScreens.activity.SharedViewModel
import com.analogics.tpaymentsapos.rootUiScreens.activity.localSharedViewModel
import com.analogics.tpaymentsapos.rootUiScreens.confirmation.viewmodel.ConfirmationViewModel
import com.analogics.tpaymentsapos.rootUiScreens.dialogs.CustomDialogBuilder
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.CommonTopAppBar
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.CustomSwitch
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.FooterButtons
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.GenericCard
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.TextView
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.calculateTotalAmount
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.formatAmount
import com.analogics.tpaymentsapos.ui.theme.dashboardOrangeColor
import com.analogics.tpaymentsapos.ui.theme.dimens
import com.analogics.tpaymentsapos.ui.theme.tipBColor


@Composable
fun ConfirmationView(navHostController: NavHostController, customTipAmount : Double? =null, viewModel: ConfirmationViewModel = hiltViewModel()) {
    val sharedViewModel= localSharedViewModel.current
    val transAmount = sharedViewModel.objRootAppPaymentDetail.txnAmount?:0.00
    val sgstAmount = sharedViewModel.objRootAppPaymentDetail.SGST?:0.00
    val cgstAmount = sharedViewModel.objRootAppPaymentDetail.CGST?:0.00
    val tipAmount by remember {viewModel.tipAmount}
    var isTipEnabled by remember { viewModel.isTipEnabled }
    val totalAmount = calculateTotalAmount(transAmount, tipAmount, sgstAmount, cgstAmount)
    var isDialogVisible by remember { mutableStateOf(false) }

    Column {
        CommonTopAppBar(
            onBackButtonClick = { viewModel.onBack(navHostController, sharedViewModel) }
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
            backgroundColor = MaterialTheme.colorScheme.primary,
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
                    color = MaterialTheme.colorScheme.tertiary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(bottom = MaterialTheme.dimens.DP_11_CompactMedium)
                        .align(Alignment.Start)
                )
                TextView(
                    text = formatAmount(totalAmount),
                    fontSize = MaterialTheme.dimens.SP_35_CompactMedium,
                    color = MaterialTheme.colorScheme.tertiary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Start),
                )
            }
        }

        TransactionSummaryCard(transAmount, tipAmount, sgstAmount, cgstAmount, sharedViewModel)

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
                        color = MaterialTheme.colorScheme.tertiary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = MaterialTheme.dimens.DP_20_CompactMedium)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    CustomSwitch(
                        checked = isTipEnabled,
                        onCheckedChange = { viewModel.onTipToggle(it, sharedViewModel) },
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
                        onClick = { viewModel.onTipPercentChange(1, sharedViewModel) },
                        enabled = isTipEnabled,
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = if (viewModel.selectedButton.intValue == 1 && isTipEnabled) {
                                dashboardOrangeColor
                            } else {
                                tipBColor.copy(alpha = if (isTipEnabled) 1f else 0.5f)
                            },
                            contentColor = MaterialTheme.colorScheme.tertiary
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
                        Text(text = viewModel.getTipPercentLabel(1))
                    }

                    Button(
                        onClick = { viewModel.onTipPercentChange(2, sharedViewModel) },
                        enabled = isTipEnabled,
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = if (viewModel.selectedButton.intValue == 2 && isTipEnabled) {
                                dashboardOrangeColor
                            } else {
                                tipBColor.copy(alpha = if (isTipEnabled) 1f else 0.5f)
                            },
                            contentColor = MaterialTheme.colorScheme.tertiary
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
                        Text(text = viewModel.getTipPercentLabel(2))
                    }

                    Button(
                        onClick = { viewModel.onTipPercentChange(3, sharedViewModel) },
                        enabled = isTipEnabled,
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = if (viewModel.selectedButton.intValue == 3 && isTipEnabled) {
                                dashboardOrangeColor
                            } else {
                                tipBColor.copy(alpha = if (isTipEnabled) 1f else 0.5f)
                            },
                            contentColor = MaterialTheme.colorScheme.tertiary
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
                        Text(text = viewModel.getTipPercentLabel(3))
                    }

                    Button(
                        onClick = { viewModel.onCustomTip(navHostController, sharedViewModel) },
                        enabled = isTipEnabled,
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = if (viewModel.selectedButton.intValue == 4 && isTipEnabled) {
                                dashboardOrangeColor
                            } else {
                                tipBColor.copy(alpha = if (isTipEnabled) 1f else 0.5f)
                            },
                            contentColor = MaterialTheme.colorScheme.tertiary
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
            firstButtonOnClick = { /*viewModel.onCancel(navHostController)*/ isDialogVisible = true },
            secondButtonTitle = stringResource(id = R.string.confirm_btn),
            secondButtonOnClick = { viewModel.onConfirm(navHostController, sharedViewModel) }
        )

        if (isDialogVisible) {
            CustomDialogBuilder.create()
                .setTitle("Are you sure want to Cancel ?")
                .setSubtitle("")
                .setSmallText("")
                .setShowCloseButton(true) // Can set to false if you don't want the close button
                .setCancelable(true)
                .setBackgroundColor(androidx.compose.material.MaterialTheme.colors.surface)
                .setProgressColor(color = MaterialTheme.colorScheme.primary) // Orange color
                .setShowProgressIndicator(false)
                .setOnCancelAction {
                    navHostController.navigate(AppNavigationItems.ConfirmationScreen.route)
                }
                .setOnConfirmAction {
                    navHostController.navigate(AppNavigationItems.DashBoardScreen.route)
                }
                .setShowButtons(true)
                .setNavAction {
                    navHostController.popBackStack()
                }
                .buildDialog(onClose = { isDialogVisible = false })

        }
    }

    LaunchedEffect(Unit) {
        viewModel.onLoad(customTipAmount,sharedViewModel)
    }
}

@Composable
fun TipOptionButton(tip: String, selectedTip: String, onSelect: (String) -> Unit) {
    Button(
        onClick = { onSelect(tip) },
        shape = RoundedCornerShape(MaterialTheme.dimens.DP_21_CompactMedium),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = if (tip == selectedTip) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.tertiary
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
    igstAmount: Double,
    sharedViewModel: SharedViewModel
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
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(bottom = MaterialTheme.dimens.DP_5_CompactMedium)
            )

            Spacer(modifier = Modifier.height(MaterialTheme.dimens.DP_11_CompactMedium))

            // Transaction Amount
            sharedViewModel.objRootAppPaymentDetail.txnAmount?.toDouble()?.let {
                TransactionSummaryItem(
                    label = stringResource(id = R.string.tnx_amount),
                    amount = it
                )
            }

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
                color = MaterialTheme.colorScheme.tertiary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.CenterVertically)
            )
        }

        TextView(
            text = formatAmount(amount),
            fontSize = MaterialTheme.dimens.SP_17_CompactMedium,
            color = MaterialTheme.colorScheme.tertiary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterVertically)
        )
    }
}


