package com.analogics.tpaymentsapos.rootUiScreens.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.analogics.tpaymentsapos.R
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.AppButton
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.CommonLayout
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.FooterButtons
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.HeaderImage
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.OkButton
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.TopBoldText
import com.analogics.tpaymentsapos.ui.theme.dimens
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
            TopBoldText(stringResource(id = R.string.confirm_btn))

            HeaderImage(
                imageName = "logout" // Name of the drawable resource (without the file extension)
            )

            Text(
                text = stringResource(id = R.string.end_shift),
                fontSize = MaterialTheme.dimens.SP_19_CompactMedium,
                color = Color.LightGray,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center, // Center align the text within the Text composable
                modifier = Modifier
                    .fillMaxWidth() // Ensure the Text composable takes up the full width of the parent
                    .padding(top = MaterialTheme.dimens.DP_20_CompactMedium, bottom = MaterialTheme.dimens.DP_20_CompactMedium)
                    .align(Alignment.CenterHorizontally) // Center align the Text composable within its parent
            )

            Spacer(modifier = Modifier.height(MaterialTheme.dimens.DP_30_CompactMedium)) // Blank space added here


            FooterButtons(
                firstButtonTitle = stringResource(id = R.string.cancel),
                firstButtonOnClick = { navHostController.navigate(AppNavigationItems.TrainingScreen.route) },
                secondButtonTitle = stringResource(id = R.string.yes),
                secondButtonOnClick = { navHostController.navigate(AppNavigationItems.TrainingScreen.route) }
            )

            Spacer(modifier = Modifier.height(MaterialTheme.dimens.DP_20_CompactMedium)) // Blank space added here

            Box(
                modifier = Modifier.padding(top = MaterialTheme.dimens.DP_10_CompactMedium)
            ) {
                AppButton(
                    onClick = {
                        navHostController.navigate(AppNavigationItems.TrainingScreen.route)
                    },
                    title = stringResource(id = R.string.print_shift_report),
                )
            }
        }
    }
}

