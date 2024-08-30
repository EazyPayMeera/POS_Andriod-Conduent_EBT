package com.analogics.tpaymentsapos.rootUiScreens.settings.config

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.analogics.tpaymentsapos.R // Import the R class for resource access
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.CommonTopAppBar
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.*
import com.analogics.tpaymentsapos.ui.theme.tipBgColor


@Composable
fun ConfigurationView(navHostController: NavHostController) {
    var isEnableTipping by remember { mutableStateOf(false) }
    var isTrainingModeEnabled by remember { mutableStateOf(false) }
    var isAutoPrintReportEnabled by remember { mutableStateOf(false) }
    var isPromptInvoiceNumberEnabled by remember { mutableStateOf(false) }
    var isAutoPrintMerchantReceiptEnabled by remember { mutableStateOf(false) }
    var isTaxesEnabled by remember { mutableStateOf(false) }

    val settingsItems = listOf(
        SettingsItem(
            imageRes = R.drawable.config_training_mode,
            text = stringResource(id = R.string.training_mode),
            isChecked = isTrainingModeEnabled,
            onCheckedChange = { isTrainingModeEnabled = it }
        ),
        SettingsItem(
            imageRes = R.drawable.config_auto_print_report,
            text = stringResource(id = R.string.auto_report_print),
            isChecked = isAutoPrintReportEnabled,
            onCheckedChange = { isAutoPrintReportEnabled = it }
        ),
        SettingsItem(
            imageRes = R.drawable.config_invoice_prompt,
            text = stringResource(id = R.string.prompt_invoice_no),
            isChecked = isPromptInvoiceNumberEnabled,
            onCheckedChange = { isPromptInvoiceNumberEnabled = it }
        ),
        SettingsItem(
            imageRes = R.drawable.config_auto_m_print,
            text = stringResource(id = R.string.auto_print_merchant),
            isChecked = isAutoPrintMerchantReceiptEnabled,
            onCheckedChange = { isAutoPrintMerchantReceiptEnabled = it }
        ),
        SettingsItem(
            imageRes = R.drawable.config_tipping,
            text = stringResource(id = R.string.enable_tipping),
            isChecked = isEnableTipping,
            onCheckedChange = { isEnableTipping = it }
        ),
        SettingsItem(
            imageRes = R.drawable.config_tax,
            text = stringResource(id = R.string.taxes),
            isChecked = isTaxesEnabled,
            onCheckedChange = { isTaxesEnabled = it }
        )
    )

    Column {
        CommonTopAppBar(
            title = stringResource(id = R.string.Configuration),
            onBackButtonClick = { navHostController.popBackStack() }
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.elevatedCardElevation(10.dp)
        ) {
            Column {
                settingsItems.forEachIndexed { index, item ->
                    SettingsSurface(
                        modifier = Modifier.fillMaxWidth(),
                        item = item
                    )

                    if (index < settingsItems.size - 1) {
                        Divider(color = Color(0xFFB3B3B3), thickness = 1.dp)
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
    }

data class SettingsItem(
    val imageRes: Int,
    val text: String,
    val isChecked: Boolean,
    val onCheckedChange: (Boolean) -> Unit
)



@Composable
fun TippingView(
    type: ConfigurableViewType
) {
    val options = when (type) {
        ConfigurableViewType.Percentage -> listOf("10%", "20%", "30%")
        ConfigurableViewType.Taxes -> listOf("Tax 1", "Tax 3")
    }

    val title = when (type) {
        ConfigurableViewType.Percentage -> stringResource(R.string.adjust_per)
        ConfigurableViewType.Taxes -> stringResource(R.string.adjust_per)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = tipBgColor)
            .padding(16.dp)

    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Text(
                text = title,
                style = if (type == ConfigurableViewType.Percentage) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                options.forEach { option ->
                    Card(
                        modifier = Modifier
                            .width(80.dp)
                            .height(40.dp)
                            .border(1.dp, Color.Gray),
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxSize()
                                .background(color = Color.White)
                        ) {
                            Text(
                                text = option,
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}


enum class ConfigurableViewType {
    Percentage,
    Taxes
}

@Composable
fun SettingsSurface(
    modifier: Modifier = Modifier,
    item: SettingsItem,
) {
    Surface(
        modifier = Modifier.height(60.dp),
        color = Color.White
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = item.imageRes),
                contentDescription = item.text,
                modifier = Modifier.size(24.dp)
            )

            Text(
                text = item.text,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        CustomSwitch(
            checked = item.isChecked,
            onCheckedChange = item.onCheckedChange,
            checkedImage = R.drawable.switch_checked, // Your checked drawable
            uncheckedImage = R.drawable.switch_unchecked, // Your unchecked drawable
        )
    }
}


