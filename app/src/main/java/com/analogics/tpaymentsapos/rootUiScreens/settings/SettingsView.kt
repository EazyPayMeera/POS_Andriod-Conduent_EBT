package com.analogics.tpaymentsapos.rootUiScreens.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.analogics.tpaymentsapos.R // Import the R class for resource access
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.CommonTopAppBar
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.SettingsLowerSurface
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.SettingsMiddleSurface
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.SettingsUpperSurface


@Composable
fun SettingsView(navHostController: NavHostController) {
    Column {
        CommonTopAppBar(
            title = "Settings",
            onBackButtonClick = { navHostController.popBackStack() }
        )

        // Custom Surface with top and bottom padding, and clickable
        SettingsUpperSurface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp) // Horizontal padding
                .padding(top = 14.dp, bottom = 0.dp) // Top and bottom padding
                .clickable { navHostController.navigate(AppNavigationItems.LanguageScreen.route) },
            elevation = 20.dp, // Elevation for shadow effect
            color = MaterialTheme.colorScheme.background, // Background color for Surface
            height = 60.dp // Customizable height parameter
        ) {
            // Use Row to place text and images horizontally
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp), // Adjust padding as needed
                horizontalArrangement = Arrangement.Start, // Space between elements
                verticalAlignment = Alignment.CenterVertically // Center vertically
            ) {
                // Leading image
                Image(
                    painter = painterResource(id = R.drawable.language), // Replace with your image resource
                    contentDescription = "Language Icon",
                    modifier = Modifier.size(24.dp) // Adjust size as needed
                )

                // Set Language text
                Text(
                    text = "Set Language",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(horizontal = 8.dp) // Space between text and images
                )
                // Spacer between Upper and Middle Surface
                Spacer(modifier = Modifier.width(125.dp)) // Adjust the height as needed
                // Trailing image
                Image(
                    painter = painterResource(id = R.drawable.rightarrow), // Replace with your image resource
                    contentDescription = "Arrow Icon",
                    modifier = Modifier.size(24.dp) // Adjust size as needed
                )
            }
        }

        // Middle Surface with top and bottom padding, and clickable
        SettingsMiddleSurface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp) // Horizontal padding
                .padding(top = 1.dp, bottom = 0.dp) // Top and bottom padding
                .clickable { /* Handle click for SettingsMiddleSurface */ },
            elevation = 20.dp, // Elevation for shadow effect
            color = MaterialTheme.colorScheme.background, // Background color for Surface
            height = 60.dp // Customizable height parameter
        ) {
            // Use Row to place text and images horizontally
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp), // Adjust padding as needed
                horizontalArrangement = Arrangement.Start, // Space between elements
                verticalAlignment = Alignment.CenterVertically // Center vertically
            ) {
                // Leading image
                Image(
                    painter = painterResource(id = R.drawable.password), // Replace with your image resource
                    contentDescription = "Language Icon",
                    modifier = Modifier.size(24.dp) // Adjust size as needed
                )

                // Change Password text
                Text(
                    text = "Change Password",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(horizontal = 8.dp) // Space between text and images
                )

                // Spacer between Upper and Middle Surface
                Spacer(modifier = Modifier.width(90.dp)) // Adjust the height as needed
                // Trailing image
                Image(
                    painter = painterResource(id = R.drawable.rightarrow), // Replace with your image resource
                    contentDescription = "Arrow Icon",
                    modifier = Modifier.size(24.dp), // Adjust size as needed
                )
            }
        }

        // Lower Surface with top and bottom padding, and clickable
        SettingsLowerSurface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp) // Horizontal padding
                .padding(top = 1.dp, bottom = 0.dp) // Top and bottom padding
                .clickable { navHostController.navigate(AppNavigationItems.ConfigurationScreen.route) },
            elevation = 20.dp, // Elevation for shadow effect
            color = MaterialTheme.colorScheme.background, // Background color for Surface
            height = 60.dp // Customizable height parameter
        ) {
            // Use Row to place text and images horizontally
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp), // Adjust padding as needed
                horizontalArrangement = Arrangement.Start, // Space between elements
                verticalAlignment = Alignment.CenterVertically // Center vertically
            ) {
                // Leading image
                Image(
                    painter = painterResource(id = R.drawable.settings), // Replace with your image resource
                    contentDescription = "Language Icon",
                    modifier = Modifier.size(24.dp) // Adjust size as needed
                )

                // Change Password text
                Text(
                    text = "Configuration",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(horizontal = 8.dp) // Space between text and images
                )

                // Spacer between Upper and Middle Surface
                Spacer(modifier = Modifier.width(120.dp)) // Adjust the height as needed

                // Trailing image
                Image(
                    painter = painterResource(id = R.drawable.rightarrow), // Replace with your image resource
                    contentDescription = "Arrow Icon",
                    modifier = Modifier.size(24.dp) // Adjust size as needed
                )
            }
        }
    }
}


