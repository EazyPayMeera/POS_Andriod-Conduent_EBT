package com.analogics.tpaymentsapos.rootUtils.genericComposeUI

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
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
        GenericCard(
            modifier = Modifier
                .padding(top = MaterialTheme.dimens.DP_11_CompactMedium)
                .fillMaxWidth(),
            elevation = MaterialTheme.dimens.DP_11_CompactMedium,
            shape = RoundedCornerShape(MaterialTheme.dimens.DP_20_CompactMedium)
        ) {
            Column(
                modifier = Modifier
                    .padding(MaterialTheme.dimens.DP_10_CompactMedium)
            ) {
                DrawerMenuItem(
                    icon = Icons.Default.Chat,
                    label = stringResource(id = R.string.set_lang),
                    function = {
                        onMenuItemClick("Settings")
                        navHostController.navigate(AppNavigationItems.LanguageScreen.route)
                    }
                )
                Divider(color = MaterialTheme.colorScheme.onSecondary, thickness = MaterialTheme.dimens.DP_1_CompactMedium)
                DrawerMenuItem(
                    icon = Icons.Default.VpnKey,
                    label = stringResource(id = R.string.change_password),
                    function = {
                        onMenuItemClick("Settings")
                        navHostController.navigate(AppNavigationItems.ChangePasswordScreen.route)
                    }
                )
                Divider(color = MaterialTheme.colorScheme.onSecondary, thickness = MaterialTheme.dimens.DP_1_CompactMedium)
                DrawerMenuItem(
                    icon = Icons.Default.Settings,
                    label = stringResource(id = R.string.Configuration),
                    function = {
                        onMenuItemClick("Settings")
                        navHostController.navigate(AppNavigationItems.ConfigurationScreen.route)
                    }
                )
                Divider(color = MaterialTheme.colorScheme.onSecondary, thickness = MaterialTheme.dimens.DP_1_CompactMedium)
                DrawerMenuItem(
                    icon = Icons.Default.Logout,
                    label = stringResource(id = R.string.log_out),
                    function = {
                        onMenuItemClick("Logout")
                        navHostController.navigate(AppNavigationItems.ConfirmShiftScreen.route)
                    }
                )
            }
        }
    }
}



@Composable
fun DrawerMenuItem(icon: ImageVector, label: String, function: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = MaterialTheme.dimens.DP_25_CompactMedium)
            .clickable { function() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(MaterialTheme.dimens.DP_33_CompactMedium),
            tint = MaterialTheme.colorScheme.onSecondary
        )
        Spacer(modifier = Modifier.width(MaterialTheme.dimens.DP_24_CompactMedium))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.weight(1f))
        Icon(
            imageVector = Icons.Default.ArrowForward,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSecondary
        )
    }
}
