// ConfirmationView.kt

package com.eazypaytech.posafrica.rootUiScreens.confirmation.view

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.runtime.collectAsState
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
import com.eazypaytech.paymentservicecore.models.TxnType
import com.eazypaytech.posafrica.R
import com.eazypaytech.posafrica.navigation.AppNavigationItems
import com.eazypaytech.posafrica.rootUiScreens.activity.SharedViewModel
import com.eazypaytech.posafrica.rootUiScreens.activity.localSharedViewModel
import com.eazypaytech.posafrica.rootUiScreens.confirmation.viewmodel.ConfirmationViewModel
import com.eazypaytech.posafrica.rootUiScreens.dialogs.CustomDialogBuilder
import com.eazypaytech.posafrica.rootUiScreens.settings.config.PercentButton
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.CommonTopAppBar
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.CustomSwitch
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.FooterButtons
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.GenericCard
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.TextView
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.calculateTotalAmount
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.formatAmount
import com.eazypaytech.posafrica.ui.theme.dimens
import com.eazypaytech.posafrica.ui.theme.tipBColor


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ConfirmationView(navHostController: NavHostController, customTipAmount : Double? =null, customServiceCharge : Double?, viewModel: ConfirmationViewModel = hiltViewModel()) {
    val sharedViewModel= localSharedViewModel.current
    val transAmount = sharedViewModel.objRootAppPaymentDetail.txnAmount?:0.00
    val vat = sharedViewModel.objRootAppPaymentDetail.VAT?:0.00
    val tipAmount by remember {viewModel.tipAmount}
    val serviceCharge by remember {viewModel.serviceCharge}
    var isTipEnabled by remember { viewModel.isTipButtonEnabled }
    var isServiceChargeEnabled by remember { viewModel.isServiceChargeButtonEnabled }
    val totalAmount = calculateTotalAmount(transAmount, tipAmount, vat, serviceCharge)
    var isDialogVisible by remember { mutableStateOf(false) }

    val totalAmountFetch = viewModel.totalAmountFetch.collectAsState().value

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
                    //bottom = MaterialTheme.dimens.DP_5_CompactMedium
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
                        .padding(bottom = MaterialTheme.dimens.DP_2_CompactMedium)
                        .align(Alignment.Start)
                )
                TextView(
                    text = (if(sharedViewModel.objRootAppPaymentDetail.txnType == TxnType.REFUND) totalAmountFetch else formatAmount(totalAmount)) ?: "",
                    fontSize = MaterialTheme.dimens.SP_31_CompactMedium,
                    color = MaterialTheme.colorScheme.tertiary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Start),
                )
            }
        }


        TransactionSummaryCard(transAmount, tipAmount, serviceCharge, vat, sharedViewModel)

        AddTipCard(sharedViewModel,viewModel,navHostController,isTipEnabled)

        //Spacer(modifier = Modifier.height(MaterialTheme.dimens.DP_4_CompactMedium))

        AddServiceChargeCard(sharedViewModel,viewModel,navHostController,isServiceChargeEnabled)

        FooterButtons(
            firstButtonTitle = stringResource(id = R.string.cancel_btn),
            firstButtonOnClick = { isDialogVisible = true },
            secondButtonTitle = stringResource(id = R.string.confirm_btn),
            secondButtonOnClick = { viewModel.onConfirm(navHostController, sharedViewModel) }
        )

        if (isDialogVisible) {
            CustomDialogBuilder.create()
                .setTitle(stringResource(id = R.string.cancel_dialogue))
                .setSubtitle(stringResource(id = R.string.dialogue_cancel_request))
                .setSmallText("")
                .setShowCloseButton(false) // Can set to false if you don't want the close button
                .setCancelButtonText(stringResource(id = R.string.yes))
                .setConfirmButtonText(stringResource(id = R.string.cancel_no))
                .setCancelable(true)
                .setAutoOff(false)
                .setBackgroundColor(androidx.compose.material.MaterialTheme.colors.surface)
                .setProgressColor(color = MaterialTheme.colorScheme.primary) // Orange color
                .setShowProgressIndicator(false)
                .setOnCancelAction {
                    navHostController.navigate(AppNavigationItems.DashBoardScreen.route)
                }
                .setOnConfirmAction {
                    //navHostController.navigate(AppNavigationItems.ConfirmationScreen.route)
                }
                .setShowButtons(true)
                .setNavAction {
                    navHostController.popBackStack()
                }
                .buildDialog(onClose = { isDialogVisible = false })

        }
    }

    LaunchedEffect(Unit) {
        viewModel.onLoad(customTipAmount,customServiceCharge,sharedViewModel)
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
fun AddTipCard(
    sharedViewModel: SharedViewModel,
    viewModel: ConfirmationViewModel,
    navHostController: NavHostController,
    isTipEnabled : Boolean
)
{
    sharedViewModel.objRootAppPaymentDetail.txnType.takeIf { it == TxnType.PURCHASE && sharedViewModel.objPosConfig?.isTipEnabled==true }?.let {
        GenericCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = MaterialTheme.dimens.DP_24_CompactMedium,
                    end = MaterialTheme.dimens.DP_24_CompactMedium,
                    top = MaterialTheme.dimens.DP_10_CompactMedium,
                    //bottom = MaterialTheme.dimens.DP_10_CompactMedium
                ),
            elevation = MaterialTheme.dimens.DP_10_CompactMedium,
            shape = RoundedCornerShape(MaterialTheme.dimens.DP_18_CompactMedium),
        ) {
            Column(
                modifier = Modifier
                    .padding(
                        start = MaterialTheme.dimens.DP_20_CompactMedium,
                        end = MaterialTheme.dimens.DP_20_CompactMedium
                    )
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextView(
                        text = stringResource(id = R.string.add_tip),
                        fontSize = MaterialTheme.dimens.SP_18_CompactMedium,
                        color = if (isTipEnabled) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSecondary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = MaterialTheme.dimens.DP_2_CompactMedium)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    CustomSwitch(
                        checked = isTipEnabled,
                        onCheckedChange = { viewModel.onTipToggle(it) },
                        checkedImage = R.drawable.switch_checked,
                        uncheckedImage = R.drawable.switch_unchecked,
                    )
                }

                //Spacer(modifier = Modifier.height(MaterialTheme.dimens.DP_4_CompactMedium))

                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = MaterialTheme.dimens.DP_5_CompactMedium),
                    horizontalArrangement = Arrangement.SpaceEvenly

                ) {

                    Button(
                        onClick = { viewModel.onTipPercentChange(PercentButton.PERCENT1, sharedViewModel) },
                        enabled = isTipEnabled,
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = if (viewModel.selectedTipButton.value == PercentButton.PERCENT1 && isTipEnabled) {
                                MaterialTheme.colorScheme.primary
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
                        Text(text = viewModel.getTipPercentLabel(PercentButton.PERCENT1, sharedViewModel))
                    }

                    Button(
                        onClick = { viewModel.onTipPercentChange(PercentButton.PERCENT2, sharedViewModel) },
                        enabled = isTipEnabled,
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = if (viewModel.selectedTipButton.value == PercentButton.PERCENT2 && isTipEnabled) {
                                MaterialTheme.colorScheme.primary
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
                        Text(text = viewModel.getTipPercentLabel(PercentButton.PERCENT2, sharedViewModel))
                    }

                    Button(
                        onClick = { viewModel.onTipPercentChange(PercentButton.PERCENT3, sharedViewModel) },
                        enabled = isTipEnabled,
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = if (viewModel.selectedTipButton.value == PercentButton.PERCENT3 && isTipEnabled) {
                                MaterialTheme.colorScheme.primary
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
                        Text(text = viewModel.getTipPercentLabel(PercentButton.PERCENT3, sharedViewModel))
                    }

                    Button(
                        onClick = { viewModel.onCustomTip(navHostController, sharedViewModel) },
                        enabled = isTipEnabled,
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = if (viewModel.selectedTipButton.value == PercentButton.CUSTOM && isTipEnabled) {
                                MaterialTheme.colorScheme.primary
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
    }
}

@Composable
fun AddServiceChargeCard(
    sharedViewModel: SharedViewModel,
    viewModel: ConfirmationViewModel,
    navHostController: NavHostController,
    isServiceChargeEnabled : Boolean
)
{
    sharedViewModel.objRootAppPaymentDetail.txnType.takeIf { it == TxnType.PURCHASE && sharedViewModel.objPosConfig?.isTipEnabled==true }?.let {
        GenericCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = MaterialTheme.dimens.DP_24_CompactMedium,
                    end = MaterialTheme.dimens.DP_24_CompactMedium,
                    top = MaterialTheme.dimens.DP_10_CompactMedium,
                    //bottom = MaterialTheme.dimens.DP_10_CompactMedium
                ),
            elevation = MaterialTheme.dimens.DP_10_CompactMedium,
            shape = RoundedCornerShape(MaterialTheme.dimens.DP_18_CompactMedium),
        ) {
            Column(
                modifier = Modifier
                    .padding(
                        start = MaterialTheme.dimens.DP_20_CompactMedium,
                        end = MaterialTheme.dimens.DP_20_CompactMedium
                    )
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextView(
                        text = stringResource(id = R.string.add_service_charge),
                        fontSize = MaterialTheme.dimens.SP_18_CompactMedium,
                        color = if (isServiceChargeEnabled) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSecondary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = MaterialTheme.dimens.DP_2_CompactMedium)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    CustomSwitch(
                        checked = isServiceChargeEnabled,
                        onCheckedChange = { viewModel.onServiceChargeToggle(it) },
                        checkedImage = R.drawable.switch_checked,
                        uncheckedImage = R.drawable.switch_unchecked,
                    )
                }

                //Spacer(modifier = Modifier.height(MaterialTheme.dimens.DP_4_CompactMedium))

                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = MaterialTheme.dimens.DP_5_CompactMedium),
                    horizontalArrangement = Arrangement.SpaceEvenly

                ) {

                    Button(
                        onClick = { viewModel.onServiceChargePercentChange(PercentButton.PERCENT1, sharedViewModel) },
                        enabled = isServiceChargeEnabled,
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = if (viewModel.selectedServiceChargeButton.value == PercentButton.PERCENT1 && isServiceChargeEnabled) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                tipBColor.copy(alpha = if (isServiceChargeEnabled) 1f else 0.5f)
                            },
                            contentColor = MaterialTheme.colorScheme.tertiary
                        ),
                        shape = RoundedCornerShape(MaterialTheme.dimens.DP_15_CompactMedium),
                        modifier = Modifier,
                            //.padding(horizontal = MaterialTheme.dimens.DP_4_CompactMedium),
                        elevation = ButtonDefaults.elevation(
                            defaultElevation = MaterialTheme.dimens.DP_20_CompactMedium,
                            pressedElevation = MaterialTheme.dimens.DP_20_CompactMedium, // Adjust pressed elevation based on isTipEnabled
                            disabledElevation = MaterialTheme.dimens.DP_20_CompactMedium
                        ),
                        contentPadding = PaddingValues(horizontal = MaterialTheme.dimens.DP_15_CompactMedium, vertical = MaterialTheme.dimens.DP_10_CompactMedium)
                    ) {
                        Text(text = viewModel.getServiceChargePercentLabel(PercentButton.PERCENT1, sharedViewModel))
                    }

                    Button(
                        onClick = { viewModel.onServiceChargePercentChange(PercentButton.PERCENT2, sharedViewModel) },
                        enabled = isServiceChargeEnabled,
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = if (viewModel.selectedServiceChargeButton.value == PercentButton.PERCENT2 && isServiceChargeEnabled) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                tipBColor.copy(alpha = if (isServiceChargeEnabled) 1f else 0.5f)
                            },
                            contentColor = MaterialTheme.colorScheme.tertiary
                        ),
                        shape = RoundedCornerShape(MaterialTheme.dimens.DP_15_CompactMedium),
                        modifier = Modifier,
                        contentPadding = PaddingValues(horizontal = MaterialTheme.dimens.DP_15_CompactMedium, vertical = MaterialTheme.dimens.DP_10_CompactMedium),
                            //.padding(horizontal = MaterialTheme.dimens.DP_4_CompactMedium),
                        elevation = ButtonDefaults.elevation(
                            defaultElevation = MaterialTheme.dimens.DP_20_CompactMedium,
                            pressedElevation = MaterialTheme.dimens.DP_20_CompactMedium, // Adjust pressed elevation based on isTipEnabled
                            disabledElevation = MaterialTheme.dimens.DP_20_CompactMedium
                        )

                    ) {
                        Text(text = viewModel.getServiceChargePercentLabel(PercentButton.PERCENT2, sharedViewModel))
                    }

                    Button(
                        onClick = { viewModel.onServiceChargePercentChange(PercentButton.PERCENT3, sharedViewModel) },
                        enabled = isServiceChargeEnabled,
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = if (viewModel.selectedServiceChargeButton.value == PercentButton.PERCENT3 && isServiceChargeEnabled) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                tipBColor.copy(alpha = if (isServiceChargeEnabled) 1f else 0.5f)
                            },
                            contentColor = MaterialTheme.colorScheme.tertiary
                        ),
                        shape = RoundedCornerShape(MaterialTheme.dimens.DP_15_CompactMedium),
                        modifier = Modifier,
                        contentPadding = PaddingValues(horizontal = MaterialTheme.dimens.DP_15_CompactMedium, vertical = MaterialTheme.dimens.DP_10_CompactMedium),
                            //.padding(horizontal = MaterialTheme.dimens.DP_4_CompactMedium),
                        elevation = ButtonDefaults.elevation(
                            defaultElevation = MaterialTheme.dimens.DP_20_CompactMedium,
                            pressedElevation = MaterialTheme.dimens.DP_20_CompactMedium, // Adjust pressed elevation based on isTipEnabled
                            disabledElevation = MaterialTheme.dimens.DP_20_CompactMedium
                        )
                    ) {
                        Text(text = viewModel.getServiceChargePercentLabel(PercentButton.PERCENT3, sharedViewModel))
                    }

                    Button(
                        onClick = { viewModel.onCustomServiceCharge(navHostController, sharedViewModel) },
                        enabled = isServiceChargeEnabled,
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = if (viewModel.selectedServiceChargeButton.value == PercentButton.CUSTOM && isServiceChargeEnabled) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                tipBColor.copy(alpha = if (isServiceChargeEnabled) 1f else 0.5f)
                            },
                            contentColor = MaterialTheme.colorScheme.tertiary
                        ),
                        shape = RoundedCornerShape(MaterialTheme.dimens.DP_15_CompactMedium),
                        //modifier = Modifier.padding(horizontal = MaterialTheme.dimens.DP_4_CompactMedium),
                        modifier = Modifier,
                        contentPadding = PaddingValues(horizontal = MaterialTheme.dimens.DP_15_CompactMedium, vertical = MaterialTheme.dimens.DP_10_CompactMedium),
                            //.padding(horizontal = MaterialTheme.dimens.DP_4_CompactMedium),
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
    }
}

