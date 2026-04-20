package com.eazypaytech.pos.features.settings.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.eazypaytech.pos.R
import com.eazypaytech.pos.navigation.AppNavigationItems
import com.eazypaytech.pos.features.activity.ui.localSharedViewModel
import com.eazypaytech.pos.features.dialogs.ui.CustomDialogBuilder
import com.eazypaytech.pos.core.ui.components.inputfields.CommonTopAppBar
import com.eazypaytech.pos.core.ui.components.buttons.CustomSwitch
import com.eazypaytech.pos.core.themes.dimens


@Composable
fun ConfigurationScreen(navHostController: NavHostController, viewModel: ConfigViewModel = hiltViewModel()) {
    var sharedViewModel = localSharedViewModel.current
    val isBatchOpen = viewModel.isBatchOpen.collectAsState().value
    val isAdmin = viewModel.isAdmin.collectAsState().value
    val context = LocalContext.current


    val settingsItems = listOf(

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
            imageRes = R.drawable.settings,
            text = stringResource(id = R.string.settings),
            isChecked = viewModel.isSettings.value,
            onCheckedChange = { if(isAdmin) navHostController.navigate(AppNavigationItems. SettingsScreen.route) else viewModel.onShowAdminOnly(context)},
            isArrow = true,
            onArrowChange = { if(isAdmin) navHostController.navigate(AppNavigationItems. SettingsScreen.route) else viewModel.onShowAdminOnly(context)},
            isEnabled = isAdmin
        ),
        SettingsItem(
            imageRes = R.drawable.settings,
            text = stringResource(id = R.string.reader_setting),
            isChecked = viewModel.isTap.value,
            onCheckedChange = { if(isAdmin) navHostController.navigate(AppNavigationItems.ReaderSettingScreen.route) else viewModel.onShowAdminOnly(context)},
            isArrow = true,
            onArrowChange = { if(isAdmin) navHostController.navigate(AppNavigationItems.ReaderSettingScreen.route) else viewModel.onShowAdminOnly(context)},
            isEnabled = isAdmin
        )

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


enum class PercentButton(val value: Int) {
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


