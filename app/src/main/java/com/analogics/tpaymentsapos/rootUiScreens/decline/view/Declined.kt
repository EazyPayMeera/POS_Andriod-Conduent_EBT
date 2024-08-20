package com.analogics.tpaymentsapos.rootUiScreens.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavHostController
import com.analogics.tpaymentsapos.R
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.CommonLayout
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.OkButton
import com.analogics.tpaymentsapos.ui.theme.dimens

@Composable
fun DeclineView(navHostController: NavHostController, totalAmount: String) {
    CommonLayout(
        title = stringResource(id = R.string.decline),
        imageResId = R.drawable.close
    ) {
        Spacer(modifier = Modifier.height(MaterialTheme.dimens.DP_10_CompactMedium)) // Blank space added here

        Text(
            text = stringResource(id = R.string.decline_u),
            fontSize = MaterialTheme.dimens.SP_27_CompactMedium,
            color = Color.Black,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(bottom = MaterialTheme.dimens.DP_20_CompactMedium)
                .align(Alignment.CenterHorizontally) // Center the subheader text
        )

        Spacer(modifier = Modifier.height(MaterialTheme.dimens.DP_20_CompactMedium)) // Blank space added here

        Image(
            painter = painterResource(id = R.drawable.approve), // Replace with your image resource
            contentDescription = null, // Decorative image
            modifier = Modifier
                .size(MaterialTheme.dimens.DP_110_CompactMedium)
                .padding(bottom = MaterialTheme.dimens.DP_24_CompactMedium)
                .align(Alignment.CenterHorizontally) // Center the image
        )
        Spacer(modifier = Modifier.height(MaterialTheme.dimens.DP_30_CompactMedium)) // Blank space added here

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            CircularMenu(
                onPrintClick = {
                    // Do something on Print click
                },
                onMenuOptionClick = { option ->
                    // Handle circular menu option clicks
                    // For demonstration, navigate to EnterEmailScreen
                    navHostController?.navigate(AppNavigationItems.EnterEmailScreen.route)
                }
            )
        }
        Spacer(modifier = Modifier.height(MaterialTheme.dimens.DP_10_CompactMedium))
        Box(
            modifier = Modifier.padding(top = MaterialTheme.dimens.DP_10_CompactMedium)
        ) {
            OkButton(
                onClick = {
                    navHostController.navigate(AppNavigationItems.TrainingScreen.route)
                },
                title = stringResource(id = R.string.done)
            )
        }
    }
}
