package com.analogics.tpaymentsapos.rootUiScreens.settings.config

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.analogics.tpaymentsapos.R
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import com.analogics.tpaymentsapos.rootUiScreens.activity.SharedViewModel
import com.analogics.tpaymentsapos.rootUiScreens.activity.localSharedViewModel
import com.analogics.tpaymentsapos.rootUiScreens.dialogs.CustomDialogBuilder
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.CommonTopAppBar
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.CustomSwitch
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.toPercentFormat
import com.analogics.tpaymentsapos.ui.theme.dimens


@Composable
fun ConfigurationView(navHostController: NavHostController, viewModel: ConfigViewModel = hiltViewModel()) {
    var sharedViewModel = localSharedViewModel.current
    val isBatchOpen = viewModel.isBatchOpen.collectAsState().value
    val isAdmin = viewModel.isAdmin.collectAsState().value
    val context = LocalContext.current


    val settingsItems = listOf(
        SettingsItem(
            imageRes = R.drawable.config_training_mode,
            text = stringResource(id = R.string.training_mode),
            isChecked = viewModel.isTrainingMode.value,
            onCheckedChange = {
                if (isAdmin) {
                    if (isBatchOpen != true) {
                        viewModel.onDemoModeChange(it, sharedViewModel)
                    } else {
                        viewModel.onShowBatchOpen(context)
                    }
                } else {
                    viewModel.onShowAdminOnly(context)
                }
            },
            isArrow = false,
            onArrowChange = {},
            isEnabled = isAdmin
        ),
        SettingsItem(
            imageRes = R.drawable.config_invoice_prompt,
            text = stringResource(id = R.string.prompt_invoice_no),
            isChecked = viewModel.isPromptInvoiceNumber.value,
            onCheckedChange = { if(isAdmin) viewModel.onPromptInvoiceNumberChange(it, sharedViewModel) else viewModel.onShowAdminOnly(context)},
            isArrow = false,
            onArrowChange = {},
            isEnabled = isAdmin
        ),
        SettingsItem(
            imageRes = R.drawable.config_tipping,
            text = stringResource(id = R.string.enable_tipping),
            isChecked = viewModel.isTippingEnabled.value,
            onCheckedChange = { if(isAdmin) viewModel.onTippingEnabledChange(it, sharedViewModel) else viewModel.onShowAdminOnly(context)},
            isArrow = false,
            onArrowChange = {},
            isEnabled = isAdmin
        ),
        SettingsItem(
            imageRes = R.drawable.config_tax,
            text = stringResource(id = R.string.vat),
            isChecked = viewModel.isTaxEnabled.value,
            onCheckedChange = { if(isAdmin) viewModel.onTaxEnabledChange(it, sharedViewModel) else viewModel.onShowAdminOnly(context)},
            isArrow = false,
            onArrowChange = {},
            isEnabled = isAdmin
        ),
        SettingsItem(
            imageRes = R.drawable.config_auto_print_report,
            text = stringResource(id = R.string.receipt_details),
            isChecked = viewModel.isAutoPrintReport.value,
            onCheckedChange = { if(isAdmin) navHostController.navigate(AppNavigationItems. ReceiptDetailsScreen.route) else viewModel.onShowAdminOnly(context)},
            isArrow = true,
            onArrowChange = { if(isAdmin) navHostController.navigate(AppNavigationItems. ReceiptDetailsScreen.route) else viewModel.onShowAdminOnly(context)},
            isEnabled = isAdmin
        ),
        SettingsItem(
            imageRes = R.drawable.config_auto_print_report,
            text = stringResource(id = R.string.auto_report_print),
            isChecked = viewModel.isAutoPrintReport.value,
            onCheckedChange = { viewModel.onAutoPrintReportChange(it, sharedViewModel) },
            isArrow = false,
            onArrowChange = {},
            isEnabled = true
        ),
        SettingsItem(
            imageRes = R.drawable.config_auto_m_print,
            text = stringResource(id = R.string.auto_print_merchant),
            isChecked = viewModel.isAutoPrintMerchant.value,
            onCheckedChange = { viewModel.onAutoPrintMerchantChange(it, sharedViewModel) },
            isArrow = false,
            onArrowChange = { },
            isEnabled = true
        ),
        SettingsItem(
            imageRes = R.drawable.time,
            text = stringResource(id = R.string.inactivity_timeout),
            isChecked = viewModel.isInactivity.value,
            onCheckedChange = { if(isAdmin) navHostController.navigate(AppNavigationItems.InactivityTimeoutScreen.route) else viewModel.onShowAdminOnly(context)},
            isArrow = true,
            onArrowChange = { if(isAdmin) navHostController.navigate(AppNavigationItems.InactivityTimeoutScreen.route) else viewModel.onShowAdminOnly(context)},
            isEnabled = isAdmin
        ),
        SettingsItem(
            imageRes = R.drawable.batch_id,
            text = stringResource(id = R.string.batch_id),
            isChecked = viewModel.isBatchId.value,
            onCheckedChange = { if(isAdmin) navHostController.navigate(AppNavigationItems.BatchIdScreen.route) else viewModel.onShowAdminOnly(context)},
            isArrow = true,
            onArrowChange = { if(isAdmin) navHostController.navigate(AppNavigationItems.BatchIdScreen.route) else viewModel.onShowAdminOnly(context)},
            isEnabled = isAdmin
        ),
    )

    Column {
        CommonTopAppBar(
            title = stringResource(id = R.string.Configuration),
            onBackButtonClick = { viewModel.onBack(navHostController) }
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MaterialTheme.dimens.DP_24_CompactMedium),
            elevation = CardDefaults.elevatedCardElevation(MaterialTheme.dimens.DP_11_CompactMedium)
        ) {
            LazyColumn {
                items(settingsItems.size) { index ->
                    val item = settingsItems[index]
                    SettingsSurface(
                        modifier = Modifier.fillMaxWidth(),
                        item = item,
                        isEnabled = item.isEnabled
                    )

                    if (index == 2 && item.isChecked) {
                        TippingView(ConfigurableViewType.Percentage, navHostController, viewModel, sharedViewModel, item.isEnabled)
                    }

                    if (index == 3 && item.isChecked) {
                        TippingView(ConfigurableViewType.Taxes, navHostController, viewModel, sharedViewModel, item.isEnabled)
                    }

                    if (index == 7 && item.isChecked) {
                        TippingView(ConfigurableViewType.Inactivity_Timeout, navHostController, viewModel, sharedViewModel, item.isEnabled)
                    }
                    if (index == 8 && item.isChecked) {
                        TippingView(ConfigurableViewType.Batch_Id, navHostController, viewModel, sharedViewModel, item.isEnabled)
                    }

                    if (index < settingsItems.size - 1) {
                        Divider(
                            color = MaterialTheme.colorScheme.secondary,
                            thickness = MaterialTheme.dimens.DP_1_CompactMedium
                        )
                    }
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.onLoad(sharedViewModel)
    }

    CustomDialogBuilder.ShowComposed()
}


data class SettingsItem(
    val imageRes: Int,
    val text: String,
    val isChecked: Boolean,
    val onCheckedChange: (Boolean) -> Unit,
    val isArrow: Boolean,
    val onArrowChange: () -> Unit, // Change to a function without parameters
    var isEnabled: Boolean

)



@Composable
fun TippingView(
    type: ConfigurableViewType,
    navHostController: NavHostController,
    viewModel: ConfigViewModel,
    sharedViewModel: SharedViewModel,
    isEnabled : Boolean
) {
    var timeoutDuration by remember { mutableStateOf(sharedViewModel.objPosConfig?.inactivityTimeout?.toString() ?: "") }
    var batchId by remember { mutableStateOf(sharedViewModel.objPosConfig?.batchId?.toString() ?: "") }
    val options = when (type) {
        ConfigurableViewType.Percentage -> listOf(
            viewModel.getTipPercentLabel(TipButton.PERCENT1, sharedViewModel),
            viewModel.getTipPercentLabel(TipButton.PERCENT2, sharedViewModel),
            viewModel.getTipPercentLabel(TipButton.PERCENT3, sharedViewModel)
        )
        ConfigurableViewType.Taxes -> listOf(
            stringResource(id = R.string.tax_label_vat) + "\n" + sharedViewModel.objPosConfig?.vatPercent.toPercentFormat()
        )
        ConfigurableViewType.Inactivity_Timeout -> listOf(stringResource(R.string.set_timeout)) // Keep for consistency
        ConfigurableViewType.Batch_Id -> listOf(stringResource(R.string.set_batch_id))
    }

    val title = when (type) {
        ConfigurableViewType.Percentage -> stringResource(R.string.adjust_per)
        ConfigurableViewType.Taxes -> stringResource(R.string.adjust_per)
        ConfigurableViewType.Inactivity_Timeout -> stringResource(R.string.set_timeout)
        ConfigurableViewType.Batch_Id -> stringResource(R.string.set_batch_id)
    }
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .then(if (!isEnabled) Modifier.alpha(0.5f) else Modifier) // Dim surface when isAdmin is false
            .clickable(enabled = isEnabled) {}, // Make it non-clickable if isAdmin is false
        color = MaterialTheme.colorScheme.onPrimary
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = MaterialTheme.colorScheme.onPrimary)
                .padding(MaterialTheme.dimens.DP_4_CompactMedium)
                .then(if (!isEnabled) Modifier.alpha(0.5f) else Modifier) // Dim surface when isAdmin is false
                .clickable(enabled = isEnabled) {}, // Make it non-clickable if isAdmin is false
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                        .padding(bottom = MaterialTheme.dimens.DP_10_CompactMedium)
                )
                if (type == ConfigurableViewType.Inactivity_Timeout) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()  // Ensures the Box takes only the required height
                    ) {
                        OutlinedTextField(
                            value = timeoutDuration,
                            onValueChange = { newValue -> timeoutDuration = newValue },
                            label = { Text(stringResource(id = R.string.timeout)) },
                            placeholder = { Text(stringResource(id = R.string.enter_timeout)) },
                            textStyle = TextStyle(textAlign = TextAlign.Center),
                            modifier = Modifier
                                .align(Alignment.Center)  // Center the TextField within the Box
                                .width(MaterialTheme.dimens.DP_160_CompactMedium)
                                .height(MaterialTheme.dimens.DP_60_CompactMedium), // Adjust height as needed
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary, // Orange color for focused state
                                unfocusedBorderColor = MaterialTheme.colorScheme.primaryContainer, // Light grey color for unfocused state
                                focusedLabelColor = MaterialTheme.colorScheme.primary, // Orange color for focused label
                                unfocusedLabelColor = MaterialTheme.colorScheme.primaryContainer, // Light grey color for unfocused label,
                                cursorColor = Color.Transparent
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    viewModel.onInactivityTimeoutChange(
                                        navHostController,
                                        timeoutDuration.toInt(),
                                        sharedViewModel
                                    )
                                }
                            )
                        )
                    }
                } else if (type == ConfigurableViewType.Batch_Id) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()  // Ensures the Box takes only the required height
                    ) {
                        OutlinedTextField(
                            value = batchId,
                            onValueChange = { newValue -> batchId = newValue },
                            label = { Text(stringResource(id = R.string.batch_id)) },
                            placeholder = { Text(stringResource(id = R.string.set_batch_id)) },
                            textStyle = TextStyle(textAlign = TextAlign.Center),
                            modifier = Modifier
                                .align(Alignment.Center)  // Center the TextField within the Box
                                .width(MaterialTheme.dimens.DP_160_CompactMedium)
                                .height(MaterialTheme.dimens.DP_60_CompactMedium), // Adjust height as needed
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary, // Orange color for focused state
                                unfocusedBorderColor = MaterialTheme.colorScheme.primaryContainer, // Light grey color for unfocused state
                                focusedLabelColor = MaterialTheme.colorScheme.primary, // Orange color for focused label
                                unfocusedLabelColor = MaterialTheme.colorScheme.primaryContainer, // Light grey color for unfocused label,
                                cursorColor = Color.Transparent
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    viewModel.onBatchIdChange(navHostController,batchId.toInt(), sharedViewModel)
                                }
                            )
                        )
                    }
                } else {
                    // For other types (Percentage, Taxes), show the Card components
                    val cardHeight = if (type == ConfigurableViewType.Taxes) {
                        MaterialTheme.dimens.DP_50_CompactMedium
                    } else {
                        MaterialTheme.dimens.DP_40_CompactMedium
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .padding(MaterialTheme.dimens.DP_20_CompactMedium),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        options.forEach { option ->
                            Card(
                                modifier = Modifier
                                    .width(MaterialTheme.dimens.DP_115_CompactMedium)
                                    .height(cardHeight),
                                onClick = {
                                    when (type) {
                                        ConfigurableViewType.Percentage -> viewModel.onTipPercentChange(
                                            when (options.indexOf(option)) {
                                                0 -> TipButton.PERCENT1
                                                1 -> TipButton.PERCENT2
                                                2 -> TipButton.PERCENT3
                                                else -> TipButton.NONE
                                            }, navHostController
                                        )

                                        ConfigurableViewType.Taxes -> viewModel.onTaxPercentChange(
                                            options.indexOf(option),
                                            navHostController
                                        )

                                        ConfigurableViewType.Inactivity_Timeout -> {
                                            // No action needed here for Inactivity Timeout
                                        }

                                        ConfigurableViewType.Batch_Id -> {
                                            // No action needed here for Inactivity Timeout
                                        }
                                    }
                                },
                                elevation = CardDefaults.elevatedCardElevation(MaterialTheme.dimens.DP_4_CompactMedium)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(color = MaterialTheme.colorScheme.onPrimary)
                                ) {
                                    Text(
                                        text = option,
                                        style = MaterialTheme.typography.bodyMedium,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(MaterialTheme.dimens.DP_4_CompactMedium)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}



enum class ConfigurableViewType {
    Percentage,
    Taxes,
    Inactivity_Timeout,
    Batch_Id
}

enum class TipButton(val value: Int) {
    NONE(0),
    PERCENT1(1),
    PERCENT2(2),
    PERCENT3(3),
    CUSTOM(4),
}

@Composable
fun SettingsSurface(
    modifier: Modifier = Modifier,
    item: SettingsItem,
    isEnabled: Boolean
) {
    Surface(
        modifier = modifier
            .height(MaterialTheme.dimens.DP_60_CompactMedium)
            .then(if (!isEnabled) Modifier.alpha(0.5f) else Modifier) // Dim surface when isAdmin is false
            .clickable(enabled = isEnabled) {}, // Make it non-clickable if isAdmin is false
        color = MaterialTheme.colorScheme.onPrimary
    ) {
        SettingsContent(
            item = item
        )
    }
}
@Composable
fun SettingsContent(
    item: SettingsItem
) {
    // Handle the click event to toggle the switch
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = MaterialTheme.dimens.DP_24_CompactMedium)
            .clickable {
                item.onCheckedChange(!item.isChecked) // Toggle the switch state
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = MaterialTheme.dimens.DP_20_CompactMedium), // Added padding for better touch area
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = item.imageRes),
                    contentDescription = item.text,
                    modifier = Modifier.size(MaterialTheme.dimens.DP_23_CompactMedium) // Adjust size as needed
                )

                Text(
                    text = item.text,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = MaterialTheme.dimens.DP_20_CompactMedium)
                )
            }
            if(item.isArrow)
            {
                Icon(
                    imageVector = Icons.Default.ArrowForwardIos,
                    contentDescription = "Close",
                    tint = Color.Gray,
                    modifier = Modifier
                        .size(MaterialTheme.dimens.DP_40_CompactMedium)
                        .clickable {
                            item.onArrowChange()
                        }
                )
            }
            else {
                CustomSwitch(
                    checked = item.isChecked,
                    onCheckedChange = { newCheckedState ->
                        item.onCheckedChange(newCheckedState)
                    },
                    checkedImage = R.drawable.switch_checked, // Your checked drawable
                    uncheckedImage = R.drawable.switch_unchecked, // Your unchecked drawable
                )
            }
        }
    }
}


