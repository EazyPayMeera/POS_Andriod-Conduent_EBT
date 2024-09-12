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
import androidx.compose.ui.unit.dp
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
            .padding(12.dp)
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
                    .padding(bottom = 8.dp), // Apply padding here
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = R.string.application_name),
                    style = MaterialTheme.typography.headlineSmall.copy(
                        color = Color(0xFFFFA500) // Orange color
                    )
                )
                Spacer(modifier = Modifier.width(20.dp))
                IconButton(
                    onClick = onCloseDrawer,
                    modifier = Modifier.size(30.dp) // Adjust the size here
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Drawer"
                    )
                }
            }
        }

        Divider(color = MaterialTheme.colorScheme.onSecondary, thickness = 1.dp)
        GenericCard(
            modifier = Modifier
                .padding(top = 10.dp)
                .fillMaxWidth(),
            elevation = 10.dp,
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(MaterialTheme.dimens.DP_10_CompactMedium)
            ) {
                DrawerMenuItem(
                    icon = Icons.Default.Chat,
                    label = "Set Language",
                    function = {
                        onMenuItemClick("Settings")
                        navHostController.navigate(AppNavigationItems.LanguageScreen.route)
                    }
                )
                Divider(color = MaterialTheme.colorScheme.onSecondary, thickness = 1.dp)
                DrawerMenuItem(
                    icon = Icons.Default.VpnKey,
                    label = "Change Password",
                    function = {
                        onMenuItemClick("Settings")
                        navHostController.navigate(AppNavigationItems.ChangePasswordScreen.route)
                    }
                )
                Divider(color = MaterialTheme.colorScheme.onSecondary, thickness = 1.dp)
                DrawerMenuItem(
                    icon = Icons.Default.Settings,
                    label = "Configuration",
                    function = {
                        onMenuItemClick("Settings")
                        navHostController.navigate(AppNavigationItems.ConfigurationScreen.route)
                    }
                )
                Divider(color = MaterialTheme.colorScheme.onSecondary, thickness = 1.dp)
                DrawerMenuItem(
                    icon = Icons.Default.Logout,
                    label = "Logout",
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
            modifier = Modifier.size(30.dp),
            tint = MaterialTheme.colorScheme.onSecondary
        )
        Spacer(modifier = Modifier.width(16.dp))
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
