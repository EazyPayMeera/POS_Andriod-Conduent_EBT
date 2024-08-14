package com.analogics.tpaymentsapos.rootUiScreens.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
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
fun ConfigurationView(navHostController: NavHostController) {
    var isEnableTipping by remember { mutableStateOf(false) }
    Column {
        CommonTopAppBar(
            title = "Configuration",
            onBackButtonClick = { navHostController.popBackStack() }
        )

        // Custom Surface with top and bottom padding, and clickable
        SettingsUpperSurface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp) // Horizontal padding
                .padding(top = 14.dp, bottom = 0.dp) // Top and bottom padding
                .clickable { },
            elevation = 20.dp, // Elevation for shadow effect
            color = MaterialTheme.colorScheme.background, // Background color for Surface
            height = 60.dp // Customizable height parameter
        ) {
            // State to manage the toggle switch
            var isSwitchedOn by remember { mutableStateOf(false) }
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
                    text = "Training Mode",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(horizontal = 8.dp) // Space between text and images
                )
                // Spacer between Upper and Middle Surface
                Spacer(modifier = Modifier.width(128.dp)) // Adjust the height as needed
                // Toggle switch
                // Toggle switch
                Switch(
                    checked = isSwitchedOn,
                    onCheckedChange = { isSwitchedOn = it }, // Update state on toggle
                    colors = SwitchDefaults.colors(
                        checkedBorderColor = Color(0xFFF7931E),
                        checkedThumbColor = Color(0xFFF7931E),
                        uncheckedBorderColor = Color.Gray,
                        checkedTrackColor = Color.White
                    ),
                    modifier = Modifier
                        .scale(0.75f) // Scale down the switch
                        .padding(4.dp) // Padding around the switch
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
            color = MaterialTheme.colorScheme.background
        ) {
            // State to manage the toggle switch
            var isSwitchedOn by remember { mutableStateOf(false) }
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
                    text = "Auto Print Report on Sign Out",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(horizontal = 8.dp) // Space between text and images
                )

                // Spacer between Upper and Middle Surface
                Spacer(modifier = Modifier.width(15.dp)) // Adjust the height as needed
                // Toggle switch
                Switch(
                    checked = isSwitchedOn,
                    onCheckedChange = { isSwitchedOn = it }, // Update state on toggle
                    colors = SwitchDefaults.colors(
                        checkedBorderColor = Color(0xFFF7931E),
                        checkedThumbColor = Color(0xFFF7931E),
                        uncheckedBorderColor = Color.Gray,
                        checkedTrackColor = Color.White
                    ),
                    modifier = Modifier
                        .scale(0.75f) // Scale down the switch
                        .padding(4.dp) // Padding around the switch
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
            color = MaterialTheme.colorScheme.background
        ) {
            // State to manage the toggle switch
            var isSwitchedOn by remember { mutableStateOf(false) }
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
                    text = "Prompt Invoice Number",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(horizontal = 8.dp) // Space between text and images
                )

                // Spacer between Upper and Middle Surface
                Spacer(modifier = Modifier.width(55.dp)) // Adjust the height as needed
                // Toggle switch
                Switch(
                    checked = isSwitchedOn,
                    onCheckedChange = { isSwitchedOn = it }, // Update state on toggle
                    colors = SwitchDefaults.colors(
                        checkedBorderColor = Color(0xFFF7931E),
                        checkedThumbColor = Color(0xFFF7931E),
                        uncheckedBorderColor = Color.Gray,
                        checkedTrackColor = Color.White
                    ),
                    modifier = Modifier
                        .scale(0.75f) // Scale down the switch
                        .padding(4.dp) // Padding around the switch
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
            color = MaterialTheme.colorScheme.background
        ) {
            // State to manage the toggle switch
            var isSwitchedOn by remember { mutableStateOf(false) }
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
                    text = "Auto Print Merchant Receipt",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(horizontal = 8.dp) // Space between text and images
                )

                // Spacer between Upper and Middle Surface
                Spacer(modifier = Modifier.width(22.dp)) // Adjust the height as needed
                // Toggle switch
                Switch(
                    checked = isSwitchedOn,
                    onCheckedChange = { isSwitchedOn = it }, // Update state on toggle
                    colors = SwitchDefaults.colors(
                        checkedBorderColor = Color(0xFFF7931E),
                        checkedThumbColor = Color(0xFFF7931E),
                        uncheckedBorderColor = Color.Gray,
                        checkedTrackColor = Color.White
                    ),
                    modifier = Modifier
                        .scale(0.75f) // Scale down the switch
                        .padding(4.dp) // Padding around the switch
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
            color = MaterialTheme.colorScheme.background
        ) {
            // State to manage the toggle switch
            var isSwitchedOn by remember { mutableStateOf(false) }

            // Use Row to place text and images horizontally
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp), // Adjust padding as needed
                horizontalArrangement = Arrangement.Start, // Space between elements
                verticalAlignment = Alignment.CenterVertically// Center vertically
            ) {

                // Leading image
                Image(
                    painter = painterResource(id = R.drawable.password), // Replace with your image resource
                    contentDescription = "Language Icon",
                    modifier = Modifier.size(24.dp) // Adjust size as needed
                )

                // Change Password text
                Text(
                    text = "Enable Tipping",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(horizontal = 8.dp) // Space between text and images
                )

                // Spacer between Upper and Middle Surface
                Spacer(modifier = Modifier.width(123.dp)) // Adjust the height as needed
                // Toggle switch
                Switch(
                    checked = isEnableTipping,
                    onCheckedChange = { isEnableTipping = it },
                    colors = SwitchDefaults.colors(
                        checkedBorderColor = Color(0xFFF7931E),
                        checkedThumbColor = Color(0xFFF7931E),
                        uncheckedBorderColor = Color.Gray,
                        checkedTrackColor = Color.White
                    ),
                    modifier = Modifier
                        .scale(0.75f) // Scale down the switch
                        .padding(4.dp) // Padding around the switch
                )
            }

        }
        if(isEnableTipping)
        {
            Box(modifier = Modifier.fillMaxWidth().background(color = Color.White))
            {
                Text ("ABCD")
            }
        }
        // Lower Surface with top and bottom padding, and clickable
        SettingsLowerSurface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp) // Horizontal padding
                .padding(top = 1.dp, bottom = 0.dp) // Top and bottom padding
                .clickable { /* Handle click for SettingsLowerSurface */ },
            elevation = 20.dp, // Elevation for shadow effect
            color = MaterialTheme.colorScheme.background, // Background color for Surface
            height = 60.dp // Customizable height parameter
        ) {
            // State to manage the toggle switch
            var isSwitchedOn by remember { mutableStateOf(false) }
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
                    text = "Taxes",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(horizontal = 8.dp) // Space between text and images
                )

                // Spacer between Upper and Middle Surface
                Spacer(modifier = Modifier.width(190.dp)) // Adjust the height as needed

                // Toggle switch
                Switch(
                    checked = isSwitchedOn,
                    onCheckedChange = { isSwitchedOn = it }, // Update state on toggle
                    colors = SwitchDefaults.colors(
                        checkedBorderColor = Color(0xFFF7931E),
                        checkedThumbColor = Color(0xFFF7931E),
                        uncheckedBorderColor = Color.Gray,
                        checkedTrackColor = Color.White
                    ),
                    modifier = Modifier
                        .scale(0.75f) // Scale down the switch
                        .padding(4.dp) // Padding around the switch
                )
            }
        }
    }
}


