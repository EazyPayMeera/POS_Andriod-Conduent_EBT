package com.eazypaytech.posafrica.rootUiScreens.enteremail.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.eazypaytech.posafrica.R
import com.eazypaytech.posafrica.navigation.AppNavigationItems
import com.eazypaytech.posafrica.rootUiScreens.dialogs.CustomDialogBuilder
import com.eazypaytech.posafrica.rootUiScreens.enteremail.viewmodel.EnterEmailViewModel
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.CommonTopAppBar
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.FooterButtons
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.GenericCard
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.ImageView
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.OutlinedTextField
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.TextView
import com.eazypaytech.posafrica.ui.theme.dimens


@Composable
fun EnterEmailView(navHostController: NavHostController) {
    // Get ViewModel instance
    val viewModel: EnterEmailViewModel = hiltViewModel()
    var isDialogVisible by remember { mutableStateOf(false) }
    // Collect the state from ViewModel
    val email by viewModel.email.collectAsState()

    Column {
        CommonTopAppBar(
            onBackButtonClick = { navHostController.popBackStack() }
        )

        GenericCard(
            modifier = Modifier.padding(MaterialTheme.dimens.DP_24_CompactMedium)
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(MaterialTheme.dimens.DP_30_CompactMedium)
            ) {
                TextView(
                    text = stringResource(id = R.string.enter_email),
                    fontSize = MaterialTheme.dimens.SP_21_CompactMedium,
                    color = MaterialTheme.colorScheme.tertiary,
                    fontWeight = FontWeight.Bold,
                    1,
                    Modifier.padding(MaterialTheme.dimens.DP_24_CompactMedium),
                    textAlign = TextAlign.Center
                )
                ImageView(
                    imageId = R.drawable.card, size = MaterialTheme.dimens.DP_60_CompactMedium,
                    shape = RectangleShape, // Example shape, can be any Shape
                    alignment = Alignment.Center,
                    contentDescription = "",
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { newValue -> viewModel.updateEmail(newValue) },
                    shape = RoundedCornerShape(MaterialTheme.dimens.DP_13_CompactMedium),
                    placeholder = stringResource(id = R.string.email),
                    textStyle = TextStyle(fontWeight = FontWeight.Bold, fontSize = MaterialTheme.dimens.SP_21_CompactMedium),
                    keyboardType = KeyboardType.Email,
                    onDoneAction = {
                        viewModel.navigateToEmailScreen(navHostController)
                    },
                    isPassword = false
                )

            }

        }

        FooterButtons(
            firstButtonTitle = stringResource(id = R.string.cancel_btn),
            firstButtonOnClick = { /*viewModel.navigateToTrainingScreen(navHostController)*/ isDialogVisible = true },
            secondButtonTitle = stringResource(id = R.string.confirm_btn),
            secondButtonOnClick = { viewModel.navigateToEmailScreen(navHostController) }
        )

        if (isDialogVisible) {
            CustomDialogBuilder.create()
                .setTitle(stringResource(id = R.string.cancel_dialogue))
                .setSubtitle(stringResource(id = R.string.dialogue_cancel_request))
                .setSmallText("")
                .setShowCloseButton(false) // Can set to false if you don't want the close button
                .setCancelButtonText(stringResource(id = R.string.yes))
                .setConfirmButtonText(stringResource(id = R.string.cancel_no))
                .setCancelable(true)
                .setAutoOff(false)
                .setBackgroundColor(androidx.compose.material.MaterialTheme.colors.surface)
                .setProgressColor(color = MaterialTheme.colorScheme.primary) // Orange color
                .setShowProgressIndicator(false)
                .setOnCancelAction {
                    navHostController.navigate(AppNavigationItems.DashBoardScreen.route)
                }
                .setOnConfirmAction {
                    navHostController.navigate(AppNavigationItems.EnterEmailScreen.route)
                }
                .setShowButtons(true)
                .setNavAction {
                    navHostController.popBackStack()
                }
                .buildDialog(onClose = { isDialogVisible = false })

        }
    }
}
