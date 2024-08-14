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

//
//@Composable
//fun ConfigurationView(navHostController: NavHostController) {
//    var isEnableTipping by remember { mutableStateOf(false) }
//    Column {
//        CommonTopAppBar(
//            title = "Configuration",
//            onBackButtonClick = { navHostController.popBackStack() }
//        )
//
//        // Custom Surface with top and bottom padding, and clickable
//        SettingsUpperSurface(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(horizontal = 16.dp) // Horizontal padding
//                .padding(top = 14.dp, bottom = 0.dp) // Top and bottom padding
//                .clickable { },
//            elevation = 20.dp, // Elevation for shadow effect
//            color = MaterialTheme.colorScheme.background, // Background color for Surface
//            height = 60.dp // Customizable height parameter
//        ) {
//            // State to manage the toggle switch
//            var isSwitchedOn by remember { mutableStateOf(false) }
//            // Use Row to place text and images horizontally
//            Row(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(horizontal = 16.dp), // Adjust padding as needed
//                horizontalArrangement = Arrangement.Start, // Space between elements
//                verticalAlignment = Alignment.CenterVertically // Center vertically
//            ) {
//                // Leading image
//                Image(
//                    painter = painterResource(id = R.drawable.language), // Replace with your image resource
//                    contentDescription = "Language Icon",
//                    modifier = Modifier.size(24.dp) // Adjust size as needed
//                )
//
//                // Set Language text
//                Text(
//                    text = "Training Mode",
//                    style = MaterialTheme.typography.bodyLarge,
//                    modifier = Modifier.padding(horizontal = 8.dp) // Space between text and images
//                )
//                // Spacer between Upper and Middle Surface
//                Spacer(modifier = Modifier.width(128.dp)) // Adjust the height as needed
//                // Toggle switch
//                // Toggle switch
//                Switch(
//                    checked = isSwitchedOn,
//                    onCheckedChange = { isSwitchedOn = it }, // Update state on toggle
//                    colors = SwitchDefaults.colors(
//                        checkedBorderColor = Color(0xFFF7931E),
//                        checkedThumbColor = Color(0xFFF7931E),
//                        uncheckedBorderColor = Color.Gray,
//                        checkedTrackColor = Color.White
//                    ),
//                    modifier = Modifier
//                        .scale(0.75f) // Scale down the switch
//                        .padding(4.dp) // Padding around the switch
//                )
//            }
//        }
//
//        // Middle Surface with top and bottom padding, and clickable
//        SettingsMiddleSurface(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(horizontal = 16.dp) // Horizontal padding
//                .padding(top = 1.dp, bottom = 0.dp) // Top and bottom padding
//                .clickable { /* Handle click for SettingsMiddleSurface */ },
//            elevation = 20.dp, // Elevation for shadow effect
//            color = MaterialTheme.colorScheme.background, // Background color for Surface
//            height = 60.dp // Customizable height parameter
//        ) {
//            // State to manage the toggle switch
//            var isSwitchedOn by remember { mutableStateOf(false) }
//            // Use Row to place text and images horizontally
//            Row(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(horizontal = 16.dp), // Adjust padding as needed
//                horizontalArrangement = Arrangement.Start, // Space between elements
//                verticalAlignment = Alignment.CenterVertically // Center vertically
//            ) {
//                // Leading image
//                Image(
//                    painter = painterResource(id = R.drawable.password), // Replace with your image resource
//                    contentDescription = "Language Icon",
//                    modifier = Modifier.size(24.dp) // Adjust size as needed
//                )
//
//                // Change Password text
//                Text(
//                    text = "Auto Print Report on Sign Out",
//                    style = MaterialTheme.typography.bodyLarge,
//                    modifier = Modifier.padding(horizontal = 8.dp) // Space between text and images
//                )
//
//                // Spacer between Upper and Middle Surface
//                Spacer(modifier = Modifier.width(15.dp)) // Adjust the height as needed
//                // Toggle switch
//                Switch(
//                    checked = isSwitchedOn,
//                    onCheckedChange = { isSwitchedOn = it }, // Update state on toggle
//                    colors = SwitchDefaults.colors(
//                        checkedBorderColor = Color(0xFFF7931E),
//                        checkedThumbColor = Color(0xFFF7931E),
//                        uncheckedBorderColor = Color.Gray,
//                        checkedTrackColor = Color.White
//                    ),
//                    modifier = Modifier
//                        .scale(0.75f) // Scale down the switch
//                        .padding(4.dp) // Padding around the switch
//                )
//            }
//        }
//
//        // Middle Surface with top and bottom padding, and clickable
//        SettingsMiddleSurface(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(horizontal = 16.dp) // Horizontal padding
//                .padding(top = 1.dp, bottom = 0.dp) // Top and bottom padding
//                .clickable { /* Handle click for SettingsMiddleSurface */ },
//            elevation = 20.dp, // Elevation for shadow effect
//            color = MaterialTheme.colorScheme.background, // Background color for Surface
//            height = 60.dp // Customizable height parameter
//        ) {
//            // State to manage the toggle switch
//            var isSwitchedOn by remember { mutableStateOf(false) }
//            // Use Row to place text and images horizontally
//            Row(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(horizontal = 16.dp), // Adjust padding as needed
//                horizontalArrangement = Arrangement.Start, // Space between elements
//                verticalAlignment = Alignment.CenterVertically // Center vertically
//            ) {
//                // Leading image
//                Image(
//                    painter = painterResource(id = R.drawable.password), // Replace with your image resource
//                    contentDescription = "Language Icon",
//                    modifier = Modifier.size(24.dp) // Adjust size as needed
//                )
//
//                // Change Password text
//                Text(
//                    text = "Prompt Invoice Number",
//                    style = MaterialTheme.typography.bodyLarge,
//                    modifier = Modifier.padding(horizontal = 8.dp) // Space between text and images
//                )
//
//                // Spacer between Upper and Middle Surface
//                Spacer(modifier = Modifier.width(55.dp)) // Adjust the height as needed
//                // Toggle switch
//                Switch(
//                    checked = isSwitchedOn,
//                    onCheckedChange = { isSwitchedOn = it }, // Update state on toggle
//                    colors = SwitchDefaults.colors(
//                        checkedBorderColor = Color(0xFFF7931E),
//                        checkedThumbColor = Color(0xFFF7931E),
//                        uncheckedBorderColor = Color.Gray,
//                        checkedTrackColor = Color.White
//                    ),
//                    modifier = Modifier
//                        .scale(0.75f) // Scale down the switch
//                        .padding(4.dp) // Padding around the switch
//                )
//            }
//        }
//
//        // Middle Surface with top and bottom padding, and clickable
//        SettingsMiddleSurface(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(horizontal = 16.dp) // Horizontal padding
//                .padding(top = 1.dp, bottom = 0.dp) // Top and bottom padding
//                .clickable { /* Handle click for SettingsMiddleSurface */ },
//            elevation = 20.dp, // Elevation for shadow effect
//            color = MaterialTheme.colorScheme.background, // Background color for Surface
//            height = 60.dp // Customizable height parameter
//        ) {
//            // State to manage the toggle switch
//            var isSwitchedOn by remember { mutableStateOf(false) }
//            // Use Row to place text and images horizontally
//            Row(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(horizontal = 16.dp), // Adjust padding as needed
//                horizontalArrangement = Arrangement.Start, // Space between elements
//                verticalAlignment = Alignment.CenterVertically // Center vertically
//            ) {
//                // Leading image
//                Image(
//                    painter = painterResource(id = R.drawable.password), // Replace with your image resource
//                    contentDescription = "Language Icon",
//                    modifier = Modifier.size(24.dp) // Adjust size as needed
//                )
//
//                // Change Password text
//                Text(
//                    text = "Auto Print Merchant Receipt",
//                    style = MaterialTheme.typography.bodyLarge,
//                    modifier = Modifier.padding(horizontal = 8.dp) // Space between text and images
//                )
//
//                // Spacer between Upper and Middle Surface
//                Spacer(modifier = Modifier.width(22.dp)) // Adjust the height as needed
//                // Toggle switch
//                Switch(
//                    checked = isSwitchedOn,
//                    onCheckedChange = { isSwitchedOn = it }, // Update state on toggle
//                    colors = SwitchDefaults.colors(
//                        checkedBorderColor = Color(0xFFF7931E),
//                        checkedThumbColor = Color(0xFFF7931E),
//                        uncheckedBorderColor = Color.Gray,
//                        checkedTrackColor = Color.White
//                    ),
//                    modifier = Modifier
//                        .scale(0.75f) // Scale down the switch
//                        .padding(4.dp) // Padding around the switch
//                )
//            }
//        }
//
//        // Middle Surface with top and bottom padding, and clickable
//        SettingsMiddleSurface(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(horizontal = 16.dp) // Horizontal padding
//                .padding(top = 1.dp, bottom = 0.dp) // Top and bottom padding
//                .clickable { /* Handle click for SettingsMiddleSurface */ },
//            elevation = 20.dp, // Elevation for shadow effect
//            color = MaterialTheme.colorScheme.background, // Background color for Surface
//            height = if (isEnableTipping) 100.dp else 60.dp //stomizable height parameter
//        ) {
//            // State to manage the toggle switch
//            var isSwitchedOn by remember { mutableStateOf(false) }
//
//            // Use Row to place text and images horizontally
//            Row(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(horizontal = 16.dp), // Adjust padding as needed
//                horizontalArrangement = Arrangement.Start, // Space between elements
//                verticalAlignment = Alignment.CenterVertically// Center vertically
//            ) {
//
//                // Leading image
//                Image(
//                    painter = painterResource(id = R.drawable.password), // Replace with your image resource
//                    contentDescription = "Language Icon",
//                    modifier = Modifier.size(24.dp) // Adjust size as needed
//                )
//
//                // Change Password text
//                Text(
//                    text = "Enable Tipping",
//                    style = MaterialTheme.typography.bodyLarge,
//                    modifier = Modifier.padding(horizontal = 8.dp) // Space between text and images
//                )
//
//                // Spacer between Upper and Middle Surface
//                Spacer(modifier = Modifier.width(123.dp)) // Adjust the height as needed
//                // Toggle switch
//                Switch(
//                    checked = isEnableTipping,
//                    onCheckedChange = { isEnableTipping = it },
//                    colors = SwitchDefaults.colors(
//                        checkedBorderColor = Color(0xFFF7931E),
//                        checkedThumbColor = Color(0xFFF7931E),
//                        uncheckedBorderColor = Color.Gray,
//                        checkedTrackColor = Color.White
//                    ),
//                    modifier = Modifier
//                        .scale(0.75f) // Scale down the switch
//                        .padding(4.dp) // Padding around the switch
//                )
//            }
//        }
//
//        // Lower Surface with top and bottom padding, and clickable
//        SettingsLowerSurface(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(horizontal = 16.dp) // Horizontal padding
//                .padding(top = 1.dp, bottom = 0.dp) // Top and bottom padding
//                .clickable { /* Handle click for SettingsLowerSurface */ },
//            elevation = 20.dp, // Elevation for shadow effect
//            color = MaterialTheme.colorScheme.background, // Background color for Surface
//            height = 60.dp // Customizable height parameter
//        ) {
//            // State to manage the toggle switch
//            var isSwitchedOn by remember { mutableStateOf(false) }
//            // Use Row to place text and images horizontally
//            Row(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(horizontal = 16.dp), // Adjust padding as needed
//                horizontalArrangement = Arrangement.Start, // Space between elements
//                verticalAlignment = Alignment.CenterVertically // Center vertically
//            ) {
//                // Leading image
//                Image(
//                    painter = painterResource(id = R.drawable.settings), // Replace with your image resource
//                    contentDescription = "Language Icon",
//                    modifier = Modifier.size(24.dp) // Adjust size as needed
//                )
//
//                // Change Password text
//                Text(
//                    text = "Taxes",
//                    style = MaterialTheme.typography.bodyLarge,
//                    modifier = Modifier.padding(horizontal = 8.dp) // Space between text and images
//                )
//
//                // Spacer between Upper and Middle Surface
//                Spacer(modifier = Modifier.width(190.dp)) // Adjust the height as needed
//
//                // Toggle switch
//                Switch(
//                    checked = isSwitchedOn,
//                    onCheckedChange = { isSwitchedOn = it }, // Update state on toggle
//                    colors = SwitchDefaults.colors(
//                        checkedBorderColor = Color(0xFFF7931E),
//                        checkedThumbColor = Color(0xFFF7931E),
//                        uncheckedBorderColor = Color.Gray,
//                        checkedTrackColor = Color.White
//                    ),
//                    modifier = Modifier
//                        .scale(0.75f) // Scale down the switch
//                        .padding(4.dp) // Padding around the switch
//                )
//            }
//        }
//    }
//}




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
            .background(color = Color.White)
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


