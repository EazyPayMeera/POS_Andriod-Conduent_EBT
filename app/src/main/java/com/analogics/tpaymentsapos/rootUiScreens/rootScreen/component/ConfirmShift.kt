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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.analogics.tpaymentsapos.R
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.CommonLayout
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.ConfirmationButton
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.OkButton
import kotlinx.coroutines.delay

@Composable
fun ConfirmShiftView(navHostController: NavHostController) {
    val OrangeColor = Color(0xFFFFA500)
    CommonLayout(
        title = "NAMEHERE",
        imageResId = R.drawable.close
    ) {
        Spacer(modifier = Modifier.height(20.dp)) // Blank space added here

        Text(
            text = "Confirm",
            fontSize = 24.sp,
            color = Color.Black,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(bottom = 20.dp)
                .align(Alignment.CenterHorizontally) // Center the subheader text
        )

        Spacer(modifier = Modifier.height(20.dp)) // Blank space added here

        Image(
            painter = painterResource(id = R.drawable.logout), // Replace with your image resource
            contentDescription = null, // Decorative image
            modifier = Modifier
                .size(50.dp)
                .padding(bottom = 16.dp)
                .align(Alignment.CenterHorizontally) // Center the image
        )

        Spacer(modifier = Modifier.height(20.dp)) // Blank space added here

        Text(
            text = "Are you sure you want to end your shift",
            fontSize = 16.sp,
            color = Color.Gray,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(bottom = 20.dp)
                .align(Alignment.CenterHorizontally) // Center the subheader text
        )

        Spacer(modifier = Modifier.height(20.dp)) // Blank space added here

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 20.dp)
        ){
            ConfirmationButton(
                onClick = {
                    navHostController?.navigate(AppNavigationItems.TrainingScreen.route)
                },
                title = "CANCEL"
            )

            Spacer(modifier = Modifier.width(20.dp)) // Blank space added here

            ConfirmationButton(
                onClick = {
                    navHostController?.navigate(AppNavigationItems.TrainingScreen.route)
                },
                title = "YES"
            )
        }

        Spacer(modifier = Modifier.height(20.dp)) // Blank space added here

        // Fourth set of buttons
        Button(
            onClick = { /* Handle print receipt click */ },
            modifier = Modifier
                .size(260.dp)
                .padding(8.dp),
            colors = ButtonDefaults.buttonColors(backgroundColor = OrangeColor),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text(text = "Print Shift Report", color = Color.Black)
        }

    }
}
