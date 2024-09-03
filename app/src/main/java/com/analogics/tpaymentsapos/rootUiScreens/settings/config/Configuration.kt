package com.analogics.tpaymentsapos.rootUiScreens.settings.config

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavHostController
import com.analogics.tpaymentsapos.R
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.CommonTopAppBar
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.CustomSwitch
import com.analogics.tpaymentsapos.ui.theme.dimens


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
                .padding(MaterialTheme.dimens.DP_24_CompactMedium),
            elevation = CardDefaults.elevatedCardElevation(MaterialTheme.dimens.DP_11_CompactMedium)
        ) {
            Column {
                settingsItems.forEachIndexed { index, item ->
                    SettingsSurface(
                        modifier = Modifier.fillMaxWidth(),
                        item = item
                    )

                    if (index < settingsItems.size - 1) {
                        Divider(color = colorResource(id = R.color.white), thickness = MaterialTheme.dimens.DP_1_CompactMedium)
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
        ConfigurableViewType.Percentage -> listOf(stringResource(id = R.string.ten), stringResource(id = R.string.fifteen), stringResource(id = R.string.twenty))
        ConfigurableViewType.Taxes -> listOf(stringResource(id = R.string.tax_1), stringResource(id = R.string.tax_2))
    }

    val title = when (type) {
        ConfigurableViewType.Percentage -> stringResource(R.string.adjust_per)
        ConfigurableViewType.Taxes -> stringResource(R.string.adjust_per)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = Color.White)
            .padding(MaterialTheme.dimens.DP_24_CompactMedium)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = MaterialTheme.dimens.DP_24_CompactMedium)
        ) {
            Text(
                text = title,
                style = if (type == ConfigurableViewType.Percentage) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = MaterialTheme.dimens.DP_20_CompactMedium)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                options.forEach { option ->
                    Card(
                        modifier = Modifier
                            .width(MaterialTheme.dimens.DP_100_CompactMedium)
                            .height(MaterialTheme.dimens.DP_40_CompactMedium)
                            .border(MaterialTheme.dimens.DP_1_CompactMedium, Color.Gray),
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
        modifier = Modifier.height(MaterialTheme.dimens.DP_60_CompactMedium),
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
            .padding(horizontal = MaterialTheme.dimens.DP_24_CompactMedium),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = item.imageRes),
                contentDescription = item.text,
                modifier = Modifier.size(MaterialTheme.dimens.DP_23_CompactMedium) // Need to change here
            )

            Text(
                text = item.text,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(start = MaterialTheme.dimens.DP_20_CompactMedium)
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