@Composable
fun TransactionSummaryCard(
    amountDouble: Double,
    tipAmount: Double,
    serviceCharge: Double,
    vat: Double,
    sharedViewModel: SharedViewModel
) {
    GenericCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = MaterialTheme.dimens.DP_24_CompactMedium,
                end = MaterialTheme.dimens.DP_24_CompactMedium,
                top = MaterialTheme.dimens.DP_10_CompactMedium,
                //bottom = MaterialTheme.dimens.DP_10_CompactMedium
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
                    .padding(bottom = MaterialTheme.dimens.DP_2_CompactMedium)
            )

            //Spacer(modifier = Modifier.height(MaterialTheme.dimens.DP_11_CompactMedium))

            // Transaction Amount
            sharedViewModel.objRootAppPaymentDetail.txnAmount?.toDouble()?.let {
                TransactionSummaryItem(
                    label = stringResource(id = R.string.tnx_amount),
                    amount = it
                )
            }

            // Tip Amount
            sharedViewModel.objPosConfig?.isTipEnabled?.takeIf { it == true }?.let {
                TransactionSummaryItem(
                    label = stringResource(id = R.string.tip_amt),
                    amount = tipAmount
                )
            }

            // Service Charge
            sharedViewModel.objPosConfig?.isServiceChargeEnabled?.takeIf { it == true }?.let {
                TransactionSummaryItem(
                    label = stringResource(id = R.string.service_charge),
                    amount = serviceCharge
                )
            }

            sharedViewModel.objPosConfig?.isTaxEnabled?.takeIf { it == true }?.let {
                // VAT
                TransactionSummaryItem(
                    label = stringResource(id = R.string.vat_amt),
                    amount = vat
                )
            }
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
                tint = MaterialTheme.colorScheme.primary, // Adjust the color to match the bullet color
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


