package com.analogics.tpaymentsapos.rootUtils.genericComposeUI

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.analogics.tpaymentsapos.R
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import com.analogics.tpaymentsapos.ui.theme.dimens


@Composable
fun DrawerContent(
    navHostController: NavHostController,
    onMenuItemClick: (String) -> Unit
) {
    Column {
        DrawerItem("Settings") {
            onMenuItemClick("Settings")
            navHostController.navigate(AppNavigationItems.SettingsScreen.route)
        }
        DrawerItem("Option 2") {
            onMenuItemClick("Option 2")
           // navHostController.navigate(AppNavigationItems.Option2Screen.route)
        }
    }
}


@Composable
fun DrawerItem(label: String, onClick: () -> Unit) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = MaterialTheme.dimens.DP_15_CompactMedium),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            ImageView(
                imageId = R.drawable.config_tax,
                size = MaterialTheme.dimens.DP_30_CompactMedium)


            TextView(
                text = label,
                fontSize = MaterialTheme.dimens.SP_16_CompactMedium,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(start = MaterialTheme.dimens.DP_20_CompactMedium)
            )
        }

     ImageView(imageId = R.drawable.rightarrow,size = MaterialTheme.dimens.DP_30_CompactMedium)
    }
}
