package com.analogics.tpaymentsapos.rootUtils.genericComposeUI

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import com.analogics.paymentservicecore.constants.AppConstants
import com.analogics.tpaymentsapos.BuildConfig
import com.analogics.tpaymentsapos.R
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import com.analogics.tpaymentsapos.ui.theme.dimens

@Composable
fun CustomDrawerContent(
    onCloseDrawer: () -> Unit,
    navHostController: NavHostController,
    onMenuItemClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .background(Color(0xFFFAFAF7)) // Match the drawer background color
            .padding(MaterialTheme.dimens.DP_13_CompactMedium)
    ) {
        // Header with Name and Close Icon
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onCloseDrawer() } // Make the whole box clickable
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = MaterialTheme.dimens.DP_20_CompactMedium), // Apply padding here
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = R.string.application_name),
                    style = MaterialTheme.typography.headlineSmall.copy(
                        color = MaterialTheme.colorScheme.primary // Orange color
                    )
                )
                Spacer(modifier = Modifier.width(MaterialTheme.dimens.DP_21_CompactMedium))
                IconButton(
                    onClick = onCloseDrawer,
                    modifier = Modifier.size(MaterialTheme.dimens.DP_33_CompactMedium) // Adjust the size here
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = ""
                    )
                }
            }
        }

        Divider(color = MaterialTheme.colorScheme.onSecondary, thickness = MaterialTheme.dimens.DP_1_CompactMedium)

        val drawersItems = listOf(
            DrawerItem(
                imageRes = Icons.Default.Chat,
                text = stringResource(id = R.string.set_lang),
                isChecked = false,
                onCheckedChange = { navHostController.navigate(AppNavigationItems.LanguageScreen.route) }
            ),
            DrawerItem(
                imageRes = Icons.Default.AccountCircle,
                text = stringResource(id = R.string.user_man),
                isChecked = false,
                onCheckedChange = { navHostController.navigate(AppNavigationItems.UserManagementScreen.route) }
            ),
/*            DrawerItem(
                imageRes = Icons.Default.VpnKey,
                text = stringResource(id = R.string.change_password),
                isChecked = false,
                onCheckedChange = { navHostController.navigate(AppNavigationItems.ChangePasswordScreen.route) }
            ),*/
            DrawerItem(
                imageRes = Icons.Default.Settings,
                text = stringResource(id = R.string.Configuration),
                isChecked = false,
                onCheckedChange = { navHostController.navigate(AppNavigationItems.ConfigurationScreen.route) }
            ),
            DrawerItem(
                imageRes = Icons.Default.Key,
                text = stringResource(id = R.string.reactivate_device),
                isChecked = false,
                onCheckedChange = { onMenuItemClick(AppConstants.BUTTON_CLICK_EVENT_RE_ACTIVATE_DEVICE) }
            ),
            DrawerItem(
                imageRes = Icons.Default.Logout,
                text = stringResource(id = R.string.log_out),
                isChecked = false,
                onCheckedChange = { navHostController.navigate(AppNavigationItems.ConfirmShiftScreen.route) }
            )
        )

        Column {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = MaterialTheme.dimens.DP_20_CompactMedium),
                elevation = CardDefaults.elevatedCardElevation(MaterialTheme.dimens.DP_11_CompactMedium)
            ) {
                Column {
                    drawersItems.forEachIndexed { index, item ->
                        DrawersSurface(
                            modifier = Modifier.fillMaxWidth(),
                            item = item
                        )

                        index.takeIf { it < drawersItems.size - 1 }?.let {
                            Divider(
                                modifier = Modifier.fillMaxWidth(),
                                thickness = MaterialTheme.dimens.DP_1_CompactMedium,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(MaterialTheme.dimens.DP_24_CompactMedium)
        ) {
            Text(
                text = stringResource(id = R.string.login_version) + BuildConfig.VERSION_NAME,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(MaterialTheme.dimens.DP_24_CompactMedium)
            )
        }
    }

}

data class DrawerItem(
    val imageRes: ImageVector,
    val text: String,
    val isChecked: Boolean,
    val onCheckedChange: (Boolean) -> Unit
)

@Composable
fun DrawersSurface(
    modifier: Modifier = Modifier,
    item: DrawerItem,
) {
    Surface(
        modifier = Modifier.height(MaterialTheme.dimens.DP_60_CompactMedium),
        color = MaterialTheme.colorScheme.onPrimary
    ) {
        DrawersContent(
            item = item
        )

    }
}

@Composable
fun DrawersContent(
    item: DrawerItem
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
                .padding(vertical = MaterialTheme.dimens.DP_24_CompactMedium), // Added padding for better touch area
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = item.imageRes,  // Use Icon for ImageVector
                    contentDescription = item.text,
                    modifier = Modifier.size(MaterialTheme.dimens.DP_28_CompactMedium)
                )

                Text(
                    text = item.text,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = MaterialTheme.dimens.DP_20_CompactMedium)
                )
            }

            CustomSwitch(
                checked = item.isChecked,
                onCheckedChange = { newCheckedState ->
                    item.onCheckedChange(newCheckedState)
                },
                checkedImage = R.drawable.arrow, // Your checked drawable
                uncheckedImage = R.drawable.arrow, // Your unchecked drawable
                imageSize = MaterialTheme.dimens.DP_23_CompactMedium
            )
        }
    }
}


