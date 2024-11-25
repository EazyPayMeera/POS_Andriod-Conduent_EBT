package com.analogics.tpaymentsapos.rootUiScreens.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import com.analogics.tpaymentsapos.R
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.CommonTopAppBar
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.SettingsLowerSurface
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.SettingsMiddleSurface
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.SettingsUpperSurface
import com.analogics.tpaymentsapos.ui.theme.dimens


@Composable
fun SettingsView(navHostController: NavHostController) {
    Column {
        CommonTopAppBar(
            title = stringResource(id = R.string.settings),
            onBackButtonClick = { navHostController.popBackStack() }
        )

        // Custom Surface with top and bottom padding, and clickable
        SettingsUpperSurface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MaterialTheme.dimens.DP_24_CompactMedium) // Horizontal padding
                .padding(top = MaterialTheme.dimens.DP_22_CompactMedium) // Top and bottom padding
                .clickable { navHostController.navigate(AppNavigationItems.LanguageScreen.route) },
            elevation = MaterialTheme.dimens.DP_21_CompactMedium, // Elevation for shadow effect
            color = MaterialTheme.colorScheme.background, // Background color for Surface
            height = MaterialTheme.dimens.DP_60_CompactMedium // Customizable height parameter
        ) {
            // Use Row to place text and images horizontally
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = MaterialTheme.dimens.DP_24_CompactMedium), // Adjust padding as needed
                horizontalArrangement = Arrangement.Start, // Space between elements
                verticalAlignment = Alignment.CenterVertically // Center vertically
            ) {
                // Leading image
                Image(
                    painter = painterResource(id = R.drawable.lan), // Replace with your image resource
                    contentDescription = "",
                    modifier = Modifier.size(MaterialTheme.dimens.DP_23_CompactMedium) // Adjust size as needed // need to change here
                )

                // Set Language text
                Text(
                    text = stringResource(id = R.string.set_lang),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(horizontal = MaterialTheme.dimens.DP_20_CompactMedium) // Space between text and images
                )
                // Spacer between Upper and Middle Surface
                Spacer(modifier = Modifier.width(MaterialTheme.dimens.DP_125_CompactMedium)) // Adjust the height as needed
                // Trailing image
                Image(
                    painter = painterResource(id = R.drawable.rightarrow), // Replace with your image resource
                    contentDescription = "",
                    modifier = Modifier.size(MaterialTheme.dimens.DP_23_CompactMedium) // Adjust size as needed // Need to change here
                )
            }
        }

        SettingsMiddleSurface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MaterialTheme.dimens.DP_24_CompactMedium) // Horizontal padding
                .padding(top = MaterialTheme.dimens.DP_1_CompactMedium) // Top and bottom padding
                .clickable { /* Handle click for SettingsMiddleSurface */ },
            elevation = MaterialTheme.dimens.DP_21_CompactMedium, // Elevation for shadow effect
            color = MaterialTheme.colorScheme.background
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = MaterialTheme.dimens.DP_24_CompactMedium), // Adjust padding as needed
                horizontalArrangement = Arrangement.Start, // Space between elements
                verticalAlignment = Alignment.CenterVertically // Center vertically
            ) {
                // Leading image
                Image(
                    painter = painterResource(id = R.drawable.password), // Replace with your image resource
                    contentDescription = "",
                    modifier = Modifier.size(MaterialTheme.dimens.DP_23_CompactMedium) // Adjust size as needed // need to change here
                )

                // Change Password text
                Text(
                    text = stringResource(id = R.string.change_password),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(horizontal = MaterialTheme.dimens.DP_20_CompactMedium) // Space between text and images
                )

                // Spacer between Upper and Middle Surface
                Spacer(modifier = Modifier.width(MaterialTheme.dimens.DP_90_CompactMedium)) // Adjust the height as needed
                // Trailing image
                Image(
                    painter = painterResource(id = R.drawable.rightarrow), // Replace with your image resource
                    contentDescription = "",
                    modifier = Modifier.size(MaterialTheme.dimens.DP_23_CompactMedium), // Adjust size as needed // Need to change here 24.dp
                )
            }
        }

        // Lower Surface with top and bottom padding, and clickable
        SettingsLowerSurface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MaterialTheme.dimens.DP_24_CompactMedium) // Horizontal padding
                .padding(top = MaterialTheme.dimens.DP_1_CompactMedium) // Top and bottom padding
                .clickable { navHostController.navigate(AppNavigationItems.ConfigurationScreen.route) },
            elevation = MaterialTheme.dimens.DP_21_CompactMedium, // Elevation for shadow effect
            color = MaterialTheme.colorScheme.background, // Background color for Surface
            height = MaterialTheme.dimens.DP_60_CompactMedium // Customizable height parameter
        ) {
            // Use Row to place text and images horizontally
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = MaterialTheme.dimens.DP_24_CompactMedium), // Adjust padding as needed
                horizontalArrangement = Arrangement.Start, // Space between elements
                verticalAlignment = Alignment.CenterVertically // Center vertically
            ) {
                // Leading image
                Image(
                    painter = painterResource(id = R.drawable.settings), // Replace with your image resource
                    contentDescription = "",
                    modifier = Modifier.size(MaterialTheme.dimens.DP_23_CompactMedium) // Adjust size as needed Need to change here 24.dp
                )
                Text(
                    text = stringResource(id = R.string.Configuration),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(horizontal = MaterialTheme.dimens.DP_20_CompactMedium) // Space between text and images
                )

                // Spacer between Upper and Middle Surface
                Spacer(modifier = Modifier.width(MaterialTheme.dimens.DP_120_CompactMedium)) // Adjust the height as needed

                // Trailing image
                Image(
                    painter = painterResource(id = R.drawable.rightarrow), // Replace with your image resource
                    contentDescription = "",
                    modifier = Modifier.size(MaterialTheme.dimens.DP_23_CompactMedium) // Adjust size as needed need to change here 24.dp
                )
            }
        }
    }
}


