package com.analogics.tpaymentsapos.rootUiScreens.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.analogics.tpaymentsapos.R
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.AppButton
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.CommonLayout
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.ConfirmationButton
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.HeaderImage
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.OkButton
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.TopBoldText
import kotlinx.coroutines.delay

@Composable
fun ConfirmShiftView(navHostController: NavHostController) {
    CommonLayout(
        title = "NAMEHERE",
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally, // Center align items horizontally
            verticalArrangement = Arrangement.Center, // Center align items vertically
            modifier = Modifier.fillMaxSize() // Fill the available size
        ) {
            TopBoldText("Confirm")

            HeaderImage(
                imageName = "logout" // Name of the drawable resource (without the file extension)
            )

            Text(
                text = "Are you sure you want to\n end your shift?",
                fontSize = 16.sp,
                color = Color.LightGray,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center, // Center align the text within the Text composable
                modifier = Modifier
                    .fillMaxWidth() // Ensure the Text composable takes up the full width of the parent
                    .padding(top = 20.dp, bottom = 20.dp)
                    .align(Alignment.CenterHorizontally) // Center align the Text composable within its parent
            )

            Spacer(modifier = Modifier.height(30.dp)) // Blank space added here

            Row(
                horizontalArrangement = Arrangement.Center, // Center align buttons horizontally
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 20.dp)
            ) {
                ConfirmationButton(
                    onClick = {
                        navHostController.navigate(AppNavigationItems.TrainingScreen.route)
                    },
                    title = "CANCEL"
                )

                Spacer(modifier = Modifier.width(30.dp)) // Blank space added here

                ConfirmationButton(
                    onClick = {
                        navHostController.navigate(AppNavigationItems.TrainingScreen.route)
                    },
                    title = "YES"
                )
            }

            Spacer(modifier = Modifier.height(20.dp)) // Blank space added here

            Box(
                modifier = Modifier.padding(top = 10.dp)
            ) {
                AppButton(
                    onClick = {
                        navHostController.navigate(AppNavigationItems.TrainingScreen.route)
                    },
                    title = "Print Shift Report",
                )
            }
        }
    }
}

